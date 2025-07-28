package dto;

import entity.Rating;

import java.util.List;

public class RatingDto {
    private long id;

    private long orderId;

    private long rate;

    private String comment;

    private List<String> logobase64; // تغییر از String به List<String>

    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getLogobase64() { // به‌روز کردن گتر
        return logobase64;
    }

    public void setLogobase64(List<String> logobase64) { // به‌روز کردن ستر
        this.logobase64 = logobase64;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}