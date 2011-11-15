package com.yammer.dropwizard.examples

import javax.ws.rs.core.MediaType
import javax.ws.rs.{POST, Consumes, Path}
import com.codahale.logula.Logging

@Path("/upload")
@Consumes(Array(MediaType.WILDCARD))
class UploadResource extends Logging {
  @POST
  def upload(body: String) {
    log.info("New upload: %s", body)
  }
}
