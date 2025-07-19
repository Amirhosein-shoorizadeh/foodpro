      package HttpHandeler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.*;
import dto.FoodDto;
import entity.Food;
import exception.UnauthorizedException;
import org.json.JSONArray;
import org.json.JSONObject;
import util.JwtUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


      public class ItemHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Unauthorized");
        }
        String token = authHeader.substring(7); // حذف "Bearer "

        if ("GET".equalsIgnoreCase(method)) {
            if (path.matches("^/items/\\d+$")) {
                handleGetItemById(exchange,token);
            }else if(path.matches("/items")){
                GetItems(exchange,token);
            }
            else {
                sendResponse(exchange, 404, "Path not found");
            }
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleGetItemById(HttpExchange exchange,String token) throws IOException {
        try {
            String phone = JwtUtil.validateToken(token);
            Gson gson = new Gson();
            String[] paths = exchange.getRequestURI().getPath().split("/");
            long id = Long.parseLong(paths[paths.length - 1]);
            Food food = FoodDao.getFoodById(id);
            if (food == null) {
                sendResponse(exchange, 404, "Item not found");
                return;
            }
            FoodDto foodDto = new FoodDto(food);
            String json = gson.toJson(foodDto,FoodDto.class);
            sendResponse(exchange, 200, json);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid item ID format");
        } catch (SecurityException e) {
            sendResponse(exchange, 401, "Unauthorized");
        }
    }

    private void GetItems(HttpExchange exchange,String token) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        String phone = JwtUtil.validateToken(token);
        JSONObject jsonObject = new JSONObject(requestBody);
        String search = jsonObject.optString("search",null);
        long price = Long.parseLong(jsonObject.optString("price",null));
        JSONArray jsonArray = jsonObject.optJSONArray("keywords");
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            keywords.add(jsonArray.getString(i));
        }
        List<Food> foods =  FoodDao.searchFoods(search,price,keywords);
        List<FoodDto> dtos = foods.stream()
                .map(FoodDto::new)
                .toList();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(dtos);
        sendResponse(exchange, 200, json);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

}
