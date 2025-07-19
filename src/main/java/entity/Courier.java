package entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class Courier extends User{



    public Courier( String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address,User_Status userStatus) {
        super( password, name, phone,email,profileImageBase64,bankinfo,Address,userStatus);

    }
    public Courier(){}
}