      package HttpHandeler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.*;
import dto.FoodDto;
import entity.Food;

import java.io.IOException;
import java.sql.SQLException;


public class ItemHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath().replaceAll("/+$", "");

        if ("GET".equalsIgnoreCase(method)) {
            if (path.matches("^/items/\\d+$")) {
                handleGetItemById(exchange);
            } else {
                sendResponse(exchange, 404, "Path not found");
            }
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleGetItemById(HttpExchange exchange) throws IOException {
        try {
            Gson gson = new Gson();
            String[] paths = exchange.getRequestURI().getPath().split("/");
            long id = Long.parseLong(paths[paths.length - 1]);
            Food food = FoodDao.getFoodById(id);
            if (food == null) {
                sendResponse(exchange, 404, "Item not found");
                return;
            }
            FoodDto foodDto = new FoodDto(food);
            String json = gson.toJson(foodDto,FoodDto.class);
            sendResponse(exchange, 200, json);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid item ID format");
        } catch (SecurityException e) {
            sendResponse(exchange, 401, "Unauthorized");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

}
