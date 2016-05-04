package com.example.helloworld;

import io.dropwizard.codegen.Something;
import no.bouvet.jsonclient.JsonClient;

public class MyService {
  private final JsonClient jsonClient = new JsonClient();

  public Something addSomething(Something dataToPost) {
    return jsonClient.http().post("http://localhost:8080/something/add", dataToPost).object(Something.class);
  }

  public Something getSomething() {
    return jsonClient.http().get("http://localhost:8080/something/get").object(Something.class);
  }
}
