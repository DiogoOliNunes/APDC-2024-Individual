package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

import com.google.cloud.datastore.*;
import com.google.common.hash.Hashing;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Path("/changePassword")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePasswordResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

    public ChangePasswordResource() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON) //ele tem que confirmar entre a pass que deu no login e a que mete agora?
    public Response changePassword(@CookieParam("session::apdc") Cookie cookie, ChangePasswordData data) {
        LOG.fine("Attempt to change user password.");

        if (cookie == null || cookie.getValue() == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("User log in invalid.").build();
        }

        String value = cookie.getValue();
        String[] values = value.split("\\.");

        String signatureNew = SignatureUtils.calculateHMac(key, values[0]+"."+values[1]+"."+values[2]+"."+values[3]+"."+values[4]);
        String signatureOld = values[5];

        if(!signatureNew.equals(signatureOld)) {
            return Response.status(Response.Status.FORBIDDEN).entity("User log in invalid.").build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(values[0]);
        Entity user = datastore.get(userKey);

        if(user == null|| data.validPassword(user.getString("user_pwd")))
            return Response.status(Response.Status.FORBIDDEN).entity("New password is not valid. Try again.").build();

        Entity.Builder builder = Entity.newBuilder(userKey);
        user.getProperties().forEach(builder::set);

        String hashedPass = Hashing.sha512().hashString(data.newPassword, StandardCharsets.UTF_8).toString();

        builder.set("user_pwd", hashedPass);

        datastore.put(builder.build());

        return Response.ok().entity("Users' password successfully changed.").build();
    }
}
