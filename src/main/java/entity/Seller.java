package entity;

import jakarta.persistence.*;

import java.util.*;


@Entity
public class Seller extends User {

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Restaurant> restaurants = new ArrayList<>();





    public Seller() {
    }

    public Seller(  String password, String name, String phone, String email, String profileImageBase64,Bankinfo bankinfo, String Address) {
        super( password, name, phone,email,profileImageBase64,bankinfo,Address);

    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }
}
