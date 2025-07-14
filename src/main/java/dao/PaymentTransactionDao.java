package dao;

import entity.*;
import exception.NotFoundException;
import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static Set<PaymentTransaction> GetTransactionSet(String searchFood,String buyerKeyword,String methodKeyword,String statusKeyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT pt FROM PaymentTransaction pt
            JOIN pt.buyer b
            JOIN pt.order o
            JOIN o.foods f
            WHERE 1=1
        """);

            if (searchFood != null && !searchFood.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(f.name), ' ', '') LIKE :foodKw ");
            }
            if (buyerKeyword != null && !buyerKeyword.isBlank()) {
                jpql.append(" AND REPLACE(LOWER(b.full_name), ' ', '') LIKE :buyerKw ");
            }
            if (methodKeyword != null && !methodKeyword.isBlank()) {
                jpql.append(" AND LOWER(pt.method) = :methodKw ");
            }
            if (statusKeyword != null && !statusKeyword.isBlank()) {
                jpql.append(" AND LOWER(pt.status) = :statusKw ");
            }

            Query<PaymentTransaction> query = session.createQuery(jpql.toString(), PaymentTransaction.class);

            if (searchFood != null && !searchFood.isBlank()) {
                query.setParameter("foodKw", "%" + searchFood.toLowerCase().replace(" ", "") + "%");
            }
            if (buyerKeyword != null && !buyerKeyword.isBlank()) {
                query.setParameter("buyerKw", "%" + buyerKeyword.toLowerCase().replace(" ", "") + "%");
            }
            if (methodKeyword != null && !methodKeyword.isBlank()) {
                query.setParameter("methodKw", methodKeyword.toLowerCase());
            }
            if (statusKeyword != null && !statusKeyword.isBlank()) {
                query.setParameter("statusKw", statusKeyword.toLowerCase());
            }

            return new HashSet<>(query.getResultList());
        }
    }


}
