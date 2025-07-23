package entity;

import dto.RatingDto;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long Order_Id;

    @Column(nullable = false)
    private long Rate;

    @Column
    private String comment;

    @ElementCollection // برای ذخیره لیست به عنوان مجموعه‌ای از مقادیر
    @Column(name = "logobase64") // نام ستون برای لیست
    private List<String> logobase64 = new ArrayList<>(); // تغییر از String به List<String> با مقدار پیش‌فرض

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @OneToOne
    @JoinColumn(name = "buyer_id", nullable = false, unique = true)
    private Buyer buyer;

    @Column
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public Rating() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrder_Id() {
        return Order_Id;
    }

    public void setOrder_Id(long order_Id) {
        Order_Id = order_Id;
    }

    public long getRate() {
        return Rate;
    }

    public void setRate(long rate) {
        Rate = rate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getLogobase64() { // به‌روز کردن گتر
        return logobase64;
    }

    public void setLogobase64(List<String> logobase64) { // به‌روز کردن ستر
        this.logobase64 = logobase64;
    }

    public Rating(long order_Id, String comment, long rate, List<String> logobase64) { // به‌روز کردن سازنده
        Order_Id = order_Id;
        this.comment = comment;
        Rate = rate;
        this.logobase64 = logobase64 != null ? new ArrayList<>(logobase64) : new ArrayList<>();
    }

    public Rating(RatingDto rateDto, Buyer buyer) { // به‌روز کردن سازنده
        Order_Id = rateDto.getOrderId();
        Rate = rateDto.getRate();
        this.comment = rateDto.getComment();
        this.logobase64 = rateDto.getLogobase64() != null ? new ArrayList<>(rateDto.getLogobase64()) : new ArrayList<>();
        this.buyer = buyer;
        this.userName = buyer.getFull_name();
    }
}