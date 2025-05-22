package entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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
    private int price;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column
    private String ImagePath;

    @Enumerated(EnumType.STRING)
    private  CategoryFood categoryFood;

    public Food() {}

    public Food(String name, int price, Restaurant restaurant, CategoryFood categoryFood) {
        this.name = name;
        this.price = price;
        this.restaurant = restaurant;
        this.categoryFood = categoryFood;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public CategoryFood getCategoryFood() {
        return categoryFood;
    }

    public void setCategoryFood(CategoryFood categoryFood) {
        this.categoryFood = categoryFood;
    }
}
