package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Food> foods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "restaurant_comments", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
}
