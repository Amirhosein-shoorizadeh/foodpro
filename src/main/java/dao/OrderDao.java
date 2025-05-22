package dao;
import entity.*;
import org.hibernate.*;
import util.HibernateUtil;
import java.util.List;

public class OrderDao {
    private OrderDao() {}

    public static void saveOrder(Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(order);
            tx.commit();
        }
    }

    public static List<Order> getOrdersByRestaurant(Long restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Order WHERE restaurant_id = :restaurantId";
            return session.createNativeQuery(sql, Order.class)
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();
        }
    }
}
