package itcr.controllers;

import itcr.graphics.FileExplorer;
import itcr.graphics.MyPcConfig;
import itcr.graphics.Hard8086;
import itcr.model.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class DesktopScreenController {
  private JFrame parent;
  private MemoryManager memoryManager;
  private Scheduler scheduler;

  public DesktopScreenController(JFrame parent) {
    this.parent = parent;
    CPU cpu = new CPU();
    this.memoryManager = new MemoryManager();
    this.scheduler = new Scheduler(cpu, memoryManager);
    this.loadInitialFilesInMemory();
  }

  public void loadInitialFilesInMemory() {
    
    String[] fileNames = { "createfile.asm", "createfile1.asm", "createfile2.asm", "createfile3.asm", "createfile5.asm", "createfile4.asm", "createfile7.asm", "createfile6.asm" };
    String[] fileContent = { "INT _09H\nINT _10H" , 
                             "MOV AX, 1\nINT _08H\nINT _21H",
                             "MOV AX, 3\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nPUSH AX\nPUSH AX\nPUSH AX\nPUSH AX\nPUSH AX\nPUSH AX\nPOP BX\nPUSH AX" ,
                             "MOV AX, 5\nMOV BX, AX\nMOV AX, 10\nMOV DX, BX",
                             "MOV AX, 1\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nMOV DX, BX" , 
                             "MOV AX, 2\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nPUSH AX\nMOV CX, 100",
                             "MOV AX, 3\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nMOV DX, BX" ,
                             "MOV AX, 5\nMOV BX, AX\nMOV AX, 10\nPUSH AX\nMOV BX, 100\nMOV DX, BX"  
                            };
    
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

  public void openHard8086() {
    Hard8086 hard8086 = new Hard8086(parent, scheduler);
    hard8086.setVisible(true);
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