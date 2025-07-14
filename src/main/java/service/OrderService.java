package service;

import dao.OrderDao;
import dao.UserDao;
import dao.PaymentTransactionDao;
import entity.*;
import exception.ConflictExceptin;
import exception.ForbiddenException;
import exception.NotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class OrderService {
    public static JSONObject convertOrderToJson(Order order) {
        JSONObject obj = new JSONObject();
        obj.put("id", order.getId());
        obj.put("delivery_address", order.getDeliveryAddress());
        obj.put("customer_id", order.getBuyer().getId());
        obj.put("vendor_id", order.getRestaurant().getId());
        obj.put("coupon_id", order.getCoupon().getId());

        JSONArray itemIds = new JSONArray(order.getFoods().stream()
                .map(Food::getId)
                .toList());

        obj.put("item_ids", itemIds);
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
                        long PayPrice = order.getPayPrice();
                        if(Method.equals("online")) {
                            order.setStatus(OrderStatus.WAITING_VENDOR);
                            return new PaymentTransaction(order,buyer,TransactionMethod.online,TransactionStatus.SUCCESS);
                        }
                        else if(Method.equals("wallet")) {
                            long BuyerWalletBalance = buyer.getBankinfo().getWalletBalance();
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
}
