package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import dao.UserDao;
import dto.SignupResponseDto;
import dto.UserProfileDto;
import org.glassfish.grizzly.http.util.Header;
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
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());

        if ("POST".equalsIgnoreCase(method)) {

            if (path.equals("/auth/register")) {
                handleSignup(exchange, body);
            } else if (path.equals("/auth/login")) {
                handleLogin(exchange, body);
            } else {
                sendResponse(exchange, 404, "Path not found");
            }
        }
        else if ("GET".equalsIgnoreCase(method)) {
            if(path.equals("/auth/profile)")) {
                GetCurrentProfile(exchange);
            }
            else {
                sendResponse(exchange, 404, "Path not found");
            }
        }
        else if ("PUT".equalsIgnoreCase(method)) {
            if(path.matches("/auth/profile/\\d+")){
                UpdateCurrentProfile(exchange, body);

            }else {
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
            long userId = UserManager.handleSignup(temp);
            String token = JwtUtil.generateToken(temp.phone);
            SignupResponseDto signtemp = new  SignupResponseDto("registered successfullyl",userId,token);
            String json = gson.toJson(signtemp);
            sendResponse(exchange, 200, json);

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
    private void GetCurrentProfile(HttpExchange exchange) throws IOException {
        try {
            Gson gson = new Gson();
            Headers hedders = exchange.getRequestHeaders();
            String authHeader  = hedders.getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String phone = JwtUtil.validateToken(token);
                if(phone == null) {
                    sendResponse(exchange, 401, "{\"error\": \"" + "Unauthorized" + "\"}");
                    return;
                }
                UserProfileDto userProfileDto= UserManager.GetCurrentProfile(phone);
                String json = gson.toJson(userProfileDto);
                sendResponse(exchange, 200, json);
            }else {
                sendResponse(exchange, 401, "{\"error\": \"" + "Unauthorized" + "\"}");
            }

        }catch (InvalidUserDataException  e) {
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void UpdateCurrentProfile(HttpExchange exchange, String body) throws IOException {
        try {
            Gson gson = new Gson();
            Headers hedders = exchange.getRequestHeaders();
            String authHeader  = hedders.getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String phone = JwtUtil.validateToken(token);
                if(phone == null) {
                    sendResponse(exchange, 401, "{\"error\": \"" + "Unauthorized" + "\"}");
                    return;
                }
                UserProfileDto userProfileDto = gson.fromJson(body,UserProfileDto.class);
                UserManager.UpdateUserProfile(userProfileDto, phone);
                sendResponse(exchange, 200,"{\"message\": \"" + "updated successfully" + "\"}");

            }else {
                sendResponse(exchange, 401, "{\"error\": \"" + "Unauthorized" + "\"}");
            }

        }catch ( ConflictExceptin e ) {
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NotFoundException e){
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (Exception e) {
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

