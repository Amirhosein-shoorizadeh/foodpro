package dto;

public class RatingDto {
    private int value;
    private String comment;
    private long orderId;

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
}
