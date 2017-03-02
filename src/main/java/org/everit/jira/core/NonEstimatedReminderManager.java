package org.everit.jira.core;

public interface NonEstimatedReminderManager {
  // TODO implement this
  public void registerNonEstimatedReminder(int timeInMinutes);

  public void registerNonEstimatedReminderWithDefaultTime();

  public void unregisterNonEstimatedReminder();
}
