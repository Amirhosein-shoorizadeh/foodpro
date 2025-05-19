package dto;
import dao.UserDao;
import entity.*;

import com.sun.net.httpserver.HttpExchange;

public class UserManager {
    public static boolean handleSignup( SignUpManager dto ) {
        User user;
        switch (dto.userType.toLowerCase()) {
            case "customer":
                user = new Customer(dto.username, dto.password, dto.name, dto.family, dto.phoneNumber, dto.email, dto.address);
                break;
            case "seller":
                user = new Seller(dto.username, dto.password, dto.name, dto.family, dto.phoneNumber, dto.email, dto.address);
                break;
            default:
                return false; // نوع نامعتبر
        }

        return UserDao.save(user);
    }
}
