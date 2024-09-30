package itcr.graphics;

import itcr.controllers.DesktopScreenController;
import itcr.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.google.gson.JsonObject;

/**
 * Hard8086 class represents a custom floating window that simulates the
 * Hard8086 environment.
 * It includes registers, a console, and a memory map.
 */
public class Hard8086 extends FloatingWindow<Scheduler> {
  private Map<Integer, JTextArea> registersAreas;
  private JTextArea consoleArea;
  private JTextField inputField;
  private JTree memoryMapTree;
  private ExecutorService interruptExecutor;
  private final int NUM_CORES = 5;
  private int numCPUs = 1;
  private boolean firstStep = true;
  public DesktopScreenController desktopScreenControllerRef = null;
  private StatsTab statsPanel;

  // styling
  private static final Color BACKGROUND_COLOR = new Color(240, 240, 245);
  private static final Color ACCENT_COLOR = new Color(70, 130, 180);
  private static final Color TEXT_COLOR = new Color(50, 50, 50);
  private static final Color BUTTON_COLOR = new Color(100, 149, 237);
  private static final Color CONSOLE_BG_COLOR = new Color(25, 25, 25);
  private static final Color CONSOLE_TEXT_COLOR = new Color(0, 255, 0);

  private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
  private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
  private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
  private static final Font CONSOLE_FONT = new Font("Consolas", Font.PLAIN, 14);
  private static final Font REGISTER_FONT = new Font("Consolas", Font.PLAIN, 13);

  /**
   * Constructor for Hard8086.
   * Initializes the Hard8086 simulator window.
   *
   * @param parent    the parent frame
   * @param scheduler the scheduler to manage the simulation
   */
  public Hard8086(JFrame parent, Scheduler scheduler) {
    super(parent, "Hard8086 Simulator", scheduler);
    setSize(950, 600);
    this.numCPUs = scheduler.getNumCPUs();

    interruptExecutor = Executors.newSingleThreadExecutor();
    startInterruptHandler();
  }

  /**
   * Initializes the components of the Hard8086 simulator.
   */
  @Override
  protected void initComponents() {
    if (this.registersAreas == null) {
      this.registersAreas = new HashMap<>();
    }

    setBackground(BACKGROUND_COLOR);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setFont(LABEL_FONT);
    tabbedPane.setBackground(BACKGROUND_COLOR);

    JPanel mainPanel = createMainTab();
    tabbedPane.addTab("Main", mainPanel);

    StatsTab statsPanel = new StatsTab();
    tabbedPane.addTab("Stats", statsPanel);
    this.statsPanel = statsPanel;

    add(tabbedPane, BorderLayout.CENTER);
    updateRegistersDisplay();
  }

  private JPanel createMainTab() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBackground(BACKGROUND_COLOR);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    if (this.numCPUs < 1) {
      if (controller.getNumCPUs() < 1) {
        this.numCPUs = 1;
      } else {
        this.numCPUs = controller.getNumCPUs();
      }
    }

    // Left Panel (Registers and Console)
    JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
    leftPanel.setBackground(BACKGROUND_COLOR);

    // Registers Panel
    JPanel registersPanel = new JPanel(new GridLayout(numCPUs, NUM_CORES, 5, 5));
    registersPanel.setBackground(BACKGROUND_COLOR);

    for (int cpu = 0; cpu < numCPUs; cpu++) {
      for (int core = 0; core < NUM_CORES; core++) {
        JTextArea registerArea = new JTextArea(10, 10);
        registerArea.setEditable(false);
        registerArea.setFont(REGISTER_FONT);
        registerArea.setBackground(Color.WHITE);
        registerArea.setForeground(TEXT_COLOR);

        int key = cpu * NUM_CORES + core;
        this.registersAreas.put(key, registerArea);

        JScrollPane scrollPane = new JScrollPane(registerArea);
        scrollPane.setBorder(createStyledBorder("CPU " + cpu + " Core " + core));
        registersPanel.add(scrollPane);
      }
    }

    leftPanel.add(registersPanel, BorderLayout.NORTH);

    // Console Area
    consoleArea = new JTextArea(10, 50);
    consoleArea.setEditable(false);
    consoleArea.setFont(CONSOLE_FONT);
    consoleArea.setBackground(CONSOLE_BG_COLOR);
    consoleArea.setForeground(CONSOLE_TEXT_COLOR);
    consoleArea.setCaretColor(CONSOLE_TEXT_COLOR);
    JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
    consoleScrollPane.setBorder(createStyledBorder("Console"));
    leftPanel.add(consoleScrollPane, BorderLayout.CENTER);

    // Right Panel (Memory Map)
    memoryMapTree = new JTree();
    memoryMapTree.setRootVisible(false);
    memoryMapTree.setFont(LABEL_FONT);
    memoryMapTree.setBackground(Color.WHITE);
    expandAllNodes(memoryMapTree, 0, -1);
    JScrollPane memoryMapScrollPane = new JScrollPane(memoryMapTree);
    memoryMapScrollPane.setBorder(createStyledBorder("Memory Map"));

    // Split Pane
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, memoryMapScrollPane);
    splitPane.setResizeWeight(0.6);
    splitPane.setBackground(BACKGROUND_COLOR);
    mainPanel.add(splitPane, BorderLayout.CENTER);

    // Control Panel
    JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
    controlPanel.setBackground(BACKGROUND_COLOR);

    // Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    buttonPanel.setBackground(BACKGROUND_COLOR);
    String[] buttonLabels = { "Execute", "Step", "Clean", "Load Files", "Update Memory Map" };
    for (String label : buttonLabels) {
      JButton button = createStyledButton(label);
      buttonPanel.add(button);
    }

    controlPanel.add(buttonPanel, BorderLayout.SOUTH);
    mainPanel.add(controlPanel, BorderLayout.SOUTH);

    updateRegistersDisplay();
    updateMemoryMap();

    return mainPanel;
  }

  private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setFont(BUTTON_FONT);
    button.setBackground(BUTTON_COLOR);
    button.setForeground(Color.WHITE);
    button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    button.setFocusPainted(false);
    button.addActionListener(this::handleButtonAction);
    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        button.setBackground(BUTTON_COLOR.brighter());
      }

      @Override
      public void mouseExited(MouseEvent e) {
        button.setBackground(BUTTON_COLOR);
      }
    });
    return button;
  }

  private Border createStyledBorder(String title) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
        BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            TITLE_FONT,
            ACCENT_COLOR));
  }

  /**
   * Updates the display of registers.
   */
  private void updateRegistersDisplay() {

    if (registersAreas.isEmpty()) {
      return;
    }

    int totalCores = controller.getTotalCores();
    for (int i = 0; i < totalCores; i++) {
      int cpuId = i / NUM_CORES;
      int coreId = i % NUM_CORES;

      JTextArea area = registersAreas.get(i);
      if (area != null) {
        String registers = controller.getRegisters(cpuId, coreId);
        String extraRegisters = controller.getExtraRegisters(cpuId, coreId);
        area.setText(registers + "\n" + extraRegisters);
      }
    }
  }

  // Handle button actions
  private void handleButtonAction(ActionEvent e) {
    Object source = e.getSource();
    if (source instanceof JButton) {
      JButton button = (JButton) source;
      switch (button.getText()) {
        case "Execute":
          executeAllInstructions();
          break;
        case "Step":
          executeNextInstruction();
          break;
        case "Clean":
          cleanConsole();
          break;
        case "Load Files":
          loadFiles();
          break;
        case "Update Memory Map":
          updateMemoryMap();
          break;
      }
    }
  }

  private void updateStatsTab() {
    SwingUtilities.invokeLater(() -> {
      Map<Integer, Map<String, JsonObject>> allStats = controller.getAllCPUStats();
      statsPanel.updateStats(allStats);
    });
  }

  /**
   * Updates the memory map displayed in the memory map tree.
   */
  private void updateMemoryMap() {
    MemoryMap memoryMap = controller.memoryManager.getMainMemoryMap();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Memory");

    addMemorySection(root, memoryMap.kernel);
    addMemorySection(root, memoryMap.os);
    addMemorySection(root, memoryMap.userSpace);
    addMemorySection(root, memoryMap.secondaryStorage);

    DefaultMutableTreeNode processesNode = new DefaultMutableTreeNode("Allocated Processes");
    for (MemoryMap.MemorySection process : memoryMap.allocatedProcesses) {
      addMemorySection(processesNode, process);
    }
    root.add(processesNode);

    DefaultMutableTreeNode freeSpacesNode = new DefaultMutableTreeNode("Free Spaces");
    for (MemoryMap.MemorySection freeSpace : memoryMap.freeSpaces) {
      addMemorySection(freeSpacesNode, freeSpace);
    }
    root.add(freeSpacesNode);

    DefaultMutableTreeNode stacksNode = new DefaultMutableTreeNode("Allocated Stacks");
    for (MemoryMap.MemorySection stack : memoryMap.allocatedStacks) {
      addMemorySection(stacksNode, stack);
    }
    root.add(stacksNode);

    DefaultMutableTreeNode stringsNode = new DefaultMutableTreeNode("Stored Strings");
    for (MemoryMap.MemorySection string : memoryMap.storedStrings) {
      addMemorySection(stringsNode, string);
    }
    root.add(stringsNode);

    DefaultMutableTreeNode filesNode = new DefaultMutableTreeNode("Stored Files");
    for (MemoryMap.MemorySection file : memoryMap.storedFiles) {
      addMemorySection(filesNode, file);
    }
    root.add(filesNode);

    memoryMapTree.setModel(new DefaultTreeModel(root));
    expandAllNodes(memoryMapTree, 0, -1);
  }

  /**
   * Adds a memory section to the parent node in the memory map tree.
   *
   * @param parent  the parent node
   * @param section the memory section to add
   */
  private void addMemorySection(DefaultMutableTreeNode parent, MemoryMap.MemorySection section) {
    StringBuilder nodeText = new StringBuilder();
    nodeText.append(String.format("%s (%d - %d)", section.name, section.start, section.end));

    if (section.additionalInfo != null && !section.additionalInfo.isEmpty()) {
      nodeText.append(" | ").append(section.additionalInfo);
    }

    DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeText.toString());
    parent.add(node);

    for (MemoryMap.MemorySection subSection : section.subSections) {
      addMemorySection(node, subSection);
    }
  }

  /**
   * Loads files into the memory.
   */
  private void loadFiles() {
    FileLoaderDialog dialog = new FileLoaderDialog((JFrame) SwingUtilities.getWindowAncestor(this), controller);
    dialog.setVisible(true);
    java.util.List<String> selectedFiles = dialog.getSelectedFiles();

    if (!selectedFiles.isEmpty()) {
      StringBuilder sb = new StringBuilder("Selected files:\n");
      for (String fileName : selectedFiles) {
        String strInstructions = controller.memoryManager.getFile(fileName);
        String assemblerErrors = Assembler.validateFormat(strInstructions);
        if (assemblerErrors != null) {
          JOptionPane.showMessageDialog(this, assemblerErrors, "Error " + fileName, JOptionPane.ERROR_MESSAGE);
          return;
        }

        String[] instructions = strInstructions.split("\n");
        itcr.model.Process process = createProcess(instructions);
        for (String instruction : instructions) {
          controller.memoryManager.storeInstruction("P" + process.getProcessId(), instruction);
        }
        controller.addProcess(process);
        sb.append(fileName).append("\n");
      }
    }
  }

  /**
   * Creates a process from the given instructions.
   *
   * @param instructions the instructions for the process
   * @return the created process
   */
  private itcr.model.Process createProcess(String[] instructions) {
    int processSize = 0;
    for (String instruction : instructions) {
      if (instruction.substring(0, 2).equals("//")) {
        continue;
      }
      processSize += instruction.split("//")[0].length();
    }

    String processId = "P" + itcr.model.Process.processCounter;

    int baseAddress = this.controller.memoryManager.allocateMemory(processId, processSize);

    for (int i = 0; i < instructions.length; i++) {
      if (instructions[i].substring(0, 2).equals("//")) {
        continue;
      }

      controller.memoryManager.storeInstruction(processId, instructions[i].split("//")[0]);
    }

    int qtyInstructions = controller.memoryManager.getQtyInstructions(processId);
    itcr.model.Process process = new itcr.model.Process(qtyInstructions);

    ProcessControlBlock pcb = new ProcessControlBlock(itcr.model.Process.processCounter++, baseAddress, processSize, 1);
    if (!controller.memoryManager.storeBCP(processId, pcb.toJsonString())) {
      consoleArea.append("Error storing BCP for process " + processId + "\n");
    }
    process.setPCB(pcb);
    if (!controller.memoryManager.allocateStack(processId)) {
      consoleArea.append("Error allocating stack for process " + processId + "\n");
    }

    return process;
  }

  /**
   * Starts the interrupt handler.
   */
  private void startInterruptHandler() {
    interruptExecutor.submit(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          InterruptMessage message = InterruptQueue.takeMessage();
          handleInterrupt(message);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  /**
   * Handles an interrupt message.
   *
   * @param message the interrupt message
   */
  private void handleInterrupt(InterruptMessage message) {
    SwingUtilities.invokeLater(() -> {
      switch (message.getCode()) {
        case _10H:
          printToConsole(message.getMessage());
          break;
        case _09H:
        case _08H:
          String input = readFromConsole();
          UserInputHandler.provideInput(message.getProcessId(), input);
          updateRegistersDisplay();
          break;
        default:
          break;
      }
    });
  }

  /**
   * Executes all instructions.
   *
   * @param e the action event
   */
  private void executeAllInstructions() {
    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        while (controller.hasProcessesToExecute() && !isCancelled()) {
          executeNextInstructionInBackground();
          Thread.sleep(1000);
        }
        return null;
      }

      @Override
      protected void done() {
        SwingUtilities.invokeLater(() -> {
          consoleArea.append("All instructions executed\n");
          updateRegistersDisplay();
          updateMemoryMap();
        });
      }
    }.execute();
  }

  /**
   * Executes the next instruction in the background.
   */
  private void executeNextInstructionInBackground() {
    try {
      if (true) { // Change this condition as needed
        if (firstStep) {
          firstStep = false;
          controller.executeInstruction();
        }
        controller.executeInstruction();
        SwingUtilities.invokeLater(this::updateGUI);
      }
    } catch (Exception ex) {
      final String errorMessage = ex.getMessage();
      SwingUtilities.invokeLater(() -> consoleArea.append("Error: " + errorMessage + "\n"));
    }
  }

  /**
   * Updates the GUI components.
   */
  private void updateGUI() {
    updateRegistersDisplay();
    updateMemoryMap();
    updateStatsTab();
  }

  /**
   * Executes the next instruction.
   */
  private synchronized void executeNextInstruction() {
    try {
      if (true) {
        if (firstStep) {
          this.firstStep = false;
          controller.executeInstruction();
        }
        controller.executeInstruction();
        updateRegistersDisplay();
        updateMemoryMap();
        updateStatsTab();
      }
    } catch (Exception ex) {
      System.out.println("Error: " + ex.getMessage());
      consoleArea.append("Error: " + ex.getMessage() + "\n");
    }
  }

  /**
   * Cleans the console area.
   *
   */
  private void cleanConsole() {
    consoleArea.setText("");
  }

  /**
   * Expands all nodes in the JTree.
   *
   * @param tree          the JTree to expand
   * @param startingIndex the starting index
   * @param rowCount      the row count
   */
  private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
    for (int i = startingIndex; i < rowCount; ++i) {
      tree.expandRow(i);
    }

    if (tree.getRowCount() != rowCount) {
      expandAllNodes(tree, rowCount, tree.getRowCount());
    }
  }

  /**
   * Prints a message to the console area.
   *
   * @param message the message to print
   */
  public void printToConsole(String message) {
    consoleArea.append(message + "\n");
  }

  /**
   * Reads input from the console.
   *
   * @return the input from the console
   */
  public String readFromConsole() {
    return JOptionPane.showInputDialog(this, "Enter input:");
  }

  /**
   * Disposes the Hard8086 window and shuts down the interrupt executor.
   */
  @Override
  public void dispose() {
    interruptExecutor.shutdownNow();
    controller.reset();
    desktopScreenControllerRef.changeScheduler(controller);
    super.dispose();
  }
}