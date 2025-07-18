package dao;

import entity.Coupon;
import entity.User;
import exception.UserAlreadyExistsException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import static dao.UserDao.isRegistered;

public class CouponDao {
    private static final Object lock = new Object(); // قفل اختصاصی

    public static void save(Coupon coupon) {
        synchronized (lock) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                Coupon existing = session.createQuery("from Coupon where couponCode = :code", Coupon.class)
                        .setParameter("code", coupon.getCouponCode())
                        .uniqueResult();
                if (existing != null) {
                    throw new RuntimeException("Coupon code already exists!");
                }

                session.getTransaction().begin();
                session.persist(coupon);
                session.getTransaction().commit();
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                session.close();
            }
        }
    }
    public static Coupon findByCouponId(long couponId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Coupon WHERE id = :couponId", Coupon.class)
                    .setParameter("couponId", couponId)
                    .uniqueResult();
        }
    }


}
