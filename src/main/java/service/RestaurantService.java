package service;
import dao.*;
import dto.FoodDto;
import dto.RestaurantDto;
import entity.*;
import exception.*;
import org.json.JSONObject;
import util.JwtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        ((Seller) user).getRestaurants().add(restaurant);
        UserDao.update(user);
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
                Food food =  new Food(name,imageBase64,description,price,supply,keywords);
                restaurant.addFood(food);
                FoodDao.save(food);
                RestaurantDao.update(restaurant);
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
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            Food food = FoodDao.getFoodById(foodId);
            if(food == null){
                throw new NotFoundException("Food not found");
            }
            if(food.getRestaurant().getId() != restaurantId){
                throw new ForbiddenException("restaurant does not have access to this restaurant");
            }
            restaurant.getFoods().remove(food);
            FoodDao.delete(food);
            RestaurantDao.update(restaurant);
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
            Menu menu = new Menu(title);
            restaurant.addMenu(menu);
            MenuDao.save(menu);
            RestaurantDao.update(restaurant);
        }
    }
    public static void DeleteMenu(String SellerPhone,long restaurantId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            Menu menu = MenuDao.getMenu(restaurantId,title);
            if(menu == null){
                throw new NotFoundException("Menu not found");
            }
            restaurant.removeMenu(menu);
            MenuDao.delete(menu);
            RestaurantDao.update(restaurant);
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
            menu.addFood(food);
            FoodDao.update(food);
            MenuDao.update(menu);
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
            Menu menu = MenuDao.getMenu(restaurantId,title);
            if(!menu.getFoods().contains(food)){
                throw new NotFoundException("Food not found in this Menu");
            }
            menu.getFoods().remove(food);
            FoodDao.update(food);
            MenuDao.update(menu);
        }
    }


    public static List<Order> GetListOfOrder(String SellerPhone,long restaurantId,JSONObject jsonObject){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            String Status = jsonObject.optString("status",null);
            String Search = jsonObject.optString("search",null);
            String user = jsonObject.optString("user",null);
            String courier = jsonObject.optString("courier",null);
            OrderStatus orderStatus = OrderStatus.valueOf(Status.toUpperCase());
            List<Order> orders = OrderDao.RestaurantSearchOrders(restaurantId,orderStatus,Search,user,courier);
            return orders;
        }
        return null;
    }









    public static void ChangeStatusOfOrder(String SellerPhone,long order_id,String status){
        Order order = OrderDao.getOrderById(order_id);
        if(order == null){
            throw new NotFoundException("Order not found");
        }
        Restaurant restaurant = order.getRestaurant();
        Set<Food> foods = order.getFoods();
        if(canModifyRestaurant(SellerPhone,restaurant.getId())){

            if(status.equals("accepted")){
                if( order.getStatus() == OrderStatus.SUBMITTED){
                    order.setStatus(OrderStatus.WAITING_VENDOR);
                    for (Food food :  foods){
                        food.MinusSupply();
                        FoodDao.update(food);
                    }
                }else {
                    throw new ForbiddenException("This order is not in the correct stage");
                }
            }else if(status.equals("rejected")){
                if(order.getStatus() == OrderStatus.SUBMITTED){
                    order.setStatus(OrderStatus.CANCELLED);
                }else {
                    throw new ForbiddenException("This order is not in the correct stage");
                }
            }else if(status.equals("served")){
                if(order.getStatus() == OrderStatus.WAITING_VENDOR){
                    order.setStatus(OrderStatus.FINDING_COURIER);
                }else {
                    throw new ForbiddenException("This order is not in the correct stage");
                }
            }else {
                throw new ForbiddenException("The status you provided for the order is invalid");
            }
            RestaurantDao.update(restaurant);
            OrderDao.update(order);
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
