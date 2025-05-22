package entity;

import jakarta.persistence.*;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String Id;

    @Column( nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullname;


    @Column( nullable = false, unique = true)
    private String phone;

    @Column( unique = true)
    private String email;



    @Column(nullable = true)
    private String profileImageBase64;

    @Embedded
    private Bankinfo bankinfo;

    @Column(nullable = false)
    private String Address;


    public User( String password, String name, String phone, String email, String profileImageBase64, Bankinfo bankinfo, String Address) {

        this.password = password;
        this.fullname = name;
        this.phone = phone;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
        this.bankinfo = bankinfo;
        this.Address = Address;
    }

    public User() {
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getfullname() {
        return fullname;
    }

    public void setfullName(String name) {
        this.fullname = name;
    }

    public String getphone() {
        return phone;
    }

    public void setPhone(String phoneNumber) {this.phone = phoneNumber;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public abstract String getPhone();
}

