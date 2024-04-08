package pt.unl.fct.di.apdc.firstwebapp.servlets;

import com.google.cloud.storage.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MediaResourceServlet extends HttpServlet {


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException { //download
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Path objectPath = Paths.get(req.getPathInfo());
        if(objectPath.getNameCount() != 2) {
            throw new IllegalArgumentException("The URL is not formed as expected. "
            + "Expecting /gcs/<bucket>/<object>");
        }
        String bucketName = objectPath.getName(0).toString();
        String srcFilename = objectPath.getName(1).toString();
        Blob blob = storage.get(BlobId.of(bucketName, srcFilename));
        blob.downloadTo(resp.getOutputStream());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { //upload
        Path objectPath = Paths.get(req.getPathInfo());
        if (objectPath.getNameCount() != 2) {
            throw new IllegalArgumentException("The URL is not formed as expected. "
                    + "Expecting /gcs/<bucket>/<object>");
        }
        String bucketName = objectPath.getName(0).toString();
        String srcFilename = objectPath.getName(1).toString();

        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, srcFilename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(req.getContentType()).build();
        Blob blob = storage.create(blobInfo, req.getInputStream());
    }
}
