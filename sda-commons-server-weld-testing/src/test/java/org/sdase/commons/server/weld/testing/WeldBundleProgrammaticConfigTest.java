package org.sdase.commons.server.weld.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sdase.commons.server.testing.DropwizardConfigurationHelper.configFrom;

import io.dropwizard.cli.Cli;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.JarLocation;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sdase.commons.server.weld.testing.test.AppConfiguration;
import org.sdase.commons.server.weld.testing.test.WeldExampleApplication;

class WeldBundleProgrammaticConfigTest {

  private static final String LOCALHOST = "http://localhost:";

  @RegisterExtension
  static final WeldAppExtension<AppConfiguration> APP =
      new WeldAppExtension<>(
          WeldExampleApplication.class,
          configFrom(AppConfiguration::new).withPorts(4567, 0).withRootPath("/api/*").build());

  @Test
  void testResource() {
    String response =
        APP.client().target(LOCALHOST + 4567 + "/api/dummy").request().get(String.class);
    assertThat(response).isNotNull().isEqualTo("hello foo");
  }

  @Test
  void testServlet() {
    Response response =
        APP.client().target(LOCALHOST + APP.getLocalPort() + "/foo").request().get();
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  void testCommand() {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    WeldExampleApplication app = APP.getApplication();

    final JarLocation location = mock(JarLocation.class);
    final Bootstrap<AppConfiguration> bootstrap = new Bootstrap<>(APP.getApplication());
    when(location.toString()).thenReturn("dw-thing.jar");
    when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
    bootstrap.addCommand(app.getTestCommand());

    Cli cli = new Cli(location, bootstrap, stdOut, stdErr);

    assertThat(cli.run("testDW")).isEmpty();

    assertThat(app.getTestCommand().getResult()).isEqualTo("foo");
  }

  @Test
  void testTask() {
    WeldExampleApplication app = APP.getApplication();

    try (Response response =
        APP.client()
            .target(LOCALHOST + APP.getAdminPort() + "/tasks/runTestTask")
            .request()
            .post(Entity.entity("", MediaType.TEXT_PLAIN))) {
      assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
      assertThat(app.getTestJobResult()).isEqualTo("foo");
    }
  }
}
