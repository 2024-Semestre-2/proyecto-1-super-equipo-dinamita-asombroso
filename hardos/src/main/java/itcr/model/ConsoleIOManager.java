package itcr.model;

import java.util.Scanner;

public class ConsoleIOManager implements IOManager {
  private Scanner scanner = new Scanner(System.in);

  @Override
  public void print(String message) {
    System.out.println(message);
  }

  @Override
  public String readInput() {
    return scanner.nextLine();
  }
}