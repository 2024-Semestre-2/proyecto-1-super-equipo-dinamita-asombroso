package itcr.controllers;

import itcr.graphics.FileExplorer;
import itcr.graphics.MyPcConfig;
import itcr.models.MemoryManager;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class DesktopScreenController {
  private JFrame parent;
  private MemoryManager memoryManager;

  public DesktopScreenController(JFrame parent) {
    this.parent = parent;
    this.memoryManager = new MemoryManager();
    this.loadInitialFilesInMemory();
  }

  public void loadInitialFilesInMemory() {
    String[] fileNames = { "file1.txt", "file2.txt"};
    String[] fileContent = { "Contenido del archivo 1", "Contenido del archivo 2" };

    boolean allFilesStored = false;

    for (int i = 0; i < fileNames.length; i++) {
      allFilesStored = memoryManager.storeFile(fileNames[i], fileContent[i]);
    }

    if (!allFilesStored) {
      JOptionPane.showMessageDialog(null, "No se pudieron cargar todos los archivos en memoria");
    }
  }

  public void openMyComputer() {
    MyPcConfigController myPcConfigController = new MyPcConfigController(this, memoryManager);
    MyPcConfig myPcConfig = new MyPcConfig(parent, myPcConfigController);
    myPcConfig.setVisible(true);
  }

  public void openRecycleBin() {
    JOptionPane.showMessageDialog(null, "Abriendo Papelera de Reciclaje");
  }

  public void openFileExplorer() {
    FileExplorerController fileExplorerController = new FileExplorerController(memoryManager);
    FileExplorer fileExplorer = new FileExplorer(parent, fileExplorerController);
    fileExplorer.setVisible(true);
  }

  public void loadConfigurationFromFile(String configFilePath, String fileType) {
    try {
      memoryManager.loadConfigurationFromFile(configFilePath, fileType);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error al cargar la configuración: " + e.getMessage());
    }
  }

  public String validateConfiguration(int kernelSize, int osSize, int mainMemorySize, int secondaryMemorySize,
      int virtualMemorySize) {
    if (kernelSize < 0 || osSize < 0 || mainMemorySize < 0 || secondaryMemorySize < 0 || virtualMemorySize < 0) {
      return "Los tamaños no pueden ser negativos";
    }

    if (kernelSize + osSize + virtualMemorySize > mainMemorySize) {
      return "El tamaño del kernel, sistema operativo y memoria virtual no pueden ser mayor al tamaño de la memoria principal";
    }

    return null;
  }

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