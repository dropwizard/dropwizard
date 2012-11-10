package com.yammer.dropwizard.scala.params.tests

import org.junit.Test
import javax.ws.rs.WebApplicationException
import com.simple.simplespec.Spec
import com.yammer.dropwizard.scala.params.IntParam

class IntParamTest extends Spec {

  class `A valid int parameter` {
    val param = IntParam("40")

    @Test def `has an int value` = {
      param.value.must(be(40))
    }
  }

  class `An invalid int parameter` {
    @Test def `throws a WebApplicationException with an error message` = {
      evaluating {
        IntParam("poop")
      }.must(throwAnExceptionLike {
        case e: WebApplicationException => {
          val response = e.getResponse
          response.getStatus.must(be(400))
          response.getEntity.must(be("Invalid parameter: poop (Must be an integer value.)"))
        }
      })
    }
  }

}
