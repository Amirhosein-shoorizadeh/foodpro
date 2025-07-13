package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Buyer extends User {

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();


    public Buyer() {
    }

    public Buyer(String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address) {
        super(password, name, phone,email,profileImageBase64,bankinfo,Address);

    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<PaymentTransaction> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(List<PaymentTransaction> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }
}
