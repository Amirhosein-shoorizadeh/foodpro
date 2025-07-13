package entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Courier extends User{

    @OneToMany(mappedBy = "courier",cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Order> orders = new ArrayList<>();


    public Courier( String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address){
        super( password, name, phone,email,profileImageBase64,bankinfo,Address);

    }
    public Courier(){}
}