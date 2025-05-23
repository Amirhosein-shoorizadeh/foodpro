package dao;
import entity.*;
import org.hibernate.*;
import util.HibernateUtil;
import java.util.List;

public class CartDao {

    public CartDao(){}

    public static Cart getbyId(String id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            CartDao cartDao = new CartDao();
            String sql = "SELECT * FROM Cart WHERE id = :id";
            return (Cart) session.createNativeQuery(sql,Cart.class).setParameter("id", id).uniqueResult();

        }
    }

    public static void addItemToCart(Cart cart, CartItem item) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            cart = session.merge(cart);
            item.setCart(cart);
            cart.getItems().add(item);
            session.persist(item);

            tx.commit();
        }
    }

    public static void removeItemFromCart(Cart cart, CartItem item) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            cart = session.merge(cart);
            item.setCart(null);
            cart.getItems().remove(item);
            tx.commit();
        }
    }



}
