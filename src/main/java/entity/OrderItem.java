package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_item")
public class  OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private Food food;

    private int quantity;

    public OrderItem() {}

    public OrderItem(Order order, Food food, int quantity) {
        this.order = order;
        this.food = food;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
