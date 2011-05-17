package com.yammer.dropwizard.etc

import javax.servlet._
import javax.servlet.http.HttpServletResponse

class CacheBustingFilter extends Filter {
  def init(filterConfig: FilterConfig) {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (response.isInstanceOf[HttpServletResponse]) {
      response.asInstanceOf[HttpServletResponse].setHeader("Cache-Control", "must-revalidate,no-cache,no-store")
    }
  }

  def destroy() {}
}
