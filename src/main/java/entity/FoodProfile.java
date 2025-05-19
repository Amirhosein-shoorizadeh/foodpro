package entity;

import jakarta.persistence.*;

@Entity
public class FoodProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column()
    private String ImagePath;

    @OneToOne
    @JoinColumn(name = "Food_id", referencedColumnName = "id")
    private Food food;

}
