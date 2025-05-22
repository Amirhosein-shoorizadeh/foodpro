package dao;
import entity.*;
import org.hibernate.*;
import util.HibernateUtil;

import java.util.List;

public class RestaurantDao {
    private RestaurantDao() {}

    public static void save(Restaurant restaurant) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(restaurant);
            tx.commit();
        }
    }

    public static Restaurant getById(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Restaurant WHERE id = :id";
            return (Restaurant) session.createNativeQuery(sql, Restaurant.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
    }

    public static List<Restaurant> getRestaurantsBySeller(String sellerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Restaurant WHERE seller_id = :sellerId";
            return session.createNativeQuery(sql, Restaurant.class)
                    .setParameter("sellerId", sellerId)
                    .getResultList();
        }
    }
}
