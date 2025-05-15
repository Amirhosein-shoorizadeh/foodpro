package entity;

import jakarta.persistence.*;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "Username",unique = true, nullable = false)
    private String Username;

    @Column(name = "Password",nullable = false)
    private String Password;

    @Column(name = "Name",nullable = false)
    private String Name;

    @Column(name = "Family",nullable = false)
    private String Family;

    @Column(name = "phoneNumber",nullable = false,unique = true)
    private String PhoneNumber;

    @Column(name = "Email",unique = true)
    private String Email;

    @Column(name = "Address",nullable = false)
    private String Address;

    public User( String username, String password, String name, String family, String phoneNumber, String email, String address) {
        this.Username = username;
        this.Password = password;
        this.Name = name;
        this.Family = family;
        this.PhoneNumber = phoneNumber;
        this.Email = email;
        this.Address = address;
    }
    public User() {}


    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFamily() {
        return Family;
    }

    public void setFamily(String family) {
        Family = family;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}

