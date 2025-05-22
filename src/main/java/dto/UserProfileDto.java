package dto;

import entity.Bankinfo;

public class UserProfileDto {
    public long id;
    public String full_name;
    public String phone;
    public String email;
    public String role;
    public String address;
    public String profileImageBase64;
    public Bankinfo bank_info;

    public UserProfileDto(long id, String full_name, String phone, String email, String role, String address, String profileImageBase64, Bankinfo bank_info) {
        this.id = id;
        this.full_name = full_name;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.address = address;
        this.profileImageBase64 = profileImageBase64;
    }
}
