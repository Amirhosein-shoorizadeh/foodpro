package entity;

import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue
    private String id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Food food;

    private int quantity;

    @ManyToOne
    private Restaurant restaurant;

    public OrderItem() {}
    public OrderItem(Order order, Food food, int quantity, Restaurant restaurant) {
        this.order = order;
        this.food = food;
        this.quantity = quantity;
        this.restaurant = restaurant;
    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public Order getOrder() {return order;}

    public void setOrder(Order order) {this.order = order;}

    public Food getFood() {return food;}

    public void setFood(Food food) {this.food = food;}

    public int getQuantity() {return quantity;}

    public void setQuantity(int quantity) {this.quantity = quantity;}

    public Restaurant getRestaurant() {return restaurant;}

    public void setRestaurant(Restaurant restaurant) {this.restaurant = restaurant;}
}
