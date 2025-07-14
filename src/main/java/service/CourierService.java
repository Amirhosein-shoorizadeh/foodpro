package service;

import dao.OrderDao;
import dao.UserDao;
import entity.*;
import exception.ForbiddenException;
import exception.InvalidUserDataException;
import exception.NotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

public class CourierService {
    public static List<Order> GetaVailableRequests(String Phone){
        User user = UserDao.getByPhone(Phone);
        if(user == null){
            throw new NotFoundException("user not found");
        }
        if (user instanceof Courier ) {
            return OrderDao.getOrdersByStatus(OrderStatus.FINDING_COURIER);
        } else {
            throw new ForbiddenException("you are not a Courier");
        }
    }

    public static Order ChangeStatusOfRequest(String Phone, long order_id,String status){
        User user = UserDao.getByPhone(Phone);
        if(user == null){
            throw new NotFoundException("user not found");
        }
        if (user instanceof Courier ) {
            Courier courier = (Courier) user;
            Order order = OrderDao.getOrderById(order_id);
            if(order == null){
                throw new NotFoundException("order not found");
            }
            if(status.equalsIgnoreCase("accepted")){
                if(order.getStatus() == OrderStatus.FINDING_COURIER){
                    order.setStatus(OrderStatus.ACCEPTED_BY_COURIER);
                    order.setCourier(courier);
                }else{
                    throw new ForbiddenException("This order is not in the correct stage");
                }
            }else if (status.equalsIgnoreCase("received")){
                if(order.getStatus() == OrderStatus.ACCEPTED_BY_COURIER){
                    order.setStatus(OrderStatus.ON_THE_WAY);
                }else{
                    throw new ForbiddenException("This order is not in the correct stage");
                }

            }else if(status.equalsIgnoreCase("delivered")){
                if(order.getStatus() == OrderStatus.ON_THE_WAY){
                    order.setStatus(OrderStatus.COMPLETED);
                }else {
                    throw new ForbiddenException("This order is not in the correct stage");
                }

            }else {
                throw new InvalidUserDataException("invalid status");
            }
            OrderDao.update(order);
            return order;
        }else {
            throw new ForbiddenException("you are not a Courier");
        }
    }

    public static Set<Order> GetHistory(String Phone, JSONObject jsonObject){
        User user = UserDao.getByPhone(Phone);
        if(user == null){
            throw new NotFoundException("user not found");
        }
        if (!(user instanceof Courier) ) {
            throw new ForbiddenException("you are not a Courier");
        }
        String search = jsonObject.getString("search");
        String vendor = jsonObject.getString("vendor");
        String buyer = jsonObject.getString("user");
        Courier courier = (Courier) user;
        Set<Order> orders = OrderDao.CourierSearchOrders(courier.getId(),search,vendor,buyer);
        return orders;
    }

}
