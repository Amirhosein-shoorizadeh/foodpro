package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Menos")
public class Meno {
    @Id
    @GeneratedValue
    private int Id;
}
