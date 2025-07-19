package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.FoodDto;
import dto.RestaurantDto;
import entity.Food;
import entity.Order;
import entity.Restaurant;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import service.OrderService;
import service.RestaurantService;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class RestaurantHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized");
            }
            String token = authHeader.substring(7); // حذف "Bearer "
            if (method.equals("GET")) {
                if(path.equals("/restaurants/mine")) {
                    GetListRestaurant(exchange,token);
                }else if(path.matches("/restaurants/\\d+/orders")) {
                    String[] pathParts = path.split("/");
                    long restaurantId = Long.parseLong(pathParts[2]);
                    GetListOfOrders(exchange,restaurantId,token);
                }
            }
            else if (method.equals("POST")) {
                if (path.equals("/restaurants")) {
                    CreateRestaurant(exchange,token);
                }else if(path.matches("/restaurants/\\d+/item")) {
                    String[] pathParts = path.split("/");
                    long id = Long.parseLong(pathParts[2]);
                    AddFoodToRestaurant(exchange, id,token);
                }else if(path.matches("/restaurants/\\d+/menu")) {
                    String[] pathParts = path.split("/");
                    long id = Long.parseLong(pathParts[2]);
                    AddMenu(exchange, id,token);
                }
            }
            else if (method.equals("PUT")) {
                if (path.matches("/restaurants/\\d+")) {
                    String[] pathParts = path.split("/");
                    long id = Long.parseLong(pathParts[pathParts.length - 1]);
                    UpdateRestaurant(exchange, id,token);
                }else if(path.matches("/restaurants/\\d+/item/\\d+")) {
                    String[] pathParts = path.split("/");
                    long restaurantId = Long.parseLong(pathParts[2]);
                    long foodId = Long.parseLong(pathParts[4]);
                    UpdateFood(exchange, restaurantId, foodId,token);
                }else if(path.matches("/restaurants/\\d+/menu/.+")) {
                    String[] pathParts = path.split("/");
                    long restaurantId = Long.parseLong(pathParts[2]);
                    String title = pathParts[4];
                    AddFoodToMenu(exchange, restaurantId, title,token);
                }
            }
            else if (method.equals("DELETE")) {
                if(path.matches("/restaurants/\\d+/item/\\d+")){
                    String[] pathParts = path.split("/");
                    long restaurantId = Long.parseLong(pathParts[2]);
                    long foodId = Long.parseLong(pathParts[4]);
                    DeleteFood(exchange, restaurantId, foodId,token);
                }else if(path.matches("/restaurants/\\d+/menu/.+") && (path.split("/").length == 5) ){
                    String[] pathParts = path.split("/");
                    long restaurantId = Long.parseLong(pathParts[2]);
                    String title = pathParts[4];
                    DeleteMenu(exchange, restaurantId, title,token);
                }else if(path.matches("/restaurants/\\d+/menu/.+/\\d+") && (path.split("/").length == 6)) {
                    System.out.println(6666);
                    String[] pathParts = path.split("/");
                    long restaurantId = Long.parseLong(pathParts[2]);
                    String title = pathParts[4];
                    long foodId = Long.parseLong(pathParts[5]);
                    DeleteFoodFromMenu(exchange,restaurantId,title,foodId,token);
                }
            }else if(method.equals("PATCH")) {
                if (path.equals("/restaurants/orders/\\d+")) {
                    String[] pathParts = path.split("/");
                    long OrderId = Long.parseLong(pathParts[3]);
                    ChangeStatusOfOrder(exchange, OrderId,token);
                }
            }
            else{
                throw new NotFoundException("Not found method");
            }
        }catch (UnauthorizedException e){
            sendResponse(exchange, 401, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (NotFoundException e){
            sendResponse(exchange, 404, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (ForbiddenException e){
            sendResponse(exchange, 403, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (InvalidUserDataException e){
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (ConflictExceptin e){
            sendResponse(exchange, 409, "{\"error\": \"" + e.getMessage() + "\"}");
        }catch (Exception e){
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }

    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    private void CreateRestaurant(HttpExchange exchange,String token) throws IOException {

        Gson gson = new Gson();

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        RestaurantDto restaurantDto = gson.fromJson(requestBody, RestaurantDto.class);
        long id = RestaurantService.createRestaurant(token, restaurantDto);
        restaurantDto.setId(id);

        String responseJson = gson.toJson(restaurantDto,RestaurantDto.class);
        sendResponse(exchange, 201, responseJson);

    }

    private void GetListRestaurant(HttpExchange exchange,String token) throws IOException {
        List<Restaurant> restaurants = RestaurantService.getSellerRestaurants(token);
        JSONArray response = new JSONArray();
        for (Restaurant restaurant : restaurants) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", restaurant.getId());
            jsonObject.put("name", restaurant.getName());
            jsonObject.put("address", restaurant.getAddress());
            jsonObject.put("phone", restaurant.getPhone());
            jsonObject.put("logoBase64",restaurant.getLogobase64());
            jsonObject.put("tax_fee",restaurant.getTax_fee());
            jsonObject.put("additional_fee",restaurant.getAdditional_fee());
            response.put(jsonObject);
        }
        sendResponse(exchange, 200, response.toString());
    }

    private void UpdateRestaurant(HttpExchange exchange,long restaurant_id,String token) throws IOException {

        Gson gson = new Gson();

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

    }

    private void AddFoodToRestaurant(HttpExchange exchange,long restaurant_id,String token) throws IOException {

        Gson gson = new Gson();

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        FoodDto foodDto = gson.fromJson(requestBody, FoodDto.class);
        String phone = JwtUtil.validateToken(token);
        FoodDto foodDtoOut = RestaurantService.AddFood(phone,foodDto,restaurant_id);
        String json = gson.toJson(foodDtoOut, FoodDto.class);
        sendResponse(exchange, 200, json);
    }

    private void UpdateFood(HttpExchange exchange,long restaurant_id,long food_id,String token) throws IOException {

        Gson gson = new Gson();
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");

        FoodDto foodDto = gson.fromJson(requestBody, FoodDto.class);
        foodDto.id = food_id;
        foodDto.vendor_id=restaurant_id;
        String phone = JwtUtil.validateToken(token);

        foodDto = RestaurantService.UpdateFood(phone,foodDto,restaurant_id);
        String json = gson.toJson(foodDto, FoodDto.class);
        sendResponse(exchange, 200, json);

    }

    private void DeleteFood(HttpExchange exchange,long restaurant_id,long food_id,String token) throws IOException {

        String phone = JwtUtil.validateToken(token);

        RestaurantService.DeleteFood(phone,restaurant_id,food_id);
        sendResponse(exchange, 200,"{\"message\": \"Food item removed successfully\"}" );

    }

    private void AddMenu(HttpExchange exchange,long restaurant_id,String token) throws IOException {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        String phone = JwtUtil.validateToken(token);

        JSONObject json = new JSONObject(requestBody);
        String title = json.getString("title");
        RestaurantService.AddMenu(phone,restaurant_id,title);
        sendResponse(exchange, 200,"{\"title\": \"" + title+ "\"}");
    }
    private void DeleteMenu(HttpExchange exchange,long restaurant_id,String title,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);

        RestaurantService.DeleteMenu(phone,restaurant_id,title);
        sendResponse(exchange, 200,"{\"message\": \"Food menu removed from restaurant successfully\"}" );
    }

    private void AddFoodToMenu(HttpExchange exchange,long restaurant_id,String title,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        long foodId = jsonObject.getLong("item_id");
        RestaurantService.AddFoodToMenu(phone,restaurant_id,foodId,title);
        sendResponse(exchange, 200,"{\"message\": \"Food item created and added to restaurant successfully\"}" );
    }
    private void DeleteFoodFromMenu(HttpExchange exchange,long restaurant_id,String title,long food_id,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        System.out.println(888);

        System.out.println(5555);
        RestaurantService.DeleteFoodFromMenu(phone,restaurant_id,food_id,title);
        sendResponse(exchange, 200,"{\"message\": \"Item removed from restaurant menu successfully\"}" );
    }
    private void GetListOfOrders(HttpExchange exchange,long restaurant_id,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        Set<Order> orders =RestaurantService.GetListOfOrder(phone,restaurant_id,jsonObject);
        JSONArray array = new JSONArray();
        for(Order order : orders){
            array.put(OrderService.convertOrderToJson(order));
        }
        sendResponse(exchange,200,array.toString());
    }

    private void ChangeStatusOfOrder(HttpExchange exchange,long order_id,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        String status = jsonObject.getString("status");
        RestaurantService.ChangeStatusOfOrder(phone,order_id,status);
        sendResponse(exchange, 200,"{\"message\": \"" + "Order status changed successfully" + "\"}");
    }



}