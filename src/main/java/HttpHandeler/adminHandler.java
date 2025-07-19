package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.CouponDao;
import dao.OrderDao;
import dao.PaymentTransactionDao;
import dao.UserDao;
import dto.CouponDto;
import dto.UserProfileDto;
import entity.*;
import exception.ForbiddenException;
import exception.InvalidUserDataException;
import exception.NotFoundException;
import exception.UnauthorizedException;
import entity.*;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import service.OrderService;
import util.JwtUtil;

import java.time.LocalDate;
import java.util.stream.Collectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class adminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            String path = exchange.getRequestURI().getPath();
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());

            if (exchange.getRequestMethod().equals("GET")) {
                if (path.equals("/admin/users")) {
                    handle_UserList(exchange, body);
                } else if (path.equals("/admin/orders")) {
                    AdminSearchOrder(exchange, body);
                } else if (path.equals("/admin/coupons")) {
                    handle_CouponList(exchange, body);
                } else if (path.equals("/admin/coupons/\\d+")) {
                    handle_getcoupons(exchange);
                } else if(path.equals("/admin/transactions")) {
                    AdminSearchTransaction(exchange, body);
                }else {throw new NotFoundException("Not Found Path");}
            } else if (exchange.getRequestMethod().equals("POST")) {
                if (path.equals("/admin/coupons")) {
                    handle_CreateCoupon(exchange, body);
                }
            } else if (exchange.getRequestMethod().equals("PATCH")) {
                if (path.equals("/admin/users/\\d+/status")) {
                    handle_UserApproval(exchange, body);
                }
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                if (path.equals("/admin/coupons/\\d+")) {
                    handle_DeleteCoupon(exchange);
                }
            } else if (exchange.getRequestMethod().equals("PUT")) {
                if (path.matches("/admin/coupons/\\d+")) {
                    handle_UpdateCoupon(exchange, body);
                }
            } else if (exchange.getRequestMethod().equals("HEAD")) {
            } else {
                System.out.println("Invalid request");
            }
        } catch (UnauthorizedException e) {
            sendResponse(exchange, 401, "Unauthorized");
        } catch (ForbiddenException e) {
            sendResponse(exchange, 403, "Forbidden");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "Not Found");
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handle_UserList(HttpExchange exchange, String body) throws IOException {
        Gson gson = new Gson();
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);

        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }

        var users = UserDao.getAllExceptAdmins();
        var dtoList = users.stream().map(u -> new UserProfileDto(u.getId(), u.getFull_name(), u.getPhone(), u.getEmail(), u.getClass().getSimpleName(), // role
                u.getAddress(), u.getProfileImageBase64(), u.getBankinfo() != null ? new Bankinfo(u.getBankinfo().getBank_name(), u.getBankinfo().getAccount_number()) : null)).toList();

        String json = gson.toJson(dtoList);
        sendResponse(exchange, 200, json);


    }

    private void handle_UserApproval(HttpExchange exchange, String body) throws IOException {
        Gson gson = new Gson();
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);
        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }
        String path = exchange.getRequestURI().getPath();
        Long userId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
        User change = UserDao.findById(userId);
        change.setUser_status(User_Status.Availble);
        UserDao.update(change);

    }

    private void handle_CreateCoupon(HttpExchange exchange, String body) throws IOException {
        try {
            Gson gson = new Gson();
            Headers headers = exchange.getRequestHeaders();
            String authHeader = headers.getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized");
            }

            String token = authHeader.substring(7);
            String phone = JwtUtil.validateToken(token);

            var user = UserDao.getByPhone(phone);
            if (!(user instanceof Admin)) {
                throw new ForbiddenException("Forbidden");
            }

            CouponDto dto = gson.fromJson(body, CouponDto.class);

            Coupon coupon = dto.toEntity();

            CouponDao.save(coupon);

            sendResponse(exchange, 201, "{\"message\": \"Coupon created successfully\"}");
        } catch (InvalidUserDataException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid coupon data: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            throw e;
        }

    }

    private void handle_CouponList(HttpExchange exchange, String body) throws IOException {
        Gson gson = new Gson();
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }
        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);

        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }
        var coupons = CouponDao.getAll();
        var dtoList = coupons.stream().map(c ->
                new CouponDto(
                        c.getId(),
                        c.getCouponCode(),
                        c.getType().name(),
                        c.getValue(),
                        c.getMinPrice(),
                        c.getUserCount(),
                        c.getStartDate().toString(),
                        c.getEndDate().toString()
                )).toList();

        String json = gson.toJson(dtoList);
        sendResponse(exchange, 200, json);
    }

    private void handle_UpdateCoupon(HttpExchange exchange, String body) throws IOException {
        Gson gson = new Gson();
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);

        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }

        String path = exchange.getRequestURI().getPath();
        Long couponId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));

        CouponDto CouponDto = gson.fromJson(body, CouponDto.class);

        Coupon updated = new Coupon();
        updated.setId(couponId);
        updated.setCouponCode(CouponDto.getCoupon_code());
        updated.setType(Coupon.Type.valueOf(CouponDto.getType()));
        updated.setValue(CouponDto.getValue());
        updated.setMinPrice(CouponDto.getMin_price());
        updated.setUserCount(CouponDto.getUser_count());
        updated.setStartDate(LocalDate.parse(CouponDto.getStart_date()));
        updated.setEndDate(LocalDate.parse(CouponDto.getEnd_date()));

        boolean success = CouponDao.update(updated);
        if (!success) {
            throw new RuntimeException("Failed to update coupon");
        }

        sendResponse(exchange, 200, "{\"message\": \"Coupon updated successfully\"}");
    }

    private void handle_DeleteCoupon(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);

        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }

        String path = exchange.getRequestURI().getPath();
        Long couponId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));

        boolean success = CouponDao.deleteById(couponId);
        if (!success) {
            throw new NotFoundException("Coupon not found");
        }

        sendResponse(exchange, 200, "{\"message\": \"Coupon deleted successfully\"}");
    }

    private void handle_getcoupons(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);


        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }

        String path = exchange.getRequestURI().getPath();
        Long couponId = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));

        Coupon coupon = CouponDao.getByCode(couponId);
        if (coupon == null) {
            throw new NotFoundException("Coupon not found");
        }

        CouponDto dto = new CouponDto(coupon);
        String json = new Gson().toJson(dto);

        sendResponse(exchange, 200, json);
    }

    private void AdminSearchOrder(HttpExchange exchange, String body) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);
        if (phone == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }

        JSONObject jsonObject = new JSONObject(body);
        String Search = jsonObject.optString("search",null);
        String Vendor  = jsonObject.optString("vendor",null);
        String Courier = jsonObject.optString("courier",null);
        String Customer = jsonObject.optString("customer",null);
        String Status  = jsonObject.optString("status",null);
        OrderStatus orderStatus = OrderStatus.valueOf(Status.toUpperCase());
        Set<Order> orders = OrderDao.AdminSearchOrders(Vendor,orderStatus,Search,Customer,Courier);
        JSONArray jsonArray = new JSONArray();
        for (Order order : orders) {
            jsonArray.put(OrderService.convertOrderToJson(order));
        }
        sendResponse(exchange, 200, jsonArray.toString());
    }

    private void AdminSearchTransaction(HttpExchange exchange, String body) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7);
        String phone = JwtUtil.validateToken(token);
        if (phone == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        var user = UserDao.getByPhone(phone);
        if (!(user instanceof Admin)) {
            throw new ForbiddenException("Forbidden");
        }

        JSONObject jsonObject = new JSONObject(body);
        String Search = jsonObject.optString("search",null);
        String User  = jsonObject.optString("user",null);
        String Method  = jsonObject.optString("method",null);
        String Status  = jsonObject.optString("status",null);
        Set<PaymentTransaction> TransActions = PaymentTransactionDao.GetTransactionSet(Search,User,Method,Status);
        JSONArray jsonArray = new JSONArray();
        for (PaymentTransaction transaction : TransActions) {
            JSONObject transactionJson = new JSONObject();
            transactionJson.put("id",transaction.getId());
            transactionJson.put("order_id",transaction.getOrder().getId());
            transactionJson.put("user_id",transaction.getBuyer().getId());
            transactionJson.put("method",transaction.getMethod());
            transactionJson.put("status",transaction.getStatus());
            jsonArray.put(transactionJson);
        }
        sendResponse(exchange, 200, jsonArray.toString());
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

}

