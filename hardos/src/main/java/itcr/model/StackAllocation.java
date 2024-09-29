package itcr.model;

/**
 * StackAllocation class represents a block of memory allocated for a stack.
 */
public class StackAllocation {
  int startIndex;
  int size;

  /**
   * Constructor for StackAllocation.
   *
   * @param startIndex the start index of the stack in memory
   * @param size       the size of the stack
   */
  StackAllocation(int startIndex, int size) {
    this.startIndex = startIndex;
    this.size = size;
  }
}