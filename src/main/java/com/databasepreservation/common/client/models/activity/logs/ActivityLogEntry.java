package com.databasepreservation.common.client.models.activity.logs;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.databasepreservation.common.client.index.IsIndexed;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogEntry extends IsIndexed {

  private String uuid;
  private String address;
  private Date datetime;
  private String username;
  private String actionComponent;
  private String actionMethod;
  private String relatedObjectID;
  private LogEntryState logEntryState;
  private long duration;
  private long lineNumber = -1;
  private List<LogEntryParameter> parameters;

  /**
   * Constructs an empty {@link ActivityLogEntry}.
   */
  public ActivityLogEntry() {
    this.logEntryState = LogEntryState.UNKNOWN;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Date getDatetime() {
    return datetime;
  }

  public void setDatetime(Date datetime) {
    this.datetime = datetime;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getActionComponent() {
    return actionComponent;
  }

  public void setActionComponent(String actionComponent) {
    this.actionComponent = actionComponent;
  }

  public String getActionMethod() {
    return actionMethod;
  }

  public void setActionMethod(String actionMethod) {
    this.actionMethod = actionMethod;
  }

  public String getRelatedObjectID() {
    return relatedObjectID;
  }

  public void setRelatedObjectID(String relatedObjectID) {
    this.relatedObjectID = relatedObjectID;
  }

  public LogEntryState getLogEntryState() {
    return logEntryState;
  }

  public void setLogEntryState(LogEntryState logEntryState) {
    this.logEntryState = logEntryState;
  }


  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public long getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(long lineNumber) {
    this.lineNumber = lineNumber;
  }

  public List<LogEntryParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<LogEntryParameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityLogEntry that = (ActivityLogEntry) o;
    return getDuration() == that.getDuration() &&
        getLineNumber() == that.getLineNumber() &&
        Objects.equals(getUuid(), that.getUuid()) &&
        Objects.equals(getAddress(), that.getAddress()) &&
        Objects.equals(getDatetime(), that.getDatetime()) &&
        Objects.equals(getUsername(), that.getUsername()) &&
        Objects.equals(getActionComponent(), that.getActionComponent()) &&
        Objects.equals(getActionMethod(), that.getActionMethod()) &&
        Objects.equals(getRelatedObjectID(), that.getRelatedObjectID()) &&
        getLogEntryState() == that.getLogEntryState();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUuid(), getAddress(), getDatetime(), getUsername(), getActionComponent(), getActionMethod(), getRelatedObjectID(), getLogEntryState(), getDuration(), getLineNumber());
  }

  @Override
  public String toString() {
    return "LogEntry [uuid=" + uuid + ", address=" + address + ", datetime=" + datetime + ", username="
        + username + ", actionComponent=" + actionComponent + ", actionMethod=" + actionMethod + ", relatedObjectID="
        + relatedObjectID + ", duration=" + duration + ", lineNumber=" + lineNumber
        + ", parameters=" + parameters + ", state=" + logEntryState.name() + "]";
  }
}
