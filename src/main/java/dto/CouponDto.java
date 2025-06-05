package dto;
import entity.Coupon;

import java.time.LocalDate;



public class CouponDto {
    public String coupon_code;
    public String type;
    public Double value;
    public Integer min_price;
    public Integer user_count;
    public String start_date;
    public String end_date;

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


