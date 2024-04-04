package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery;
import com.google.common.hash.Hashing;
import pt.unl.fct.di.apdc.firstwebapp.util.*;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;

import com.google.gson.Gson;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	//Settings that must be in the database
	public static final String SU = "SU";
	public static final String GBO = "GBO";
	public static final String GA = "GA";
	public static final String USER = "USER";
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	//public static Map<String, UserData> users = new HashMap<String, UserData>();

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	public LoginResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		if (user.getString("user_estado").equals("INATIVO"))
			return Response.status(Status.FORBIDDEN).entity("User is inactive.").build();

		if(!checkPassword(data, user)) {
			return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
		}

		String id = UUID.randomUUID().toString();
		long currentTime = System.currentTimeMillis();
		String fields = data.username+"."+ id +"."+user.getString("user_role")+"."+currentTime+"."+1000*60*60*2;

		String signature = SignatureUtils.calculateHMac(key, fields);

		if(signature == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while signing token. See logs.").build();
		}

		String value =  fields + "." + signature;
		NewCookie cookie = new NewCookie("session::apdc", value, "/", null, "comment", 1000*60*60*2, false, true);

		return Response.ok().cookie(cookie).build();
	}

	private static boolean checkPassword(LoginData data, Entity user)  {
		String hashedPass = Hashing.sha512().hashString(data.password, StandardCharsets.UTF_8).toString();

		return user != null && user.getString("user_pwd").equals(hashedPass);
	}

	public static boolean checkPermissions(String[] cookieData, String role) {

		int otherUserRole = convertRole(role);
		int userInSessionRole = convertRole(cookieData[2]);

		if(userInSessionRole < otherUserRole) {
			return false;
		} else if (userInSessionRole == otherUserRole && userInSessionRole != 3) {
			return false;
		}

		if(System.currentTimeMillis() > (Long.valueOf(cookieData[3]) + Long.valueOf(cookieData[4])*1000)) {
			return false;
		}

		return true;
	}

	public static int convertRole(String role) {
		int result = 0;

		switch(role) {
			case GBO:
				result = 1;
				break;
			case GA:
				result = 2;
				break;
			case SU:
				result = 3;
				break;
			default:
				break;
		}
		return result;
	}

	/*
	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		UserData user = users.get(username);

		if(user != null) {
			return Response.ok().entity(g.toJson(false)).build();
		}

		return Response.ok().entity(g.toJson(true)).build();
	}

	 */

	/*
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(UserData data) {
		LOG.fine("Attempting to create user with username: " + data.username);

		UserData user = users.get(data.username);

		if(user != null) {
			return Response.status(Status.FORBIDDEN).entity("User with username " + data.username + " already exists.").build();
		}

		users.put(data.username, data);

		return Response.ok().build();
	}

	 */

}
