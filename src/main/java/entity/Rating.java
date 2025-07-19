package entity;

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
    @Column
    private String logobase64;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;



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

    public String getLogobase64() {
        return logobase64;
    }

    public void setLogobase64(String logobase64) {
        this.logobase64 = logobase64;
    }

    public Rating(long order_Id, long id, String comment, long rate, String logobase64) {
        Order_Id = order_Id;
        this.id = id;
        this.comment = comment;
        Rate = rate;
        this.logobase64 = logobase64;
    }

}
