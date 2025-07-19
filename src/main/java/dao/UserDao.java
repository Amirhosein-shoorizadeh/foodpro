package dao;

import entity.Buyer;
import entity.Token;
import entity.User;
import exception.UnauthorizedException;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import util.HibernateUtil;
import exception.UserAlreadyExistsException;

import java.util.List;


public class UserDao {
    private static final Object lock = new Object(); // قفل اختصاصی

    public static boolean save(User user) {
        synchronized (lock) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                if (user.getPhone() != null && isRegistered(user.getPhone()) != null) {
                    throw new UserAlreadyExistsException("Phone number already registered: " + user.getPhone());
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

    public static User getByPhone(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM User WHERE phone = :phone";
            return session.createNativeQuery(sql, User.class)
                    .setParameter("phone", phone)
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

    public static User isRegistered(String phone) {
        try (Session session = HibernateUtil.getSessionFactory().openSession();) {
            return session.createQuery("FROM User WHERE phone = :phone ", User.class).setParameter("phone", phone).uniqueResult();
        }
    }

    public static boolean isPhoneExists(String phone) {

        try (Session session = HibernateUtil.getSessionFactory().openSession();) {
            Object result = session.createNativeQuery(
                            "SELECT 1 FROM User WHERE phone = :phone LIMIT 1")
                    .setParameter("phone", phone)
                    .uniqueResult();
            return result != null;

        }
    }
    public static List<User> getAllExceptAdmins() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            List<User> users = session.createQuery("FROM User u WHERE TYPE(u) <> Admin", User.class)
                    .getResultList();
            session.getTransaction().commit();
            return users;
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }
    public static User findById(long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.get(User.class, id);
        } finally {
            session.close();
        }
    }

    public static boolean UserisBuyer (String token) throws UnauthorizedException {
        Token authToken = TokenDao.findByToken(token);
        if (authToken == null) {
            throw new UnauthorizedException("not logined");
        }
        String phone = authToken.getPhoneNumber();
        if(getByPhone(phone) instanceof Buyer) {
            return true;
        }else{
            throw new UnauthorizedException("No data Acess" );
        }
    }

}


