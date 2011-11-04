package com.yammer.dropwizard.lifecycle

trait Managed {
  def start() {}
  def stop() {}
}
