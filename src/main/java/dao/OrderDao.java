package dao;
import entity.*;
import exception.NotFoundException;
import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderDao {
    private OrderDao() {}

    public static void save(Order order,List<OrderItem> orderItems) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            session.persist(order); // یا session.save(order)

            for (OrderItem item : orderItems) {
                item.setOrder(order);
                session.persist(item);
            }

            session.update(order); // اگر لازم است، مثلا برای مقدار نهایی پرداخت یا coupon

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to submit order", e);
        }
    }

    public static void update(Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(order);
            tx.commit();
        }
    }
    public static void delete(Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(order);
            tx.commit();
        }
    }

    public static List<Order> getOrdersByRestaurant(Long restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM orders WHERE restaurant_id = :restaurantId";
            return session.createNativeQuery(sql, Order.class)
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();
        }
    }

    public static Order getOrderById(Long orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM orders WHERE id = :orderId";
            return  (Order) session.createNativeQuery(sql, Order.class).uniqueResult();
        }
    }
    public static List<Order> getOrdersByStatus(OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM orders WHERE status = :status";
            return session.createNativeQuery(sql, Order.class).setParameter("status", status.name()).getResultList();
        }
    }

    public static Set<Order> RestaurantSearchOrders(Long restaurantId, String status,
                                                    String foodKeyword, String buyerKeyword, String courierKeyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.restaurant r
            JOIN FETCH o.buyer b
            LEFT JOIN FETCH o.courier c
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.food f
            WHERE 1=1
        """);

            if (restaurantId != null && restaurantId > 0) {
                jpql.append(" AND o.restaurant.id = :restaurantId ");
            }
            if (status != null) {
                jpql.append(" AND o.status = :status ");
            }
            if (foodKeyword != null && !foodKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(f.name), ' ', '') LIKE :foodKw ");
            }
            if (buyerKeyword != null && !buyerKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(b.full_name), ' ', '') LIKE :buyerKw ");
            }
            if (courierKeyword != null && !courierKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(c.full_name), ' ', '') LIKE :courierKw ");
            }

            Query<Order> query = session.createQuery(jpql.toString(), Order.class);

            if (restaurantId != null && restaurantId > 0) {
                query.setParameter("restaurantId", restaurantId);
            }
            if (status != null) {
                query.setParameter("status", status);
            }
            if (foodKeyword != null && !foodKeyword.isBlank()) {
                query.setParameter("foodKw", "%" + foodKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (buyerKeyword != null && !buyerKeyword.isBlank()) {
                query.setParameter("buyerKw", "%" + buyerKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (courierKeyword != null && !courierKeyword.isBlank()) {
                query.setParameter("courierKw", "%" + courierKeyword.toLowerCase().replace(" ", "") + "%");
            }

            return new HashSet<>(query.getResultList());
        }
    }




    public static Set<Order> CourierSearchOrders(Long courierId, String food, String vendor, String buyer) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder jpql = new StringBuilder("""
           SELECT DISTINCT o FROM Order o
           JOIN FETCH o.buyer b
           JOIN FETCH o.restaurant r
           LEFT JOIN FETCH o.courier c
           LEFT JOIN FETCH o.orderItems oi
           LEFT JOIN FETCH oi.food f
           WHERE o.courier.id = :courierId
        """);

            if (food != null && !food.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(f.name), ' ', '') LIKE :foodKw ");
            }
            if (buyer != null && !buyer.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(b.full_name), ' ', '') LIKE :buyerKw ");
            }
            if (vendor != null && !vendor.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(r.name), ' ', '') LIKE :vendorKw ");
            }

            Query<Order> query = session.createQuery(jpql.toString(), Order.class)
                    .setParameter("courierId", courierId);

            if (food != null && !food.isBlank()) {
                query.setParameter("foodKw", "%" + food.toLowerCase().replace(" ", "") + "%");
            }
            if (buyer != null && !buyer.isBlank()) {
                query.setParameter("buyerKw", "%" + buyer.toLowerCase().replace(" ", "") + "%");
            }
            if (vendor != null && !vendor.isBlank()) {
                query.setParameter("vendorKw", "%" + vendor.toLowerCase().replace(" ", "") + "%");
            }

            List<Order> resultList = query.getResultList();
            return new HashSet<>(resultList);
        }
    }


    public static Set<Order> AdminSearchOrders(String restaurantKeyword, OrderStatus status,
                                               String foodKeyword, String buyerKeyword, String courierKeyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.restaurant r
            JOIN FETCH o.buyer b
            LEFT JOIN FETCH o.courier c
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.food f
            WHERE o.status = :status
        """);

            if (restaurantKeyword != null && !restaurantKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(r.name), ' ', '') LIKE :restaurantKw ");
            }
            if (foodKeyword != null && !foodKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(f.name), ' ', '') LIKE :foodKw ");
            }
            if (buyerKeyword != null && !buyerKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(b.full_name), ' ', '') LIKE :buyerKw ");
            }
            if (courierKeyword != null && !courierKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(c.full_name), ' ', '') LIKE :courierKw ");
            }

            Query<Order> query = session.createQuery(jpql.toString(), Order.class)
                    .setParameter("status", status);

            if (restaurantKeyword != null && !restaurantKeyword.isBlank()) {
                query.setParameter("restaurantKw", "%" + restaurantKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (foodKeyword != null && !foodKeyword.isBlank()) {
                query.setParameter("foodKw", "%" + foodKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (buyerKeyword != null && !buyerKeyword.isBlank()) {
                query.setParameter("buyerKw", "%" + buyerKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (courierKeyword != null && !courierKeyword.isBlank()) {
                query.setParameter("courierKw", "%" + courierKeyword.toLowerCase().replace(" ", "") + "%");
            }

            List<Order> resultList = query.getResultList();
            return new HashSet<>(resultList);
        }
    }


    public static Set<Order> BuyerSearch(String restaurantKeyword, String foodKeyword, long buyerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.restaurant r
            JOIN FETCH o.buyer b
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.food f
            WHERE b.id = :id
        """);

            if (restaurantKeyword != null && !restaurantKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(r.name), ' ', '') LIKE :restaurantKw ");
            }
            if (foodKeyword != null && !foodKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(f.name), ' ', '') LIKE :foodKw ");
            }

            Query<Order> query = session.createQuery(jpql.toString(), Order.class)
                    .setParameter("id", buyerId);

            if (restaurantKeyword != null && !restaurantKeyword.isBlank()) {
                query.setParameter("restaurantKw", "%" + restaurantKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (foodKeyword != null && !foodKeyword.isBlank()) {
                query.setParameter("foodKw", "%" + foodKeyword.toLowerCase().replace(" ", "") + "%");
            }

            List<Order> resultList = query.getResultList();
            return new HashSet<>(resultList);
        }
    }

    public static boolean isThatForThisOne(User user, Long orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Order order = session.get(Order.class, orderId);
            if (order == null) {
                throw new NotFoundException("Order not found");
            }
            boolean a = order.getBuyer().getId() == (user.getId());
            return a;
        }
    }



}
