package entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Buyer buyer;

    private TransactionMethod method;

    private TransactionStatus status;

    private LocalDateTime paymentDate;

    private double amount;

    public PaymentTransaction() {}
    public PaymentTransaction(Order order, Buyer buyer, TransactionMethod method,LocalDateTime paymentDate, TransactionStatus status,double amount) {
        this.order = order;
        this.buyer = buyer;
        this.method = method;
        this.status = status;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public TransactionMethod getMethod() {
        return method;
    }

    public void setMethod(TransactionMethod method) {
        this.method = method;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


}


