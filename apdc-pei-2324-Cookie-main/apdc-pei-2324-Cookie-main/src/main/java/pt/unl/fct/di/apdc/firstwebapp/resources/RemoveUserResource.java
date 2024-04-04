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

import static pt.unl.fct.di.apdc.firstwebapp.resources.LoginResource.convertRole;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveUserResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
    public RemoveUserResource(){}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(@CookieParam("session::apdc") Cookie cookie, ChangeUserData data) {
        LOG.fine("Attempt to remove " + data.username + " from database.");

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);

        if(user == null || !checkPermissions(cookie, user.getString("user_role")))
            return Response.status(Response.Status.FORBIDDEN).entity("User not allowed to remove this account.").build();

        datastore.delete(userKey);

        return Response.ok().entity(data.username + " successfully removed from database.").build();
    }

    private boolean checkPermissions(Cookie cookie, String role){
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

        int userToBeRemovedRole = convertRole(role);
        int userInSessionRole = convertRole(values[2]);

        if (userInSessionRole == 0 && userToBeRemovedRole != 0)
            return false;
        if (userInSessionRole == 1)
            return false;
        if (userInSessionRole == 2 && userToBeRemovedRole >= 2)
            return false;

        if(System.currentTimeMillis() > (Long.valueOf(values[3]) + Long.valueOf(values[4])*1000)) {
            return false;
        }
        return true;
    }
}
