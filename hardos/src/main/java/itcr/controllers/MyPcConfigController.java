package itcr.controllers;

import itcr.model.MemoryManager;

/**
 * The MyPcConfigController class manages the configuration settings for the PC,
 * including memory sizes and loading configurations from files.
 */
public class MyPcConfigController {
  private final MemoryManager memoryManager;
  public DesktopScreenController desktopScreenControllerRef = null;

  /**
   * Constructs a MyPcConfigController with the specified MemoryManager.
   *
   * @param memoryManager the memory manager
   */
  public MyPcConfigController(MemoryManager memoryManager) {
    this.memoryManager = memoryManager;
  }

  /**
   * Retrieves the kernel size.
   *
   * @return the kernel size
   */
  public int getKernelSize() {
    return memoryManager.getKernelSize();
  }

  /**
   * Sets the kernel size.
   *
   * @param kernelSize the new kernel size
   */
  public void setKernelSize(int kernelSize) {
    memoryManager.setKernelSize(kernelSize);
  }

  /**
   * Retrieves the operating system size.
   *
   * @return the operating system size
   */
  public int getOsSize() {
    return memoryManager.getOsSize();
  }

  /**
   * Sets the operating system size.
   *
   * @param osSize the new operating system size
   */
  public void setOsSize(int osSize) {
    memoryManager.setOsSize(osSize);
  }

  /**
   * Retrieves the main memory size.
   *
   * @return the main memory size
   */
  public int getMainMemorySize() {
    return memoryManager.getMainMemorySize();
  }

  /**
   * Sets the main memory size.
   *
   * @param mainMemorySize the new main memory size
   */
  public void setMainMemorySize(int mainMemorySize) {
    memoryManager.setMainMemorySize(mainMemorySize);
  }

  /**
   * Retrieves the secondary memory size.
   *
   * @return the secondary memory size
   */
  public int getSecondaryMemorySize() {
    return memoryManager.getSecondaryMemorySize();
  }

  /**
   * Sets the secondary memory size.
   *
   * @param secondaryMemorySize the new secondary memory size
   */
  public void setSecondaryMemorySize(int secondaryMemorySize) {
    memoryManager.setSecondaryMemorySize(secondaryMemorySize);
  }

  /**
   * Retrieves the virtual memory size.
   *
   * @return the virtual memory size
   */
  public int getVirtualMemorySize() {
    return memoryManager.getVirtualMemorySize();
  }

  /**
   * Sets the virtual memory size.
   *
   * @param virtualMemorySize the new virtual memory size
   */
  public void setVirtualMemorySize(int virtualMemorySize) {
    memoryManager.setVirtualMemorySize(virtualMemorySize);
  }

  /**
   * Validates the memory configuration.
   *
   * @param kernelSize the size of the kernel
   * @param osSize the size of the operating system
   * @param mainMemorySize the size of the main memory
   * @param secondaryMemorySize the size of the secondary memory
   * @param virtualMemorySize the size of the virtual memory
   * @return an error message if the configuration is invalid, otherwise null
   */
  public String validateConfiguration(int kernelSize, int osSize, int mainMemorySize, int secondaryMemorySize, int virtualMemorySize, int numCPUs) {
    if (kernelSize < 0 || osSize < 0 || mainMemorySize < 0 || secondaryMemorySize < 0 || virtualMemorySize < 0) {
      return "Los tamaños no pueden ser negativos";
    }

    if (kernelSize + osSize + virtualMemorySize > mainMemorySize) {
      return "El tamaño del kernel, sistema operativo y memoria virtual no pueden ser mayor al tamaño de la memoria principal";
    }

    if (numCPUs < 1) {
      return "Debe haber al menos un CPU";
    }

    return null;
  }

  /**
   * Loads the configuration from a file.
   *
   * @param configFilePath the path to the configuration file
   * @param fileType the type of the configuration file
   */
  public void loadConfigurationFromFile(String configFilePath, String fileType) {
    try {
      memoryManager.loadConfigurationFromFile(configFilePath, fileType);
      desktopScreenControllerRef.loadInitialFilesInMemory();
    } catch (Exception e) {
      throw new RuntimeException("Error loading configuration: " + e.getMessage(), e);
    }
  }

  public int getNumCPUs() {
    return this.desktopScreenControllerRef.getNumCpus();
  }

  public void setNumCpus(int numCPUs) {
    this.desktopScreenControllerRef.setNumCpus(numCPUs);
  }
}