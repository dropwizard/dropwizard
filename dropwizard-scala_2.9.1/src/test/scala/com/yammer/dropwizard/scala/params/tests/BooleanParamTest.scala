package com.yammer.dropwizard.scala.params.tests

import org.junit.Test
import javax.ws.rs.WebApplicationException
import com.simple.simplespec.Spec
import com.yammer.dropwizard.scala.params.BooleanParam

class BooleanParamTest extends Spec {

  class `A valid boolean parameter` {
    val param = BooleanParam("true")

    @Test def `has a boolean value` = {
      param.value.must(be(true))
    }
  }

  class `An invalid boolean parameter` {
    @Test def `throws a WebApplicationException with an error message` = {
      evaluating {
        BooleanParam("poop")
      }.must(throwAnExceptionLike {
        case e: WebApplicationException => {
          val response = e.getResponse
          response.getStatus.must(be(400))
          response.getEntity.must(be("Invalid parameter: poop (Must be \"true\" or \"false\".)"))
        }
      })
    }
  }

}
