package itcr.controllers;

import itcr.graphics.FileExplorer;
import itcr.graphics.MyPcConfig;
import itcr.graphics.Hard8086;
import itcr.model.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The DesktopScreenController class manages the main desktop screen
 * functionalities,
 * including loading initial files into memory, opening various system
 * components,
 * and validating memory configurations.
 */
public class DesktopScreenController {
  private final JFrame parent;
  private MemoryManager memoryManager;
  private Scheduler scheduler;

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
  public void loadInitialFilesInMemory() {
    String[] fileNames = {
        "suma_numeros.asm",
        "contador_descendente.asm",
        "calculo_sumatoria.asm",
        "manejo_archivos.asm",
        "comparacion_numeros.asm",
        "entrada_salida.asm",
        "operaciones_pila.asm",
        "saltos_condicionales.asm"
    };

    String[] fileContent = {
        // suma_numeros.asm
        "// Suma dos números ingresados por el usuario\n" +
            "INT _09H\n" +
            "ADD DX\n" +
            "INT _09H\n" +
            "ADD DX\n" +
            "STORE DX\n" +
            "INT _10H\n" +
            "INT _20H",

        // contador_descendente.asm
        "// Cuenta desde 10 hasta 0\n" +
            "MOV AX, 10\n" +
            "MOV BX, 0\n" +
            "MOV DX, AX\n" +
            "INT _10H\n" +
            "DEC AX\n" +
            "MOV DX, AX\n" +
            "CMP AX, BX\n" +
            "JNE -5\n" +
            "INT _20H",

        // calculo_sumatoria.asm
        "// Calcula la sumatoria desde 1 hasta AX\n" +
            "INT _09H\n" +
            "MOV AX, DX\n" + // el usuario ingresa el valor de AX SUMATORIA(AX) = 1 + 2 + 3 + ... + AX
            "MOV CX, AX\n" + // guardamos el valor de AX en CX para usarlo en el ciclo
            "MOV BX, 1\n" + // el numero que se va a sumar
            "ADD BX\n" + // Usamos BX para sumar al ACUMULADOR (donde se guarda la sumatoria)
            "CMP BX, CX\n" + // comparamos si ya llegamos al valor de AX
            "JE +3\n" + // si es igual, terminamos
            "INC BX\n" + // si no, incrementamos BX
            "JMP -4\n" + // volvemos al ciclo
            "STORE DX\n" + // para imprimir el resultado
            "INT _10H\n" + // imprimimos el resultado
            "INT _20H", // terminamos

        // manejo_archivos.asm
        "// Crea, escribe, lee y elimina un archivo\n" +
            "MOV AX, 0\n" +
            "INT _08H\n" +
            "INT _21H\n" +
            "MOV AX, 3\n" +
            "MOV CX, BX\n" +
            "INT _08H\n" +
            "INT _21H\n" +
            "MOV AX, 2\n" +
            "INT _21H\n" +
            "MOV AX, 5\n" +
            "INT _21H\n" +
            "INT _20H",

        // comparacion_numeros.asm
        "// Compara dos números e imprime el mayor\n" +
            "INT _09H\n" +
            "MOV AX, DX\n" +
            "INT _09H\n" +
            "MOV BX, DX\n" +
            "CMP AX, BX\n" +
            "JE +4\n" +
            "JNE +2\n" +
            "MOV DX, AX\n" +
            "JMP +2\n" +
            "MOV DX, BX\n" +
            "INT _10H\n" +
            "INT _20H",

        // entrada_salida.asm
        "// Lee una cadena y la imprime\n" +
            "INT _08H\n" +
            "MOV AX, 2\n" +
            "MOV CX, BX\n" +
            "INT _21H\n" +
            "INT _20H",

        // operaciones_pila.asm
        "// Realiza operaciones usando la pila\n" +
            "MOV AX, 5\n" +
            "PUSH AX\n" +
            "MOV AX, 10\n" +
            "PUSH AX\n" +
            "POP BX\n" +
            "POP AX\n" +
            "ADD BX\n" +
            "MOV DX, AX\n" +
            "INT _10H\n" +
            "INT _20H",

        // saltos_condicionales.asm
        "// Demuestra el uso de saltos condicionales\n" +
            "MOV AX, 0\n" +
            "MOV BX, 5\n" +
            "CMP AX, BX\n" +
            "JE +4\n" +
            "INC AX\n" +
            "CMP AX, BX\n" +
            "JNE -3\n" +
            "MOV DX, AX\n" +
            "INT _10H\n" +
            "INT _20H"
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
    myPcConfigController.desktopScreenControllerRef = this;
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
    hard8086.desktopScreenControllerRef = this;
    hard8086.setVisible(true);
  }

  /**
   * Loads configuration from a file.
   *
   * @param configFilePath the path to the configuration file
   * @param fileType       the type of the configuration file
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
   * @param kernelSize          the size of the kernel
   * @param osSize              the size of the operating system
   * @param mainMemorySize      the size of the main memory
   * @param secondaryMemorySize the size of the secondary memory
   * @param virtualMemorySize   the size of the virtual memory
   * @return an error message if the configuration is invalid, otherwise null
   */
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

  public void changeScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
    this.memoryManager = scheduler.cpu.memory;
  }

  public void updateMemorySize(int kernelSize, int osSize, int mainMemorySize, int secondaryMemorySize,
      int virtualMemorySize) {
    memoryManager.setKernelSize(kernelSize);
    memoryManager.setOsSize(osSize);
    memoryManager.setMainMemorySize(mainMemorySize);
    memoryManager.setSecondaryMemorySize(secondaryMemorySize);
    memoryManager.setVirtualMemorySize(virtualMemorySize);
  }
}