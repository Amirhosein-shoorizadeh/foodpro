package dao;
import entity.*;
import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.io.IOException;
import java.util.ArrayList;
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
            throw new RuntimeException(e);
        }
    }
    public static List<Restaurant> getAllRestaurants() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Restaurant", Restaurant.class).list();
        } catch (HibernateException e) {
            throw new RuntimeException("Error", e);
        }
    }

    public static List<Restaurant> searchRestaurants(String search, List<String> keywords) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT r FROM Restaurant r ");
            boolean hasKeywords = keywords != null && !keywords.isEmpty();
            boolean hasSearch = search != null && !search.trim().isEmpty();

            if (hasKeywords) {
                hql.append("JOIN r.foods f JOIN f.keywords k ");
            }

            List<String> conditions = new ArrayList<>();
            if (hasSearch) {
                conditions.add("LOWER(r.name) LIKE :search");
            }
            if (hasKeywords) {
                conditions.add("LOWER(k) IN :keywords");
            }

            if (!conditions.isEmpty()) {
                hql.append("WHERE ").append(String.join(" OR ", conditions));
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);

            if (hasSearch) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }
            if (hasKeywords) {
                query.setParameter("keywords", keywords.stream().map(String::toLowerCase).toList());
            }

            return query.getResultList();
        }
    }

    public static List<Restaurant> getBestRestaurant() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
            SELECT o.restaurant
            FROM Order o
            GROUP BY o.restaurant
            ORDER BY COUNT(o.id) DESC
        """;

            return session.createQuery(hql, Restaurant.class)
                    .setMaxResults(10)
                    .getResultList();
        } catch (HibernateException e) {
            throw new RuntimeException("خطا در دریافت رستوران‌های پرفروش", e);
        }
    }




}
