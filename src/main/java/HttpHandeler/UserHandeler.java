package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import dao.TokenDao;
import dao.UserDao;
import dto.SignupResponseDto;
import dto.UserProfileDto;
import entity.Token;
import dto.SignUpManager;
import service.UserManager;
import entity.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import exception.*;
import util.JwtUtil;
import service.*;

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
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());

        if ("POST".equalsIgnoreCase(method)) {

            if (path.equals("/auth/register")) {
                handleSignup(exchange, body);
            } else if (path.equals("/auth/login")) {
                handleLogin(exchange, body);
            } else if (path.equals("/auth/logout")) {
                handleLogout(exchange);

            } else {
                sendResponse(exchange, 404, "Path not found");
            }
        } else if ("GET".equalsIgnoreCase(method)) {
            if (path.equals("/auth/profile")) {
                GetCurrentProfile(exchange);
            } else {
                sendResponse(exchange, 404, "Path not found");
            }
        } else if ("PUT".equalsIgnoreCase(method)) {
            if (path.matches("/auth/profile")) {
                UpdateCurrentProfile(exchange, body);

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
            if (InvalidInput.checkInput_Register(temp)) {
                long userId = UserManager.handleSignup(temp);
                String token = JwtUtil.generateToken(temp.phone);
                Token tokenEntity = new Token(token, temp.phone, JwtUtil.getExpirationDate(token), JwtUtil.getExpirationDate(token), false);
                TokenDao.save(tokenEntity);
                SignupResponseDto signtemp = new SignupResponseDto("registered successfullyl", userId, token);
                String json = gson.toJson(signtemp);
                sendResponse(exchange, 200, json);
            }
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
                Token tokenEntity = new Token(token, loginDto.phone, JwtUtil.getExpirationDate(token), JwtUtil.getExpirationDate(token), false);
                TokenDao.save(tokenEntity);
                sendResponse(exchange, 200, "{\"token\": \"" + token + "\"}");
            } else {
                sendResponse(exchange, 401, "Invalid credentials");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            TokenDao.revoke(token);
            Token tokenTemp = TokenDao.findByToken(token);
            TokenDao.delete(tokenTemp);
            sendResponse(exchange, 200, "{\"message\": \"Logged out successfully\"}");
        } else {
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
        }
    }

    private void GetCurrentProfile(HttpExchange exchange) throws IOException {
        try {
            Gson gson = new Gson();
            Headers hedders = exchange.getRequestHeaders();
            String authHeader = hedders.getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String phone = JwtUtil.validateToken(token);
                if (phone == null) {
                    sendResponse(exchange, 401, "{\"error\": \"" + "Unauthorized" + "\"}");
                    return;
                }
                UserProfileDto userProfileDto = UserManager.GetCurrentProfile(phone);
                String json = gson.toJson(userProfileDto);
                sendResponse(exchange, 200, json);
            } else {
                sendResponse(exchange, 401, "{\"error\": \"" + "Unauthorized" + "\"}");
            }

        } catch (InvalidUserDataException e) {
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void UpdateCurrentProfile(HttpExchange exchange, String body) throws IOException {
        try {
            Gson gson = new Gson();
            Headers hedders = exchange.getRequestHeaders();
            String authHeader = hedders.getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String phone = JwtUtil.validateToken(token);
                if (phone == null) {
                    throw new UnauthorizedException("Unauthrized");
                }
                UserProfileDto userProfileDto = gson.fromJson(body, UserProfileDto.class);
                if (InvalidInput.checkInput_EditProfile(userProfileDto)) {
                    UserManager.UpdateUserProfile(userProfileDto, phone);
                    sendResponse(exchange, 200, "{\"message\": \"" + "updated successfully" + "\"}");
                }
            } else {
                throw new UnauthorizedException("Unauthorized");
            }

        } catch (UnauthorizedException e) {
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (ConflictExceptin e) {
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
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

