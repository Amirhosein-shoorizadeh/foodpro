package entity;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "Seller", cascade = CascadeType.ALL)
    private ArrayList<Restaurant> Restaurants = new ArrayList<Restaurant>();

    @ElementCollection
    @CollectionTable(name = "Comment", joinColumns = @JoinColumn(name = "RestuarantId"))
    @Column(name = "Comments")
    private ArrayList<String> Comment = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "Seller-id", nullable = false)
    private Seller seller;
}