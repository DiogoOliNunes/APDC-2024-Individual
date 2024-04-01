package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

    public String username;
    public String email;
    public String name;
    public String phone;
    public String password;
    public String confirmation;
    public String role;
    public String state;

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
        this.role = "USER";
        this.state = "INATIVO";
    }

    public boolean validRegistration() {
        return (!username.isBlank() && !email.isBlank() && email.contains("@") && !name.isBlank()
                && !phone.isBlank() && validPassword());
    }

    private boolean validPassword() {
        return (!password.isBlank() && password.length() > 5 && password.equals(confirmation));
    }

}