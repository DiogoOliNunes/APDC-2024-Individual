package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.common.hash.Hashing;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.*;
import static pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData.validRole;

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
        Entity userInSession = datastore.get(userKey);

        Key userToBeChangedKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity userToBeChanged = datastore.get(userToBeChangedKey);

        if (values[2].equals("USER") && !data.username.equals(userInSession.getString("name")))
            return Response.status(Response.Status.FORBIDDEN).entity("User can't change other users attributes.")
                    .build();
        if (!canChange(values[2], data.attribute, userToBeChanged.getString("user_role")))
            return Response.status(Response.Status.FORBIDDEN).entity("User not allowed to change this user attribute.")
                    .build();

        Entity.Builder builder = Entity.newBuilder(userToBeChangedKey);
        userToBeChanged.getProperties().forEach(builder::set);

        if (!validNewAttribute(data.attribute, data.newAttribute))
            return Response.status(Response.Status.FORBIDDEN).entity("New attribute invalid.").build();
        if (data.attribute.equals("user_pwd")) {
            String hashedPass = Hashing.sha512().hashString(data.newAttribute, StandardCharsets.UTF_8).toString();
            builder.set("user_pwd", hashedPass);
        } else {
            builder.set(data.attribute, data.newAttribute);
        }

        datastore.put(builder.build());

        return Response.ok().entity("User attribute successfully changed.").build();
    }

    private boolean canChange(String userInSessionRole, String attribute, String userToBeChangedRole) {
        switch (userInSessionRole) {
            case ("USER"):
                return !attribute.equals("name") && !attribute.equals("user_email") && !attribute.equals("user_name")
                        && !attribute.equals("user_role") && !attribute.equals("user_estado");
            case ("GBO"):
                return userToBeChangedRole.equals("USER") && !attribute.equals("name");
            case ("GA"):
                return (userToBeChangedRole.equals("USER") || userToBeChangedRole.equals("GBO"))
                        && !attribute.equals("name");
            case ("SU"):
                return !userToBeChangedRole.equals("SU") && !attribute.equals("name");
        }
        return false;
    }

    public static boolean validNewAttribute(String attribute, String newAttribute) {
        if (newAttribute.isBlank())
            return false;
        if (attribute.equals("user_phone") || attribute.equals("user_nif"))
            return onlyDigits(newAttribute);
        if (attribute.equals("user_perfil"))
            return newAttribute.equals("PUBLIC") || newAttribute.equals("PRIVATE");
        if (attribute.equals("user_pwd"))
            return validPassword(newAttribute);
        if (attribute.equals("user_email"))
            return newAttribute.contains("@");
        if (attribute.equals("user_name") || attribute.equals("user_ocupation"))
            return onlyLetters(newAttribute);
        if (attribute.equals("user_role"))
            return validRole(newAttribute);
        if (attribute.equals("user_estado"))
            return newAttribute.equals("INATIVO") || newAttribute.equals("ATIVO");
        return false;
    }
}
