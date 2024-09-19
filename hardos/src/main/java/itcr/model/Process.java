package itcr.model;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

public class Process {
  private ProcessControlBlock pcb;
  private List<String> instructions;
  public int currentInstructionIndex;
  private static int processCounter = 0;
  private List<Process> children;
  private Process parent;
  private int exitCode;

  public Process(int baseAddress, int processSize, int priority, List<String> instructions) {
    this.pcb = new ProcessControlBlock(processCounter++, baseAddress, processSize, priority);
    this.instructions = new ArrayList<>(instructions);
    this.currentInstructionIndex = 0;
    this.children = new ArrayList<>();
    this.exitCode = -1;
  }

  public String getNextInstruction() {
    if (hasMoreInstructions()) {
      String instruction = instructions.get(currentInstructionIndex);
      currentInstructionIndex++;
      pcb.incrementProgramCounter();
      return instruction;
    }
    return null;
  }

  public void resetInstructionPointer() {
    currentInstructionIndex = 0;
    pcb.setProgramCounter(0);
  }

  public boolean hasMoreInstructions() {
    return currentInstructionIndex < instructions.size();
  }

  public ProcessControlBlock getPCB() {
    return pcb;
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

  public void pushToStack(int value) {
    pcb.pushToStack(value);
  }

  public int popFromStack() {
    return pcb.popFromStack();
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
    if (index >= 0 && index < instructions.size()) {
      this.currentInstructionIndex = index;
      pcb.setProgramCounter(index);
    } else {
      throw new IndexOutOfBoundsException("Invalid instruction index");
    }
  }
}