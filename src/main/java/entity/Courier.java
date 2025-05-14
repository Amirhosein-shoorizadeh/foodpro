package entity;

import jakarta.persistence.Entity;

@Entity
public class Courier extends User{
    public Courier(String username, String password, String name, String family, String phoneNumber, String email, String address){
        super(username, password, name, family, phoneNumber, email, address);
    }
    public Courier(){}
}
