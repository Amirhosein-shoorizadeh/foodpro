package entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Entity
public class Seller extends User {

    @OneToMany(mappedBy = "Seller", cascade = CascadeType.ALL)
    private ArrayList<Restaurant> Restaurants = new ArrayList<Restaurant>();

    public Seller(String username, String password, String name, String family, String phoneNumber, String email, String address) {
        super(username, password, name, family, phoneNumber, email, address);
    }

    public Seller() {
    }

    public ArrayList<Restaurant> getRestaurants() {
        return Restaurants;
    }

}