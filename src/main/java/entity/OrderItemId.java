package entity;

import jakarta.persistence.*;

@Entity
public class OrderItemId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long itemId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderItemId() {}
    public OrderItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
