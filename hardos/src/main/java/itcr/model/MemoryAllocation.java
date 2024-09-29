package itcr.model;

/**
 * MemoryAllocation class represents a block of memory allocation with a start
 * index, size, and used space.
 */
public class MemoryAllocation {
  int startIndex;
  int size;
  int used;

  /**
   * Constructor for MemoryAllocation.
   *
   * @param startIndex the start index of the memory block
   * @param size       the size of the memory block
   */
  MemoryAllocation(int startIndex, int size) {
    this.startIndex = startIndex;
    this.size = size;
    this.used = 0;
  }
}