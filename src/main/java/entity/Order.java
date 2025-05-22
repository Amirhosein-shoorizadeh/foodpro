package entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Order {
    @Id @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Date createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}
    public Order(Customer customer, Date createdAt) {
        this.customer = customer;
        this.createdAt = createdAt;
    }
}
