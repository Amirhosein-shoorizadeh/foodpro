package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.util.concurrent.ConcurrentHashMap;

@Entity
public class Seller extends User{

    @OneToMany(mappedBy = "Seller")
    ConcurrentHashMap<String,Restaurant> Restaurants;

}
