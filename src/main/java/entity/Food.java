package entity;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @Column(name = "Name", nullable = false)
    private String Name;

    @ElementCollection
    @CollectionTable(name = "Discirption", joinColumns = @JoinColumn(name = "FoodId"))
    @Column(name = "Discirptions")
    private ArrayList<String> Discirption = new ArrayList<>();


    @ElementCollection
    @CollectionTable(name = "Comment", joinColumns = @JoinColumn(name = "FoodId"))
    @Column(name = "Comments")
    private ArrayList<String> Comment = new ArrayList<>();

    @Column(name = "Number", nullable = false)
    private int Number;

    @ManyToOne
    @JoinColumn(name = "Restuarant")
    private Restaurant Restaurant;


    public Food(String name, int number) {
        this.Name = name;
        this.Number = number;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public Restaurant getRestaurant() {
        return Restaurant;
    }
}
