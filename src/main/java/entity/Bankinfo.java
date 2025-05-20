package entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Bankinfo {
    private String AccountNumber;
    private String BankName;
}
