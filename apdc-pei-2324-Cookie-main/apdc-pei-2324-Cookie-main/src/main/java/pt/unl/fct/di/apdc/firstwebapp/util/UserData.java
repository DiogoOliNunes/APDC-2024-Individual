package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserData {
	
	public String username;
	public String password;
	public String role;
	public String state;
	
	public UserData() {
		
	}
	
	public UserData(String username, String password, String role, String state) {
		this.username = username;
		this.password = password;
	}

}
