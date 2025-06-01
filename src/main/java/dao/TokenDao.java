package dao;

import entity.Token;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import util.HibernateUtil;

public class TokenDao {

    public static void save(Token token) {

        try(Session session = HibernateUtil.getSessionFactory().openSession();) {
            session.beginTransaction();
            session.persist(token);
            session.getTransaction().commit();
            System.out.println("save token");

        }
    }

    public static boolean isRevoked(String tokenString) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            TypedQuery<Token> query = session.createQuery(
                    "FROM Token t WHERE t.token = :token AND t.revoked = true", Token.class);
            query.setParameter("token", tokenString);
            return !query.getResultList().isEmpty();
        } finally {
            session.close();
        }
    }

    public static void revoke(String tokenString) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            Token token = session.createQuery("FROM Token t WHERE t.token = :token", Token.class)
                    .setParameter("token", tokenString)
                    .getSingleResult();
            token.setRevoked(true);
            session.merge(token);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public static Token findByToken(String token) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Token t WHERE t.token = :token", Token.class)
                    .setParameter("token", token)
                    .uniqueResult();
        } finally {
            session.close();
        }
    }

    public static void delete(Token token) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.remove(token);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }
    public static void deleteExpiredAndRevokedTokens() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.createNativeQuery(
                    "DELETE FROM Token WHERE revoked = TRUE OR expiresAt < (strftime('%s','now') * 1000)"
            ).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

}
