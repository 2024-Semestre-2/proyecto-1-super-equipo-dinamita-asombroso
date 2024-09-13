package itcr.model;

import java.util.List;

public class Process {
  private ProcessControlBlock pcb;
  private List<String> instructions;
  private int currentInstructionIndex;

  private static int processCounter = 0;

  public Process(int baseAddress, int processSize, int priority, List<String> instructions) {
    this.pcb = new ProcessControlBlock(processCounter++, baseAddress, processSize, priority);
    this.instructions = instructions;
    this.currentInstructionIndex = 0;
  }

  public String getNextInstruction() {
    if (currentInstructionIndex < instructions.size()) {
      return instructions.get(currentInstructionIndex++);
    }
    return null;
  }

  public void resetInstructionPointer() {
    currentInstructionIndex = 0;
  }

  public boolean hasMoreInstructions() {
    return currentInstructionIndex < instructions.size();
  }

  public ProcessControlBlock getPCB() {
    return pcb;
  }

  public void updateState(ProcessState newState) {
    pcb.updateState(newState);
  }

  public void setRegister(int index, int value) {
    pcb.getRegisters()[index] = value;
  }

  public int getRegister(int index) {
    return pcb.getRegisters()[index];
  }

  public void pushToStack(int value) {
    pcb.pushToStack(value);
  }

  public int popFromStack() {
    return pcb.popFromStack();
  }

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

  public void setCurrentInstructionIndex(int index) {
    this.currentInstructionIndex = index;
  }


  public int[] getRegister() {
    return this.pcb.getRegisters();
  }
}