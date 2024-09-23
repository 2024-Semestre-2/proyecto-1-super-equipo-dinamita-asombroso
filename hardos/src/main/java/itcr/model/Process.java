package itcr.model;

import java.util.List;
import java.util.ArrayList;

public class Process {
  private ProcessControlBlock pcb;
  private int currentInstructionIndex;
  public static int processCounter = 0;
  private List<Process> children;
  private Process parent;
  private int exitCode;
  private int qtyInstructions;

  public Process(int qtyInstructions) {
    this.currentInstructionIndex = 0;
    this.children = new ArrayList<>();
    this.exitCode = -1;
    this.qtyInstructions = qtyInstructions;
  }

  public Process(ProcessControlBlock pcb) {
    this.pcb = pcb;
    this.currentInstructionIndex = 0;
    this.children = new ArrayList<>();
    this.exitCode = -1;
  }

  public int getQtyInstructions() {
    return this.qtyInstructions;
  }

  public void setQtyInstructions(int qtyInstructions) {
    this.qtyInstructions = qtyInstructions;
  }

  public void resetInstructionPointer() {
    currentInstructionIndex = 0;
    pcb.setProgramCounter(0);
  }

  public ProcessControlBlock getPCB() {
    return pcb;
  }

  public void setPCB(ProcessControlBlock pcb) {
    this.pcb = pcb;
  }

  public void updateState(ProcessState newState) {
    pcb.updateState(newState);
    if (newState == ProcessState.TERMINATED) {
      pcb.calculateTurnaroundTime();
    }
  }

  public void setRegister(int index, int value) {
    if (index >= 0 && index < pcb.getRegisters().length) {
      pcb.getRegisters()[index] = value;
    } else {
      throw new IndexOutOfBoundsException("Invalid register index");
    }
  }

  public int getRegister(int index) {
    if (index >= 0 && index < pcb.getRegisters().length) {
      return pcb.getRegisters()[index];
    } else {
      throw new IndexOutOfBoundsException("Invalid register index");
    }
  }

  public void addChild(Process child) {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(Process child) {
    children.remove(child);
  }

  public List<Process> getChildren() {
    return new ArrayList<>(children);
  }

  public void setParent(Process parent) {
    this.parent = parent;
  }

  public Process getParent() {
    return parent;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void updateCpuTimeUsed(long timeUsed) {
    pcb.updateCpuTimeUsed(timeUsed);
  }

  // Accesors
  public int getProcessId() {
    return pcb.getProcessId();
  }

  public int getBaseAddress() {
    return pcb.getBaseAddress();
  }

  public int getProcessSize() {
    return pcb.getProcessSize();
  }

  public int getPriority() {
    return pcb.getPriority();
  }

  public ProcessState getState() {
    return pcb.getState();
  }

  public int getProgramCounter() {
    return pcb.getProgramCounter();
  }

  public int getCurrentInstructionIndex() {
    return currentInstructionIndex;
  }

  public int[] getRegisters() {
    return pcb.getRegisters();
  }

  public void setCurrentInstructionIndex(int index) {
    this.currentInstructionIndex = index;
    pcb.setProgramCounter(index);
  }
}