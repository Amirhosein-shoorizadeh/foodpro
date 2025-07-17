package dto;

import entity.Restaurant;

public class RestaurantDto {
    private long id;
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private int tax_fee;
    private long additional_fee;

    public RestaurantDto() {}
    public RestaurantDto(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.phone = restaurant.getPhone();
        this.logoBase64=restaurant.getLogobase64();
        this.tax_fee = restaurant.getTax_fee();
        this.additional_fee = restaurant.getAdditional_fee();
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
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
}
