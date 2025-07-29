package dao;

import entity.Coupon;
import entity.Order;
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
                session.update(updated);
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

                // اول کوپن را لود کن
                Coupon coupon = session.get(Coupon.class, id);
                if (coupon == null) {
                    session.getTransaction().rollback();
                    return false;
                }

                // همه Orderهایی که از این کوپن استفاده می‌کنن پیدا کن
                String hql = "FROM Order o WHERE o.coupon.id = :couponId";
                List<Order> ordersUsingCoupon = session.createQuery(hql, Order.class)
                        .setParameter("couponId", id)
                        .getResultList();

                // قطع ارتباط
                for (Order order : ordersUsingCoupon) {
                    order.setCoupon(null);
                    session.merge(order); // یا session.update(order);
                }

                // حالا کوپن رو حذف کن
                session.remove(coupon);

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


    public static Coupon getByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println(56565);
            return session.createQuery("FROM Coupon c WHERE c.couponCode = :code", Coupon.class)
                    .setParameter("code", code)
                    .uniqueResult();
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
