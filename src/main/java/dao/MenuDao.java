package dao;

import entity.Menu;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class MenuDao {
    public static void save(Menu menu) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(menu);
            tx.commit();
        }
    }
    public static void update(Menu menu) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(menu);
            tx.commit();
        }
    }
    public static void delete(Menu menu) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(menu);
            tx.commit();
        }
    }

    public static boolean isMenuExists(long restaurantId, String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Object result = session.createNativeQuery(
                            "SELECT 1 FROM Menu WHERE restaurant_id = :restaurantId AND title = :title LIMIT 1")
                    .setParameter("restaurantId", restaurantId)
                    .setParameter("title", title)
                    .uniqueResult();
            return result != null;
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
    }

    public static Menu getMenu(long restaurantId, String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Menu m WHERE m.restaurant.id = :restaurantId AND m.title = :title", Menu.class)
                    .setParameter("restaurantId", restaurantId)
                    .setParameter("title", title)
                    .setMaxResults(1)
                    .uniqueResult();
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
    }




}
