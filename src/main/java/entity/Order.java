package entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "Orders")
public class Order {
    @Id @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    private Date createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}
    public Order(Buyer buyer, Date createdAt) {
        this.buyer = buyer;
        this.createdAt = createdAt;
    }
}
