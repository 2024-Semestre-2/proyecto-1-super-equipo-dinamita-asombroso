package itcr.execution;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import itcr.model.*;
import itcr.models.*;

public class Hard8086 extends JFrame {
  private CPU cpu;
  private Scheduler scheduler;
  private MemoryManager memoryManager;
  private Map<Integer, JTextArea> registersAreas;
  private JTextArea consoleArea;
  private JTextField inputField;
  private int count;

  public Hard8086(CPU cpu, Scheduler scheduler, MemoryManager memoryManager) {
    this.cpu = cpu;
    this.scheduler = scheduler;
    this.memoryManager = memoryManager;

    setTitle("Hard8086 Simulator");
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout());
    count = 0;

    JPanel registersPanel = new JPanel(new GridLayout(1, cpu.getNumCores()));
    registersAreas = new HashMap<>();

    for (int i = 0; i < cpu.getNumCores(); i++) {
      JTextArea registerArea = new JTextArea(10, 15);
      registerArea.setEditable(false);
      registersAreas.put(i, registerArea);
      JScrollPane scrollPane = new JScrollPane(registerArea);
      scrollPane.setBorder(BorderFactory.createTitledBorder("Core " + i + " Registers"));
      registersPanel.add(scrollPane);
    }

    add(registersPanel, BorderLayout.NORTH);

    consoleArea = new JTextArea(10, 50);
    consoleArea.setEditable(false);
    JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
    consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
    add(consoleScrollPane, BorderLayout.CENTER);

    JPanel controlPanel = new JPanel(new BorderLayout());
    inputField = new JTextField(20);
    controlPanel.add(inputField, BorderLayout.CENTER);

    JButton executeButton = new JButton("Execute Next Instruction");
    executeButton.addActionListener(this::executeNextInstruction);
    controlPanel.add(executeButton, BorderLayout.EAST);

    add(controlPanel, BorderLayout.SOUTH);

    updateRegistersDisplay();
  }

  private void executeNextInstruction(ActionEvent e) {
    try {
      scheduler.executeInstruction();
      System.out.println("ejecuciones: "  + count++);
      updateRegistersDisplay();

      CPU cpu = scheduler.cpu;
      MemoryManager memory = cpu.memory;
      memory.printAllInstructions("P" + cpu.runningProcesses[0].getProcessId());

      consoleArea.append("Executed next instruction\n");
    } catch (Exception ex) {
      consoleArea.append("Error: " + ex.getMessage() + "\n");
    }
  }

  private void updateRegistersDisplay() {
    for (int i = 0; i < cpu.getNumCores(); i++) {
      JTextArea area = registersAreas.get(i);
      area.setText(cpu.getRegisters(i));
    }
  }

  public void printToConsole(String message) {
    consoleArea.append(message + "\n");
  }

  public String readFromConsole() {
    return JOptionPane.showInputDialog(this, "Enter input:");
  }
}