package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.RestaurantDao;
import dao.UserDao;
import dto.RestaurantDto;
import entity.Buyer;
import entity.Restaurant;
import entity.User;
import exception.ForbiddenException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class FavoriteHandler implements HttpHandler {
    private static final Gson gson = new Gson();

    @Override
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
                if (path.matches("/favorites/")) {
                    handle_GetFavoriteList(exchange, token);
                } else {
                    sendResponse(exchange, 404, "Path not found");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                if (path.matches("/favorites/\\d+")) {
                    handle_UpdateFavorite( exchange, token);
                } else {
                    sendResponse(exchange, 404, "Path not found");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (path.matches("/favorites/\\d+")) {
                    handle_DeleteFavorite(exchange, token);
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

    private void handle_GetFavoriteList(HttpExchange exchange, String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);

        if (user != null) {
            if (user instanceof Buyer buyer) {
                List<Restaurant> favorites = buyer.getFavoriteRestaurants();

                List<RestaurantDto> dtoList = favorites.stream()
                        .map(RestaurantDto::new)
                        .collect(Collectors.toList());

                String json = gson.toJson(dtoList);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(json.getBytes());
                os.close();
            } else {
                throw new ForbiddenException("Only buyers can have favorite restaurants.");
            }
        } else {
            throw new NotFoundException("User not found.");
        }
    }

    private void handle_DeleteFavorite(HttpExchange exchange, String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        if (user != null) {
            if (user instanceof Buyer buyer) {
                String[] paths = exchange.getRequestURI().getPath().split("/");
                long id = Long.parseLong(paths[paths.length - 1]);
                if (buyer.deleteFavoriteRestaurant(id)) {
                    UserDao.update(buyer);
                    sendResponse(exchange, 200, "Success");
                }
            } else {
                throw new ForbiddenException("Only buyers can have favorite restaurants.");
            }
        } else {
            throw new NotFoundException("User not found.");
        }
    }

    private void handle_UpdateFavorite(HttpExchange exchange, String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        if (user != null) {
            if (user instanceof Buyer buyer) {
                String[] paths = exchange.getRequestURI().getPath().split("/");
                long id = Long.parseLong(paths[paths.length - 1]);
                Restaurant mewRestaurant = RestaurantDao.getById(id);
                buyer.addFavoriteRestaurant(mewRestaurant);
                UserDao.update(buyer);
                sendResponse(exchange, 200, "Success");
            }
        }
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}
