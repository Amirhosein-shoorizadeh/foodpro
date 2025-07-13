package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UserDao;
import entity.PaymentTransaction;
import entity.User;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import service.OrderService;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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
                }else{throw new NotFoundException("Not Found PATH");}
            }
            else if(method.equals("POST")) {
                if(path.equals("/wallet/top-up")) {
                    TopUpWallet(exchange,token);
                }
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


}