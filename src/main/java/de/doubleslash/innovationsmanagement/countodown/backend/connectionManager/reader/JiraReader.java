package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.reader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.JiraConnector;
import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.connector.JiraConnection;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class JiraReader extends JiraConnector implements Reader {

  private static final Logger logger = LoggerFactory.getLogger(JiraReader.class);

  private static final String SEARCH = "search/";
  private static final String JQL = "jql=";
  private static final String MAX_RESULT = "&maxResults=";
  private static final String BROWSE = "browse/";
  private static final int MAX_RESULT_VALUE = 20;
  private static final String START_AT = "&startAt=";
  private static final int MAX_RESULTS_TO_LOAD = 500;

  public JiraReader(final ObserverableValueImplementation<Throwable> latestException,
      final ThreadPoolExecutor threadPool) {
    super(latestException, threadPool);
  }

  public void LogInJira(final String username, final String password, final String jiraAdress)
      throws IllegalArgumentException {
    final JiraConnection conn = new JiraConnection(username, password, jiraAdress);
    try {
      addNewJiraConnection(username, jiraAdress, conn);
    } catch (final IllegalArgumentException ia) {
      conn.deleteConnection();
      throw ia;
    }
  }

  public void LogOutJira(final String username, final String jiraAdress) {
    final JiraConnection toDelete = removeJiraConnection(username, jiraAdress);
    toDelete.deleteConnection();
  }

  public void LogOutAll() {
    final Collection<JiraConnection> allJiraConnections = removeAllJiraConnections();
    for (final JiraConnection conn : allJiraConnections) {
      conn.deleteConnection();
    }
  }

  public boolean isServerAdressValid(final String jiraAdress) {
    try {
      new JiraConnection(null, null, jiraAdress);
    } catch (final IllegalArgumentException iae) {
      if (logger.isTraceEnabled()) {
        logger.trace(iae.getMessage());
      }
      return false;
    }
    return true;
  }

  public BlockingQueue<Task> loadSequential(final JiraQueryBuilder query) {
    return createBlockingQueueWithPoisonPillFilledByRunnable(getSequentialLoader(query));
  }

  public Consumer<BlockingQueue<Task>> getSequentialLoader(final JiraQueryBuilder query) {
    final JiraConnection connection = getJiraConnection(query.getUserName(), query.getJiraAdress());
    return createSequentialJiraLoader(SEARCH, JQL + query, connection);
  }

  public Consumer<BlockingQueue<Task>> createSequentialJiraLoader(final String restCommand,
      final String parameter, final JiraConnection connection) {
    final StringBuilder abstractAdress = authorisisedCommandAdress(restCommand, parameter);
    abstractAdress.append(MAX_RESULT);

    final StringBuilder outerAdress = new StringBuilder(abstractAdress).append(0);
    int totalResults = getTotalAmountOfResultsForAdress(outerAdress, connection);
    if (logger.isDebugEnabled()) {
      logger.debug("There is a total of " + totalResults + " Tickets to load");
    }
    if (totalResults > MAX_RESULTS_TO_LOAD) {
      if (logger.isDebugEnabled()) {
        logger.debug("Total Amount of Tickets to load exceeds maximum and will be capped at: "
            + MAX_RESULTS_TO_LOAD);
      }
      totalResults = MAX_RESULTS_TO_LOAD;
    }
    final int totalResultsFinal = totalResults;

    final Consumer<BlockingQueue<Task>> cons = new Consumer<BlockingQueue<Task>>() {

      int startAt = 0;
      final int total = totalResultsFinal;
      final StringBuilder adress = new StringBuilder(abstractAdress);
      AtomicLong loadTime = null;
      AtomicLong convertTime = null;

      {
        if (logger.isTraceEnabled()) {
          loadTime = new AtomicLong();
          convertTime = new AtomicLong();
        }
      }

      final List<Future<List<Task>>> joinList = new LinkedList<>();

      @Override
      public void accept(final BlockingQueue<Task> queue) {
        adress.append(MAX_RESULT_VALUE).append(START_AT);
        do {
          final int startAtFinal = startAt;
          final Future<List<Task>> future = threadPool.submit(() -> {
            return loadTasksStartingAt(startAtFinal, adress, connection);
          });
          joinList.add(future);
          startAt += MAX_RESULT_VALUE;
        } while (startAt < total);

        for (final Future<List<Task>> taksList : joinList) {
          try {
            queue.addAll(taksList.get());
          } catch (final InterruptedException e) {
            logger.warn("interrupted");
            break;
          } catch (final ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
              cause = e;
            }
            latestException.set(cause);
            continue;
          }
        }
        if (logger.isTraceEnabled()) {
          logger.trace("Loading from Jira took:  " + loadTime.get() / 1000 + " seconds and "
              + loadTime.get() % 1000 + " millis");
          logger.trace("Converting Issues to Tasks took:  " + convertTime.get() / 1000
              + " seconds and " + convertTime.get() % 1000 + " millis");
          logger.trace("Loading and Converting done by: " + joinList.size() + " Threads");
        }
      }

      private List<Task> loadTasksStartingAt(final int startAt, final CharSequence abstractAdress,
          final JiraConnection connection) {

        long currentTime = 0;
        if (logger.isTraceEnabled()) {
          currentTime = System.currentTimeMillis();
        }

        final StringBuilder adress = new StringBuilder(abstractAdress);
        adress.append(startAt);
        final List<Issue> issueList = adressToIssueList(adress, connection);
        if (logger.isTraceEnabled()) {
          loadTime.addAndGet(System.currentTimeMillis() - currentTime);
          currentTime = System.currentTimeMillis();
        }

        final List<Task> returnList = new LinkedList<Task>();
        if (issueList == null) {
          return returnList;
        }
        for (final Issue i : issueList) {
          final Task newTask = issueToTask(i);
          newTask.setOnlineSource(connection.getAdress() + BROWSE + newTask.getTitle());
          if (newTask != null) {
            returnList.add(newTask);
          }
        }
        if (logger.isTraceEnabled()) {
          convertTime.addAndGet(System.currentTimeMillis() - currentTime);
        }
        return returnList;
      }
    };
    return cons;
  }
}
