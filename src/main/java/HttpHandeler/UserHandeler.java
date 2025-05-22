package HttpHandeler;

import com.google.gson.Gson;
import dao.UserDao;
import service.SignUpManager;
import service.UserManager;
import entity.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import exception.*;
import util.JwtUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class UserHandeler implements HttpHandler {
    static class LoginDto {
        String phone;
        String password;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equalsIgnoreCase(method)) {
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());

            if (path.equals("/user/signup")) {
                handleSignup(exchange, body); // فقط همین خط کافیه
            } else if (path.equals("/user/login")) {
                handleLogin(exchange, body);
            } else {
                sendResponse(exchange, 404, "Path not found");
            }
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleSignup(HttpExchange exchange, String body) throws IOException {
        try {
            Gson gson = new Gson();
            SignUpManager temp = gson.fromJson(body, SignUpManager.class);
            UserManager.handleSignup(temp);
            sendResponse(exchange, 200, "User registered successfully");

        } catch (UserAlreadyExistsException e) {
            sendResponse(exchange, 409, e.getMessage());
        } catch (InvalidUserDataException e) {
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void handleLogin(HttpExchange exchange, String body) throws IOException {
        try {
            Gson gson = new Gson();
            LoginDto loginDto = gson.fromJson(body, LoginDto.class);
            User user = UserDao.login(loginDto.phone, loginDto.password);
            if (user != null) {
                String token = JwtUtil.generateToken(loginDto.phone);
                sendResponse(exchange, 200, "{\"token\": \"" + token + "\"}");
            } else {
                sendResponse(exchange, 401, "Invalid credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error");
        }
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }


}

