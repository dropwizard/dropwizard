package com.yammer.dropwizard.examples

import javax.ws.rs.core.MediaType
import javax.ws.rs.{POST, Consumes, Path}
import com.yammer.dropwizard.Logging
import com.yammer.metrics.annotation.Timed

@Path("/upload")
@Consumes(Array(MediaType.WILDCARD))
class UploadResource extends Logging {
  @POST
  @Timed
  def upload(body: String) {
    logger.info("New upload: %s", body)
  }
}
