package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraConnection {

  private final static Logger logger = LoggerFactory.getLogger(JiraConnection.class);

  private static final String REST_AUTH = "rest/auth/1/session";
  private static final String OS_AUTH = "?os_authType=basic";
  private final static String AUTH = "Authorization";
  private static final String HTTPS = "https://";
  private static final char SEPERATOR = '/';
  private static final char USERPASS_SEPERATOR = ':';
  private final static String JIRA = "jira";
  private final static String BASIC = "Basic ";
  private final static String INV_USERPASS = "Unknown Username, Password Combination";
  private final static String INV_SERVER = "Invalid Serveradress";

  private final String userPass;

  private final String jiraAdress;

  public JiraConnection(final String username, final String password, final String adress)
      throws IllegalArgumentException {
    if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
      this.userPass = null;
    } else {
      this.userPass = BASIC
          + Base64.encodeBase64String((username + USERPASS_SEPERATOR + password).getBytes());
    }
    if (adress == null || adress.isEmpty()) {
      throw new IllegalArgumentException(INV_SERVER);
    }
    this.jiraAdress = HTTPS + adress + SEPERATOR + JIRA + SEPERATOR;
    if (userPass == null && !serverIsValid()) {// server is tested separately
      throw new IllegalArgumentException(INV_SERVER);
    }

    if (userPass != null && !passwordIsValid()) {
      throw new IllegalArgumentException(INV_USERPASS);
    }

  }

  private boolean serverIsValid() {
    return urlIsValid(jiraAdress);
  }

  private boolean passwordIsValid() {
    final String adress = jiraAdress + REST_AUTH + OS_AUTH;
    return urlIsValid(adress);
  }

  private boolean urlIsValid(final String adress) {
    try {
      final URL url = new URL(adress);
      fireURL(url, false);
      return true;
    } catch (final IOException ignore) {
      if (logger.isInfoEnabled()) {
        logger.info(ignore.toString());
      }
      return false;
    }
  }

  public void deleteConnection() {
    try {
      final URL url = new URL(jiraAdress + REST_AUTH);
      fireURL(url, RequestMethod.DELETE);
    } catch (final IOException e) {
      logger.warn("Couldn't logout from Jira.\n" + e.toString());
    }
  }

  protected URL stringToURL(final String adress) throws MalformedURLException,
      UnsupportedEncodingException {
    return new URL(URLEncoder.encode(adress, "utf8").replace("%2F", "/").replace("%3A", ":")
        .replace("%3F", "?").replace("%26", "&").replace("%3D", "="));
  }

  public String fireURL(final String adress) throws IOException {
    final URL url = stringToURL(jiraAdress + adress);
    if (userPass == null) {
      throw new IllegalArgumentException("Cannot Access URL: " + url + ". No Password set!");
    }
    return fireURL(url, true);
  }

  protected String fireURL(final URL url, final RequestMethod method) throws IOException {
    return fireURL(url, method, true);
  }

  protected String fireURL(final URL url, final boolean useErrorStreamIfNecessarry)
      throws IOException {
    return fireURL(url, RequestMethod.GET, useErrorStreamIfNecessarry);
  }

  protected String fireURL(final URL url, final RequestMethod method,
      final boolean useErrorStreamIfNecessarry) throws IOException {
    final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    connection.setRequestMethod(method.toString());
    if (userPass != null) {
      connection.setRequestProperty(AUTH, userPass);
    }
    InputStream stream;
    try {
      stream = connection.getInputStream();
    } catch (final IOException e) {
      if (!useErrorStreamIfNecessarry) {
        throw e;
      }
      stream = connection.getErrorStream();
    }
    return IOUtils.toString(stream, "UTF-8");
  }

  public enum RequestMethod {
    GET,
    POST,
    HEAD,
    OPTIONS,
    PUT,
    DELETE,
    TRACE;
  }

  public String getAdress() {
    return jiraAdress;
  }

  @Override
  protected void finalize() throws Throwable {
    deleteConnection();
    super.finalize();
  }
}
