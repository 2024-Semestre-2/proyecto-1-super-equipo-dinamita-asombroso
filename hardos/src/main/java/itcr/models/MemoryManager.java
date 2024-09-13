package itcr.models;

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
  private static final int DEFAULT_MAIN_MEMORY_SIZE = 2000;
  private static final int DEFAULT_VIRTUAL_MEMORY_SIZE = 64;
  private static final int DEFAULT_SECONDARY_STORAGE_SIZE = 2000;
  private static final int KB = 1024;

  private int mainMemorySize;
  private int virtualMemorySize;
  private int secondaryMemorySize;

  private String[] virtualMemory;
  private byte[] mainMemory;
  private byte[] secondaryStorage;

  private Map<String, MemoryAllocation> mainMemoryIndex;
  private Map<String, FileInfo> secondaryStorageIndex;
  private Map<String, List<MemoryAllocation>> processInstructions;

  private int kernelSize;
  private int osSize;
  private int userSpaceSize;

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
    this.userSpaceSize = mainMemorySize - kernelSize - osSize;
    initializeMemory();
  }

  private void initializeMemory() {
    this.mainMemory = new byte[mainMemorySize * KB];
    this.secondaryStorage = new byte[secondaryMemorySize * KB];
    virtualMemory = new String[virtualMemorySize * KB];

    this.mainMemoryIndex = new HashMap<>();
    this.secondaryStorageIndex = new HashMap<>();
    this.processInstructions = new HashMap<>();
  }

  public void printAllInstructions(String processName) {
    List<MemoryAllocation> allocations = processInstructions.get(processName);
    if (allocations != null && !allocations.isEmpty()) {
      System.out.println("All stored instructions for process " + processName + ":");
      for (int i = 0; i < allocations.size(); i++) {
        String instruction = getInstruction(processName, i);
        String instructionAddress = String.format("%04X", allocations.get(i).startIndex);
        String instructionSize = String.format("%04d", allocations.get(i).size);
        String instructionAddressDecimal = String.format("%04d", allocations.get(i).startIndex);
        System.out.println("Instruction " + i + ": " + instruction + " | Address: " + instructionAddress + " | Size: "
            + instructionSize + " | Decimal Address: " + instructionAddressDecimal);
      }
    } else {
      System.out.println("No instructions stored for process " + processName);
    }
  }

  public void printFirstTenKernelBytes() {
    System.out.println("First ten kernel bytes:");
    for (int i = 0; i < 10; i++) {
      System.out.print(mainMemory[i] + " ");
    }
    System.out.println();
  }

  public void printFirstTenOSBytes() {
    int osSpaceStart = kernelSize * KB;
    int osSpaceEnd = osSpaceStart + 100;
    
    for (int i = osSpaceStart; i < osSpaceEnd; i++) {
      System.out.print(mainMemory[i] + " ");
    }
    System.out.println();
  }

  // Main memory management methods

  public boolean storeInstruction(String processName, String instruction) {
    byte[] instructionBytes = instruction.getBytes();
    MemoryAllocation allocation = allocateUserSpace(instructionBytes.length);
    if (allocation != null) {
      System.arraycopy(instructionBytes, 0, mainMemory, allocation.startIndex, instructionBytes.length);
      processInstructions.computeIfAbsent(processName, k -> new ArrayList<>()).add(allocation);
      return true;
    }
    return false;
  }

  public boolean storeNumber(String processName, int number) {
    MemoryAllocation allocation = allocateUserSpace(Integer.BYTES);
    if (allocation != null) {
      ByteBuffer.wrap(mainMemory, allocation.startIndex, Integer.BYTES).putInt(number);
      String key = processName + "_number";
      mainMemoryIndex.put(key, allocation);
      return true;
    }
    return false;
  }

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

  public String getInstruction(String processName, int index) {
    List<MemoryAllocation> allocations = processInstructions.get(processName);
    if (allocations != null && index >= 0 && index < allocations.size()) {
      MemoryAllocation allocation = allocations.get(index);
      byte[] instructionBytes = new byte[allocation.size];
      System.arraycopy(mainMemory, allocation.startIndex, instructionBytes, 0, allocation.size);
      return new String(instructionBytes);
    }
    return null;
  }

  public Integer getNumber(String processName, int index) {
    String key = processName + "_number";
    MemoryAllocation allocation = mainMemoryIndex.get(key);
    if (allocation != null) {
      return ByteBuffer.wrap(mainMemory, allocation.startIndex, Integer.BYTES).getInt();
    }
    return null;
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

  public List<FileInfo> getFileList() {
    List<FileInfo> fileList = new ArrayList<>();
    for (Map.Entry<String, FileInfo> entry : secondaryStorageIndex.entrySet()) {
      String fileName = entry.getKey();
      FileInfo fileInfo = entry.getValue();
      fileList.add(new FileInfo(fileName, fileInfo.size)); // in bytes
    }
    return fileList;
  }

  // Auxiliary methods for memory management

  private MemoryAllocation allocateUserSpace(int size) {
    int userSpaceStart = (kernelSize + osSize) * KB;
    for (int i = userSpaceStart; i < mainMemory.length; i++) {
      if (mainMemory[i] == 0) {
        int j = i;
        while (j < i + size && j < mainMemory.length && mainMemory[j] == 0) {
          j++;
        }
        if (j - i == size) {
          return new MemoryAllocation(i, size);
        }
      }
    }
    return null;
  }

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
}