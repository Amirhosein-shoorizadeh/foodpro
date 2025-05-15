package entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer buyer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToMany
    @JoinTable(
            name = "order_food",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<Food> foods = new ArrayList<>();

    public Order() {}

    public Order(Customer buyer, Restaurant restaurant, List<Food> foods) {
        this.buyer = buyer;
        this.restaurant = restaurant;
        this.foods = foods;
    }

    // Getters & setters
}
