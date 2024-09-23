package itcr.model;

public class InterruptMessage {
  private int coreId;
  private InterruptCode code;
  private String message;
  private int processId;

  public InterruptMessage(int coreId, InterruptCode code, String message, int processId) {
    this.coreId = coreId;
    this.code = code;
    this.message = message;
    this.processId = processId;
  }

  // accesors
  public int getCoreId() {
    return coreId;
  }

  public InterruptCode getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public int getProcessId() {
    return processId;
  }

  public String toString() {
    return "Core " + coreId + " - " + code + ": " + message;
  }
}