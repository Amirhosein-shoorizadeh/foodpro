package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Food> foods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "restaurant_comments", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
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

    public long getId() {return id;}

    public void setId(long id) {this.id = id;}

    public List<Food> getFoods() {return foods;}

    public void setFoods(List<Food> foods) {this.foods = foods;}

    public List<String> getComments() {return comments;}

    public void setComments(List<String> comments) {this.comments = comments;}

    public Seller getSeller() {return seller;}

    public void setSeller(Seller seller) {this.seller = seller;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public String getLogobace64() {return logobace64;}

    public void setLogobace64(String logobace64) {this.logobace64 = logobace64;}
}
