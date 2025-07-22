package service;
import dao.*;
import dto.FoodDto;
import dto.RestaurantDto;
import entity.*;
import exception.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import util.HibernateUtil;
import util.JwtUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
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
    public static void DeleteMenu(String sellerPhone, long restaurantId, String title) {
        if (!canModifyRestaurant(sellerPhone, restaurantId)) return;
          OrderDao.delete(restaurantId,title);
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
            if (!menu.getFoods().contains(food)) {
                menu.addFood(food);
                MenuDao.update(menu); // فقط menu رو update کن
            }
        }
    }

    public static void DeleteFoodFromMenu(String SellerPhone,long restaurantId,long foodId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
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
            System.out.println("kjsbvksvkjsndcjknskcnksnc");
            Menu menu = MenuDao.getMenu(restaurantId,title);
            if(!menu.getFoods().contains(food)){
                throw new NotFoundException("Food not found in this Menu");
            }
            if (menu.getRestaurant() == null) {
                throw new RuntimeException("restaurant is null in menu before update");
            }
            menu.setRestaurant(restaurant);
            menu.removeFood(food);
            MenuDao.update(menu);
            FoodDao.update(food);
        }
    }


    public static Set<Order> GetListOfOrder(String SellerPhone,long restaurantId,JSONObject jsonObject){
        System.out.println(3333);
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            System.out.println(4444);
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            String Status = jsonObject.optString("status",null);
            System.out.println(5);
            String Search = jsonObject.optString("search",null);
            System.out.println(5);
            String user = jsonObject.optString("user",null);
            System.out.println(5);
            String courier = jsonObject.optString("courier",null);
            System.out.println(5);
            System.out.println(Status);
            System.out.println(Search);
            System.out.println(user);
            System.out.println(courier);
            Set<Order> orders = OrderDao.RestaurantSearchOrders(restaurantId,Status,Search,user,courier);
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
        List<OrderItem> orderItems = order.getOrderItems();
        if(canModifyRestaurant(SellerPhone,restaurant.getId())){

            if(status.equals("accepted")){
                if( order.getStatus() == OrderStatus.SUBMITTED){
                    order.setStatus(OrderStatus.WAITING_VENDOR);
                    LocalDateTime now = LocalDateTime.now();
                    String updatedAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    order.setUpdatedAt(updatedAt);
                }else {
                    throw new ForbiddenException("This order is not in the correct stage");
                }
            }else if(status.equals("rejected")){
                if(order.getStatus() == OrderStatus.SUBMITTED){
                    order.setStatus(OrderStatus.CANCELLED);

                    LocalDateTime now = LocalDateTime.now();
                    String updatedAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    order.setUpdatedAt(updatedAt);

                    double balance = order.getPayPrice();
                    Buyer buyer = order.getBuyer();
                    buyer.getBankinfo().increaseWalletBalance(balance);
                    UserDao.update(buyer);
                    for (OrderItem orderItem : orderItems) {
                        Food food = orderItem.getFood();
                        food.PlusSupply(orderItem.getQuantity());
                        FoodDao.update(food);
                    }
                }else {
                    throw new ForbiddenException("This order is not in the correct stage");
                }
            }else if(status.equals("served")){
                if(order.getStatus() == OrderStatus.WAITING_VENDOR){
                    order.setStatus(OrderStatus.FINDING_COURIER);

                    LocalDateTime now = LocalDateTime.now();
                    String updatedAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    order.setUpdatedAt(updatedAt);
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

    public static List<Food> GetFoodsOfRestaurant(String SellerPhone,long restaurantId){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            return restaurant.getFoods();
        }
        return null;
    }
    public static List<Menu> GetMenu(String SellerPhone,long restaurantId){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            List<Menu> menus = restaurant.getMenus();
            return menus;
        }
        return null;
    }
    public static Set<Food> GetFoodsForMenu(String SellerPhone,long restaurantId,String title){
        if(canModifyRestaurant(SellerPhone,restaurantId)){
            Restaurant restaurant = RestaurantDao.getById(restaurantId);
            List<Menu> menus = restaurant.getMenus();
            for(Menu menu : menus){
                if(menu.getTitle().equals(title)){
                    return menu.getFoods();
                }
            }
        }
        return null;
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
