package HttpHandeler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.*;
import entity.*;
import exception.*;
import org.json.JSONArray;
import org.json.JSONObject;
import service.OrderService;
import util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                }else if(path.equals("/orders/active")){
                    GetOrdersActive(exchange,token);
                }
                else if(path.equals("/carts")) {
                    GetCartsOfBuyer(exchange,token);
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
                }else if(path.matches("/orders/\\d+") && path.split("/")[1].equals("orders")) {
                    String[] pathParts = path.split("/");
                    long restaurant_id = Long.parseLong(pathParts[2]);
                    AddFoodToCart(exchange,restaurant_id,token);
                }else if(path.equals("/coupons")) {
                    CheckCoupon(exchange,token);
                }else if(path.equals("/orders/history")) {
                    System.out.println(9966996);
                    GetOrderHistory(exchange,token);
                }else if(path.matches("/orders/delete/\\d+") && path.split("/")[2].equals("delete")) {
                    String[] pathParts = path.split("/");
                    long order_id = Long.parseLong(pathParts[3]);
                    DeleteFoodFromCart(exchange,order_id,token);
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
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                List<PaymentTransaction> transactions = PaymentTransactionDao.getUserTransaction(phone);
                JSONArray array = new JSONArray();
                for (PaymentTransaction transaction : transactions){
                    JSONObject object = new JSONObject();
                    object.put("order_id",transaction.getOrder() != null ? transaction.getOrder().getId() : -1);
                    object.put("amount",transaction.getAmount());
                    object.put("id",transaction.getId());
                    object.put("user_id",user.getId());
                    object.put("status",transaction.getStatus());
                    object.put("method",transaction.getMethod());
                    object.put("date_time", transaction.getPaymentDate().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    array.put(object);
                }
                System.out.println(array.toString());
                sendResponse(exchange, 200, array.toString());
            }else throw  new ForbiddenException("Forbidden");
        }else throw  new NotFoundException("Not Found User");

    }

    public void TopUpWallet(HttpExchange exchange,String token) throws IOException {

        String phone = JwtUtil.validateToken(token);

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
        System.out.println(999999);


        String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
        JSONObject jsonObject = new JSONObject(requestBody);
        long Order_id = jsonObject.getLong("order_id");
        String Method = jsonObject.getString("method");
        double Amount = jsonObject.getDouble("amount");
        String Address = jsonObject.getString("address");


        PaymentTransaction paymentTransaction = OrderService.MakeOnlinePayment(phone, Order_id, Method,Address,Amount);

        PaymentTransactionDao.savePaymentTransaction(paymentTransaction);
        JSONObject response = new JSONObject();
        response.put("id", paymentTransaction.getId());
        response.put("order_id", paymentTransaction.getOrder() != null ? paymentTransaction.getOrder().getId() : -1);
        response.put("user_id", paymentTransaction.getBuyer().getId());
        response.put("method", Method);
        response.put("status", paymentTransaction.getStatus().name());
        System.out.println(paymentTransaction.getStatus().name());
        response.put("date_time", paymentTransaction.getPaymentDate().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("amount",paymentTransaction.getAmount());
        System.out.println(response.toString());
        sendResponse(exchange, 200, response.toString());
    }

    private void GetOrderWithId(HttpExchange exchange,String token,long order_id) throws IOException {
        String phone = JwtUtil.validateToken(token);

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
        System.out.println(1);
        System.out.println(2);

        User user = UserDao.getByPhone(phone);
        System.out.println(2);
        if(user != null){
            System.out.println(3);
            if(user instanceof Buyer){
                System.out.println(4);
                Buyer buyer = (Buyer)user;
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                JSONObject jsonObject = new JSONObject(requestBody);
                String Search = jsonObject.optString("search",null);
                String Vendor = jsonObject.optString("vendor",null);
                System.out.println(Search);
                System.out.println(Vendor);
                Set<Order> orders = OrderDao.BuyerSearch(Vendor,Search,buyer.getId());
                JSONArray jsonArray = new JSONArray();
                for(Order order : orders){
                    if(order.getStatus() == OrderStatus.COMPLETED){
                        jsonArray.put(OrderService.convertOrderToJson(order));
                    }
                }
                sendResponse(exchange, 200, jsonArray.toString());
            }else throw  new ForbiddenException("Forbidden");
        }else throw new NotFoundException("Not Found User");
    }
    private void AddFoodToCart(HttpExchange exchange,long restaurant_id,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        System.out.println(222222);

        if(user != null){
            if(user instanceof Buyer){

                Buyer buyer = (Buyer)user;
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                JSONObject jsonObject = new JSONObject(requestBody);
                long food_id = jsonObject.getLong("food_id");
                int quantity = jsonObject.getInt("quantity");
                Food food = FoodDao.getFoodById(food_id);

                Restaurant restaurant = RestaurantDao.getById(restaurant_id);
                if(food != null){
                    if(food.getRestaurant().getId() == restaurant_id){
                        if(food.getSupply() >= quantity){
                            Order cart = OrderDao.getCartBuyer(buyer.getId(),restaurant_id);
                            System.out.println(cart);
                            if(cart != null){
                                 cart.addOrderItems(new OrderItem(cart,food,quantity));
                                 cart.addRawPrice(food.getPrice()*quantity);
                                 cart.calculatePayPrice();
                                 OrderDao.update(cart);
                                 food.MinusSupply(quantity);
                                 FoodDao.update(food);
                            }else{
                                cart = new Order();
                                cart.addOrderItems(new OrderItem(cart,food,quantity));
                                cart.setRestaurant(restaurant);
                                cart.setTaxFee(restaurant.getTax_fee());
                                cart.setAdditionalFee(restaurant.getAdditional_fee());
                                cart.setCourierFee(30000);
                                cart.addRawPrice(food.getPrice()*quantity);
                                cart.calculatePayPrice();
                                cart.setBuyer(buyer);
                                cart.setStatus(OrderStatus.NON_SUBMITTED);
                                OrderDao.save(cart);
                                food.MinusSupply(quantity);
                                FoodDao.update(food);
                            }
                            sendResponse(exchange, 200, "OK");
                        }else throw new ForbiddenException("Forbidden");
                    }else throw new ForbiddenException("Forbidden");
                }else throw new NotFoundException("Not Found Food");
            }
        } else throw new NotFoundException("Not Found User");
    }

    private void DeleteFoodFromCart(HttpExchange exchange,long order_id,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        System.out.println(1111);
        if(user != null){
            if(user instanceof Buyer){
                Buyer buyer = (Buyer)user;
                System.out.println(2222);

                String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                JSONObject jsonObject = new JSONObject(requestBody);
                long food_id = jsonObject.getLong("food_id");
                int quantity = jsonObject.getInt("quantity");
                Food food = FoodDao.getFoodById(food_id);
                Order order = OrderDao.getOrderById(order_id);
                System.out.println(33333);
                if(order != null){
                    List<OrderItem> orderItems = order.getOrderItems();
                    OrderItem oI = null;
                    System.out.println(4444);
                    for(OrderItem orderItem : orderItems){
                        if(orderItem.getFood().getId() == food.getId() && orderItem.getQuantity() == quantity){
                            oI = orderItem;
                            System.out.println(5555);
                            food.PlusSupply(quantity);
                            FoodDao.update(food);
                            System.out.println(6666);
                            double raw_price = order.getRawPrice();
                            order.setRawPrice((long) (raw_price-(food.getPrice()*quantity)));
                            order.calculatePayPrice();
                            System.out.println(7777);
                            break;
                        }
                    }

                    if(oI != null){
                        order.getOrderItems().remove(oI);
                    }
                    if(order.getOrderItems().isEmpty()){
                        System.out.println(8888);
                        OrderDao.delete(order);
                        System.out.println(9999);
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("delete", "success");
                        sendResponse(exchange, 200,jsonObject1.toString());
                        System.out.println(10000);
                        return;
                    }
                    System.out.println(14141);
                    Coupon coupon = order.getCoupon();
                    if(coupon != null){
                        if(order.getPayPrice() >= coupon.getMinPrice() && coupon.getUserCount()>0){
                            LocalDate nowDate = LocalDate.now();
                            if((nowDate.isAfter(coupon.getStartDate()) && nowDate.isBefore(coupon.getEndDate())) || (nowDate.isEqual(coupon.getStartDate()) || nowDate.isEqual(coupon.getEndDate())) ){
                                order.calculatePayPrice();
                                CouponDao.update(coupon);
                            }
                            else {
                             order.setCoupon(null);
                             int user_count = coupon.getUserCount();
                             coupon.setUserCount(user_count + 1);
                             CouponDao.update(coupon);
                            }
                        }else {
                            order.setCoupon(null);
                            int user_count = coupon.getUserCount();
                            coupon.setUserCount(user_count + 1);
                            CouponDao.update(coupon);
                        }
                    }
                    System.out.println(12121212);
                    OrderDao.update(order);
                    System.out.println(131313);
                    JSONObject response = OrderService.convertOrderToJson(order);
                    sendResponse(exchange, 200, response.toString());
                }else throw  new ForbiddenException("Forbidden");
            }else throw  new ForbiddenException("Forbidden");
        }else throw new UnauthorizedException("Unauthorized");

    }

    private void GetCartsOfBuyer(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                Buyer buyer = (Buyer)user;
                List<Order> orders = OrderDao.getCartsOfBuyer(buyer.getId());
                JSONArray jsonArray = new JSONArray();
                for(Order order : orders){
                    jsonArray.put(OrderService.convertOrderToJson(order));
                }
                sendResponse(exchange, 200, jsonArray.toString());
            }else throw  new ForbiddenException("Forbidden");
        }else throw new NotFoundException("Not Found User");
    }

    private void CheckCoupon(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
                JSONObject jsonObject = new JSONObject(requestBody);
                String coupon_code = jsonObject.optString("coupon_code",null);
                long order_id = jsonObject.getLong("order_id");
                Coupon coupon = CouponDao.getByCode(coupon_code);
                Order order = OrderDao.getOrderById(order_id);
                if(coupon != null){
                    if(order != null){
                        if(order.getPayPrice() >= coupon.getMinPrice() && coupon.getUserCount()>0){
                            LocalDate nowDate = LocalDate.now();
                            if((nowDate.isAfter(coupon.getStartDate()) && nowDate.isBefore(coupon.getEndDate())) || (nowDate.isEqual(coupon.getStartDate()) || nowDate.isEqual(coupon.getEndDate())) ){
                                order.setCoupon(coupon);
                                order.calculatePayPrice();
                                coupon.MinusUserCount();
                                OrderDao.update(order);
                                CouponDao.update(coupon);
                                JSONObject response = new JSONObject();
                                response.put("pay_price",order.getPayPrice());
                                response.put("coupon_id",coupon.getId());
                                sendResponse(exchange, 200, response.toString());

                            }else  throw new ForbiddenException("Forbidden");
                        }else throw  new ForbiddenException("Forbidden");
                    }else throw new NotFoundException("Not Found Coupon");
                }else throw new NotFoundException("Not Found Order");
            }else throw  new ForbiddenException("Forbidden");
        }else throw new NotFoundException("Not Found User");
    }

    private void GetOrdersActive(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                Buyer buyer = (Buyer)user;
                List<Order> orders = OrderDao.getOrdersActive(buyer.getId());
                JSONArray array = new JSONArray();
                for (Order order : orders){
                    array.put(OrderService.convertOrderToJson(order));
                }
                sendResponse(exchange, 200, array.toString());

            }else throw  new ForbiddenException("Forbidden");
        }else throw  new NotFoundException("Not Found User");
    }

    private void GetTransAction(HttpExchange exchange,String token) throws IOException {
        String phone = JwtUtil.validateToken(token);
        User user = UserDao.getByPhone(phone);
        if(user != null){
            if(user instanceof Buyer){
                List<PaymentTransaction> transactions = PaymentTransactionDao.getUserTransaction(phone);
                JSONArray array = new JSONArray();
                for (PaymentTransaction transaction : transactions){
                    JSONObject object = new JSONObject();
                    object.put("order_id",transaction.getOrder().getId());
                    object.put("amount",transaction.getAmount());
                    object.put("id",transaction.getId());
                    object.put("user_id",user.getId());
                    object.put("status",transaction.getStatus());
                    object.put("method",transaction.getMethod());
                    object.put("date_time",transaction.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    array.put(object);
                }
                sendResponse(exchange, 200, array.toString());
            }else throw  new ForbiddenException("Forbidden");
        }else throw  new NotFoundException("Not Found User");
    }


}