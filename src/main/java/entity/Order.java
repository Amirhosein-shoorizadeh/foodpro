package entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "Orders")
public class Order {
    @Id @GeneratedValue
    private long id;

    private String deliveryAddress;
    private long customerId;
    private long vendorId;
    private long couponId; // nullable

    @OneToMany
    private List<OrderItemId> itemIds = new ArrayList<>();

    private long rawPrice;
    private int taxFee;
    private long additionalFee;
    private int courierFee;
    private long payPrice;
    private long courierId; // nullable

    @Enumerated(EnumType.STRING)
    private Status status; // could be validated against enum
    private String createdAt;
    private String updatedAt;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    public Order() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getVendorId() {
        return vendorId;
    }

    public void setVendorId(long vendorId) {
        this.vendorId = vendorId;
    }

    public long getCouponId() {
        return couponId;
    }

    public void setCouponId(long couponId) {
        this.couponId = couponId;
    }

    public List<OrderItemId> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<OrderItemId> itemIds) {
        this.itemIds = itemIds;
    }

    public long getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(long rawPrice) {
        this.rawPrice = rawPrice;
    }

    public int getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }

    public long getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(long additionalFee) {
        this.additionalFee = additionalFee;
    }

    public int getCourierFee() {
        return courierFee;
    }

    public void setCourierFee(int courierFee) {
        this.courierFee = courierFee;
    }

    public long getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(long payPrice) {
        this.payPrice = payPrice;
    }

    public long getCourierId() {
        return courierId;
    }

    public void setCourierId(long courierId) {
        this.courierId = courierId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }
}
