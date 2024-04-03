package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
    public String currentPassword;
    public String confirmation;
    public String newPassword;

    public ChangePasswordData() {

    }

    public ChangePasswordData(String currentPassword, String newPassword, String confirmation) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmation = confirmation;
    }

    public boolean validPassword(String userPassword) {
        return (userPassword.equals(currentPassword) && currentPassword.equals(confirmation) && validNewPassword());
    }

    private boolean validNewPassword() {
        return (!newPassword.isBlank() && newPassword.length() > 5);
    }}
