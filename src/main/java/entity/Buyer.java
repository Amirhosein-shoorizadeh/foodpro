package entity;

import jakarta.persistence.*;

import java.util.*;

@Entity
public class Buyer extends User {

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    public List<Order> orders = new ArrayList<>();


    public Buyer() {
    }

    public Buyer(String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address,User_Status userStatus) {
        super(password, name, phone,email,profileImageBase64,bankinfo,Address,userStatus);

    }
}
