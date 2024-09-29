package itcr.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * InterruptQueue class represents a thread-safe queue for handling interrupt
 * messages.
 */
public class InterruptQueue {
  private static BlockingQueue<InterruptMessage> queue = new LinkedBlockingQueue<>();

  /**
   * Adds an interrupt message to the queue.
   *
   * @param message the interrupt message to add
   */
  public static void addMessage(InterruptMessage message) {
    queue.offer(message);
  }

  /**
   * Takes an interrupt message from the queue, waiting if necessary until an
   * element becomes available.
   *
   * @return the interrupt message taken from the queue
   * @throws InterruptedException if interrupted while waiting
   */
  public static InterruptMessage takeMessage() throws InterruptedException {
    return queue.take();
  }

  /**
   * Clears all interrupt messages from the queue.
   */
  public static void clear() {
    queue.clear();
  }
}