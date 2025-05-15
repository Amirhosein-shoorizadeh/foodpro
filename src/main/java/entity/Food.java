package entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "food_description", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "description")
    private List<String> description = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "food_comments", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();

    @Column(nullable = false)
    private int number;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public Food() {}

    public Food(String name, int number) {
        this.name = name;
        this.number = number;
    }

    // Getters & Setters
}