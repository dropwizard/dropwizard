package com.yammer.dropwizard.scala.params.tests

import org.junit.Test
import javax.ws.rs.WebApplicationException
import com.simple.simplespec.Spec
import com.yammer.dropwizard.scala.params.LongParam

class LongParamTest extends Spec {

  class `A valid long parameter` {
    private val param = LongParam("40")

    @Test def `has an int value` = {
      param.value.must(be(40L))
    }
  }

  class `An invalid long parameter` {
    @Test def `throws a WebApplicationException with an error message` = {
      evaluating {
        LongParam("poop")
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
