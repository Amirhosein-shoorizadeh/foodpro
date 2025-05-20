package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Customer extends User {

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    public List<Order> orders = new ArrayList<>();


    public Customer() {
    }

    public Customer(  String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address) {
        super(password, name, phone,email,profileImageBase64,bankinfo,Address);

    }
}
