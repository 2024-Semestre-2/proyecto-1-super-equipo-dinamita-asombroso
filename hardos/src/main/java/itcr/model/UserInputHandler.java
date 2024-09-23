package itcr.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserInputHandler {
  private static ConcurrentHashMap<Integer, CompletableFuture<String>> inputFutures = new ConcurrentHashMap<>();

  public static CompletableFuture<String> requestInput(int processId) {
    CompletableFuture<String> future = new CompletableFuture<>();
    inputFutures.put(processId, future);
    return future;
  }

  public static void provideInput(int processId, String input) {
    CompletableFuture<String> future = inputFutures.remove(processId);
    if (future != null) {
      future.complete(input);
    }
  }
}