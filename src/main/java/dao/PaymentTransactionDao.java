package dao;

import entity.*;
import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;
import java.util.List;

public class PaymentTransactionDao {
    public static void savePaymentTransaction(PaymentTransaction paymentTransaction) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            session.save(paymentTransaction);
            transaction.commit();
        }
    }

    public static List<PaymentTransaction> getUserTransaction(String Phone) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String hql = "SELECT pt FROM PaymentTransaction pt " +
                    "JOIN FETCH pt.buyer " +
                    "JOIN FETCH pt.order " +
                    "WHERE pt.buyer.phone = :phone";

             return session.createQuery(hql, PaymentTransaction.class)
                    .setParameter("phone", Phone)
                    .getResultList();
        }
    }

}
