package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.OrderDao;
import dao.PaymentTransactionDao;
import dao.RestaurantDao;
import dao.UserDao;
import entity.*;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import service.OrderService;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;


public class OrderHandler implements HttpHandler {

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
            if(method.equals("GET")) {
                if(path.equals("/transactions")) {
                    GetTransactionHistory(exchange,token);
                }else if(path.matches("/orders/\\d+")) {
                    String[] pathParts = path.split("/");
                    long order_id = Long.parseLong(pathParts[2]);
                    GetOrderWithId(exchange,token,order_id);
                }else if(path.equals("/orders/history")) {
                    GetOrderHistory(exchange,token);
                }
                else{throw new NotFoundException("Not Found PATH");}
            }
            else if(method.equals("POST")) {
                if(path.equals("/wallet/top-up")) {
                    TopUpWallet(exchange,token);
                }else if(path.equals("/payment/online")) {
                    MakeOnlinePayment(exchange,token);
                }else if(path.equals("/orders")) {
                    SubmitOrder(exchange,token);
                }
                else {throw new NotFoundException("Not Found PATH");}
            }else {throw new NotFoundException("Not Found Method");}

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

    public void GetTransactionHistory(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        List<PaymentTransaction> paymentTransactions = OrderService.GetTransactionHistory(phone);
        JSONArray payments = new JSONArray();
        for(PaymentTransaction paymentTransaction : paymentTransactions){
            JSONObject transaction = new JSONObject();
            transaction.put("id", paymentTransaction.getId());
            transaction.put("order_id", paymentTransaction.getOrder().getId());
            transaction.put("user_id",paymentTransaction.getBuyer().getId());
            transaction.put("method", paymentTransaction.getMethod().name());
            transaction.put("status", paymentTransaction.getStatus().name());
            payments.put(transaction);
        }
        sendResponse(exchange, 200, payments.toString());
    }

    public void TopUpWallet(HttpExchange exchange,String token) throws IOException {

        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        long amount = jsonObject.getLong("amount");
        if(amount <= 0){
            throw new InvalidUserDataException("Amount must be greater than zero");
        }
        OrderService.TopUpWallet(phone, amount);
        sendResponse(exchange, 200, "{\"status\": \"ok\"}");
    }

    public void MakeOnlinePayment(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        long Order_id = jsonObject.getLong("order_id");
        String Method = jsonObject.getString("method");
        PaymentTransaction paymentTransaction = OrderService.MakeOnlinePayment(phone, Order_id, Method);
        PaymentTransactionDao.savePaymentTransaction(paymentTransaction);
        JSONObject response = new JSONObject();
        response.put("id", paymentTransaction.getId());
        response.put("order_id", paymentTransaction.getOrder().getId());
        response.put("user_id", paymentTransaction.getBuyer().getId());
        response.put("method", Method);
        response.put("status", paymentTransaction.getStatus().name());
        sendResponse(exchange, 200, response.toString());
    }

    private void GetOrderWithId(HttpExchange exchange,String token,long order_id) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                Buyer buyer = (Buyer)user;
                Order order = OrderDao.getOrderById(order_id);
                if(order != null){
                    if(order.getBuyer().getId() == buyer.getId()){
                        JSONObject obj = OrderService.convertOrderToJson(order);
                        sendResponse(exchange,200, obj.toString());
                    }else throw new  ForbiddenException("Forbidden");
                }else throw new NotFoundException("Not Found Order");
            }else throw  new ForbiddenException("Forbidden");
        }else throw new NotFoundException("Not Found PHONE");
    }

    private void SubmitOrder(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);

        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                Buyer buyer = (Buyer)user;

                if(jsonObject.has("items") && jsonObject.has("delivery_address") && jsonObject.has("vendor_id") ){
                    if(!jsonObject.isNull("items") && !jsonObject.isNull("delivery_address") && !jsonObject.isNull("vendor_id")){
                        if(!jsonObject.optJSONArray("items").isEmpty()){
                            Order order = OrderService.SubmitOrder(buyer,jsonObject);
                            JSONObject response = OrderService.convertOrderToJson(order);
                            sendResponse(exchange, 200, response.toString());
                        }
                        else throw new InvalidUserDataException("items empty");
                    }
                    else throw new InvalidUserDataException("invalid data");
                }
                else throw new InvalidUserDataException("invalid data");
            }
            else throw new ForbiddenException("Forbidden");
        }
        else throw new NotFoundException("Not Found User");
    }
    private void GetOrderHistory(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        if(phone == null){
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                Buyer buyer = (Buyer)user;
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                JSONObject jsonObject = new JSONObject(requestBody);
                String Search = jsonObject.optString("search",null);
                String Vendor = jsonObject.optString("vendor",null);
                Set<Order> orders = OrderDao.BuyerSearch(Vendor,Search,buyer.getId());
                JSONArray jsonArray = new JSONArray();
                for(Order order : orders){
                    jsonArray.put(OrderService.convertOrderToJson(order));
                }
                sendResponse(exchange, 200, jsonArray.toString());
            }else throw  new ForbiddenException("Forbidden");
        }else throw new NotFoundException("Not Found User");
    }


}