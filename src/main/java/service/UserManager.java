package service;

import dao.UserDao;
import dto.UserProfileDto;
import entity.*;
import exception.ConflictExceptin;
import exception.InvalidUserDataException;
import exception.NotFoundException;
import org.mindrot.jbcrypt.BCrypt;


public class UserManager {

    public static long handleSignup(SignUpManager dto) {
        if (dto == null || dto.userType == null || dto.password == null || dto.full_name == null || dto.phone == null) {
            throw new InvalidUserDataException("Missing required signup fields.");
        }
        String hashedPassword = BCrypt.hashpw(dto.password, BCrypt.gensalt());
        User user;
        switch (dto.userType.toLowerCase()) {
            case "customer":
                user = new Customer(hashedPassword, dto.full_name, dto.phone, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address);
                if (dto.address == null) {
                    throw new InvalidUserDataException("Missing required address field.");
                }
                break;
            case "seller":
                user = new Seller(hashedPassword, dto.full_name, dto.phone, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address);
                if (dto.address == null) {
                    throw new InvalidUserDataException("Missing required address field.");
                }
                break;
            case "courier":
                user = new Courier(hashedPassword, dto.full_name, dto.phone, dto.email, dto.profileImageBase64, dto.bankinfo, dto.address);
                break;
            default:
                throw new InvalidUserDataException("Invalid user type: " + dto.userType);
        }

        boolean saved = UserDao.save(user);
        if (!saved) {
            throw new RuntimeException("Unexpected error while saving user.");
        }
        return user.getId();
    }

    public static UserProfileDto GetCurrentProfile( String phone){
        User user = UserDao.getByPhone(phone);
        if(user == null){
            throw new InvalidUserDataException("User not found.");
        } else {

            return new UserProfileDto(user.getId(), user.getFull_name(), user.getPhone(), user.getEmail(),user.getClass().getSimpleName(),user.getAddress(), user.getProfileImageBase64(), user.getBankinfo());
        }
    }

    public static void UpdateUserProfile(UserProfileDto profileDto, String phone) {
        User user = UserDao.getByPhone(phone);
        if(user == null){
            throw new NotFoundException("User not found.");
        }
        if(!user.getPhone().equals(phone)){
           if(!UserDao.isPhoneExists(profileDto.phone)){
               user.setFull_name(profileDto.full_name);
               user.setPhone(profileDto.phone);
               user.setEmail(profileDto.email);
               user.setProfileImageBase64(profileDto.profileImageBase64);
               user.setAddress(profileDto.address);
               user.setBankinfo(profileDto.bank_info);
           }else{
               throw new ConflictExceptin("This phone number is already registered");
           }
        }else {
            user.setFull_name(profileDto.full_name);
            user.setEmail(profileDto.email);
            user.setProfileImageBase64(profileDto.profileImageBase64);
            user.setAddress(profileDto.address);
            user.setBankinfo(profileDto.bank_info);
        }
        UserDao.update(user);
    }
}