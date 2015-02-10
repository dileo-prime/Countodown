package de.doubleslash.innovationsmanagement.countodown.data;

public class Person extends ReadWriteLocked {

  private String firstName;
  private String lastName;

  public Person(final String firstName, final String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  private Person(final Person old) {
    this.firstName = old.firstName;
    this.lastName = old.lastName;
  }

  @SuppressWarnings("unused")
  private Person() {

  }

  public String getFirstName() {
    readLock();
    try {
      return firstName;
    } finally {
      readUnLock();
    }
  }

  public String getLastName() {
    readLock();
    try {
      return lastName;
    } finally {
      readUnLock();
    }
  }

  public void setFirstName(final String firstName) {
    writeLock();
    try {
      if (this.firstName == firstName || (firstName != null && firstName.equals(this.firstName))) {
        return; // String is immutable so test for equal, testing for same for both might be null
      }
      this.firstName = firstName;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setLastName(final String lastName) {
    writeLock();
    try {
      if (this.lastName == lastName || (lastName != null && lastName.equals(this.lastName))) {
        return; // String is immutable so test for equal, testing for same for both might be null
      }
      this.lastName = lastName;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  @Override
  public void mutateTo(final ReadWriteLocked other) throws IllegalArgumentException {
    if (other instanceof Person) {
      mutateTo((Person) other);
    } else {
      throw new IllegalArgumentException("Argument mus be instance of Person");
    }
  }

  private void mutateTo(final Person other) {
    this.firstName = other.firstName;
    this.lastName = other.lastName;

  }

  @Override
  public String toString() {
    readLock();
    try {
      return firstName + " " + lastName;
    } finally {
      readUnLock();
    }
  }

  @Override
  public ReadWriteLocked getEqual() {
    return new Person(this);
  }

}
