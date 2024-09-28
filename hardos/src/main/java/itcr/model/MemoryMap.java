package itcr.model;

import java.util.ArrayList;
import java.util.List;

public class MemoryMap {
  public static class MemorySection {
    public String name;
    public int start;
    public int end;
    public List<MemorySection> subSections;
    public String additionalInfo;

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

  public MemoryMap() {
    allocatedProcesses = new ArrayList<>();
    freeSpaces = new ArrayList<>();
    allocatedStacks = new ArrayList<>();
    storedStrings = new ArrayList<>();
    storedFiles = new ArrayList<>();
  }
}