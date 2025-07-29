package dao;
import entity.Food;
import org.hibernate.*;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class FoodDao {

    private FoodDao() {}

    public static void save(Food food) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(food);
            tx.commit();
        }
    }
    public static void update(Food food) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(food);
            tx.commit();
        }
    }

    public static void delete(Food food) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(food);
            tx.commit();
        }
    }

    public static Food getFoodById(long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Food WHERE id = :id";
            return (Food) session.createNativeQuery(sql, Food.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }
    }

    public static List<Food> getFoodsByRestaurantId(String restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT * FROM Food WHERE restaurant_id = :restaurantId";
            return session.createNativeQuery(sql, Food.class)
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();
        }
    }

    public static List<Food> searchFoods(String search, double priceLimit, List<String> keywords, int page, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT f FROM Food f ");
            boolean hasKeywords = false;
            if (keywords != null) {
                for (String k : keywords) {
                    if (!k.trim().isEmpty()) {
                        hasKeywords = true;
                        break;
                    }
                }
            }
            boolean hasSearch = search != null && !search.trim().isEmpty();
            boolean hasPrice = priceLimit > 0;

            if (hasKeywords) {
                hql.append("JOIN f.keywords k ");
            }

            List<String> conditions = new ArrayList<>();

            if (hasSearch) {
                conditions.add("LOWER(f.name) LIKE :search");
            }

            if (hasKeywords) {
                conditions.add("LOWER(k) IN :keywords");
            }

            if (hasPrice) {
                conditions.add("f.price <= :priceLimit");
            }

            if (!conditions.isEmpty()) {
                hql.append("WHERE ").append(String.join(" AND ", conditions));
            }

            Query<Food> query = session.createQuery(hql.toString(), Food.class);

            if (hasSearch) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            if (hasKeywords) {
                List<String> lowered = keywords.stream().map(String::toLowerCase).toList();
                query.setParameter("keywords", lowered);
            }

            if (hasPrice) {
                query.setParameter("priceLimit", priceLimit);
            }

            int firstResult = (page - 1) * pageSize;
            query.setFirstResult(firstResult);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    public static List<Food> getTopRatedFoods(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
            select oi.food
            from Rating r
            join r.order o
            join o.orderItems oi
            group by oi.food.id
            order by avg(r.Rate) desc
        """;

            return session.createQuery(hql, Food.class)
                    .setMaxResults(limit)
                    .list();
        }
    }



}
