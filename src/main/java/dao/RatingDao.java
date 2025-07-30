package dao;

import entity.Rating;
import entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class RatingDao {
    private static final Object lock = new Object();

    public static void saveRating(Rating rating) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(rating);
                session.getTransaction().commit();
            }
        }
    }

    public static void updateRating(Rating rating) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.merge(rating);
                session.getTransaction().commit();
            }
        }
    }

    public static void deleteRating(Rating rating) {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.remove(rating);
                session.getTransaction().commit();
            }
        }
    }

    public static List<Rating> getRatings() {
        synchronized (lock) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Query<Rating> query = session.createQuery("from Rating", Rating.class);
                return query.getResultList();
            }
        }
    }

    public static Rating findByOrderId(long orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Query<Rating> query = session.createQuery(
                        "from Rating r where r.order.id = :orderId", Rating.class);
                query.setParameter("orderId", orderId);
                return query.uniqueResult();
            }

    }

    public static List<Rating> findByItem(long itemId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
            select distinct r
            from Rating r
            join r.order o
            join o.orderItems oi
            where oi.food.id = :itemId
        """;
            return session.createQuery(hql, Rating.class)
                    .setParameter("itemId", itemId)
                    .list();
        }
    }


    public static Rating findById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Rating> query = session.createQuery(
                    "from Rating r where r.id =:id", Rating.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        }

    }
    public static boolean canEditId(User user, long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Rating rating = session.get(Rating.class, id);
            if (rating == null) {return false;}

            return rating.getBuyer().getId() == user.getId();
        }
    }

}
