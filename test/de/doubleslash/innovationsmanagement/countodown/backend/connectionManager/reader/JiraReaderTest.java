package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.reader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder.Field;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder.Operator;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class JiraReaderTest {

  JiraReader reader;
  ThreadPoolExecutor threadPool;
  ObserverableValueImplementation<Throwable> latestException;

  @Before
  public void setup() {
    this.threadPool = new ThreadPoolExecutor(10, 10 * 2, 30L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>());
    latestException = new ObserverableValueImplementation<Throwable>();
    // reader = new JiraReader("nwehrle", "######", latestException, "jira.doubleslash.de",
    // threadPool);
  }

  // @Test(expected = IllegalArgumentException.class)
  // public void testWrongUsernamePassword() {
  // reader = new JiraReader("username", "password", null, "jira.doubleslash.de");
  // }

  @Test
  public void testLoadSequential() throws ExecutionException, InterruptedException, IOException {
    final boolean showFinished = false;
    final BlockingQueue<Task> queue = reader.loadSequential(new JiraQueryBuilder(LocalDate.now(),
        LocalDate.now(), showFinished, null, null).addCriteria(Field.Project, Operator.IN, "INNO"));// ("project=PROMT");

    final Task endQ = queue.take(); // Poison Pill
    Task toPrint;

    System.out.println(latestException.getValue());

    while ((toPrint = queue.take()) != endQ) {
      System.out.println();
      System.out.println(toPrint);
    }
  }

  @Test
  public void stringToURL() throws MalformedURLException, UnsupportedEncodingException { // /:?&=~
    // String adress =
    // "https://jira.doubleslash.de/jira/rest/api/2/search/?os_authType=basic&jql=RemainingEstimate>\"0\" AND Due>=0001-01-01 AND Due<=9999-12-31&maxResults=0";
    // String adress =
    // "https://jira.doubleslash.de/jira/rest/api/2/search/?os_authType=basic&jql= filter = \"Fällige Termine\" &maxResults=0";
    final String adress = "https://jira.doubleslash.de/jira/rest/api/2/search/?os_authType=basic&jql= Assignee = currentUser() &maxResults=0";
    final URL url = new URL(URLEncoder.encode(adress, "utf8").replace("%2F", "/")
        .replace("%3A", ":").replace("%3F", "?").replace("%26", "&").replace("%3D", "="));
    System.out.println(url);
  }

  @Test
  public void testEncodingBothways() throws MalformedURLException, UnsupportedEncodingException {
    final String testString = "H�l�enb�ch�r";// "Hülßenbächör";
    System.out.println("orig: " + testString);
    final String enc = URLEncoder.encode(testString, "utf8").replace("%2F", "/")
        .replace("%3A", ":").replace("%3F", "?").replace("%26", "&").replace("%3D", "=");

    System.out.println(Charset.defaultCharset());
    final String dec = fromUTF8(enc + "+Test");
    System.out.println(dec);
  }

  public String fromUTF8(final String inp) {
    if (inp == null || inp.isEmpty()) {
      return inp;
    }

    final String enc = inp.replaceAll("%", "%25");
    try {
      System.out.println("before: " + inp);
      final String out = URLDecoder.decode(enc, Charset.defaultCharset().name());
      System.out.println("after: " + out);
      return out;
    } catch (final UnsupportedEncodingException e) {
      System.out.println("Cannot convert String inp: " + e.getMessage());
      return inp;
    }
  }
}
