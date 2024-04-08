package pt.unl.fct.di.apdc.firstwebapp.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class StartRoot extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StartRoot.class.getName());
    private static final String USERNAME = "root";
    private static final String ROLE = "SU";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "emaildaroot@rootemail.com";
    private Datastore datastore;
    private KeyFactory userKeyFactory;

    @Override
    public void init() throws ServletException {
        datastore = DatastoreOptions.getDefaultInstance().getService();
        userKeyFactory = datastore.newKeyFactory().setKind("User");

        startRoot();
    }

    private void startRoot() {
        Transaction txn = null;
        try {
            txn = datastore.newTransaction();
            Key userKey = userKeyFactory.newKey(USERNAME);
            Entity rootUser = txn.get(userKey);

            if (rootUser == null) {
                String hashedPassword = Hashing.sha512().hashString(PASSWORD, StandardCharsets.UTF_8).toString();
                Key rootKey = userKeyFactory.newKey(USERNAME);
                Entity.Builder rootUserBuilder = Entity.newBuilder(rootKey)
                        .set("user_role", ROLE)
                        .set("user_estado", "ATIVO")
                        .set("user_pwd", hashedPassword)
                        .set("user_perfil", "PUBLIC")
                        .set("user_phone", "123456789")
                        .set("user_email", EMAIL)
                        .set("user_name", USERNAME)
                        .set("user_creation_time", Timestamp.now());

                Entity root = rootUserBuilder.build();
                txn.add(root);
                txn.commit();
                LOG.info("Root created successfully.");
            } else {
                LOG.info("Root already exists.");
            }
        } catch (DatastoreException e) {
            LOG.warning("Error creating root user: " + e.getMessage());
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        } finally {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
