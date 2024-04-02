package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
    public String currentPassword;
    public String confirmation;
    public String newPassword;

    public ChangePasswordData() {

    }

    public ChangePasswordData(String currentPassword, String confirmation, String newPassword) {
        this.currentPassword = currentPassword;
        this.confirmation = confirmation;
        this.newPassword = newPassword;
    }

    public boolean validPassword(String userPassword) {
        return (userPassword.equals(currentPassword) && currentPassword.equals(confirmation) && validNewPassword());
    } //ele tem que fazer isto? userPassword.equals(currentPassword)

    private boolean validNewPassword() {
        return (!newPassword.isBlank() && newPassword.length() > 5);
    }}
