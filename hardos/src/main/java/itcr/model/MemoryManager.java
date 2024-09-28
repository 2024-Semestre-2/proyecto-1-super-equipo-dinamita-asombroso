package itcr.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Comparator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MemoryManager {
  private static final int DEFAULT_MAIN_MEMORY_SIZE = 512;
  private static final int DEFAULT_VIRTUAL_MEMORY_SIZE = 64;
  private static final int DEFAULT_SECONDARY_STORAGE_SIZE = 256;
  private static final int KB = 1024;
  private static final int STACK_SIZE = 20;
  private static final int INTEGER_SIZE = 32;
  private static final int STACK_ENTRIES = 5;

  private int mainMemorySize;
  private int virtualMemorySize;
  private int secondaryMemorySize;

  private String[] virtualMemory;
  private byte[] mainMemory;
  private byte[] secondaryStorage;

  private Map<String, MemoryAllocation> mainMemoryIndex;
  private Map<String, FileInfo> secondaryStorageIndex;
  private Map<String, StackAllocation> stackAllocations;

  private List<MemoryAllocation> freeSpaces;
  private Map<String, List<InstructionIndex>> processInstructionIndices;

  private Map<Integer, StringAllocation> stringAllocations;
  private List<MemoryAllocation> freeStringSpaces;

  private int kernelSize = 8;
  private int osSize = 128;
  private int userSpaceStart;

  public MemoryManager() {
    mainMemorySize = DEFAULT_MAIN_MEMORY_SIZE;
    virtualMemorySize = DEFAULT_VIRTUAL_MEMORY_SIZE;
    secondaryMemorySize = DEFAULT_SECONDARY_STORAGE_SIZE;
    initializeMemory();
  }

  public MemoryManager(int mainMemorySize, int secondaryMemorySize) {
    this.mainMemorySize = mainMemorySize;
    this.secondaryMemorySize = secondaryMemorySize;
    this.virtualMemorySize = DEFAULT_VIRTUAL_MEMORY_SIZE;
    initializeMemory();
  }

  public MemoryManager(int mainMemorySize, int secondaryMemorySize, int kernelSize, int osSize) {
    this.mainMemorySize = mainMemorySize;
    this.secondaryMemorySize = secondaryMemorySize;
    this.virtualMemorySize = DEFAULT_VIRTUAL_MEMORY_SIZE;
    this.kernelSize = kernelSize;
    this.osSize = osSize;
    this.userSpaceStart = mainMemorySize - kernelSize - osSize;
    initializeMemory();
  }

  private void initializeMemory() {
    this.mainMemory = new byte[mainMemorySize * KB];
    this.secondaryStorage = new byte[secondaryMemorySize * KB];
    virtualMemory = new String[virtualMemorySize * KB];

    this.mainMemoryIndex = new HashMap<>();
    this.secondaryStorageIndex = new HashMap<>();
    this.stackAllocations = new HashMap<>();

    this.userSpaceStart = (kernelSize + osSize) * KB;
    this.freeSpaces = new ArrayList<>();
    freeSpaces.add(new MemoryAllocation(userSpaceStart, (mainMemorySize - kernelSize - osSize) * KB));

    this.stringAllocations = new HashMap<>();
    this.freeStringSpaces = new ArrayList<>();
    freeStringSpaces.add(new MemoryAllocation(mainMemorySize * KB - 2048, 2048));

    this.processInstructionIndices = new HashMap<>();
  }

  // String ( main memory ) management methods

  public int storeString(String str) {
    byte[] strBytes = str.getBytes();
    for (MemoryAllocation freeSpace : freeStringSpaces) {
      if (freeSpace.size >= strBytes.length) {
        int startIndex = freeSpace.startIndex;
        System.arraycopy(strBytes, 0, mainMemory, startIndex, strBytes.length);
        stringAllocations.put(startIndex, new StringAllocation(startIndex, strBytes.length));

        freeSpace.startIndex += strBytes.length;
        freeSpace.size -= strBytes.length;
        if (freeSpace.size == 0) {
          freeStringSpaces.remove(freeSpace);
        }

        return startIndex;
      }
    }
    return -1;
  }

  public String getString(int address) {
    StringAllocation allocation = stringAllocations.get(address);
    if (allocation != null) {
      byte[] strBytes = new byte[allocation.length];
      System.arraycopy(mainMemory, allocation.startIndex, strBytes, 0, allocation.length);
      return new String(strBytes);
    }
    return null;
  }

  public void freeString(int address) {
    StringAllocation allocation = stringAllocations.remove(address);
    if (allocation != null) {
      Arrays.fill(mainMemory, allocation.startIndex, allocation.startIndex + allocation.length, (byte) 0);

      freeStringSpaces.add(new MemoryAllocation(allocation.startIndex, allocation.length));

      mergeFreStringSpaces();
    }
  }

  private void mergeFreStringSpaces() {
    freeStringSpaces.sort(Comparator.comparingInt(a -> a.startIndex));
    for (int i = 0; i < freeStringSpaces.size() - 1; i++) {
      MemoryAllocation current = freeStringSpaces.get(i);
      MemoryAllocation next = freeStringSpaces.get(i + 1);
      if (current.startIndex + current.size == next.startIndex) {
        current.size += next.size;
        freeStringSpaces.remove(i + 1);
        i--;
      }
    }
  }

  // public void printAllStrings() {
  //   System.out.println("=== Stored Strings ===");
  //   if (stringAllocations.isEmpty()) {
  //     System.out.println("No strings stored in memory.");
  //   } else {
  //     for (Map.Entry<Integer, StringAllocation> entry : stringAllocations.entrySet()) {
  //       int address = entry.getKey();
  //       StringAllocation allocation = entry.getValue();
  //       String storedString = getString(address);
  //       System.out.printf("Address: %d, Length: %d, Content: \"%s\"%n",
  //           address, allocation.length, storedString);
  //     }
  //   }
  //   System.out.println("=== Free String Spaces ===");
  //   if (freeStringSpaces.isEmpty()) {
  //     System.out.println("No free spaces for strings.");
  //   } else {
  //     for (MemoryAllocation freeSpace : freeStringSpaces) {
  //       System.out.printf("Start: %d, Size: %d bytes%n",
  //           freeSpace.startIndex, freeSpace.size);
  //     }
  //   }
  //   System.out.println("========================");
  // }

  // Main memory management methods

  // Allocating memory for a process

  public int allocateMemory(String processName, int size) {
    for (int i = 0; i < freeSpaces.size(); i++) {
      MemoryAllocation freeSpace = freeSpaces.get(i);
      if (freeSpace.size >= size) {
        MemoryAllocation allocation = new MemoryAllocation(freeSpace.startIndex, size);
        mainMemoryIndex.put(processName, allocation);

        if (freeSpace.size > size) {
          freeSpaces.set(i, new MemoryAllocation(freeSpace.startIndex + size, freeSpace.size - size));
        } else {
          freeSpaces.remove(i);
        }

        return allocation.startIndex;
      }
    }
    return -1; // No hay espacio suficiente
  }

  public int getFirstProcessInstructionAddress(String processName) {
    List<InstructionIndex> indices = processInstructionIndices.get(processName);
    if (indices != null && !indices.isEmpty()) {
      return indices.get(0).startIndex;
    }
    return -1;
  }
  
  public synchronized boolean deallocateMemory(String processName) {
    MemoryAllocation allocation = mainMemoryIndex.remove(processName);
    if (allocation != null) {
      Arrays.fill(mainMemory, allocation.startIndex, allocation.startIndex + allocation.size, (byte) 0);
      processInstructionIndices.remove(processName);
      addFreeSpace(new MemoryAllocation(allocation.startIndex, allocation.size));
      mergeFreeSpaces();
      return true;
    }
    return false;
  }

  private synchronized void addFreeSpace(MemoryAllocation newFreeSpace) {
    freeSpaces.add(newFreeSpace);
    freeSpaces.sort(Comparator.comparingInt(a -> a.startIndex));
  }

  private void mergeFreeSpaces() {
    if (freeSpaces.size() < 2)
      return;

    List<MemoryAllocation> mergedSpaces = new ArrayList<>();
    MemoryAllocation current = freeSpaces.get(0);

    for (int i = 1; i < freeSpaces.size(); i++) {
      MemoryAllocation next = freeSpaces.get(i);
      if (current.startIndex + current.size == next.startIndex) {
        // Los espacios son adyacentes, fusionarlos
        current = new MemoryAllocation(current.startIndex, current.size + next.size);
      } else {
        mergedSpaces.add(current);
        current = next;
      }
    }
    mergedSpaces.add(current);

    freeSpaces = mergedSpaces;
  }

  public boolean deallocateStack(String processId) {
    StackAllocation stackAllocation = stackAllocations.remove(processId);
    if (stackAllocation != null) {
      Arrays.fill(mainMemory, stackAllocation.startIndex, stackAllocation.startIndex + STACK_SIZE, (byte) 0);
      addFreeSpace(new MemoryAllocation(stackAllocation.startIndex, STACK_SIZE));
      mergeFreeSpaces();
      return true;
    }
    return false;
  }

  public boolean allocateStack(String processId) {
    if (stackAllocations.containsKey(processId)) {
      return false;
    }

    for (int i = 0; i < freeSpaces.size(); i++) {
      MemoryAllocation freeSpace = freeSpaces.get(i);
      if (freeSpace.size >= STACK_SIZE) {
        StackAllocation stackAllocation = new StackAllocation(freeSpace.startIndex, STACK_SIZE);
        stackAllocations.put(processId, stackAllocation);

        if (freeSpace.size > STACK_SIZE) {
          freeSpaces.set(i, new MemoryAllocation(freeSpace.startIndex + STACK_SIZE, freeSpace.size - STACK_SIZE));
        } else {
          freeSpaces.remove(i);
        }

        // Inicializar el espacio del stack con ceros
        Arrays.fill(mainMemory, stackAllocation.startIndex, stackAllocation.startIndex + STACK_SIZE, (byte) 0);

        return true;
      }
    }
    return false;
  }

  public boolean writeToStack(String processId, int index, int value) {
    StackAllocation stackAllocation = stackAllocations.get(processId);
    if (stackAllocation == null || index < 0 || index >= STACK_ENTRIES) {
      if (stackAllocation == null) {
        System.out.println("xxx err stackAllocation null");
      }
      if (index < 0 || index >= STACK_ENTRIES) {
        System.out.println("xxx err index out of bounds");
      }
      System.out.println("xxx err 1");
      return false;
    }

    int byteIndex = stackAllocation.startIndex + (index * 4);
    boolean isNegative = value < 0;
    int absValue = Math.abs(value);

    mainMemory[byteIndex] = (byte) (isNegative ? 0x80 : 0x00);

    for (int i = 1; i < INTEGER_SIZE; i++) {
      int byteOffset = i / 8;
      int bitOffset = i % 8;
      byte currentByte = mainMemory[byteIndex + byteOffset];

      if ((absValue & (1 << (i - 1))) != 0) {
        currentByte |= (1 << bitOffset);
      } else {
        currentByte &= ~(1 << bitOffset);
      }

      mainMemory[byteIndex + byteOffset] = currentByte;
    }

    return true;
  }

  // public void printStackKeyAllocations() {
  //   System.out.println("Stack Key Allocations:");
  //   for (Map.Entry<String, StackAllocation> entry : stackAllocations.entrySet()) {
  //     System.out.println(entry.getKey() + ": " + entry.getValue().startIndex);
  //   }
  // }

  public int popFromStack(String processId, int index) {
    int value = readFromStack(processId, index);
    writeToStack(processId, index, 0);
    return value;
  }

  public int readFromStack(String processId, int index) {
    StackAllocation stackAllocation = stackAllocations.get(processId);
    if (stackAllocation == null || index < 0 || index >= STACK_ENTRIES) {
      return 0;
    }

    int byteIndex = stackAllocation.startIndex + (index * 4); // 4 bytes por entero
    boolean isNegative = (mainMemory[byteIndex] & 0x80) != 0;
    int value = 0;

    for (int i = 1; i < INTEGER_SIZE; i++) {
      int byteOffset = i / 8;
      int bitOffset = i % 8;
      if ((mainMemory[byteIndex + byteOffset] & (1 << bitOffset)) != 0) {
        value |= (1 << (i - 1));
      }
    }

    return isNegative ? -value : value;
  }

  // public void printStacks() {
  //   for (Map.Entry<String, StackAllocation> entry : stackAllocations.entrySet()) {
  //     StackAllocation stackAllocation = entry.getValue();
  //     System.out.println("Stack for process " + entry.getKey() + ":");
  //     for (int i = 0; i < STACK_ENTRIES; i++) {
  //       int byteIndex = stackAllocation.startIndex + (i * 4);
  //       int value = readFromStack(entry.getKey(), i);
  //       System.out.println(i + ": " + value + " | Bytes: " + byteIndex + " - " + (byteIndex + 3));
  //     }
  //   }
  // }

  // return in a string the main memory map (processes, instructions, and stack)
  public MemoryMap getMainMemoryMap() {
    MemoryMap map = new MemoryMap();

    map.kernel = new MemoryMap.MemorySection("Kernel Space", 0, kernelSize * KB - 1, null);
    map.os = new MemoryMap.MemorySection("OS Space", kernelSize * KB, userSpaceStart - 1, null);
    map.userSpace = new MemoryMap.MemorySection("User Space", userSpaceStart, mainMemorySize * KB - 1, null);

    for (Map.Entry<String, MemoryAllocation> entry : mainMemoryIndex.entrySet()) {
      MemoryAllocation allocation = entry.getValue();
      String bcpInfo = getBCPInfo(entry.getKey());
      MemoryMap.MemorySection process = new MemoryMap.MemorySection(
          entry.getKey(),
          allocation.startIndex,
          allocation.startIndex + allocation.size - 1,
          bcpInfo);

      List<InstructionIndex> indices = processInstructionIndices.get(entry.getKey());
      if (indices != null) {
        int instructionCount = 0;
        for (InstructionIndex instructionIndex : indices) {
          String instruction = getInstruction(entry.getKey(), instructionCount++);

          process.subSections.add(new MemoryMap.MemorySection(
              "Instruction",
              instructionIndex.startIndex,
              instructionIndex.startIndex + instructionIndex.length - 1,
              instruction));
        }
      }

      map.allocatedProcesses.add(process);
    }

    for (MemoryAllocation freeSpace : freeSpaces) {
      map.freeSpaces.add(new MemoryMap.MemorySection(
          "Free Space",
          freeSpace.startIndex,
          freeSpace.startIndex + freeSpace.size - 1,
          null));
    }

    for (Map.Entry<String, StackAllocation> entry : stackAllocations.entrySet()) {
      StackAllocation stackAllocation = entry.getValue();
      String stackValues = getStackValues(entry.getKey());
      map.allocatedStacks.add(new MemoryMap.MemorySection(
          entry.getKey() + " Stack",
          stackAllocation.startIndex,
          stackAllocation.startIndex + STACK_SIZE - 1,
          stackValues));
    }

    for (Map.Entry<Integer, StringAllocation> entry : stringAllocations.entrySet()) {
      int address = entry.getKey();
      StringAllocation allocation = entry.getValue();
      String storedString = getString(address);
      map.storedStrings.add(new MemoryMap.MemorySection(
          "Str",
          address,
          address + allocation.length - 1,
          String.format("Value: \"%s\"", storedString)));
    }

    return map;
  }

  private String getBCPInfo(String processName) {
    MemoryAllocation allocation = mainMemoryIndex.get(processName + "_bcp");
    if (allocation != null) {
      byte[] bcpBytes = new byte[allocation.size];
      System.arraycopy(mainMemory, allocation.startIndex, bcpBytes, 0, allocation.size);
      return new String(bcpBytes);
    }
    return null;
  }

  private String getStackValues(String processId) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < STACK_ENTRIES; i++) {
      int value = readFromStack(processId, i);
      sb.append(value).append(", ");
    }
    return sb.toString();
  }

  // Storing and retrieving instructions

  public boolean storeInstruction(String processName, String instruction) {
    MemoryAllocation processAllocation = mainMemoryIndex.get(processName);
    if (processAllocation == null) {
      return false;
    }

    byte[] instructionBytes = instruction.getBytes();
    if (processAllocation.size - processAllocation.used < instructionBytes.length) {
      return false; // No hay espacio suficiente
    }

    int startIndex = processAllocation.startIndex + processAllocation.used;
    System.arraycopy(instructionBytes, 0, mainMemory, startIndex, instructionBytes.length);

    processInstructionIndices
        .computeIfAbsent(processName, k -> new ArrayList<>())
        .add(new InstructionIndex(startIndex, instructionBytes.length));

    processAllocation.used += instructionBytes.length;

    return true;
  }

  // public void printInstructions(String processId) {
  //   List<InstructionIndex> indices = processInstructionIndices.get(processId);
  //   if (indices == null) {
  //     System.out.println("No instructions stored for process " + processId);
  //   } else {
  //     System.out.println("Instructions for process " + processId + ":");
  //     for (int i = 0; i < indices.size(); i++) {
  //       InstructionIndex instructionIndex = indices.get(i);
  //       byte[] instructionBytes = new byte[instructionIndex.length];
  //       System.arraycopy(mainMemory, instructionIndex.startIndex, instructionBytes, 0, instructionIndex.length);
  //       System.out.println(i + ": " + new String(instructionBytes));
  //     }
  //   }
  // }

  public String getInstruction(String processName, int index) {
    List<InstructionIndex> indices = processInstructionIndices.get(processName);
    if (indices == null || index < 0 || index >= indices.size()) {
      return null;
    }

    InstructionIndex instructionIndex = indices.get(index);
    byte[] instructionBytes = new byte[instructionIndex.length];
    System.arraycopy(mainMemory, instructionIndex.startIndex, instructionBytes, 0, instructionIndex.length);
    return new String(instructionBytes);
  }

  public int getQtyInstructions(String processName) {
    List<InstructionIndex> indices = processInstructionIndices.get(processName);
    return indices != null ? indices.size() : 0;
  }

  // ---- BCP ----
  public boolean storeBCP(String processName, String bcpString) {
    byte[] bcpBytes = bcpString.getBytes();
    MemoryAllocation allocation = allocateOSSpace(bcpBytes.length);
    if (allocation != null) {
      System.arraycopy(bcpBytes, 0, mainMemory, allocation.startIndex, bcpBytes.length);
      mainMemoryIndex.put(processName + "_bcp", allocation);
      return true;
    }
    return false;
  }

  public String getBCP(String processName) {
    MemoryAllocation allocation = mainMemoryIndex.get(processName + "_bcp");
    if (allocation != null) {
      byte[] bcpBytes = new byte[allocation.size];
      System.arraycopy(mainMemory, allocation.startIndex, bcpBytes, 0, allocation.size);
      return new String(bcpBytes);
    }
    return null;
  }

  public boolean freeBCP(String processName) {
    MemoryAllocation allocation = mainMemoryIndex.remove(processName + "_bcp");
    if (allocation != null) {
      Arrays.fill(mainMemory, allocation.startIndex, allocation.startIndex + allocation.size, (byte) 0);
      return true;
    }
    return false;
  }

  // delete BCP from main memory (os space) and free the space (suffix _bcp) using deallocateOSSpace
  public boolean deleteBCP(String processName) {
    MemoryAllocation allocation = mainMemoryIndex.remove(processName + "");
    if (allocation != null) {
      Arrays.fill(mainMemory, allocation.startIndex, allocation.startIndex + allocation.size, (byte) 0);
      return deallocateOSSpace(allocation.startIndex, allocation.size);
    }
    return false;
  }

  // Secondary memory management methods

  public boolean storeFile(String fileName, String fileContent) {
    byte[] fileBytes = fileContent.getBytes();
    int startIndex = allocateSecondaryMemory(fileBytes.length);
    if (startIndex != -1) {
      System.arraycopy(fileBytes, 0, secondaryStorage, startIndex, fileBytes.length);
      secondaryStorageIndex.put(fileName, new FileInfo(startIndex, fileBytes.length));
      return true;
    }
    return false;
  }

  public String getFile(String fileName) {
    FileInfo fileInfo = secondaryStorageIndex.get(fileName);
    if (fileInfo != null) {
      byte[] fileBytes = new byte[fileInfo.size];
      System.arraycopy(secondaryStorage, fileInfo.startIndex, fileBytes, 0, fileInfo.size);
      return new String(fileBytes);
    }
    return null;
  }

  public void freeFile(String fileName) {
    FileInfo fileInfo = secondaryStorageIndex.remove(fileName);
    if (fileInfo != null) {
      Arrays.fill(secondaryStorage, fileInfo.startIndex, fileInfo.startIndex + fileInfo.size, (byte) 0);
    }
  }

  public void createFile(String fileName) {
    secondaryStorageIndex.put(fileName, new FileInfo(0, 0));
  }

  public void openFile(String fileName) {
    FileInfo fileInfo = secondaryStorageIndex.get(fileName);
    if (fileInfo != null) {
      fileInfo.size = 0;
    }
  }

  public List<FileInfo> getFileList() {
    List<FileInfo> fileList = new ArrayList<>();
    for (Map.Entry<String, FileInfo> entry : secondaryStorageIndex.entrySet()) {
      String fileName = entry.getKey();
      FileInfo fileInfo = entry.getValue();
      fileList.add(new FileInfo(fileName, fileInfo.size)); // in bytes
    }
    return fileList;
  }

  // public void printAllFiles() {
  //   System.out.println("=== Stored Files ===");
  //   if (secondaryStorageIndex.isEmpty()) {
  //     System.out.println("No files stored in secondary memory.");
  //   } else {
  //     for (Map.Entry<String, FileInfo> entry : secondaryStorageIndex.entrySet()) {
  //       String fileName = entry.getKey();
  //       FileInfo fileInfo = entry.getValue();
  //       String fileContent = getFile(fileName);
  //       System.out.printf("File: %s, Size: %d bytes, Content: \"%s\"%n",
  //           fileName, fileInfo.size, fileContent);
  //     }
  //   }
  //   System.out.println("====================");
  // }

  // Auxiliary methods for memory management

  private MemoryAllocation allocateOSSpace(int size) {
    int osSpaceStart = kernelSize * KB;
    int osSpaceEnd = osSpaceStart + osSize * KB;
    for (int i = osSpaceStart; i < osSpaceEnd; i++) {
      if (mainMemory[i] == 0) {
        int j = i;
        while (j < i + size && j < osSpaceEnd && mainMemory[j] == 0) {
          j++;
        }
        if (j - i == size) {
          return new MemoryAllocation(i, size);
        }
      }
    }
    return null;
  }

  private boolean deallocateOSSpace(int startIndex, int size) {
    for (int i = startIndex; i < startIndex + size; i++) {
      mainMemory[i] = 0;
    }
    return true;
  }

  private int allocateSecondaryMemory(int size) {
    int freeSpace = 0;
    int startIndex = -1;

    for (int i = 0; i < secondaryStorage.length; i++) {
      if (secondaryStorage[i] == 0) {
        if (startIndex == -1) {
          startIndex = i;
        }
        freeSpace++;
        if (freeSpace == size) {
          return startIndex;
        }
      } else {
        freeSpace = 0;
        startIndex = -1;
      }
    }

    return -1;
  }

  // Loading from files

  public void loadConfigurationFromFile(String configFilePath, String fileType) {
    switch (fileType) {
      case "text":
        loadConfigFromTextFile(configFilePath);
        break;
      case "json":
        loadConfigFromJsonFile(configFilePath);
        break;
      case "xml":
        loadConfigFromXmlFile(configFilePath);
        break;
      default:
        throw new IllegalArgumentException("Invalid file type: " + fileType);
    }
    initializeMemory();
  }

  private void loadConfigFromTextFile(String configFilePath) {
    try {
      File configFile = new File(configFilePath);
      Scanner scanner = new Scanner(new FileReader(configFile));

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split("=");
        if (parts.length == 2) {
          String key = parts[0].trim();
          int value = Integer.parseInt(parts[1].trim());
          int updatedValues = 0;

          switch (key) {
            case "mainMemorySize":
              mainMemorySize = value;
              updatedValues++;
              break;
            case "virtualMemorySize":
              virtualMemorySize = value;
              updatedValues++;
              break;
            case "secondaryMemorySize":
              secondaryMemorySize = value;
              updatedValues++;
              break;
            case "kernelSize":
              kernelSize = value;
              updatedValues++;
              break;
            case "osSize":
              osSize = value;
              updatedValues++;
              break;
          }

          if (updatedValues == 5) {
            break;
          }
          scanner.close();
          throw new IllegalArgumentException("Invalid configuration file format. Not all values were updated.");
        }
      }

      scanner.close();
    } catch (IOException e) {
      System.out.println("Error loading configuration file: " + e.getMessage());
    }
  }

  private void loadConfigFromJsonFile(String configFilePath) {
    try {
      JSONParser parser = new JSONParser();
      Object obj = parser.parse(new FileReader(configFilePath));
      JSONObject jsonConfig = (JSONObject) obj;

      mainMemorySize = ((Long) jsonConfig.get("mainMemorySize")).intValue();
      virtualMemorySize = ((Long) jsonConfig.get("virtualMemorySize")).intValue();
      secondaryMemorySize = ((Long) jsonConfig.get("secondaryMemorySize")).intValue();
      kernelSize = ((Long) jsonConfig.get("kernelSize")).intValue();
      osSize = ((Long) jsonConfig.get("osSize")).intValue();

      if (jsonConfig.size() != 5) {
        throw new IllegalArgumentException("Invalid JSON configuration file format. Not all values were updated.");
      }

    } catch (IOException | ParseException e) {
      System.out.println("Error loading JSON configuration file: " + e.getMessage());
    }
  }

  private void loadConfigFromXmlFile(String configFilePath) {
    try {
      File xmlFile = new File(configFilePath);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(xmlFile);
      doc.getDocumentElement().normalize();

      NodeList nodeList = doc.getElementsByTagName("memoryConfig");
      Element configElement = (Element) nodeList.item(0);

      mainMemorySize = Integer.parseInt(getTagValue("mainMemorySize", configElement));
      virtualMemorySize = Integer.parseInt(getTagValue("virtualMemorySize", configElement));
      secondaryMemorySize = Integer.parseInt(getTagValue("secondaryMemorySize", configElement));
      kernelSize = Integer.parseInt(getTagValue("kernelSize", configElement));
      osSize = Integer.parseInt(getTagValue("osSize", configElement));

      // Check if all values were updated
      if (configElement.getChildNodes().getLength() != 5) {
        throw new IllegalArgumentException("Invalid XML configuration file format. Not all values were updated.");
      }

    } catch (Exception e) {
      System.out.println("Error loading XML configuration file: " + e.getMessage());
    }
  }

  private String getTagValue(String tag, Element element) {
    NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
    Node node = nodeList.item(0);
    return node.getNodeValue();
  }

  // borrar jeje

  // public void printMemoryMap() {
  //   System.out.println("Memory Map:");
  //   System.out.println("Kernel Space: 0 - " + (kernelSize * KB - 1));
  //   System.out.println("OS Space: " + (kernelSize * KB) + " - " + (userSpaceStart - 1));
  //   System.out.println("User Space: " + userSpaceStart + " - " + (mainMemorySize * KB - 1));
  //   System.out.println("\nAllocated Processes:");
  //   for (Map.Entry<String, MemoryAllocation> entry : mainMemoryIndex.entrySet()) {
  //     MemoryAllocation allocation = entry.getValue();
  //     System.out.println(
  //         entry.getKey() + ": " + allocation.startIndex + " - " + (allocation.startIndex + allocation.size - 1));
  //   }
  //   System.out.println("\nFree Spaces:");
  //   for (MemoryAllocation freeSpace : freeSpaces) {
  //     System.out.println(freeSpace.startIndex + " - " + (freeSpace.startIndex + freeSpace.size - 1));
  //   }
  // }

  // public void printAllBCPs() {
  //   // Print all BCPs and their positions of bytes in mainMemory
  //   for (Map.Entry<String, MemoryAllocation> entry : mainMemoryIndex.entrySet()) {
  //     String processName = entry.getKey();
  //     if (processName.endsWith("_bcp")) {
  //       MemoryAllocation allocation = entry.getValue();
  //       byte[] bcpBytes = new byte[allocation.size];
  //       System.arraycopy(mainMemory, allocation.startIndex, bcpBytes, 0, allocation.size);
  //       System.out.println("BCP for process " + processName + ": " + new String(bcpBytes) + " | Bytes: "
  //           + allocation.startIndex + " - " + (allocation.startIndex + allocation.size - 1));
  //     }
  //   }
  // }

  // Accesors

  public int getMainMemorySize() {
    return mainMemorySize;
  }

  public int getVirtualMemorySize() {
    return virtualMemorySize;
  }

  public int getSecondaryMemorySize() {
    return secondaryMemorySize;
  }

  public int getKernelSize() {
    return kernelSize;
  }

  public int getOsSize() {
    return osSize;
  }

  public void setMainMemorySize(int mainMemorySize) {
    this.mainMemorySize = mainMemorySize;
  }

  public void setVirtualMemorySize(int virtualMemorySize) {
    this.virtualMemorySize = virtualMemorySize;
  }

  public void setSecondaryMemorySize(int secondaryMemorySize) {
    this.secondaryMemorySize = secondaryMemorySize;
  }

  public void setKernelSize(int kernelSize) {
    this.kernelSize = kernelSize;
  }

  public void setOsSize(int osSize) {
    this.osSize = osSize;
  }

  public void setVirtualMemory(String[] virtualMemory) {
    this.virtualMemory = virtualMemory;
  }

  public void setSecondaryStorage(byte[] secondaryStorage) {
    this.secondaryStorage = secondaryStorage;
  }

  public String[] getVirtualMemory() {
    return virtualMemory;
  }

  public byte[] getSecondaryStorage() {
    return secondaryStorage;
  }

  public void clearMemory() {
    initializeMemory();
  }

  private static class InstructionIndex {
    int startIndex;
    int length;

    InstructionIndex(int startIndex, int length) {
      this.startIndex = startIndex;
      this.length = length;
    }
  }

  private static class StringAllocation {
    int startIndex;
    int length;

    StringAllocation(int startIndex, int length) {
      this.startIndex = startIndex;
      this.length = length;
    }
  }
}