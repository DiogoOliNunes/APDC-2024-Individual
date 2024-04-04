package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeUserData;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static pt.unl.fct.di.apdc.firstwebapp.resources.LoginResource.checkPermissions;

@Path("/changeState")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeStateResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
    public ChangeStateResource(){}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeState(@CookieParam("session::apdc") Cookie cookie, ChangeUserData data) {
        LOG.fine("Attempt to change " + data.username + " state.");

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        String userRole = user.getString("user_role");

        if(user == null || !checkChangeState(cookie, userRole))
            return Response.status(Response.Status.FORBIDDEN).entity("User not allowed to change states.").build();

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

    private boolean checkChangeState(Cookie cookie, String role){
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

        if(values[2].equals("USER"))
            return false;

        return checkPermissions(values, role);
    }
}
