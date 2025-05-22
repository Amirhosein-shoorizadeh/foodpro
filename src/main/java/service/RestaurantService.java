package service;
import dao.RestaurantDao;
import entity.*;

public class RestaurantService {
    public static void createRestaurant(Restaurant restaurant, Seller seller) {
        restaurant.setSeller(seller);
        RestaurantDao.save(restaurant);
    }

}
