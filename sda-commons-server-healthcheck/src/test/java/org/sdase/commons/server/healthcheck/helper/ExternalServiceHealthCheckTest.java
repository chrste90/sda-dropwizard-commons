package org.sdase.commons.server.healthcheck.helper;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.codahale.metrics.health.HealthCheck.Result;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExternalServiceHealthCheckTest {

  private HttpURLConnection getConnectionMock;
  private ExternalServiceHealthCheck getHealthCheck;

  private HttpURLConnection headConnectionMock;
  private ExternalServiceHealthCheck headHealthCheck;

  @BeforeEach
  void setup() throws ProtocolException {
    getConnectionMock = Mockito.mock(HttpURLConnection.class);
    doThrow(new IllegalArgumentException()).when(getConnectionMock).setRequestMethod(anyString());
    doNothing().when(getConnectionMock).setRequestMethod("GET");
    getHealthCheck =
        new ExternalServiceHealthCheck("http://www.testurl.com", 1000, url -> getConnectionMock);

    headConnectionMock = Mockito.mock(HttpURLConnection.class);
    doThrow(new IllegalArgumentException()).when(headConnectionMock).setRequestMethod(anyString());
    doNothing().when(headConnectionMock).setRequestMethod("HEAD");
    headHealthCheck =
        new ExternalServiceHealthCheck(
            "HEAD", "http://www.testurl.com", 1000, url -> headConnectionMock);
  }

  @Test
  void testGetExternalServiceURLOk() throws Exception {
    Mockito.when(getConnectionMock.getResponseCode()).thenReturn(SC_OK);
    Result result = getHealthCheck.check();
    assertTrue(result.isHealthy());
  }

  @Test
  void testGetExternalServiceURLNoContent() throws Exception {
    Mockito.when(getConnectionMock.getResponseCode()).thenReturn(SC_NO_CONTENT);
    Result result = getHealthCheck.check();
    assertTrue(result.isHealthy());
  }

  @Test
  void testGetExternalServiceURLClientError() throws Exception {
    Mockito.when(getConnectionMock.getResponseCode()).thenReturn(SC_NOT_FOUND);
    Result result = getHealthCheck.check();
    assertFalse(result.isHealthy());
  }

  @Test
  void testGetExternalServiceURLServerError() throws Exception {
    Mockito.when(getConnectionMock.getResponseCode()).thenReturn(SC_INTERNAL_SERVER_ERROR);
    Result result = getHealthCheck.check();
    assertFalse(result.isHealthy());
  }

  @Test
  void testGetExternalServiceURLException() throws Exception {
    Mockito.when(getConnectionMock.getResponseCode()).thenThrow(new IOException());
    Result result = getHealthCheck.check();
    assertFalse(result.isHealthy());
  }

  @Test
  void testHeadExternalServiceURLOk() throws Exception {
    Mockito.when(headConnectionMock.getResponseCode()).thenReturn(SC_OK);
    Result result = headHealthCheck.check();
    assertTrue(result.isHealthy());
  }

  @Test
  void testHeadExternalServiceURLNoContent() throws Exception {
    Mockito.when(headConnectionMock.getResponseCode()).thenReturn(SC_NO_CONTENT);
    Result result = headHealthCheck.check();
    assertTrue(result.isHealthy());
  }

  @Test
  void testHeadExternalServiceURLClientError() throws Exception {
    Mockito.when(headConnectionMock.getResponseCode()).thenReturn(SC_NOT_FOUND);
    Result result = headHealthCheck.check();
    assertFalse(result.isHealthy());
    assertThat(result.getMessage()).contains("404");
    assertThat(result.getMessage()).contains("http://www.testurl.com");
  }

  @Test
  void testHeadExternalServiceURLServerError() throws Exception {
    Mockito.when(headConnectionMock.getResponseCode()).thenReturn(SC_INTERNAL_SERVER_ERROR);
    Result result = headHealthCheck.check();
    assertFalse(result.isHealthy());
    assertThat(result.getMessage()).contains("500");
    assertThat(result.getMessage()).contains("http://www.testurl.com");
  }

  @Test
  void testHeadExternalServiceURLException() throws Exception {
    Mockito.when(headConnectionMock.getResponseCode()).thenThrow(new IOException());
    Result result = headHealthCheck.check();
    assertFalse(result.isHealthy());
    assertThat(result.getMessage()).contains("http://www.testurl.com");
  }
}
