package pt.unl.fct.di.apdc.firstwebapp.util;

import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

public class RegisterData {

    public String username;
    public String email;
    public String name;
    public String phone;
    public String password;
    public String confirmation;

    public RegisterData() {
    }

    public RegisterData(String username, String email, String name, String phone, String password,
                        String confirmation) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.confirmation = confirmation;
    }

    public boolean validRegistration() {
        return (!username.isBlank() && !name.isBlank()
                && onlyDigits(phone) && validPassword(password) && password.equals(confirmation));
    }

    public static boolean validPassword(String password) {
        return (!password.isBlank() && password.length() > 5);
    }

    public static boolean validEmail(String newEmail) {
        String[] email = newEmail.split("@");
        return !newEmail.isBlank() && email.length == 2 && onlyLetters(email[0]) && onlyLetters(email[1]);
    }
    public static boolean onlyDigits(String newPhone) {
        if (newPhone.isBlank())
            return false;
        for (int i = 0; i < newPhone.length(); i++) {
            if (!Character.isDigit(newPhone.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean onlyLetters(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetter(name.charAt(i))) {
                    return false;
                }
            }
            return true;
    }
}