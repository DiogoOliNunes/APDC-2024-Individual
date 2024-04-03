package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.Builder;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Path("/listUsers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUsersResource {
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public ListUsersResource() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listUsers(@CookieParam("session::apdc") Cookie cookie) {
        LOG.fine("Attempt to list users from database.");

        if (cookie == null || cookie.getValue() == null) {
            return Response.status(Response.Status.FORBIDDEN).entity("User log in invalid.").build();
        }

        String value = cookie.getValue();
        String[] values = value.split("\\.");

        String signatureNew = SignatureUtils.calculateHMac(key, values[0]+"."+values[1]+"."+values[2]+"."+values[3]+"."+values[4]);
        String signatureOld = values[5];

        if(!signatureNew.equals(signatureOld)) {
            return Response.status(Response.Status.FORBIDDEN).entity("User not allowed to list users.").build();
        }

        String queryCommand = getQuery(values[2], values[0]);
        Query<Entity> query = Query.newGqlQueryBuilder(Query.ResultType.ENTITY, queryCommand).build();
        QueryResults<Entity> queryResults = datastore.run(query);

        List<List<String>> outputs = new ArrayList<>();
        if (values[2].equals("USER")) {
            queryResults.forEachRemaining(user -> {outputs.add(List.of(user.getString("name"),
                            user.getString("user_email"),
                            user.getString("user_name"))
                    );
                    }
            );
        }
        else {
            queryResults.forEachRemaining(user -> {outputs.add(List.of(user.getProperties().toString())
                    );
            }
            );
        }
        return Response.ok().entity(queryResults).build();
    }

    private String getQuery(String role, String username) {
        String query = null;
        switch (role) {
            case("USER"):
                /*
                query = Query.newEntityQueryBuilder().setKind("listUsers").setFilter(
                                CompositeFilter.and(
                                        PropertyFilter.hasAncestor(
                                                datastore.newKeyFactory().setKind("User").newKey(username)),
                                        PropertyFilter.eq("user_role", "USER"),
                                        PropertyFilter.eq("user_estado", "ATIVO")
                                        )
                        ).build();
                 */
                query = "SELECT * FROM listUsers WHERE user_role = 'USER' AND user_estado = 'ATIVO'";
                break;
            case ("GBO"):
                /*
                query = Query.newEntityQueryBuilder().setKind("listUsers").setFilter(
                                CompositeFilter.and(
                                        PropertyFilter.hasAncestor(
                                                datastore.newKeyFactory().setKind("User").newKey(username)),
                                        PropertyFilter.eq("user_role", "USER")
                                    )
                        ).build();
                */
                query = "SELECT * FROM listUsers WHERE user_role = 'USER'";
                break;
            case ("GA"):
                /*
                String[] rolesPermitted = new String[]{"USER", "GA", "GBO"};
                query = Query.newEntityQueryBuilder().setKind("listUsers").setFilter(
                        CompositeFilter.and(
                                PropertyFilter.hasAncestor(
                                        datastore.newKeyFactory().setKind("User").newKey(username)),
                                PropertyFilter.eq("user_role", Arrays.toString(rolesPermitted))
                        )
                ).build();
                */
                query = "SELECT * FROM listUsers WHERE user_role IN ('USER', 'GBO', 'GA')";
                break;
            case ("SU"):
                /*
                query = Query.newEntityQueryBuilder().setKind("listUsers").setFilter(
                        CompositeFilter.and(
                                PropertyFilter.hasAncestor(
                                        datastore.newKeyFactory().setKind("User").newKey(username))
                        )
                ).build();
                */
                query = "SELECT * FROM listUsers";
                break;
        }
        return query;
    }
}
