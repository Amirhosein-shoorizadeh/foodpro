package dao;

import entity.Food;
import entity.Menu;
import exception.NotFoundException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.util.HashSet;

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
    public static void delete(long restaurantId,String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {

                Menu menu = session.createQuery("""
                FROM Menu m
                JOIN FETCH m.restaurant r
                LEFT JOIN FETCH m.foods f
                WHERE r.id = :rid AND m.title = :title
            """, Menu.class)
                        .setParameter("rid", restaurantId)
                        .setParameter("title", title)
                        .uniqueResult();

                if (menu == null) throw new NotFoundException("Menu not found");


                for (Food food : new HashSet<>(menu.getFoods())) {
                    food.getMenus().remove(menu);
                    session.update(food);
                }
                menu.getFoods().clear();


                menu.getRestaurant().getMenus().remove(menu);
                session.update(menu.getRestaurant());


                session.delete(menu);

                tx.commit();
                System.out.println("Menu deleted successfully.");
            } catch (Exception e) {
                tx.rollback();
                System.err.println("Error deleting menu: " + e.getMessage());
                e.printStackTrace();
            }
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
