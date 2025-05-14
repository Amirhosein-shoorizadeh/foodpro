package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "name",unique = true, nullable = false)
    private String Name;

    @Column(name = "address",unique = true, nullable = false)
    private String Address;

    @Column(name = "phone",unique = true, nullable = false)
    private String Phone;


    @ManyToOne
    @JoinColumn(name = "seller-id")
    private Seller seller;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        this.Id = id;
    }

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }
}
