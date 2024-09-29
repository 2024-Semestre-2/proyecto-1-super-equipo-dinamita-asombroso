package itcr.model;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryMap class represents the memory layout of the system, including kernel,
 * OS, user space, and secondary storage.
 * It also keeps track of allocated processes, free spaces, allocated stacks,
 * stored strings, and stored files.
 */
public class MemoryMap {

  /**
   * MemorySection class represents a section of memory with a name, start and end
   * addresses, sub-sections, and additional information.
   */
  public static class MemorySection {
    public String name;
    public int start;
    public int end;
    public List<MemorySection> subSections;
    public String additionalInfo;

    /**
     * Constructor for MemorySection.
     *
     * @param name           the name of the memory section
     * @param start          the start address of the memory section
     * @param end            the end address of the memory section
     * @param additionalInfo additional information about the memory section
     */
    public MemorySection(String name, int start, int end, String additionalInfo) {
      this.name = name;
      this.start = start;
      this.end = end;
      this.subSections = new ArrayList<>();
      this.additionalInfo = additionalInfo;
    }
  }

  public MemorySection kernel;
  public MemorySection os;
  public MemorySection userSpace;
  public MemorySection secondaryStorage;
  public List<MemorySection> allocatedProcesses;
  public List<MemorySection> freeSpaces;
  public List<MemorySection> allocatedStacks;
  public List<MemorySection> storedStrings;
  public List<MemorySection> storedFiles;

  /**
   * Constructor for MemoryMap.
   * Initializes the lists for allocated processes, free spaces, allocated stacks,
   * stored strings, and stored files.
   */
  public MemoryMap() {
    allocatedProcesses = new ArrayList<>();
    freeSpaces = new ArrayList<>();
    allocatedStacks = new ArrayList<>();
    storedStrings = new ArrayList<>();
    storedFiles = new ArrayList<>();
  }
}