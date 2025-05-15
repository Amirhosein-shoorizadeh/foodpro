package HttpHandeler;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RestaurantHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("POST".equals(exchange.getRequestMethod())) {


            if (method.equals("ADDFOOD")) {
            } else if (method.equals("REMOVEFOOD")) {
            } else if (method.equals("PLUSFOOD")) {
            } else if (method.equals("ORDERFOOD")) {
            } else if (method.equals("INFORESTAURANT")) {
            } else if (method.equals("LISTRESTAURANT")) {
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

}
