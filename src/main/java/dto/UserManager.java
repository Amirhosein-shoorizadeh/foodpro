package dto;
import dao.UserDao;
import entity.*;

import com.sun.net.httpserver.HttpExchange;

public class UserManager {
    public static boolean handleSignup( SignUpManager dto ) {
        User user;
        switch (dto.userType.toLowerCase()) {
            case "customer":
                user = new Customer( dto.password, dto.name, dto.phoneNumber, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address );
                break;
            case "seller":
                user = new Seller(dto.password, dto.name , dto.phoneNumber, dto.email, dto.profileImageBase64,dto.bankinfo,dto.address);
                break;
             case "courier":
                 user = new Courier(dto.password,dto.name, dto.phoneNumber, dto.email,dto.profileImageBase64,dto.bankinfo, dto.address);
                 break;
            default:
                return false; // نوع نامعتبر
        }

        return UserDao.save(user);
    }
}
