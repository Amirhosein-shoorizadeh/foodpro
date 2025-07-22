package dto;

public class RatingDto {

    private long orderId;

    private long rate;

    private String comment;

    private String logobase64;

    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLogobase64() {
        return logobase64;
    }

    public void setLogobase64(String logobase64) {
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
}
