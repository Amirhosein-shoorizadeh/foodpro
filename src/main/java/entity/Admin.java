package entity;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {
    public Admin( String password, String name, String phone, String email, String profileImageBase64,Bankinfo bankinfo, String Address){
        super( password, name, phone,email,profileImageBase64,bankinfo,Address);
    }
    public Admin() {}
}