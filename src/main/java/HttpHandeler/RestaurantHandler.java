package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.RestaurantDao;
import dao.UserDao;
import entity.Restaurant;
import entity.Seller;
import entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RestaurantHandler implements HttpHandler {
    private static final String SECRET_KEY = "your-very-secure-secret"; // باید از محیط متغیرها خونده بشه

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (method.equals("GET")) {}
        else if (method.equals("POST")) {
             if (path.equals("/restaurants")) {
                 CreateRestaurant(exchange);
             }
        }
        else if (method.equals("PUT")) {}
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
            sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            return;
        }

        String token = authHeader.substring(7); // حذف "Bearer "

        try {
            // اعتبارسنجی توکن JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            // گرفتن seller_id از توکن
            String sellerId = claims.getSubject(); // فرض می‌کنیم seller_id توی subject توکنه

            // گرفتن اطلاعات فروشنده از دیتابیس
            User user = UserDao.getById(sellerId);
            if (user == null) {
                sendResponse(exchange, 401, "{\"error\": \"Seller not found\"}");
                return;
            }

            if(!(user instanceof Seller)) {
                sendResponse(exchange, 401, "{\"error\": \"Not a Seller\"}");
                return;
            }

            // خواندن بدنه درخواست
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");

            // پردازش JSON
            JSONObject json = new JSONObject(requestBody);
            String name = json.getString("name");
            String address = json.getString("address");
            String phone = json.getString("phone");
            String logoBase64 = json.optString("logoBase64", null);

            // اعتبارسنجی ورودی‌ها
            if (name == null || name.isEmpty() || address == null || address.isEmpty() || phone == null || phone.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid input: name, address, and phone are required\"}");
                return;
            }

            // ایجاد رستوران جدید
            Restaurant restaurant = new Restaurant( (Seller) user, address, phone, logoBase64);
            RestaurantDao.save(restaurant);

            // پاسخ موفقیت‌آمیز
            JSONObject responseJson = new JSONObject();
            responseJson.put("id", restaurant.getId());
            responseJson.put("name", name);
            responseJson.put("address", address);
            responseJson.put("phone", phone);
            if (logoBase64 != null) {
                responseJson.put("logoBase64", logoBase64);
            }

            sendResponse(exchange, 201, responseJson.toString());
        } catch (Exception e) {
            sendResponse(exchange, 401, "{\"error\": \"Invalid or expired token\"}");
        }
    }
}