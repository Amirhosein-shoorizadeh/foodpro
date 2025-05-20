package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Food> foods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "restaurant_comments", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column
    private String address;

    @Column
    private String phone;

    @Column
    private String logobace64;

    public Restaurant() {}
    public Restaurant(Seller seller, String address, String phone, String logobace64) {
        this.seller = seller;
        this.address = address;
        this.phone = phone;
        this.logobace64 = logobace64;
    }

}
