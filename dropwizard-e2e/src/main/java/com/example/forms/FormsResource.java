package com.example.forms;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Path("/")
public class FormsResource {
    @POST
    @Path("uploadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public StreamingOutput uploadFile(@FormDataParam("file") InputStream file,
                                      @FormDataParam("file") FormDataContentDisposition fileDisposition) {

        // Silly example that echoes back the file name and the contents
        return output -> {
            output.write(String.format("%s:\n", fileDisposition.getFileName()).getBytes(UTF_8));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = file.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        };
    }
}
