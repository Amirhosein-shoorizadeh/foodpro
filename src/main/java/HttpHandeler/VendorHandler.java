package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.RestaurantDao;
import dao.UserDao;
import entity.*;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import util.JwtUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VendorHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try{String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized");
            }
            String token = authHeader.substring(7); // حذف "Bearer "
            if ("POST".equalsIgnoreCase(method)) {
                if(path.equals("/vendors")) {
                    ListVendorsWithFilters(exchange,body,token);
                }

            }else if ("GET".equalsIgnoreCase(method)) {
                if(path.matches("/vendors/\\d+")) {
                    String[] pathParts = path.split("/");
                    long restaurant_id = Long.parseLong(pathParts[2]);
                    ViewMenuItems(exchange,token,restaurant_id);
                }
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

    private void ListVendorsWithFilters(HttpExchange exchange,String body,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if (phone == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        JSONObject obj = new JSONObject(body);
        String Search = obj.optString("search",null);
        JSONArray jsonArray = obj.optJSONArray("keywords");
        List<String> KeyWords = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                KeyWords.add(jsonArray.optString(i).toLowerCase());
            }
        }
        User user = UserDao.getByPhone(phone);
        if (user != null) {
            if(user instanceof Buyer){
                List<Restaurant> restaurants  =   RestaurantDao.searchRestaurants(Search,KeyWords);
                JSONArray restaurantArray = new JSONArray();
                for (Restaurant restaurant : restaurants) {
                    JSONObject restaurantObj = new JSONObject();
                    restaurantObj.put("id", restaurant.getId());
                    restaurantObj.put("name", restaurant.getName());
                    restaurantObj.put("address", restaurant.getAddress());
                    restaurantObj.put("phone", restaurant.getPhone());
                    restaurantObj.put("logoBase64",restaurant.getLogobase64());
                    restaurantObj.put("tax_fee",restaurant.getTax_fee());
                    restaurantObj.put("additional_fee",restaurant.getAdditional_fee());
                    restaurantArray.put(restaurantObj);
                }
                sendResponse(exchange, 200, restaurantArray.toString());
            }else {throw new ForbiddenException("Forbidden");}
        }else {throw new UnauthorizedException("Unauthorized");}
    }

    private void ViewMenuItems(HttpExchange exchange,String token,long restaurant_id) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if (phone == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(phone);
        if (user != null) {
            if(user instanceof Buyer){
                Restaurant restaurant = RestaurantDao.getById(restaurant_id);
                if (restaurant != null) {
                    JSONObject mainObj = new JSONObject();
                    JSONObject vendor = new JSONObject();
                    vendor.put("id", restaurant.getId());
                    vendor.put("name", restaurant.getName());
                    vendor.put("address", restaurant.getAddress());
                    vendor.put("phone", restaurant.getPhone());
                    vendor.put("logoBase64",restaurant.getLogobase64());
                    vendor.put("tax_fee",restaurant.getTax_fee());
                    vendor.put("additional_fee",restaurant.getAdditional_fee());
                    mainObj.put("vendor", vendor);
                    JSONArray menu_titles = new JSONArray();
                    List<Menu> menus = restaurant.getMenus();
                    for(Menu menu : menus){
                        menu_titles.put(menu.getTitle());
                        Set<Food> foods = menu.getFoods();
                        JSONArray foodArray = new JSONArray();
                        for(Food food : foods){
                            JSONObject foodObj = new JSONObject();
                            foodObj.put("id", food.getId());
                            foodObj.put("name", food.getName());
                            foodObj.put("price", food.getPrice());
                            foodObj.put("imageBase64",food.getImageBase64());
                            foodObj.put("description",food.getDescription());
                            foodObj.put("vendor_id",food.getRestaurant().getId());
                            foodObj.put("supply",food.getSupply());
                            foodObj.put("keywords",food.getKeywords());
                            foodArray.put(foodObj);
                        }
                        mainObj.put( menu.getTitle(),foodArray);
                    }
                    mainObj.put("menu_titles", menu_titles);
                    sendResponse(exchange, 200, mainObj.toString());
                }else throw new NotFoundException("Not found Restaurant");
            }else throw new ForbiddenException("Forbidden");
        }else throw new UnauthorizedException("Unauthorized");
    }
}
