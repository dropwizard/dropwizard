package ${package}

import com.yammer.dropwizard.ScalaService
import com.yammer.dropwizard.config.Environment

object ${name}Service 
  extends ScalaService[${name}Configuration]("${name}") {

  def initialize(conf: ${name}Configuration, env: Environment) {
    // TODO: implement service
  }

}

