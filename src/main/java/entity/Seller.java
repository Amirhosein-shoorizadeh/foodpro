package entity;

import jakarta.persistence.*;

import java.util.*;


@Entity
public class Seller extends User {

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Restaurant> restaurants = new ArrayList<>();

    @Column(name = "Address",nullable = false)
    private String Address;

    public Seller() {
    }

    public Seller(String username, String password, String name, String family, String phoneNumber, String email, String address) {
        super(username, password, name, family, phoneNumber, email);
        this.Address = address;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }
}
