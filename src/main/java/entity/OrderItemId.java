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

}
