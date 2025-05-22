package service;

import dao.UserDao;
import entity.*;
import exception.InvalidUserDataException;
import org.mindrot.jbcrypt.BCrypt;


public class UserManager {

    public static void handleSignup(SignUpManager dto) {
        if (dto == null || dto.userType == null || dto.password == null || dto.name == null || dto.phoneNumber == null) {
            throw new InvalidUserDataException("Missing required signup fields.");
        }
        String hashedPassword = BCrypt.hashpw(dto.password, BCrypt.gensalt());
        User user;
        switch (dto.userType.toLowerCase()) {
            case "customer":
                user = new Customer(hashedPassword, dto.name, dto.phoneNumber, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address);
                if (dto.address == null) {
                    throw new InvalidUserDataException("Missing required address field.");
                }
                break;
            case "seller":
                user = new Seller(hashedPassword, dto.name, dto.phoneNumber, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address);
                if (dto.address == null) {
                    throw new InvalidUserDataException("Missing required address field.");
                }
                break;
            case "courier":
                user = new Courier(hashedPassword, dto.name, dto.phoneNumber, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address);
                break;
            default:
                throw new InvalidUserDataException("Invalid user type: " + dto.userType);
        }

         boolean saved = UserDao.save(user);
        if (!saved) {
            throw new RuntimeException("Unexpected error while saving user.");
        }
    }
}
