package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entity.Order;
import jakarta.persistence.GeneratedValue;
import org.json.JSONArray;
import service.CourierService;
import service.OrderService;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import dao.*;
import entity.*;
import exception.*;
import org.json.JSONObject;



public class CourierHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Unauthorized");
            }
            String token = authHeader.substring(7);
            if(method.equals("GET")) {
                if(path.equals("/deliveries/available")) {
                    System.out.println(515151515);
                    GetAvailableRequests(exchange,token);
                }
                else{
                    throw new NotFoundException("path not found");
                }
            }else if(method.equals("POST")) {
                if(path.equals("/deliveries/history")) {
                    GetHistory(exchange,token);
                }else if(path.equals("/deliveries/orders")){
                    GetMyOrders(exchange,token);
                }
                else{
                    throw new NotFoundException("path not found");
                }
            }
            else if(method.equals("PUT")) {
                if(path.matches("/deliveries/\\d+")) {
                    String[] pathParts = path.split("/");
                    long order_id = Long.parseLong(pathParts[2]);
                    ChangeStatusOfRequest(exchange,order_id,token);
                }else {
                    throw new NotFoundException("path not found");
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
    private  void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private  void GetAvailableRequests(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        List<Order> orders= CourierService.GetaVailableRequests(phone);
        System.out.println(orders.size());
        JSONArray array = new JSONArray();
        for(Order order : orders){
            if(order.getStatus() != OrderStatus.NON_SUBMITTED){
                array.put(OrderService.convertOrderToJson(order));
            }
        }
        sendResponse(exchange,200,array.toString());
    }

    private void ChangeStatusOfRequest(HttpExchange exchange, long Order_Id,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        String status = jsonObject.getString("status");
        Order order = CourierService.ChangeStatusOfRequest(phone,Order_Id,status);
        JSONObject response = new JSONObject();
        response.put("message","Changed status successfully");
        JSONObject obj = OrderService.convertOrderToJson(order);
        response.put("order",obj);
        sendResponse(exchange,200,response.toString());
    }

    private void  GetHistory(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(phone);
        if(user == null){
            throw new UnauthorizedException("Unauthorized");
        }
        if(!(user instanceof Courier)){
            throw new ForbiddenException("Forbidden");
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        Set<Order> orders=CourierService.GetHistory(phone,jsonObject);
        JSONArray array = new JSONArray();
        for(Order order : orders){
            if(order.getStatus() != OrderStatus.NON_SUBMITTED){
                array.put(OrderService.convertOrderToJson(order));
            }
        }
        sendResponse(exchange,200,array.toString());
    }

    private void GetMyOrders(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(phone);
        if(user == null){
            throw new UnauthorizedException("Unauthorized");
        }
        if(!(user instanceof Courier)){
            throw new ForbiddenException("Forbidden");
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        String status = jsonObject.getString("status");
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        List<Order> orders = OrderDao.GetCourierOrders(user.getId(),orderStatus);
        JSONArray array = new JSONArray();
        for(Order order : orders){
            array.put(OrderService.convertOrderToJson(order));
        }
        sendResponse(exchange,200,array.toString());
    }





}
