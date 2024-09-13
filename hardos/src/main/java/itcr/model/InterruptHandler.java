package itcr.model;

public interface InterruptHandler {
  void handlePrint(String message);
  int handleKeyboardInput();
  void handleFileOperation(String operationCode, String fileName, String content);
}
