package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.*;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.cloud.datastore.*;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;

import com.google.gson.Gson;

@Path("/showProfile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowProfile {
    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Gson g = new Gson();

    public ShowProfile() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response show(@CookieParam("session::apdc") Cookie cookie) {
        LOG.fine("Getting user's profile...");

        if (cookie == null || cookie.getValue() == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("User's request invalid.").build();
        }

        String value = cookie.getValue();
        String[] values = value.split("\\.");

        String signatureNew = SignatureUtils.calculateHMac(key, values[0] + "." + values[1] + "." + values[2] + "."
                + values[3] + "." + values[4]);
        String signatureOld = values[5];

        if (!signatureNew.equals(signatureOld)) {
            return Response.status(Response.Status.FORBIDDEN).entity("User not allowed.").build();
        }

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(values[0]);
        Entity user = datastore.get(userKey);
        List<String> outputs = new ArrayList<>();
        outputs.add("username: " + values[0]);
        for (Map.Entry<String, Value<?>> entry : user.getProperties().entrySet()) {
            outputs.add(entry.getKey() + ": " + getValue(entry.getValue().toString()));
        }
        return Response.ok().entity(outputs).build();
    }

    public static String getValue(String string) {
        int start = string.lastIndexOf('=');
        int end = string.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            return string.substring(start + 1, end).trim();
        } else {
            return "Empty";
        }
    }

}
