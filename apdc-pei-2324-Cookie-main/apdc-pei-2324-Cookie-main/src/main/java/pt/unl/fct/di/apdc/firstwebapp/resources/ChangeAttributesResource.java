package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/changeAttributes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeAttributesResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
    public ChangeAttributesResource() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAttributes(@CookieParam("session::apdc") Cookie cookie, ChangeAttributesData data) {
        LOG.fine("Attempt to change attributes.");

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



        return Response.ok().entity("User attribute successfully changed.").build();
    }

    private boolean canChange(String role, String attribute) {
        switch (role) {
            case ("USER"):
                return !attribute.equals("name") && !attribute.equals("user_email") && !attribute.equals("user_name")
                        && !attribute.equals("user_role") && !attribute.equals("user_estado");
            case ("GBO"):
                return false;
        }
        return false;
    }
}
