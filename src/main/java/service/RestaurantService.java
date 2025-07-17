package service;
import dao.FoodDao;
import dao.MenuDao;
import dao.RestaurantDao;
import dao.UserDao;
import dto.FoodDto;
import dto.RestaurantDto;
import entity.*;
import exception.*;
import org.json.JSONObject;
import util.JwtUtil;

import java.util.List;

public class RestaurantService {


    public static long createRestaurant(String token, RestaurantDto restaurantDto) {
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
        return restaurant.getId();
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


    public static void UpdateRestaurant(String token,RestaurantDto restaurantDto)  {
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

    public static FoodDto AddFood(String SellerPhone, FoodDto foodDto,long restaurantId) {
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            if(InvalidInput.CheckInput_AddFood(foodDto)){
                Restaurant restaurant = RestaurantDao.getById(restaurantId);
                String name = foodDto.name;
                String imageBase64 = foodDto.imageBase64;
                String description = foodDto.description;
                long price = foodDto.price;
                int supply = foodDto.supply;
                List<String> keywords = foodDto.keywords;
                Food food =  new Food(restaurant,name,imageBase64,description,price,supply,keywords);
                FoodDao.save(food);
                foodDto.id =food.getId();
                foodDto.vendor_id = restaurantId;
                return foodDto;
            }
        }

        throw new InvalidUserDataException("invalid input");
    }

    public static FoodDto UpdateFood(String SellerPhone, FoodDto foodDto,long restaurantId) {
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Food food = FoodDao.getFoodById(foodDto.id);
            if(food == null){
                throw new NotFoundException("Food not found");
            }
            if(food.getRestaurant().getId() != restaurantId){
                throw new ForbiddenException("restaurant does not have access to this restaurant");
            }
            if(InvalidInput.CheckInput_AddFood(foodDto)){
                food.setName(foodDto.name);
                food.setImageBase64(foodDto.imageBase64);
                food.setDescription(foodDto.description);
                food.setPrice(foodDto.price);
                food.setSupply(foodDto.supply);
                food.setKeywords(foodDto.keywords);
                FoodDao.update(food);
                return foodDto;
            }
        }
        throw new InvalidUserDataException("invalid input");
    }

    public static void DeleteFood(String SellerPhone,long restaurantId,long foodId) {
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Food food = FoodDao.getFoodById(foodId);
            if(food == null){
                throw new NotFoundException("Food not found");
            }
            if(food.getRestaurant().getId() != restaurantId){
                throw new ForbiddenException("restaurant does not have access to this restaurant");
            }
            FoodDao.delete(food);
        }
        else {
            throw new InvalidUserDataException("invalid input");
        }
    }

    public static void AddMenu(String SellerPhone,long restaurantId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            if(MenuDao.isMenuExists(restaurantId,title)){
                throw new ConflictExceptin("Menu already exists");
            }
            MenuDao.save(new Menu(title,restaurant));
        }
    }
    public static void DeleteMenu(String SellerPhone,long restaurantId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Menu menu = MenuDao.getMenu(restaurantId,title);
            if(menu == null){
                throw new NotFoundException("Menu not found");
            }
            MenuDao.delete(menu);
        }
    }
    public static void AddFoodToMenu(String SellerPhone,long restaurantId,long foodId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Food food = FoodDao.getFoodById(foodId);
            if(food == null){
                throw new NotFoundException("Food not found");
            }
            if(food.getRestaurant().getId() != restaurantId){
                throw new ForbiddenException("restaurant does not have access to this restaurant");
            }
            Menu menu = MenuDao.getMenu(restaurantId,title);
            if(menu == null){
                throw new NotFoundException("Menu not found");
            }
            food.setMenu(menu);
            FoodDao.update(food);
        }
    }
    public static void DeleteFoodFromMenu(String SellerPhone,long restaurantId,long foodId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Food food = FoodDao.getFoodById(foodId);
            if(food == null){
                throw new NotFoundException("Food not found");
            }
            if(food.getRestaurant().getId() != restaurantId){
                throw new ForbiddenException("restaurant does not have access to this restaurant");
            }
            if(!MenuDao.isMenuExists(restaurantId,title)){
                throw new NotFoundException("Menu not found");
            }
            if(food.getMenu() == null || (!food.getMenu().getTitle().equals(title))){
                throw new NotFoundException(" food item does not belong to this menu");
            }
            food.setMenu(null);
            FoodDao.update(food);
        }
    }







    private static boolean canModifyRestaurant(String SellerPhone, long restaurantId) {
        User user = UserDao.getByPhone(SellerPhone);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (!(user instanceof Seller)) {
            throw new ForbiddenException("Only sellers can update food");
        }
        Restaurant restaurant = RestaurantDao.getById(restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found");
        }
        if(!restaurant.getSeller().getPhone().equals(SellerPhone)){
            throw new ForbiddenException("seller does not have access to this restaurant");
        }
        return true;
    }

}
