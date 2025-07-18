package entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Bankinfo {
    private String bank_name;
    private String account_number;
    private double walletBalance;

    public Bankinfo(){}
    public Bankinfo(String bank_name, String account_number) {
        this.bank_name = bank_name;
        this.account_number = account_number;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public void increaseWalletBalance(double amount){
        walletBalance += amount;
    }
    public void decreaseWalletBalance(double amount){
        walletBalance -= amount;
    }
}
