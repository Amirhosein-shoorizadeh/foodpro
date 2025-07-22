package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "Restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Food> foods = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Menu> menus = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "restaurant_comments", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "comment")
    private List<String> comments = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Order> orders = new ArrayList<>();

    @ManyToMany(mappedBy = "favoriteRestaurants")
    private List<User> likedByUsers = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column
    private String logobase64;

    @Column
    private int tax_fee;

    @Column(nullable = false)
    private long additional_fee;

    @Enumerated(EnumType.STRING)
    ApprovalStatus approval_status = ApprovalStatus.pending;


    public Restaurant() {
    }

    public Restaurant(Seller seller, String name, String address, String phone, String logobace64, int tax_fee, long additional_fee) {
        this.seller = seller;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logobase64 = logobace64;
        this.tax_fee = tax_fee;
        this.additional_fee = additional_fee;
    }

    public boolean isIdRight(Long id) {
        if (id == this.id) {
            return true;
        } else {
            return false;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogobase64() {
        return logobase64;
    }

    public void setLogobase64(String logobase64) {
        this.logobase64 = logobase64;
    }

    public int getTax_fee() {
        return tax_fee;
    }

    public void setTax_fee(int tax_fee) {
        this.tax_fee = tax_fee;
    }

    public long getAdditional_fee() {
        return additional_fee;
    }

    public void setAdditional_fee(long additional_fee) {
        this.additional_fee = additional_fee;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public ApprovalStatus getApproval_status() {
        return approval_status;
    }

    public void setApproval_status(ApprovalStatus approval_status) {
        this.approval_status = approval_status;
    }

    public void addFood(Food food) {
        foods.add(food);
        food.setRestaurant(this);
    }

    public void removeFood(Food food) {
        foods.remove(food);
        food.setRestaurant(null);
    }

    public void addMenu(Menu menu) {
        menus.add(menu);
        menu.setRestaurant(this);
    }

    public void removeMenu(Menu menu) {
        menus.remove(menu);
        menu.setRestaurant(null);
    }
}
