package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Customer extends User {

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    public List<Order> orders = new ArrayList<>();

    @Column(name = "Address",nullable = false)
    private String Address;

    public Customer() {
    }

    public Customer(String username, String password, String name, String family, String phoneNumber, String email) {
        super(username, password, name, family, phoneNumber, email);
    }
}
