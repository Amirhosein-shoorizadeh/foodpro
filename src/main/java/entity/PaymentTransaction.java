package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PaymentTransaction {
    @Id
    private long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Buyer buyer;

    private TransactionMethod method;

    private TransactionStatus status;

    public PaymentTransaction() {}
    public PaymentTransaction(Order order, Buyer buyer, TransactionMethod method, TransactionStatus status) {
        this.order = order;
        this.buyer = buyer;
        this.method = method;
        this.status = status;
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
}
