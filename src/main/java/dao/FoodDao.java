package dao;
import entity.Food;
import org.hibernate.*;
import util.HibernateUtil;

import java.util.List;

public class FoodDao {

    private FoodDao() {}

    public static void save(Food food) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(food);
            tx.commit();
        }
    }
    public static void update(Food food) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(food);
            tx.commit();
        }
    }

    public static void delete(Food food) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(food);
            tx.commit();
        }
    }

    public static Food getFoodById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Food WHERE id = :id";
            return (Food) session.createNativeQuery(sql, Food.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
    }

    public static List<Food> getFoodsByRestaurantId(String restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Food WHERE restaurant_id = :restaurantId";
            return session.createNativeQuery(sql, Food.class)
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();
        }
    }
}
