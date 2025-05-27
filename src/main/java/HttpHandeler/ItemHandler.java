package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ItemHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());
        if ("POST".equalsIgnoreCase(method)) {

        }else if ("GET".equalsIgnoreCase(method)) {}
    }
}
