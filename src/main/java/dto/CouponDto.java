package dto;

import entity.Coupon;

import java.time.LocalDate;


public class CouponDto {
    public long Id;
    public String coupon_code;
    public String type;
    public Double value;
    public Integer min_price;
    public Integer user_count;
    public String start_date;
    public String end_date;

    public CouponDto(long id, String coupon_code, String type, Double value, Integer min_price, Integer user_count, String start_date, String end_date) {
        Id = id;
        this.coupon_code = coupon_code;
        this.type = type;
        this.value = value;
        this.min_price = min_price;
        this.user_count = user_count;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getCoupon_code() {
        return coupon_code;
    }

    public void setCoupon_code(String coupon_code) {
        this.coupon_code = coupon_code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Integer getMin_price() {
        return min_price;
    }

    public void setMin_price(Integer min_price) {
        this.min_price = min_price;
    }

    public Integer getUser_count() {
        return user_count;
    }

    public void setUser_count(Integer user_count) {
        this.user_count = user_count;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public Coupon toEntity() {
        return new Coupon(
                coupon_code,
                Coupon.Type.valueOf(type), // تبدیل رشته به enum
                value,
                min_price,
                user_count,
                LocalDate.parse(start_date),
                LocalDate.parse(end_date)
        );
    }


}


