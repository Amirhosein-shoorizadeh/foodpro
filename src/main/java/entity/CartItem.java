package entity;

import jakarta.persistence.*;

@Entity
public class CartItem {
    @Id @GeneratedValue
    private long id;

    @ManyToOne
    private Cart cart;

    @ManyToOne
    private Food food;

    private int quantity;

    public CartItem() {}
    public CartItem(Cart cart, Food food, int quantity) {
        this.cart = cart;
        this.food = food;
        this.quantity = quantity;
    }

    public long getId() {return id;}

    public void setId(long id) {this.id = id;}

    public Cart getCart() {return cart;}

    public void setCart(Cart cart) {this.cart = cart;}

    public Food getFood() {return food;}

    public void setFood(Food food) {this.food = food;}

    public int getQuantity() {return quantity;}

    public void setQuantity(int quantity) {this.quantity = quantity;}
}
