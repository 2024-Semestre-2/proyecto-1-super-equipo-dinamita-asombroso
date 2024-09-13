package itcr.model;

import java.util.Scanner;

public class CLIInterruptHandler implements InterruptHandler {

  private Scanner scanner = new Scanner(System.in);

  @Override
  public void handlePrint(String message) {
    System.out.println(message);
  }

  @Override
  public int handleKeyboardInput() {
    return scanner.nextInt();
  }

  @Override
  public void handleFileOperation(String operationCode, String fileName, String content) {
    // TODO
    System.out.println("File operation: " + operationCode + " on " + fileName);
    System.out.println("Content: " + content);
  }
}
