package main;
import entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class Main {
    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        // ایجاد شی و ذخیره
        User s1 = new User("Amir Hosein");
        session.persist(s1);

        tx.commit();

        // خواندن شیء از دیتابیس
        User result = session.get(User.class, s1.getId());
        System.out.println("Read from DB: " + result.getName());

        session.close();
    }
}
