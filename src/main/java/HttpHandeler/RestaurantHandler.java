package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.RestaurantDto;
import entity.Restaurant;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import service.RestaurantService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class RestaurantHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (method.equals("GET")) {
            if(path.equals("/restaurants/mine")) {
                GetListRestaurant(exchange);
            }
        }
        else if (method.equals("POST")) {
             if (path.equals("/restaurants")) {
                 CreateRestaurant(exchange);
             }
        }
        else if (method.equals("PUT")) {
            if (path.equals("/restaurants/\\\\d+")) {
                String[] pathParts = path.split("/");
                long id = Long.parseLong(pathParts[pathParts.length - 1]);
                UpdateRestaurant(exchange, id);
            }
        }
        else if (method.equals("DELETE")) {}

    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    private void CreateRestaurant(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }

        String token = authHeader.substring(7); // حذف "Bearer "
        Gson gson = new Gson();
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
            RestaurantDto restaurantDto = gson.fromJson(requestBody, RestaurantDto.class);
            RestaurantService.createRestaurant(token, restaurantDto);

           String responseJson = gson.toJson(restaurantDto,RestaurantDto.class);

            sendResponse(exchange, 201, responseJson);
        }catch (NotFoundException e){
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (ForbiddenException e){
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (UnauthorizedException e){
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (ConflictExceptin e){
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (Exception e){
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void GetListRestaurant(HttpExchange exchange) throws IOException {
        Gson gson = new Gson();
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized");
            }

            String token = authHeader.substring(7);

            List<Restaurant> restaurants = RestaurantService.getSellerRestaurants(token);

            String responseJson = gson.toJson(restaurants);

            sendResponse(exchange, 200, responseJson);

        } catch (UnauthorizedException e) {
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (ForbiddenException e) {
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void UpdateRestaurant(HttpExchange exchange,long restaurant_id) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }
        String token = authHeader.substring(7);
        Gson gson = new Gson();
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
            RestaurantDto restaurantDto = gson.fromJson(requestBody, RestaurantDto.class);
            restaurantDto.setId(restaurant_id);

            RestaurantService.UpdateRestaurant(token, restaurantDto);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",restaurantDto.getName());
            jsonObject.put("address",restaurantDto.getAddress());
            jsonObject.put("phone",restaurantDto.getPhone());
            jsonObject.put("logoBase64",restaurantDto.getLogoBase64());
            jsonObject.put("tax_fee",restaurantDto.getTax_fee());
            jsonObject.put("additional_fee",restaurantDto.getAdditional_fee());
            sendResponse(exchange, 200, jsonObject.toString());
        }catch (NotFoundException e){
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (ForbiddenException e){
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (UnauthorizedException e){
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (ConflictExceptin e){
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (Exception e){
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }

    }
}