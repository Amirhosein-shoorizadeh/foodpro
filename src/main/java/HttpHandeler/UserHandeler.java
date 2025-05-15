package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class UserHandeler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("POST".equals(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/user/signup")) {
//handleSignup(exchange);
            } else if (path.equals("/user/login")) {
//handleLogin(exchange);
            } else {
                exchange.sendResponseHeaders(404, -1); // Not Found
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}
