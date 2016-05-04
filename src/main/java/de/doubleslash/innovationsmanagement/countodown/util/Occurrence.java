package de.doubleslash.innovationsmanagement.countodown.util;

/**
 * Occurrence is a threadsave waiting lock that can be used to sleep until an occurrence occurred.
 * Unlike a Condition this will not sleep if {@link #signal()} has been called before
 * {@link #await()}. Occurrence is meant to be used when Thread1 needs to wait for a certain
 * something to be finished in Thread2 but it is possible that Thread2 finishes before Thread1
 * reaches its waitpoint
 * 
 * @author nwehrle
 */
public class Occurrence {

  public final static long SECONDS10 = 10000L;

  volatile boolean occured = false;

  /**
   * will wait a maximum of 10 seconds. see {@link #await(long)}
   * 
   * @throws InterruptedException
   */
  synchronized public void await() throws InterruptedException {
    await(SECONDS10);
  }

  /**
   * will wait until signal is called on this object. if signal already has been called, this method
   * returns
   * 
   * @param timeout
   *          max wait time
   * @throws InterruptedException
   */
  synchronized public void await(final long timeout) throws InterruptedException {
    if (!occured) {
      this.wait(timeout);
    }
  }

  synchronized public void signal() {
    occured = true;
    this.notifyAll();
  }

}
