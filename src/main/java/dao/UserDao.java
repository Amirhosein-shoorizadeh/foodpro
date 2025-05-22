package dao;

import entity.Seller;
import entity.User;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import util.HibernateUtil;
import exception.UserAlreadyExistsException;
import exception.InvalidUserDataException;


public class UserDao {
    private static final Object lock = new Object(); // قفل اختصاصی

    public static boolean save(User user) {
        synchronized (lock) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                if (user.getphone() != null && isLogined(user.getphone()) != null) {
                    throw new UserAlreadyExistsException("Phone number already registered: " + user.getphone());
                }
                session.getTransaction().begin();
                session.persist(user);
                session.getTransaction().commit();
                return true;
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                session.close();
            }
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


    public static User login(String phone, String password) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = session.createQuery("FROM User WHERE phone = :phone", User.class).setParameter("phone", phone).uniqueResult();
            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
            return null;
        } finally {
            session.close();
        }
    }

    public static User isLogined(String phone) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM User WHERE phone = : phone ", User.class).setParameter("phone", phone).uniqueResult();
        } finally {
            session.close();
        }
    }
}


