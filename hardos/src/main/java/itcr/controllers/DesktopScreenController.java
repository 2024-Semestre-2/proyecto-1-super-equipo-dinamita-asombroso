package itcr.controllers;

import itcr.graphics.FileExplorer;
import itcr.graphics.MyPcConfig;
import itcr.graphics.Hard8086;
import itcr.model.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The DesktopScreenController class manages the main desktop screen functionalities,
 * including loading initial files into memory, opening various system components,
 * and validating memory configurations.
 */
public class DesktopScreenController {
  private final JFrame parent;
  private final MemoryManager memoryManager;
  private final Scheduler scheduler;

  /**
   * Constructs a DesktopScreenController with the specified parent JFrame.
   *
   * @param parent the parent JFrame
   */
  public DesktopScreenController(JFrame parent) {
    this.parent = parent;
    CPU cpu = new CPU();
    this.memoryManager = new MemoryManager();
    this.scheduler = new Scheduler(cpu, memoryManager);
    loadInitialFilesInMemory();
  }

  /**
   * Loads initial files into memory. For now they are only "assembly" files.
   */
  private void loadInitialFilesInMemory() {
    String[] fileNames = {
      "createfile.asm", "createfile1.asm", "createfile2.asm", "createfile3.asm",
      "createfile5.asm", "createfile4.asm", "createfile7.asm", "createfile6.asm"
    };
    String[] fileContent = {
      "MOV AX, 3\nINT _08H\nMOV CX, BX\nINT _08H\nINT _21H",
      "MOV AX, 1\nINT _08H\nINT _21H",
      "MOV AX, 3\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nPUSH AX\nPUSH AX\nPUSH AX\nPUSH AX\nPUSH AX\nPUSH AX\nPOP BX\nPUSH AX",
      "MOV AX, 5\nMOV BX, AX\nMOV AX, 10\nMOV DX, BX",
      "MOV AX, 1\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nMOV DX, BX",
      "MOV AX, 2\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nPUSH AX\nMOV CX, 100",
      "MOV AX, 3\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nMOV DX, BX",
      "MOV AX, 5\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nMOV DX, BX"
    };

    boolean allFilesStored = true;

    for (int i = 0; i < fileNames.length; i++) {
      if (!memoryManager.storeFile(fileNames[i], fileContent[i])) {
        allFilesStored = false;
      }
    }

    if (!allFilesStored) {
      JOptionPane.showMessageDialog(null, "No se pudieron cargar todos los archivos en memoria");
    }
  }

  /**
   * Opens the "My Computer" configuration window.
   */
  public void openMyComputer() {
    MyPcConfigController myPcConfigController = new MyPcConfigController(memoryManager);
    MyPcConfig myPcConfig = new MyPcConfig(parent, myPcConfigController);
    myPcConfig.setVisible(true);
  }

  /**
   * Opens the Recycle Bin.
   */
  public void openRecycleBin() {
    JOptionPane.showMessageDialog(null, "Abriendo Papelera de Reciclaje");
  }

  /**
   * Opens the File Explorer.
   */
  public void openFileExplorer() {
    FileExplorerController fileExplorerController = new FileExplorerController(memoryManager);
    FileExplorer fileExplorer = new FileExplorer(parent, fileExplorerController);
    fileExplorer.setVisible(true);
  }

  /**
   * Opens the Hard8086 emulator.
   */
  public void openHard8086() {
    Hard8086 hard8086 = new Hard8086(parent, scheduler);
    hard8086.setVisible(true);
  }

  /**
   * Loads configuration from a file.
   *
   * @param configFilePath the path to the configuration file
   * @param fileType the type of the configuration file
   */
  public void loadConfigurationFromFile(String configFilePath, String fileType) {
    try {
      memoryManager.loadConfigurationFromFile(configFilePath, fileType);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error al cargar la configuración: " + e.getMessage());
    }
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
  public String validateConfiguration(int kernelSize, int osSize, int mainMemorySize, int secondaryMemorySize, int virtualMemorySize) {
    if (kernelSize < 0 || osSize < 0 || mainMemorySize < 0 || secondaryMemorySize < 0 || virtualMemorySize < 0) {
      return "Los tamaños no pueden ser negativos";
    }

    if (kernelSize + osSize + virtualMemorySize > mainMemorySize) {
      return "El tamaño del kernel, sistema operativo y memoria virtual no pueden ser mayor al tamaño de la memoria principal";
    }

    return null;
  }

  // Getters and setters

  public int getKernelSize() {
    return memoryManager.getKernelSize();
  }

  public void setKernelSize(int kernelSize) {
    memoryManager.setKernelSize(kernelSize);
  }

  public int getOsSize() {
    return memoryManager.getOsSize();
  }

  public void setOsSize(int osSize) {
    memoryManager.setOsSize(osSize);
  }

  public int getMainMemorySize() {
    return memoryManager.getMainMemorySize();
  }

  public void setMainMemorySize(int mainMemorySize) {
    memoryManager.setMainMemorySize(mainMemorySize);
  }

  public int getSecondaryMemorySize() {
    return memoryManager.getSecondaryMemorySize();
  }

  public void setSecondaryMemorySize(int secondaryMemorySize) {
    memoryManager.setSecondaryMemorySize(secondaryMemorySize);
  }

  public int getVirtualMemorySize() {
    return memoryManager.getVirtualMemorySize();
  }

  public void setVirtualMemorySize(int virtualMemorySize) {
    memoryManager.setVirtualMemorySize(virtualMemorySize);
  }
}