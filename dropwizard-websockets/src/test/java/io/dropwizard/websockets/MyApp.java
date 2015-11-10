package io.dropwizard.websockets;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.jetty.websocket.jsr356.server.BasicServerEndpointConfig;

public class MyApp extends Application<Configuration> {

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new WebsocketBundle(
                Arrays.asList(AnnotatedEchoServer.class),
                Arrays.asList(new BasicServerEndpointConfig(EchoServer.class, "/extends-ws"))));
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws InvalidKeySpecException, NoSuchAlgorithmException, ServletException, DeploymentException {
        environment.jersey().register(new MyResource());
        environment.healthChecks().register("alive", new HealthCheck() {
            @Override
            protected HealthCheck.Result check() throws Exception {
                return HealthCheck.Result.healthy();
            }
        });
    }

    @Metered
    @Timed
    @ServerEndpoint("/annotated-ws")
    public static class AnnotatedEchoServer {
        @OnOpen
        public void myOnOpen(final Session session) throws IOException {
            session.getAsyncRemote().sendText("welcome");
        }

        @OnMessage
        public void myOnMsg(final Session session, String message) {
            session.getAsyncRemote().sendText(message.toUpperCase());
        }

        @OnClose
        public void myOnClose(final Session session, CloseReason cr) {
        }
    }

    @Metered
    @Timed
    public static class EchoServer extends Endpoint implements MessageHandler.Whole<String> {
        private Session session;

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            session.getAsyncRemote().sendText("welcome");
            this.session = session;
        }

        @Override
        public void onMessage(String message) {
            session.getAsyncRemote().sendText(message.toUpperCase());
        }
    }

    @Path("/api")
    @Produces(value = MediaType.APPLICATION_JSON)
    public static class MyResource {

        @Metered
        @GET
        public String get(@QueryParam(value = "name") String name) throws Exception {
            return "hello " + name;
        }
    }
}
