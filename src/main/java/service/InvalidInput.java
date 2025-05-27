package service;

import dto.*;
import exception.*;

public class InvalidInput {
    public static boolean checkInput_Register(SignUpManager temp) throws InvalidUserDataException {
        if (temp.full_name == null || temp.full_name.equals("")) {
            throw new InvalidUserDataException("Full name is required");
        } else if (temp.phone == null || temp.phone.equals("")) {
            throw new InvalidUserDataException("Phone number is required");
        } else if (temp.email == null || temp.email.equals("")) {
            throw new InvalidUserDataException("Email is required");
        } else if (temp.password == null || temp.password.equals("")) {
            throw new InvalidUserDataException("Password is required");
        } else if (temp.role == null || temp.role.equals("")) {
            throw new InvalidUserDataException("Role is required");
        } else if ((temp.role.equals("buyer") || temp.role.equals("seller")) && (temp.address == null || temp.address.equals(""))) {
            throw new InvalidUserDataException("Address is required");
        } else if (!temp.role.equals("buyer") && temp.bank_info == null) {
            throw new InvalidUserDataException("Bank info is required");
        }
        if (temp.phone == null || temp.phone.length() != 11 || !temp.phone.matches("\\d+")) {
            throw new InvalidUserDataException("Phone number is not valid");
        }
        return true;
    }

    public static boolean checkInput_EditProfile(UserProfileDto temp) throws InvalidUserDataException {
        if (temp.full_name == null || temp.full_name.equals("")) {
            throw new InvalidUserDataException("Full name is required");
        } else if (temp.phone == null || temp.phone.equals("")) {
            throw new InvalidUserDataException("Phone number is required");
        } else if (temp.email == null || temp.email.equals("")) {
            throw new InvalidUserDataException("Email is required");
        } else if (temp.address == null || temp.address.equals("")) {
            throw new InvalidUserDataException("Address is required");
        } else if (temp.bank_info == null) {
            throw new InvalidUserDataException("Bank info is required");
        } else if (temp.role == null || temp.role.equals("")) {
            throw new InvalidUserDataException("Role is required");
        }
        if (temp.phone == null || temp.phone.length() != 11 || !temp.phone.matches("\\d+")) {
            throw new InvalidUserDataException("Phone number is not valid");
        }
        return true;
    }

    public  static boolean CheckInput_AddFood(FoodDto foodDto) throws InvalidUserDataException {
        if(foodDto.name == null || foodDto.name.equals("")) {
            throw new InvalidUserDataException("Name is required");
        }else if(foodDto.price == 0) {
            throw new InvalidUserDataException("Price is required");
        }else if(foodDto.description == null || foodDto.description.equals("")) {
            throw new InvalidUserDataException("Description is required");
        }else if(foodDto.price < 0) {
            throw new InvalidUserDataException("Price is required");
        }else if(foodDto.supply < 0) {
            throw new InvalidUserDataException("Supply is required");
        }else if(foodDto.keywords == null || foodDto.keywords.equals("") || foodDto.keywords.isEmpty()) {
            throw new InvalidUserDataException("Keywords is required");
        }
        return true;
    }
}
