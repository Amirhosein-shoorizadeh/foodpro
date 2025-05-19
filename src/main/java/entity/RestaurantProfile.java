package entity;

import jakarta.persistence.*;

@Entity
public class RestaurantProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String ImagePath;

    @OneToOne
    @JoinColumn(name = "Restaourant_id", referencedColumnName = "id")
    private Restaurant restaurant;

    public RestaurantProfile() {}

    public RestaurantProfile(String ImagePath,Restaurant restaurant) {
        this.ImagePath = ImagePath;
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
