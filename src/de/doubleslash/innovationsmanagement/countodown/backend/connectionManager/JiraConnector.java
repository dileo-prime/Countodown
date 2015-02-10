package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.connector.JiraConnection;
import de.doubleslash.innovationsmanagement.countodown.data.Person;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class JiraConnector extends AbstractConnector {
  private static final Logger logger = LoggerFactory.getLogger(JiraConnector.class);
  // private final String JIRA_ADRESS;// = "https://jira.doubleslash.de/jira/";
  private static final String REST = "rest/api/2/";
  private static final String OS_AUTH = "?os_authType=basic";
  // fields=progress,summary,description,assignee,duedate
  private static final String FIELDS;
  static {
    final StringBuilder sb = new StringBuilder("&fields=");
    for (final Field f : Fields.class.getDeclaredFields()) {
      sb.append(f.getName()).append(',');
    }
    sb.deleteCharAt(sb.length() - 1);
    FIELDS = sb.toString();
  }
  private final static long SECONDS_PER_MINUTE = 60L;

  protected final static String JSON = ".json";
  protected final static String JIRA = "jira";

  protected final ObjectMapper mapper = JsonFactory.create();
  private final Map<ImmutablePair<String, String>, JiraConnection> connections = new TreeMap<>();

  public JiraConnector(final ObserverableValueImplementation<Throwable> latestException,
      final ThreadPoolExecutor threadPool) {
    super(threadPool, latestException);
  }

  protected void addNewJiraConnection(final String username, final String jiraAdress,
      final JiraConnection connection) throws IllegalArgumentException {
    final ImmutablePair<String, String> key = new ImmutablePair<String, String>(username,
        jiraAdress);
    final JiraConnection old = connections.put(key, connection);
    if (old != null) {
      connections.put(key, old);
      throw new IllegalArgumentException("Connection for Username: " + username + ", and adress: "
          + jiraAdress + " already existing");
    }
  }

  protected JiraConnection getJiraConnection(final String username, final String jiraAdress) {
    return connections.get(new ImmutablePair<String, String>(username, jiraAdress));
  }

  protected JiraConnection removeJiraConnection(final String username, final String jiraAdress) {
    return connections.remove(new ImmutablePair<String, String>(username, jiraAdress));
  }

  protected Collection<JiraConnection> removeAllJiraConnections() {
    final Collection<JiraConnection> ret = connections.values();
    connections.clear();
    return ret;
  }

  protected String urlToJson(final String adress, final JiraConnection connection) {
    String out = "{}";
    try {

      if (logger.isDebugEnabled()) {
        logger.debug("Reading from URL: " + adress);
      }

      out = connection.fireURL(adress);
    } catch (final IOException e) {
      logger.warn("Exception while reading from Website, adress is: " + adress);
      latestException.set(e);
    }
    return out;
  }

  private StringBuilder authorisisedCommandAdress(final String restCommand) {
    final StringBuilder adress = new StringBuilder();
    adress.append(REST).append(restCommand).append(OS_AUTH).append(FIELDS);

    return adress;
  }

  protected StringBuilder authorisisedCommandAdress(final String restCommand, final String parameter) {
    final StringBuilder adress = authorisisedCommandAdress(restCommand);
    if (parameter != null) {
      adress.append('&').append(parameter);
    }
    return adress;

  }

  private static class Result {
    int total;
    List<Issue> issues;

    List<String> errorMessages;
  }

  protected List<Issue> adressToIssueList(final CharSequence adress, final JiraConnection connection) {
    return adressToResult(adress, connection).issues;
  }

  protected int getTotalAmountOfResultsForAdress(final CharSequence adress,
      final JiraConnection connection) {
    final Result res = adressToResult(adress, connection);
    if (res.errorMessages != null) {
      latestException.set(new IllegalArgumentException(res.errorMessages.toString()));
    }
    return res.total;
  }

  private Result adressToResult(final CharSequence adress, final JiraConnection connection) {
    final String json = urlToJson(adress.toString(), connection);
    return mapper.readValue(json, Result.class);
  }

  protected Task issueToTask(final Issue i) {
    String[] assigneName = null;
    Person assigne = null;
    LocalDate dueDate = null;
    long workToDo = 0L;
    String summary = null;
    final Fields fields = i.fields;

    if (logger.isTraceEnabled()) {
      logger.trace("Creating Task from Issue: " + i.key);
    }

    if (fields == null) {
      return null;
    }

    summary = fields.summary;
    if (fields.duedate != null) {
      dueDate = LocalDate.parse(fields.duedate);
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Skipping issue " + i.key + ", no DueDate could be parsed");
      }
      return null;
    }
    if (fields.assignee != null && fields.assignee.displayName != null) {
      assigneName = i.fields.assignee.displayName.trim().split(" ", 2);
      assigne = new Person(assigneName[0], assigneName[1]);
    }
    if (fields.progress != null) {
      workToDo = (fields.progress.total - fields.progress.progress) / SECONDS_PER_MINUTE;
    }

    final Task task = new Task("" + i.id, JIRA, i.key, summary, assigne, dueDate, workToDo,
        fields.description);

    return task;

  }

  protected static class Issue {
    int id;
    public String key;
    String adress;
    public Fields fields;
  }

  private static class Fields {
    Progress progress;
    String summary;
    // Employee reporter;
    Employee assignee;
    // IdName priority; maybe later
    String duedate;
    // IdName status;
    String description;
  }

  // private static class IdName {
  // Integer id;
  // String name;
  // }

  private static class Employee {
    String displayName;
    // String self;
    // String name;
  }

  private static class Progress {
    int progress;
    int total;
  }

}
