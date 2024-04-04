package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeRoleData {

    public String username;
    public String newRole;

    public ChangeRoleData() {

    }

    public ChangeRoleData(String username, String newRole) {
        this.username = username;
        this.newRole = newRole;
    }

    public static boolean validRole(String role) {
        return role.equals("USER") || role.equals("GBO") || role.equals("GA") || role.equals("SU");
    }
}
