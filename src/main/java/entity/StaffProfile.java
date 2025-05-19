package entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

@Entity
public class StaffProfile extends Profile {
 @Embedded
    private Bankinfo bankinfo;

    public StaffProfile() {}
    public StaffProfile(User user,String ImagePath,Bankinfo bankinfo) {
        super(user,ImagePath);
        this.bankinfo = bankinfo;
    }
    public Bankinfo getBankinfo() {
        return bankinfo;
    }
    public void setBankinfo(Bankinfo bankinfo) {
        this.bankinfo = bankinfo;
    }
}
