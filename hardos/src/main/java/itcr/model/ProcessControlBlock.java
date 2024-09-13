package itcr.model;

import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.EmptyStackException;

public class ProcessControlBlock {
  private int processId;
  private ProcessState state;
  private int programCounter;
  private int[] registers; // AC, AX, BX, CX, DX
  private Stack<Integer> stack;
  private long cpuTimeUsed;
  private long startTime;
  private List<String> openFiles;
  private Process nextProcess;
  private int baseAddress;
  private int processSize;
  private int priority;

  public ProcessControlBlock(int processId, int baseAddress, int processSize, int priority) {
    this.processId = processId;
    this.state = ProcessState.NEW;
    this.programCounter = 0;
    this.registers = new int[5];
    this.stack = new Stack<>();
    this.cpuTimeUsed = 0;
    this.startTime = System.currentTimeMillis();
    this.openFiles = new ArrayList<>();
    this.baseAddress = baseAddress;
    this.processSize = processSize;
    this.priority = priority;
  }

  public void updateState(ProcessState newState) {
    this.state = newState;
  }

  public void incrementProgramCounter() {
    this.programCounter++;
  }

  public void pushToStack(int value) {
    if (stack.size() >= 5) {
      throw new StackOverflowError("Process stack is full");
    }
    stack.push(value);
  }

  public int popFromStack() {
    if (stack.isEmpty()) {
      throw new EmptyStackException();
    }
    return stack.pop();
  }

  public void addOpenFile(String fileName) {
    openFiles.add(fileName);
  }

  public void removeOpenFile(String fileName) {
    openFiles.remove(fileName);
  }

  public Process getNextPCB() {
    return nextProcess;
  }

  public int getProcessId() {
    return processId;
  }

  public ProcessState getState() {
    return state;
  }

  public int getProgramCounter() {
    return programCounter;
  }

  public int[] getRegisters() {
    return registers;
  }

  public Stack<Integer> getStack() {
    return stack;
  }

  public long getCpuTimeUsed() {
    return cpuTimeUsed;
  }

  public long getStartTime() {
    return startTime;
  }

  public List<String> getOpenFiles() {
    return openFiles;
  }

  public int getBaseAddress() {
    return baseAddress;
  }

  public int getProcessSize() {
    return processSize;
  }

  public int getPriority() {
    return priority;
  }

  public void setCpuTimeUsed(long cpuTimeUsed) {
    this.cpuTimeUsed = cpuTimeUsed;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public void setBaseAddress(int baseAddress) {
    this.baseAddress = baseAddress;
  }

  public void setProcessSize(int processSize) {
    this.processSize = processSize;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setRegisters(int[] registers) {
    System.arraycopy(registers, 0, this.registers, 0, this.registers.length);
  }

  public void setStack(Stack<Integer> stack) {
    this.stack = stack;
  }

  public void setOpenFiles(List<String> openFiles) {
    this.openFiles = openFiles;
  }

  public void setProgramCounter(int programCounter) {
    this.programCounter = programCounter;
  }

  public void setState(ProcessState state) {
    this.state = state;
  }

  public void setProcessId(int processId) {
    this.processId = processId;
  }

  public void setNextProcess(Process nextProcess) {
    this.nextProcess = nextProcess;
  }


}