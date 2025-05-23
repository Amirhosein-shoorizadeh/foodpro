package entity;

import jakarta.persistence.*;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Column( nullable = false)
    private String password;

    @Column(nullable = false)
    private String full_name;


    @Column( nullable = false, unique = true)
    private String phone;

    @Column( unique = true)
    private String email;



    @Column(nullable = true)
    private String profileImageBase64;

    @Embedded
    private Bankinfo bank_info;

    @Column(nullable = false)
    private String Address;

    @OneToOne(mappedBy = "buyer", cascade = CascadeType.ALL)
    private Cart cart;


    public User( String password, String name, String phone, String email, String profileImageBase64, Bankinfo bank_info, String Address) {

        this.password = password;
        this.full_name = name;
        this.phone = phone;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
        this.bank_info = bank_info;
        this.Address = Address;
    }
    public User(long Id,String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address){
        this(password,  name,  phone, email,  profileImageBase64, bankinfo, Address);
        this.Id = Id;
    }

    public User() {
    }


    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phoneNumber) {this.phone = phoneNumber;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }

    public Bankinfo getBankinfo() {
        return bank_info;
    }

    public void setBankinfo(Bankinfo bankinfo) {
        this.bank_info = bankinfo;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}

