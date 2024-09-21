package itcr.model;

import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;

public class ProcessControlBlock {
  private int processId;
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

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Instant.class, new InstantAdapter())
      .create();

  public String toJsonString() {
    return gson.toJson(this);
  }

  public static ProcessControlBlock fromJsonString(String jsonString) {
    return gson.fromJson(jsonString, ProcessControlBlock.class);
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

  private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      // TODO Auto-generated method stub
      return Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
      // TODO Auto-generated method stub
      return new JsonPrimitive(src.toEpochMilli());
    }
  }

}