package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.OrderDao;
import dao.RatingDao;
import dao.UserDao;
import dto.RatingDto;
import entity.*;
import exception.ForbiddenException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import org.hibernate.query.sqm.mutation.internal.Handler;
import org.json.JSONObject;
import util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class RatingHandler implements HttpHandler {
    public static final Gson gson = new Gson();

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized");
            }
            String token = authHeader.substring(7);

            if ("GET".equalsIgnoreCase(method)) {
                if (path.matches("/ratings/\\d+")) {
                    handle_GetSpecificRating(exchange, token);
                } else if (path.matches("/ratings/items/\\d+")) {
                    handle_GetAllSpeceficRatings(exchange, token);
                } else {
                    sendResponse(exchange, 404, "Path not found");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                if (path.matches("/ratings/\\d+")) {
                    handle_EditRating(exchange, token);
                } else {
                    sendResponse(exchange, 404, "Path not found");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (path.matches("/ratings/\\d+")) {
                    handle_DeleteRating(exchange, token);
                } else {
                    sendResponse(exchange, 404, "Path not found");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                if (path.matches("/ratings")) {
               handle_AddRating(exchange, token);
                } else {
                    sendResponse(exchange, 404, "Path not found");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }

        } catch (UnauthorizedException e) {
            sendResponse(exchange, 401, "Unauthorized");
        } catch (ForbiddenException e) {
            sendResponse(exchange, 403, "Forbidden");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "Not Found");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }

    }

    private void handle_AddRating(HttpExchange exchange, String token) throws IOException {
        try {
            String phone = JwtUtil.validateToken(token);
            User user = UserDao.getByPhone(phone);
            if (user != null) {
                if ((user instanceof Buyer buyer)) {

                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    RatingDto dto = new Gson().fromJson(isr, RatingDto.class);

                    if (OrderDao.isThatForThisOne(user, dto.getOrderId())) {
                        Rating rating = new Rating(dto,buyer);
                        Order order = OrderDao.getOrderById(dto.getOrderId());
                        rating.setOrder(order);
                        order.setRating(rating);
                        RatingDao.saveRating(rating);

                        sendResponse(exchange, 200, "Success");
                    } else {
                        throw new ForbiddenException("Forbidden");
                    }
                } else {
                    throw new ForbiddenException("Forbidden");
                }
            } else {
                throw new NotFoundException("User not found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void handle_GetSpecificRating(HttpExchange exchange, String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);

        if (user != null) {
            if (user instanceof Buyer || user instanceof Seller) {
                String[] paths = exchange.getRequestURI().getPath().split("/");
                long id = Long.parseLong(paths[paths.length - 1]);
                Rating rating = RatingDao.findById(id);
                sendResponse(exchange, 200, new Gson().toJson(rating));
            } else {
                throw new ForbiddenException("Forbidden");
            }
        } else {
            throw new NotFoundException("User not found");
        }
    }

    private void handle_GetAllSpeceficRatings(HttpExchange exchange, String token) throws IOException {
        try {
            String phone = JwtUtil.validateToken(token);
            User user = UserDao.getByPhone(phone);
            if (user != null) {
                if ((user instanceof Buyer || user instanceof Seller)) {
                    String[] paths = exchange.getRequestURI().getPath().split("/");
                    long orderId = Long.parseLong(paths[paths.length - 1]);
                     double avg_rating =0;
                    List<Rating> ratings = RatingDao.findByItem(orderId);
                    if (!ratings.isEmpty()) {
                        for (Rating rating : ratings) {
                            avg_rating += rating.getRate();
                        }
                        avg_rating /= ratings.size();
                    }


                    List<RatingDto> dtoList = ratings.stream()
                            .map(rating -> {
                                RatingDto dto = new RatingDto();
                                dto.setOrderId(rating.getOrder_Id());
                                dto.setRate(rating.getRate());
                                dto.setComment(rating.getComment());
                                dto.setLogobase64(rating.getLogobase64());
                                dto.setUserName(rating.getUserName());
                                return dto;
                            })
                            .collect(Collectors.toList());
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("comments", dtoList);
                    jsonObject.put("avg_rating", avg_rating);


                    String json = jsonObject.toString();
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    sendResponse(exchange, 200, json);
                } else {
                    throw new ForbiddenException("Only buyers or sellers can access ratings.");
                }
            } else {
                throw new NotFoundException("User not found");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void handle_EditRating(HttpExchange exchange, String token) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String phone = JwtUtil.validateToken(token);
            User user = UserDao.getByPhone(phone);
            if (user != null) {
                if ((user instanceof Buyer)) {
                    String[] paths = exchange.getRequestURI().getPath().split("/");
                    long orderId = Long.parseLong(paths[paths.length - 1]);
                    if (RatingDao.canEditId(user, orderId)) {
                        Rating rating = RatingDao.findByOrderId(orderId);
                        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
                        RatingDto dto = new Gson().fromJson(reader, RatingDto.class);
                        rating.setRate(dto.getRate());
                        rating.setComment(dto.getComment());
                        rating.setLogobase64(dto.getLogobase64());
                        RatingDao.updateRating(rating);
                        sendResponse(exchange, 200, "success");
                    } else {
                        throw new ForbiddenException("Forbidden");
                    }
                } else {
                    throw new ForbiddenException("Forbidden");
                }
            } else {
                throw new NotFoundException("User not found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handle_DeleteRating(HttpExchange exchange, String token) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String phone = JwtUtil.validateToken(token);
            User user = UserDao.getByPhone(phone);
            if (user != null) {
                if ((user instanceof Buyer)) {
                    String[] paths = exchange.getRequestURI().getPath().split("/");
                    long orderId = Long.parseLong(paths[paths.length - 1]);
                    if (RatingDao.canEditId(user, orderId)) {
                        Rating rating = RatingDao.findByOrderId(orderId);
                        RatingDao.deleteRating(rating);
                        sendResponse(exchange, 200, "success");
                    } else {
                        throw new ForbiddenException("Forbidden");
                    }
                } else {
                    throw new ForbiddenException("Forbidden");
                }
            } else {
                throw new NotFoundException("User not found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

}
