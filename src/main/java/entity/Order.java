package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @OneToOne(mappedBy = "Customer")
    private Customer customer;

    @OneToOne(mappedBy = "Restaurant")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "food")
    private ArrayList<Food> Foods = new ArrayList<Food>();

    public Order(Customer customer, Restaurant restaurant
            , ArrayList<Food> Foods) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.Foods = Foods;
    }

    public Long getId() {
        return Id;

    }

    public Customer getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

}
