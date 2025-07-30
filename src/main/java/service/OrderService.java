package service;

import com.google.gson.Gson;
import dao.*;
import dto.RatingDto;
import entity.*;
import exception.ConflictExceptin;
import exception.ForbiddenException;
import exception.InvalidUserDataException;
import exception.NotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;


public class OrderService {
    public static JSONObject convertOrderToJson(Order order) {
        JSONObject obj = new JSONObject();
        obj.put("id", order.getId());
        obj.put("deliveryAddress", order.getDeliveryAddress());
        obj.put("buyerName", order.getBuyer().getFull_name());
        obj.put("buyerPhone", order.getBuyer().getPhone());
        obj.put("vendorName", order.getRestaurant().getName());
        obj.put("vendorAddress", order.getRestaurant().getAddress());
        obj.put("coupon_id", order.getCoupon() == null ? JSONObject.NULL : order.getCoupon().getId());

        JSONArray itemsArray = new JSONArray();
        for (OrderItem item : order.getOrderItems()) {
            JSONObject itemObj = new JSONObject();
            itemObj.put("food_id", item.getFood().getId());
            itemObj.put("food_name", item.getFood().getName());
            itemObj.put("quantity", item.getQuantity());
            itemObj.put("logoBase64",item.getFood().getImageBase64());
            itemsArray.put(itemObj);
        }
        obj.put("items", itemsArray);


        if(order.getRating() != null){
            Rating rating = order.getRating();
            JSONObject ratingObj = new JSONObject();
            ratingObj.put("id", rating.getId());
            ratingObj.put("orderId", order.getId());
            ratingObj.put("rate", rating.getRate());
            ratingObj.put("comment", rating.getComment());
            List<String> cleanLogoList = rating.getLogobase64()
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();

            ratingObj.put("logobase64", new JSONArray(cleanLogoList));
            ratingObj.put("userName", rating.getUserName());

            obj.put("rate", ratingObj);
        }else {
            obj.put("rate", JSONObject.NULL);
        }



        obj.put("raw_price", order.getRawPrice());
        obj.put("tax_fee", order.getTaxFee());
        obj.put("additional_fee", order.getAdditionalFee());
        obj.put("courier_fee", order.getCourierFee());
        obj.put("pay_price", order.getPayPrice());
        obj.put("courierName", order.getCourier() == null ? JSONObject.NULL : order.getCourier().getFull_name());
        obj.put("status", order.getStatus().name());
        obj.put("created_at", order.getCreatedAt() != null ? order.getCreatedAt().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : JSONObject.NULL);
        obj.put("updated_at", order.getUpdatedAt() != null ? order.getUpdatedAt().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : JSONObject.NULL);

        return obj;
    }

    public static List<PaymentTransaction> GetTransactionHistory(String Phone) {
        User user = UserDao.getByPhone(Phone);
        if (user != null) {

            if(user instanceof Buyer) {
                return PaymentTransactionDao.getUserTransaction(Phone);
            }else{throw new ForbiddenException("user is not buyer");}

        }else{throw new NotFoundException("User not found");}
    }

    public static void TopUpWallet(String Phone,long Amount) {
        User user = UserDao.getByPhone(Phone);
        if (user != null) {
            if(user instanceof Buyer) {
                Buyer buyer = (Buyer)user;
                buyer.getBankinfo().increaseWalletBalance(Amount);
            }else{throw new ForbiddenException("user is not buyer");}
        }else{throw new NotFoundException("User not found");}
    }

    public static PaymentTransaction MakeOnlinePayment(String Phone,long Order_id,String Method,String Address,double Amount) {
        User user = UserDao.getByPhone(Phone);
        if (user != null) {

            if(user instanceof Buyer) {
                Buyer buyer = (Buyer)user;
                Order order ;

                if(Order_id != -1){
                    order = OrderDao.getOrderById(Order_id);
                }else {
                    order = null;
                }

                if(order != null) {
                    if(order.getStatus() == OrderStatus.NON_SUBMITTED) {

                        double PayPrice = order.getPayPrice();
                        if(Method.equals("online")) {

                            order.setStatus(OrderStatus.SUBMITTED);
                            order.setDeliveryAddress(Address);
                            order.setCreatedAt(LocalDateTime.now());
                            order.setUpdatedAt(LocalDateTime.now());
                            OrderDao.update(order);

                            return new PaymentTransaction(order,buyer,TransactionMethod.online,LocalDateTime.now(),TransactionStatus.SUCCESS , Amount);
                        }
                        else if(Method.equals("wallet")) {
                            double BuyerWalletBalance = buyer.getBankinfo().getWalletBalance();
                            if(BuyerWalletBalance >= PayPrice) {
                                buyer.getBankinfo().decreaseWalletBalance(PayPrice);
                                order.setStatus(OrderStatus.SUBMITTED);
                                order.setDeliveryAddress(Address);
                                order.setCreatedAt(LocalDateTime.now());
                                order.setUpdatedAt(LocalDateTime.now());
                                UserDao.update(buyer);
                                OrderDao.update(order);
                                return new PaymentTransaction(order,buyer,TransactionMethod.wallet,LocalDateTime.now(),TransactionStatus.SUCCESS , Amount);
                            }else {
                                PaymentTransaction payment = new PaymentTransaction(order,buyer,TransactionMethod.wallet,LocalDateTime.now(),TransactionStatus.FAILED,Amount);
                                PaymentTransactionDao.savePaymentTransaction(payment);
                                return  payment;
                            }
                        }else {
                            PaymentTransaction payment = new PaymentTransaction(order,buyer,TransactionMethod.wallet,LocalDateTime.now(),TransactionStatus.FAILED,Amount);
                            PaymentTransactionDao.savePaymentTransaction(payment);
                            throw new ForbiddenException("method is not supported");
                        }
                    }else{throw new ForbiddenException("OrderStatus is not Submitted");}

                }else{
                    if(Amount < 0){throw new ForbiddenException("Amount cannot be negative");}
                    PaymentTransaction Pt = new PaymentTransaction(null,buyer,TransactionMethod.online,LocalDateTime.now(),TransactionStatus.SUCCESS,Amount);
                    PaymentTransactionDao.savePaymentTransaction(Pt);
                    buyer.getBankinfo().increaseWalletBalance(Amount);
                    UserDao.update(buyer);
                    return Pt;
                }

            }else{throw new ForbiddenException("user is not buyer");}

        }else{throw new NotFoundException("User not found");}
    }

    public static Order SubmitOrder(Buyer buyer, JSONObject jsonObject) {
        String deliveryAddress = jsonObject.getString("delivery_address");
        long vendorId = jsonObject.getLong("vendor_id");
        JSONArray items = jsonObject.getJSONArray("item_ids");

        Restaurant restaurant = RestaurantDao.getById(vendorId);
        if (restaurant == null) throw new NotFoundException("Restaurant not found");

        List<OrderItem> orderItems = new ArrayList<>();
        long rawPrice = 0;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                if (!item.has("item_id") || !item.has("quantity"))
                    throw new InvalidUserDataException("invalid json");

                long itemId = item.getLong("item_id");
                int quantity = item.getInt("quantity");

                Food food = FoodDao.getFoodById(itemId);
                if (food == null) throw new NotFoundException("Food not found");
                if (food.getSupply() < quantity) throw new ForbiddenException("Supply less than quantity");

                food.MinusSupply(quantity);
                session.update(food); // update food supply

                rawPrice += food.getPrice() * quantity;
                OrderItem orderItem = new OrderItem(null, food, quantity); // setOrder later
                orderItems.add(orderItem);
            }

            Order order = new Order();
            order.setBuyer(buyer);
            order.setRestaurant(restaurant);
            order.setDeliveryAddress(deliveryAddress);
            order.setRawPrice(rawPrice);
            order.setTaxFee(restaurant.getTax_fee());
            order.setAdditionalFee(restaurant.getAdditional_fee());
            order.setCourierFee(30000);
            order.setStatus(OrderStatus.SUBMITTED);

            double payPrice = rawPrice + (rawPrice * restaurant.getTax_fee() / 100.0)
                    + restaurant.getAdditional_fee() + order.getCourierFee();

            if (jsonObject.has("coupon_id") && !jsonObject.isNull("coupon_id")) {
                long couponId = jsonObject.getLong("coupon_id");
                Coupon coupon = CouponDao.findByCouponId(couponId);
                if (coupon == null) throw new NotFoundException("Coupon not found");

                order.setCoupon(coupon);
                if (coupon.getType() == Coupon.Type.fixed) {
                    payPrice -= coupon.getValue();
                } else if (coupon.getType() == Coupon.Type.percent) {
                    payPrice -= payPrice * (coupon.getValue() / 100.0);
                }
            }

            order.setPayPrice(payPrice);

            LocalDateTime now = LocalDateTime.now();
            String createdAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


            session.persist(order); // Save order first to get ID

            for (OrderItem item : orderItems) {
                item.setOrder(order);
                session.persist(item);
            }

            session.update(order); // In case coupon changed payPrice
            tx.commit();

            return order;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to submit order", e);
        }
    }

}
