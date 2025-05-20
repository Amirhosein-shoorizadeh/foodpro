package entity;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String Id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column
    private String ImagePath;

    public Profile() {}
    public Profile(User user, String ImagePath) {
        this.user = user;
        this.ImagePath = ImagePath;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {Id = id;}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }
}
