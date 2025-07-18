package service;

import dao.*;
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

import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;


public class OrderService {
    public static JSONObject convertOrderToJson(Order order) {
        JSONObject obj = new JSONObject();
        obj.put("id", order.getId());
        obj.put("delivery_address", order.getDeliveryAddress());
        obj.put("customer_id", order.getBuyer().getId());
        obj.put("vendor_id", order.getRestaurant().getId());
        obj.put("coupon_id", order.getCoupon() == null ? JSONObject.NULL : order.getCoupon().getId());

        JSONArray itemsArray = new JSONArray();
        for (OrderItem item : order.getOrderItems()) {
            JSONObject itemObj = new JSONObject();
            itemObj.put("food_id", item.getFood().getId());
            itemObj.put("food_name", item.getFood().getName());
            itemObj.put("quantity", item.getQuantity());
            itemsArray.put(itemObj);
        }
        obj.put("items", itemsArray);

        obj.put("raw_price", order.getRawPrice());
        obj.put("tax_fee", order.getTaxFee());
        obj.put("additional_fee", order.getAdditionalFee());
        obj.put("courier_fee", order.getCourierFee());
        obj.put("pay_price", order.getPayPrice());
        obj.put("courier_id", order.getCourier() == null ? JSONObject.NULL : order.getCourier().getId());
        obj.put("status", order.getStatus().name());
        obj.put("created_at", order.getCreatedAt());
        obj.put("updated_at", order.getUpdatedAt());

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

    public static PaymentTransaction MakeOnlinePayment(String Phone,long Order_id,String Method) {
        User user = UserDao.getByPhone(Phone);
        if (user != null) {

            if(user instanceof Buyer) {
                Buyer buyer = (Buyer)user;
                Order order = OrderDao.getOrderById(Order_id);
                if(order != null) {
                    if(order.getStatus().name().equals("SUBMITTED")) {
                        double PayPrice = order.getPayPrice();
                        if(Method.equals("online")) {
                            order.setStatus(OrderStatus.WAITING_VENDOR);
                            return new PaymentTransaction(order,buyer,TransactionMethod.online,TransactionStatus.SUCCESS);
                        }
                        else if(Method.equals("wallet")) {
                            double BuyerWalletBalance = buyer.getBankinfo().getWalletBalance();
                            if(BuyerWalletBalance >= PayPrice) {
                                buyer.getBankinfo().decreaseWalletBalance(PayPrice);
                                order.setStatus(OrderStatus.WAITING_VENDOR);
                                UserDao.update(buyer);
                                OrderDao.update(order);
                                return new PaymentTransaction(order,buyer,TransactionMethod.wallet,TransactionStatus.SUCCESS);
                            }else {
                                order.setStatus(OrderStatus.UNPAID_AND_CANCELLED);
                                OrderDao.update(order);
                                return new PaymentTransaction(order,buyer,TransactionMethod.wallet,TransactionStatus.FAILED);
                            }
                        }else {
                            order.setStatus(OrderStatus.UNPAID_AND_CANCELLED);
                            OrderDao.update(order);
                            return new PaymentTransaction(order,buyer,TransactionMethod.wallet,TransactionStatus.FAILED);
                        }
                    }else{throw new ForbiddenException("OrderStatus is not Submitted");}

                }else {throw new NotFoundException("Order not found");}

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
            order.setCreatedAt(createdAt);

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
