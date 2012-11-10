package com.yammer.dropwizard.scala.inject.tests

import org.junit.Test
import javax.ws.rs.QueryParam
import com.sun.jersey.api.model.Parameter
import com.sun.jersey.core.spi.component.{ComponentContext, ComponentScope}
import com.sun.jersey.core.util.MultivaluedMapImpl
import com.sun.jersey.api.core.{HttpContext, ExtendedUriInfo}
import com.simple.simplespec.Spec
import com.yammer.dropwizard.scala.inject.{ScalaCollectionQueryParamInjectable, ScalaCollectionsQueryParamInjectableProvider}

class ScalaCollectionsQueryParamInjectableProviderTest extends Spec {

  class `A Scala collections query param injectable provider` {
    val httpContext = mock[HttpContext]
    val uriInfo = mock[ExtendedUriInfo]
    val params = new MultivaluedMapImpl()
    params.add("name", "one")
    params.add("name", "two")
    params.add("name", "three")

    httpContext.getUriInfo returns uriInfo
    uriInfo.getQueryParameters(any[Boolean]) returns params

    val context = mock[ComponentContext]
    val queryParam = mock[QueryParam]

    val provider = new ScalaCollectionsQueryParamInjectableProvider


    @Test def `has a per-request scope` = {
      provider.getScope.must(be(ComponentScope.PerRequest))
    }

    @Test def `returns an injectable for Seq instances` = {
      val param = new Parameter(Array(), null, null, "name", null, classOf[Seq[String]], false, "default")
      val injectable = provider.getInjectable(context, queryParam, param).asInstanceOf[ScalaCollectionQueryParamInjectable]

      injectable.getValue(httpContext).must(be(Seq("one", "two", "three")))
    }

    @Test def `returns an injectable for List instances` = {
      val param = new Parameter(Array(), null, null, "name", null, classOf[List[String]], false, "default")
      val injectable = provider.getInjectable(context, queryParam, param).asInstanceOf[ScalaCollectionQueryParamInjectable]

      injectable.getValue(httpContext).must(be(List("one", "two", "three")))
    }

    @Test def `returns an injectable for Vector instances` = {
      val param = new Parameter(Array(), null, null, "name", null, classOf[Vector[String]], false, "default")
      val injectable = provider.getInjectable(context, queryParam, param).asInstanceOf[ScalaCollectionQueryParamInjectable]

      injectable.getValue(httpContext).must(be(Vector("one", "two", "three")))
    }

    @Test def `returns an injectable for IndexedSeq instances` = {
      val param = new Parameter(Array(), null, null, "name", null, classOf[IndexedSeq[String]], false, "default")
      val injectable = provider.getInjectable(context, queryParam, param).asInstanceOf[ScalaCollectionQueryParamInjectable]

      injectable.getValue(httpContext).must(be(IndexedSeq("one", "two", "three")))
    }

    @Test def `return an injectable for Set instances` = {
      val param = new Parameter(Array(), null, null, "name", null, classOf[Set[String]], false, "default")
      val injectable = provider.getInjectable(context, queryParam, param).asInstanceOf[ScalaCollectionQueryParamInjectable]

      injectable.getValue(httpContext).must(be(Set("one", "two", "three")))
    }

    @Test def `returns an injectable for Option instances` = {
      val param = new Parameter(Array(), null, null, "name", null, classOf[Option[String]], false, "default")
      val injectable = provider.getInjectable(context, queryParam, param).asInstanceOf[ScalaCollectionQueryParamInjectable]

      injectable.getValue(httpContext).must(be(Some("one")))
    }
  }

}
