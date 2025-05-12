package entity;
import jakarta.persistence.*;


 // Use jakarta.persistence instead of javax.persistence
import java.util.Date;

@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    public User() {}

    public User(String name) {
        this.name = name;
    }

    // Getter & Setter
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
