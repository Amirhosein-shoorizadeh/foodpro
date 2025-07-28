package entity;

import exception.NotFoundException;
import jakarta.persistence.*;

import java.util.*;

@Entity
public class Buyer extends User {

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_favorite_restaurants",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();


    public Buyer() {
    }

    public Buyer(String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address, User_Status userStatus) {
        super(password, name, phone, email, profileImageBase64, bankinfo, Address, userStatus);

    }
    public boolean deleteFavoriteRestaurant(Long id) throws NotFoundException {
        boolean isDeleted = false;
        for (Restaurant restaurant : favoriteRestaurants) {
            if(restaurant.isIdRight(id)) {
                favoriteRestaurants.remove(restaurant);
                isDeleted = true;
                return  isDeleted;
            }
        }
        if (!isDeleted) {
            throw new NotFoundException("Restaurant with id " + id + " not found.");
        }else{
                return  false;
        }
    }
    public void addFavoriteRestaurant(Restaurant restaurant) throws NotFoundException {
        this.favoriteRestaurants.add(restaurant);
    }
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<PaymentTransaction> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(List<PaymentTransaction> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public List<Restaurant> getFavoriteRestaurants() {

        return favoriteRestaurants;
    }
}
