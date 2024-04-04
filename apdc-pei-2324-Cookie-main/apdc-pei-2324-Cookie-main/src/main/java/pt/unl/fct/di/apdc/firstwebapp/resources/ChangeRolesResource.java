package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static pt.unl.fct.di.apdc.firstwebapp.resources.LoginResource.checkPermissions;

@Path("/changeRoles")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeRolesResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
    public ChangeRolesResource(){
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRoles(@CookieParam("session::apdc") Cookie cookie, ChangeRoleData data) {
        LOG.fine("Attempt to change " + data.username + " roles.");

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);

        if(user == null|| !checkRoleChange(cookie, user.getString("user_role")))
            return Response.status(Response.Status.FORBIDDEN).entity("User not allowed to change roles.").build();

        Entity.Builder builder = Entity.newBuilder(userKey);
        user.getProperties().forEach(builder::set);

        builder.set("user_role", data.newRole);

        datastore.put(builder.build());

        return Response.ok().entity("Users' roles successfully changed.").build();
    }

    private boolean checkRoleChange(Cookie cookie, String role){
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

        if(values[2].equals("USER") || values[2].equals("GBO"))
            return false;

        return checkPermissions(values, role);
    }
}
