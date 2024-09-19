package itcr.model;

import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.time.Instant;

public class ProcessControlBlock {
  private final int processId;
  private ProcessState state;
  private int programCounter;
  private int[] registers;
  private ArrayDeque<Integer> stack;
  private long cpuTimeUsed;
  private Instant startTime;
  private List<String> openFiles;
  private ProcessControlBlock nextProcess;
  private int baseAddress;
  private int processSize;
  private int priority;
  private int timeSlice;
  private int waitingTime;
  private int turnaroundTime;
  private Instant lastStateChangeTime;

  public ProcessControlBlock(int processId, int baseAddress, int processSize, int priority) {
    this.processId = processId;
    this.state = ProcessState.NEW;
    this.programCounter = 0;
    this.registers = new int[5]; // AC, AX, BX, CX, DX
    this.stack = new ArrayDeque<>(10);
    this.cpuTimeUsed = 0;
    this.startTime = Instant.now();
    this.openFiles = new ArrayList<>();
    this.baseAddress = baseAddress;
    this.processSize = processSize;
    this.priority = priority;
    this.timeSlice = 0;
    this.waitingTime = 0;
    this.turnaroundTime = 0;
    this.lastStateChangeTime = Instant.now();
  }

  public void updateState(ProcessState newState) {
    Instant now = Instant.now();
    if (this.state == ProcessState.READY) {
      this.waitingTime += now.toEpochMilli() - this.lastStateChangeTime.toEpochMilli();
    }
    this.state = newState;
    this.lastStateChangeTime = now;
  }

  public void incrementProgramCounter() {
    this.programCounter++;
  }

  public void pushToStack(int value) throws StackOverflowError {
    if (stack.size() >= 10) {
      throw new StackOverflowError("Process stack is full");
    }
    stack.push(value);
  }

  public int popFromStack() throws NoSuchElementException {
    return stack.pop();
  }

  public void addOpenFile(String fileName) {
    openFiles.add(fileName);
  }

  public void removeOpenFile(String fileName) {
    openFiles.remove(fileName);
  }

  public ProcessControlBlock getNextPCB() {
    return nextProcess;
  }

  public void setNextPCB(ProcessControlBlock nextPCB) {
    this.nextProcess = nextPCB;
  }

  public int getProcessId() {
    return processId;
  }

  public void updateCpuTimeUsed(long timeUsed) {
    this.cpuTimeUsed += timeUsed;
  }

  public void setTimeSlice(int timeSlice) {
    this.timeSlice = timeSlice;
  }

  public int getTimeSlice() {
    return this.timeSlice;
  }

  public void calculateTurnaroundTime() {
    this.turnaroundTime = (int) (Instant.now().toEpochMilli() - this.startTime.toEpochMilli());
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

  public ArrayDeque<Integer> getStack() {
    return stack;
  }

  public long getCpuTimeUsed() {
    return cpuTimeUsed;
  }

  public Instant getStartTime() {
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

  public void setStartTime(Instant startTime) {
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

  public void setStack(ArrayDeque<Integer> stack) {
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

  public void setNextProcess(ProcessControlBlock nextProcess) {
    this.nextProcess = nextProcess;
  }

  public int getWaitingTime() {
    return waitingTime;
  }

  public int getTurnaroundTime() {
    return turnaroundTime;
  }

  public void setRegister(Register register, int value) {
    registers[register.ordinal()] = value;
  }

  public int getRegister(Register register) {
    return registers[register.ordinal()];
  }

}