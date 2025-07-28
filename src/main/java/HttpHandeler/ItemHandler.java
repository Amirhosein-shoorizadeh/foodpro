      package HttpHandeler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.*;
import dto.FoodDto;
import entity.Buyer;
import entity.Food;
import entity.User;
import exception.ForbiddenException;
import exception.NotFoundException;
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
            }else if(path.equals("/items/best")){
                GetBestItems(exchange,token);
            }
            else {
                sendResponse(exchange, 404, "Path not found");
            }
        }else if("POST".equalsIgnoreCase(method)) {
             if(path.matches("/items")){
                GetItems(exchange,token);
            }

        }
        else {
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
        double price = Double.parseDouble(jsonObject.optString("price",null));
        JSONArray jsonArray = jsonObject.optJSONArray("keywords");

        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            keywords.add(jsonArray.getString(i));
        }

        int page = jsonObject.optInt("page",1);
        int pageSize = jsonObject.optInt("pageSize",10);

        System.out.println(454545);

        List<Food> foods =  FoodDao.searchFoods(search,price,keywords,page,pageSize);
        List<FoodDto> dtos = foods.stream()
                .map(FoodDto::new)
                .toList();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(dtos);
        sendResponse(exchange, 200, json);
    }

    private void GetBestItems(HttpExchange exchange,String token) throws IOException {
        try{String phone = JwtUtil.validateToken(token);
            User user = UserDao.getByPhone(phone);
            if (user == null) {
                throw new UnauthorizedException("Unauthorized");
            }
            if(user instanceof Buyer){
                List<Food> foods = FoodDao.getTopRatedFoods(10);
                JSONArray jsonArray = new JSONArray();
                for (Food food : foods) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id",food.getId());
                    jsonObject.put("name",food.getName());
                    jsonObject.put("price",food.getPrice());
                    jsonObject.put("keywords",new JSONArray(food.getKeywords()));
                    jsonObject.put("imageBase64",food.getImageBase64());
                    jsonObject.put("description",food.getDescription());
                    jsonObject.put("supply",food.getSupply());
                    jsonObject.put("vendor_id",food.getRestaurant().getId());
                    jsonArray.put(jsonObject);
                }
                sendResponse(exchange, 200, jsonArray.toString());
            }else{throw new ForbiddenException("Forbidden"); }
        }catch (UnauthorizedException e){
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (NotFoundException e){
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }

    }


    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

}
