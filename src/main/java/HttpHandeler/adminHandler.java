package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UserDao;
import dto.UserProfileDto;
import entity.Admin;
import entity.Bankinfo;
import exception.*;
import service.InvalidInput;
import service.UserManager;
import util.JwtUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class adminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());

            if (exchange.getRequestMethod().equals("GET")) {
                if (path.equals("/admin/users")) {
                    handle_UserList(exchange, body);
                }
            } else if (exchange.getRequestMethod().equals("POST")) {
            } else if (exchange.getRequestMethod().equals("PUT")) {
            } else if (exchange.getRequestMethod().equals("DELETE")) {
            } else if (exchange.getRequestMethod().equals("HEAD")) {
            } else {
                sendResponse(exchange, 404, "Path not found");
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
//            Headers headers = exchange.getRequestHeaders();
//            String authHeader = headers.getFirst("Authorization");
//
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                throw new UnauthorizedException("Unauthorized");
//            }
//
//            String token = authHeader.substring(7);
//            String phone = JwtUtil.validateToken(token);
//
//            if (phone == null) {
//                throw new UnauthorizedException("Unauthorized");
//            }
//
//            var user = UserDao.getByPhone(phone);
//            if (!(user instanceof Admin)) {
//                throw new ForbiddenException("Forbidden");
//            }

            var users = UserDao.getAllExceptAdmins();
        var dtoList = users.stream()
                .map(u -> new UserProfileDto(
                        u.getId(),
                        u.getFull_name(),
                        u.getPhone(),
                        u.getEmail(),
                        u.getClass().getSimpleName(), // role
                        u.getAddress(),
                        u.getProfileImageBase64(),
                        u.getBankinfo() != null
                                ? new Bankinfo(u.getBankinfo().getBank_name(), u.getBankinfo().getAccount_number())
                                : null
                )).toList();

        String json = gson.toJson(dtoList);
            sendResponse(exchange, 200, json);


    }


    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}

