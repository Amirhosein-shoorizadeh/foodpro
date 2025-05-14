package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.util.concurrent.ConcurrentHashMap;

@Entity
public class Seller extends User{

    @OneToMany(mappedBy = "Seller")
    ConcurrentHashMap<String,Restaurant> Restaurants;

    public Seller(String username, String password, String name, String family, String phoneNumber, String email, String address){
        super(username, password, name, family, phoneNumber, email, address);
    }
    public Seller(){}

}