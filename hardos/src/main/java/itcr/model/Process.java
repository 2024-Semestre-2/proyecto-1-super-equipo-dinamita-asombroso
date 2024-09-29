package itcr.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Process class represents a process in the system, including its PCB,
 * children, parent, and various attributes.
 */
public class Process {
  private ProcessControlBlock pcb;
  private int currentInstructionIndex;
  public static int processCounter = 0;
  private List<Process> children;
  private Process parent;
  private int exitCode;
  private int qtyInstructions;

  /**
   * Constructor for Process with a specified number of instructions.
   *
   * @param qtyInstructions the number of instructions for the process
   */
  public Process(int qtyInstructions) {
    this.currentInstructionIndex = 0;
    this.children = new ArrayList<>();
    this.exitCode = -1;
    this.qtyInstructions = qtyInstructions;
  }

  /**
   * Constructor for Process with a specified PCB.
   *
   * @param pcb the Process Control Block for the process
   */
  public Process(ProcessControlBlock pcb) {
    this.pcb = pcb;
    this.currentInstructionIndex = 0;
    this.children = new ArrayList<>();
    this.exitCode = -1;
  }

  /**
   * Gets the number of instructions for the process.
   *
   * @return the number of instructions
   */
  public int getQtyInstructions() {
    return this.qtyInstructions;
  }

  /**
   * Sets the number of instructions for the process.
   *
   * @param qtyInstructions the number of instructions
   */
  public void setQtyInstructions(int qtyInstructions) {
    this.qtyInstructions = qtyInstructions;
  }

  /**
   * Resets the instruction pointer to the beginning.
   */
  public void resetInstructionPointer() {
    currentInstructionIndex = 0;
    pcb.setProgramCounter(0);
  }

  /**
   * Gets the Process Control Block (PCB) of the process.
   *
   * @return the PCB
   */
  public ProcessControlBlock getPCB() {
    return pcb;
  }

  /**
   * Sets the Process Control Block (PCB) of the process.
   *
   * @param pcb the PCB to set
   */
  public void setPCB(ProcessControlBlock pcb) {
    this.pcb = pcb;
  }

  /**
   * Updates the state of the process.
   *
   * @param newState the new state of the process
   */
  public void updateState(ProcessState newState) {
    pcb.updateState(newState);
    if (newState == ProcessState.TERMINATED) {
      pcb.calculateTurnaroundTime();
    }
  }

  /**
   * Sets the value of a register.
   *
   * @param index the index of the register
   * @param value the value to set
   */
  public void setRegister(int index, int value) {
    if (index >= 0 && index < pcb.getRegisters().length) {
      pcb.getRegisters()[index] = value;
    } else {
      throw new IndexOutOfBoundsException("Invalid register index");
    }
  }

  /**
   * Gets the value of a register.
   *
   * @param index the index of the register
   * @return the value of the register
   */
  public int getRegister(int index) {
    if (index >= 0 && index < pcb.getRegisters().length) {
      return pcb.getRegisters()[index];
    } else {
      throw new IndexOutOfBoundsException("Invalid register index");
    }
  }

  /**
   * Adds a child process.
   *
   * @param child the child process to add
   */
  public void addChild(Process child) {
    children.add(child);
    child.setParent(this);
  }

  /**
   * Removes a child process.
   *
   * @param child the child process to remove
   */
  public void removeChild(Process child) {
    children.remove(child);
  }

  /**
   * Gets the list of child processes.
   *
   * @return a list of child processes
   */
  public List<Process> getChildren() {
    return new ArrayList<>(children);
  }

  /**
   * Sets the parent process.
   *
   * @param parent the parent process to set
   */
  public void setParent(Process parent) {
    this.parent = parent;
  }

  /**
   * Gets the parent process.
   *
   * @return the parent process
   */
  public Process getParent() {
    return parent;
  }

  /**
   * Sets the exit code of the process.
   *
   * @param exitCode the exit code to set
   */
  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  /**
   * Gets the exit code of the process.
   *
   * @return the exit code
   */
  public int getExitCode() {
    return exitCode;
  }

  /**
   * Updates the CPU time used by the process.
   *
   * @param timeUsed the CPU time used
   */
  public void updateCpuTimeUsed(long timeUsed) {
    pcb.updateCpuTimeUsed(timeUsed);
  }

  // Accessors

  /**
   * Gets the process ID.
   *
   * @return the process ID
   */
  public int getProcessId() {
    return pcb.getProcessId();
  }

  /**
   * Gets the base address of the process.
   *
   * @return the base address
   */
  public int getBaseAddress() {
    return pcb.getBaseAddress();
  }

  /**
   * Gets the size of the process.
   *
   * @return the process size
   */
  public int getProcessSize() {
    return pcb.getProcessSize();
  }

  /**
   * Gets the priority of the process.
   *
   * @return the priority
   */
  public int getPriority() {
    return pcb.getPriority();
  }

  /**
   * Gets the state of the process.
   *
   * @return the process state
   */
  public ProcessState getState() {
    return pcb.getState();
  }

  /**
   * Gets the program counter of the process.
   *
   * @return the program counter
   */
  public int getProgramCounter() {
    return pcb.getProgramCounter();
  }

  /**
   * Gets the current instruction index of the process.
   *
   * @return the current instruction index
   */
  public int getCurrentInstructionIndex() {
    return currentInstructionIndex;
  }

  /**
   * Gets the registers of the process.
   *
   * @return an array of register values
   */
  public int[] getRegisters() {
    return pcb.getRegisters();
  }

  /**
   * Sets the current instruction index of the process.
   *
   * @param index the instruction index to set
   */
  public void setCurrentInstructionIndex(int index) {
    this.currentInstructionIndex = index;
    pcb.setProgramCounter(index);
  }
}