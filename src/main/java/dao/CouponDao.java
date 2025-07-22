package dao;

import entity.Coupon;
import exception.NotFoundException;
import org.hibernate.Session;
import org.hibernate.annotations.Synchronize;
import util.HibernateUtil;

import java.util.List;

public class CouponDao {
    static final Object lock1 = new Object();
    Session session = HibernateUtil.getSessionFactory().openSession();

    public static boolean save(Coupon coupon) {
        synchronized (lock1) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                session.beginTransaction();
                session.persist(coupon);
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
    }

    public static Coupon findByCouponId(long couponId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Coupon WHERE id = :couponId", Coupon.class)
                    .setParameter("couponId", couponId)
                    .uniqueResult();
        }
    }


    public static boolean update(Coupon updated) {
        synchronized (lock1) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                session.beginTransaction();
                Coupon old = session.get(Coupon.class, updated.getId());
                if (old == null) {
                    throw new NotFoundException("Coupon with id " + updated.getId() + " not found.");
                }
                old.setCouponCode(updated.getCouponCode());
                old.setType(updated.getType());
                old.setValue(updated.getValue());
                old.setMinPrice(updated.getMinPrice());
                old.setUserCount(updated.getUserCount());
                old.setStartDate(updated.getStartDate());
                old.setEndDate(updated.getEndDate());

                session.merge(old);
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
    }

    public static boolean deleteById(Long id) {
        synchronized (lock1) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
                session.beginTransaction();
                Coupon coupon = session.get(Coupon.class, id);
                if (coupon != null) {
                    session.remove(coupon);
                    session.getTransaction().commit();
                    return true;
                } else {
                    session.getTransaction().rollback();
                    return false;
                }
            } finally {
                session.close();
            }
        }
    }

    public static Coupon getByCode(Long code) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Coupon WHERE couponCode = :code", Coupon.class)
                    .setParameter("code", code)
                    .uniqueResult();
        } finally {
            session.close();
        }
    }

    public static List<Coupon> getAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Coupon", Coupon.class).getResultList();
        } finally {
            session.close();
        }
    }
}
