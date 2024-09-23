package itcr.model;

public class MemoryAllocation {
  int startIndex;
  int size;
  int used;

  MemoryAllocation(int startIndex, int size) {
    this.startIndex = startIndex;
    this.size = size;
    this.used = 0;
  }
}
