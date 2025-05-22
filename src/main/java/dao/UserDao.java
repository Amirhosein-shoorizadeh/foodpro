package dao;

import entity.Seller;
import entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import util.HibernateUtil;

public class UserDao {
    public static boolean save(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.getTransaction().begin();
            session.persist(user);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            session.getTransaction().rollback();
            return false;
        } finally {
            session.close();
        }
    }
    public static boolean update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.getTransaction().begin();

            session.update(user);

            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            session.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
    public static User getById(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM User WHERE Id = :id";
            return session.createNativeQuery(sql, Seller.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
    }


}


