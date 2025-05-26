package dao;
import entity.*;
import org.hibernate.*;
import util.HibernateUtil;

import java.io.IOException;
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

    public static void update(Restaurant restaurant) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(restaurant);
            tx.commit();
        }
    }

    public static Restaurant getById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Restaurant WHERE id = :id";
            return (Restaurant) session.createNativeQuery(sql, Restaurant.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
    }

    public static Restaurant getByPhone(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Restaurant WHERE phone = :phone";
            return (Restaurant) session.createNativeQuery(sql, Restaurant.class)
                    .setParameter("phone", phone)
                    .getSingleResult();
        }
    }


    public static List<Restaurant> getRestaurantsBySeller(long sellerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Restaurant WHERE seller_id = :sellerId";
            return session.createNativeQuery(sql, Restaurant.class)
                    .setParameter("sellerId", sellerId)
                    .getResultList();
        }
    }

    public static boolean isPhoneExists(String phone) {

        try(Session session = HibernateUtil.getSessionFactory().openSession();) {
            Object result = session.createNativeQuery(
                            "SELECT 1 FROM Restaurant WHERE phone = :phone LIMIT 1")
                    .setParameter("phone", phone)
                    .uniqueResult();
            return result != null;
        }catch (HibernateException e){
            System.out.println(e.getMessage());
            return false;
        }
    }
}
