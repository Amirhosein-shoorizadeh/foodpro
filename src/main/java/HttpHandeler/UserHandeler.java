package HttpHandeler;

import com.google.gson.Gson;
import dto.SignUpManager;
import dto.UserManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class UserHandeler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equalsIgnoreCase(method)) {
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining());

            if (path.equals("/user/signup")) {
                String response;
                Gson gson = new Gson();
                SignUpManager temp = gson.fromJson(body, SignUpManager.class);
                if ( UserManager.handleSignup(temp)) {
                    response = "Signup successful";
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "Signup failed";
                    exchange.sendResponseHeaders(400, response.length());
                }

                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();

            } else if (path.equals("/user/login")) {
                // مشابه signup
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

}

