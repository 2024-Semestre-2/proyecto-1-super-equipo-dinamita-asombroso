package itcr.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserInputHandler class handles user input requests and responses for
 * processes.
 * It uses CompletableFuture to manage asynchronous input handling.
 */
public class UserInputHandler {
  private static ConcurrentHashMap<Integer, CompletableFuture<String>> inputFutures = new ConcurrentHashMap<>();

  /**
   * Requests input for a specific process.
   *
   * @param processId the ID of the process requesting input
   * @return a CompletableFuture that will be completed with the input
   */
  public static CompletableFuture<String> requestInput(int processId) {
    CompletableFuture<String> future = new CompletableFuture<>();
    inputFutures.put(processId, future);
    return future;
  }

  /**
   * Provides input for a specific process.
   *
   * @param processId the ID of the process providing input
   * @param input     the input to provide
   */
  public static void provideInput(int processId, String input) {
    CompletableFuture<String> future = inputFutures.remove(processId);
    if (future != null) {
      future.complete(input);
    }
  }

  /**
   * Resets the input handler, clearing all pending input requests.
   */
  public static void reset() {
    inputFutures.clear();
  }
}