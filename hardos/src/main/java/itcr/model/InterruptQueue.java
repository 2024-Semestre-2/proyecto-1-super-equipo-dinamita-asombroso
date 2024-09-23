package itcr.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InterruptQueue {
  private static BlockingQueue<InterruptMessage> queue = new LinkedBlockingQueue<>();

  public static void addMessage(InterruptMessage message) {
    queue.offer(message);
  }

  public static InterruptMessage takeMessage() throws InterruptedException {
    return queue.take();
  }
}