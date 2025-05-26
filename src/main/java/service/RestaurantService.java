package service;
import dao.RestaurantDao;
import dao.UserDao;
import dto.RestaurantDto;
import entity.*;
import exception.*;
import org.json.JSONObject;
import util.JwtUtil;

import java.util.List;

public class RestaurantService {


    public static void createRestaurant(String token, RestaurantDto restaurantDto) {
        String sellerPhone = JwtUtil.validateToken(token);
        if (sellerPhone == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = UserDao.getByPhone(sellerPhone);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!(user instanceof Seller)) {
            throw new ForbiddenException("Only sellers can create restaurants");
        }

        String name = restaurantDto.getName();
        String address = restaurantDto.getAddress();
        String phone =  restaurantDto.getPhone();
        String logoBase64 = restaurantDto.getLogoBase64();
        int tax_fee = restaurantDto.getTax_fee();
        long additional_fee = restaurantDto.getAdditional_fee();

        if (name == null || name.isEmpty() || address == null || address.isEmpty() || phone == null || phone.isEmpty()) {
            throw new InvalidUserDataException("invalid input");
        }

        if(RestaurantDao.isPhoneExists(phone)){
            throw  new ConflictExceptin("Restaurant already exists");
        }

        Restaurant restaurant = new Restaurant((Seller) user, name,address, phone, logoBase64, tax_fee, additional_fee);
        RestaurantDao.save(restaurant);
    }

    public static List<Restaurant> getSellerRestaurants(String token) {

        String sellerPhone = JwtUtil.validateToken(token);
        if (sellerPhone == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(sellerPhone);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!(user instanceof Seller)) {
            throw new ForbiddenException("Only sellers can get restaurants");
        }
        return RestaurantDao.getRestaurantsBySeller(user.getId());
    }


    public static void UpdateRestaurant(String token,RestaurantDto restaurantDto) {
        String sellerPhone = JwtUtil.validateToken(token);
        if (sellerPhone == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        User user = UserDao.getByPhone(sellerPhone);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!(user instanceof Seller)) {
            throw new ForbiddenException("Only sellers can get restaurants");
        }
        Restaurant restaurant = RestaurantDao.getById(restaurantDto.getId());
        if(restaurant == null){
            throw new NotFoundException("Restaurant not found");
        }

        String name = restaurantDto.getName();
        String address = restaurantDto.getAddress();
        String phone = restaurantDto.getPhone();
        String logoBase64 = restaurantDto.getLogoBase64();
        int tax_fee = restaurantDto.getTax_fee();
        long additional_fee = restaurantDto.getAdditional_fee();

        if (name == null || name.isEmpty() || address == null || address.isEmpty() || phone == null || phone.isEmpty()) {
            throw new InvalidUserDataException("invalid input");
        }
        if(RestaurantDao.isPhoneExists(phone) && (!restaurant.getPhone().equals(phone))){
            throw  new ConflictExceptin("Restaurant already exists");
        }
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        restaurant.setLogobase64(logoBase64);
        restaurant.setTax_fee(tax_fee);
        restaurant.setAdditional_fee(additional_fee);
        RestaurantDao.update(restaurant);
    }

}
