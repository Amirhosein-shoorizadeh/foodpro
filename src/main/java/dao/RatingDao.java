package dao;

import entity.Rating;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class RatingDao {
    private final Object lock = new Object();

    public void saveRating(Rating rating) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(rating);
                session.getTransaction().commit();
            }
        }
    }

    public void updateRating(Rating rating) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.merge(rating);
                session.getTransaction().commit();
            }
        }
    }

    public void deleteRating(Rating rating) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.remove(rating);
                session.getTransaction().commit();
            }
        }
    }

    public List<Rating> getRatings() {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Query<Rating> query = session.createQuery("from Rating", Rating.class);
                return query.getResultList();
            }
        }
    }

    public Rating findByOrderId(long orderId) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Query<Rating> query = session.createQuery(
                        "from Rating r where r.order.id = :orderId", Rating.class);
                query.setParameter("orderId", orderId);
                return query.uniqueResult();
            }
        }
    }
}
