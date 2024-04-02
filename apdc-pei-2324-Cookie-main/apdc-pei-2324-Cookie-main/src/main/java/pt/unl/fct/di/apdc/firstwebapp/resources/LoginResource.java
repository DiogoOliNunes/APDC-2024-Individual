package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.common.hash.Hashing;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;

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

	public static Map<String, UserData> users = new HashMap<String, UserData>();

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

		//falta definir que quando o estado esta em INATIVO nao pode fazer login

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

	@POST
	@Path("/changeRoles")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeRoles(@CookieParam("session::apdc") Cookie cookie, ChangeRoleData data) {
		LOG.fine("Attempt to change " + data.username + " roles.");

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		if(user == null|| !checkPermissions(cookie, user.getString("user_role")))
			return Response.status(Status.FORBIDDEN).entity("User not allowed to change roles.").build();

		Entity.Builder builder = Entity.newBuilder(userKey);
		user.getProperties().forEach(builder::set);

		builder.set("user_role", data.newRole);

		datastore.put(builder.build());

		return Response.ok().entity("Users' roles successfully changed.").build();
	}

	@POST
	@Path("/changeState")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeState(@CookieParam("session::apdc") Cookie cookie, ChangeStateData data) {
		LOG.fine("Attempt to change " + data.username + " state.");

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		if(user == null || !checkPermissions(cookie, user.getString("user_role")))
			return Response.status(Status.FORBIDDEN).entity("User not allowed to change states.").build();

		String userState = user.getString("user_estado");

		Entity.Builder builder = Entity.newBuilder(userKey);
		user.getProperties().forEach(builder::set);

		if (userState.equals("INATIVO"))
			builder.set("user_estado", "ATIVO");
		else
			builder.set("user_estado", "INATIVO");

		datastore.put(builder.build());

		return Response.ok().entity("User's state successfully changed.").build();
	}

	private static boolean checkPassword(LoginData data, Entity user)  {
		String hashedPass = Hashing.sha512().hashString(data.password, StandardCharsets.UTF_8).toString();

		return user != null && user.getString("user_pwd").equals(hashedPass);
	}

	public static boolean checkPermissions(Cookie cookie, String role) {
		if (cookie == null || cookie.getValue() == null) {
			return false;
		}

		String value = cookie.getValue();
		String[] values = value.split("\\.");

		String signatureNew = SignatureUtils.calculateHMac(key, values[0]+"."+values[1]+"."+values[2]+"."+values[3]+"."+values[4]);
		String signatureOld = values[5];

		if(!signatureNew.equals(signatureOld)) {
			return false;
		}

		int neededRole = convertRole(role);
		int userInSessionRole = convertRole(values[2]);

		if(userInSessionRole < neededRole) {
			return false;
		}

		if(System.currentTimeMillis() > (Long.valueOf(values[3]) + Long.valueOf(values[4])*1000)) {

			return false;
		}


		return true;
	}

	private static int convertRole(String role) {
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

	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		UserData user = users.get(username);

		if(user != null) {
			return Response.ok().entity(g.toJson(false)).build();
		}

		return Response.ok().entity(g.toJson(true)).build();
	}

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

}
