package itcr.model;

import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.time.Instant;

/**
 * ProcessControlBlock class represents the control block of a process,
 * containing various attributes and methods for process management.
 */
public class ProcessControlBlock {
  private int processId;
  private ProcessState state;
  private int programCounter;
  private int[] registers;
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
  private int stackPointer = -1; // -1 means stack is empty
  private int cpuId = -1; // -1 means process is not running

  /**
   * Constructor for ProcessControlBlock.
   *
   * @param processId   the ID of the process
   * @param baseAddress the base address of the process
   * @param processSize the size of the process
   * @param priority    the priority of the process
   */
  public ProcessControlBlock(int processId, int baseAddress, int processSize, int priority) {
    this.processId = processId;
    this.state = ProcessState.READY;
    this.programCounter = 0;
    this.registers = new int[5]; // AC, AX, BX, CX, DX
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
    this.updateCpuTimeUsed();
  }

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Instant.class, new InstantAdapter())
      .setLenient()
      .create();

  /**
   * Converts the ProcessControlBlock to a JSON string.
   *
   * @return the JSON string representation of the ProcessControlBlock
   */
  public String toJsonString() {
    return gson.toJson(this);
  }

  /**
   * Creates a ProcessControlBlock from a JSON string.
   *
   * @param jsonString the JSON string
   * @return the ProcessControlBlock object
   */
  public static ProcessControlBlock fromJsonString(String jsonString) {
    JsonReader reader = new JsonReader(new StringReader(jsonString));
    reader.setLenient(true);
    return gson.fromJson(reader, ProcessControlBlock.class);
  }

  public boolean isReadyToRun() {
    return state == ProcessState.READY;
  }

  /**
   * Sets the last state change time to the current time and updates the CPU time
   * used.
   */
  public void setlastStateChangeTime() {
    this.lastStateChangeTime = Instant.now();
    this.updateCpuTimeUsed();
  }

  /**
   * Updates the CPU time used by the process.
   */
  public void updateCpuTimeUsed() {
    this.cpuTimeUsed = this.lastStateChangeTime.toEpochMilli() - this.startTime.toEpochMilli();
    this.cpuTimeUsed /= 1000;
  }

  /**
   * Updates the state of the process.
   *
   * @param newState the new state of the process
   */
  public void updateState(ProcessState newState) {
    Instant now = Instant.now();
    if (this.state == ProcessState.READY) {
      this.waitingTime += now.toEpochMilli() - this.lastStateChangeTime.toEpochMilli();
    }
    this.state = newState;
    this.lastStateChangeTime = now;
    this.updateCpuTimeUsed();
  }

  /**
   * Increments the program counter by one.
   */
  public void incrementProgramCounter() {
    this.programCounter++;
  }

  /**
   * Adds an open file to the list of open files.
   *
   * @param fileName the name of the file to add
   */
  public void addOpenFile(String fileName) {
    openFiles.add(fileName);
  }

  /**
   * Removes an open file from the list of open files.
   *
   * @param fileName the name of the file to remove
   */
  public void removeOpenFile(String fileName) {
    openFiles.remove(fileName);
  }

  /**
   * Sets the stack pointer.
   *
   * @param stackPointer the stack pointer to set
   */
  public void setStackPointer(int stackPointer) {
    this.stackPointer = stackPointer;
  }

  /**
   * Gets the stack pointer.
   *
   * @return the stack pointer
   */
  public int getStackPointer() {
    return stackPointer;
  }

  /**
   * Gets the next ProcessControlBlock in the list.
   *
   * @return the next ProcessControlBlock
   */
  public ProcessControlBlock getNextPCB() {
    return nextProcess;
  }

  /**
   * Sets the next ProcessControlBlock in the list.
   *
   * @param nextPCB the next ProcessControlBlock to set
   */
  public void setNextPCB(ProcessControlBlock nextPCB) {
    this.nextProcess = nextPCB;
  }

  /**
   * Gets the process ID.
   *
   * @return the process ID
   */
  public int getProcessId() {
    return processId;
  }

  /**
   * Updates the CPU time used by the process.
   *
   * @param timeUsed the CPU time used
   */
  public void updateCpuTimeUsed(long timeUsed) {
    this.cpuTimeUsed += timeUsed;
  }

  /**
   * Sets the time slice for the process.
   *
   * @param timeSlice the time slice to set
   */
  public void setTimeSlice(int timeSlice) {
    this.timeSlice = timeSlice;
  }

  /**
   * Gets the time slice for the process.
   *
   * @return the time slice
   */
  public int getTimeSlice() {
    return this.timeSlice;
  }

  /**
   * Calculates the turnaround time for the process.
   */
  public void calculateTurnaroundTime() {
    this.turnaroundTime = (int) (Instant.now().toEpochMilli() - this.startTime.toEpochMilli());
  }

  /**
   * Gets the state of the process.
   *
   * @return the process state
   */
  public ProcessState getState() {
    return state;
  }

  /**
   * Gets the program counter of the process.
   *
   * @return the program counter
   */
  public int getProgramCounter() {
    return programCounter;
  }

  /**
   * Gets the registers of the process.
   *
   * @return an array of register values
   */
  public int[] getRegisters() {
    return registers;
  }

  /**
   * Gets the CPU time used by the process.
   *
   * @return the CPU time used
   */
  public long getCpuTimeUsed() {
    return cpuTimeUsed;
  }

  /**
   * Gets the start time of the process.
   *
   * @return the start time
   */
  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Gets the list of open files for the process.
   *
   * @return a list of open files
   */
  public List<String> getOpenFiles() {
    return openFiles;
  }

  /**
   * Gets the base address of the process.
   *
   * @return the base address
   */
  public int getBaseAddress() {
    return baseAddress;
  }

  /**
   * Gets the size of the process.
   *
   * @return the process size
   */
  public int getProcessSize() {
    return processSize;
  }

  /**
   * Gets the priority of the process.
   *
   * @return the priority
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Sets the CPU time used by the process.
   *
   * @param cpuTimeUsed the CPU time used to set
   */
  public void setCpuTimeUsed(long cpuTimeUsed) {
    this.cpuTimeUsed = cpuTimeUsed;
  }

  /**
   * Sets the start time of the process.
   *
   * @param startTime the start time to set
   */
  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  /**
   * Sets the base address of the process.
   *
   * @param baseAddress the base address to set
   */
  public void setBaseAddress(int baseAddress) {
    this.baseAddress = baseAddress;
  }

  /**
   * Sets the size of the process.
   *
   * @param processSize the process size to set
   */
  public void setProcessSize(int processSize) {
    this.processSize = processSize;
  }

  /**
   * Sets the priority of the process.
   *
   * @param priority the priority to set
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Sets the registers of the process.
   *
   * @param registers the registers to set
   */
  public void setRegisters(int[] registers) {
    System.arraycopy(registers, 0, this.registers, 0, this.registers.length);
  }

  /**
   * Sets the list of open files for the process.
   *
   * @param openFiles the list of open files to set
   */
  public void setOpenFiles(List<String> openFiles) {
    this.openFiles = openFiles;
  }

  /**
   * Sets the program counter of the process.
   *
   * @param programCounter the program counter to set
   */
  public void setProgramCounter(int programCounter) {
    this.programCounter = programCounter;
  }

  /**
   * Sets the state of the process.
   *
   * @param state the state to set
   */
  public void setState(ProcessState state) {
    this.state = state;
  }

  /**
   * Sets the next process in the process control block.
   *
   * @param nextProcess the next process to set
   */
  public void setNextProcess(ProcessControlBlock nextProcess) {
    this.nextProcess = nextProcess;
  }

  /**
   * Gets the waiting time of the process.
   *
   * @return the waiting time
   */
  public int getWaitingTime() {
    return waitingTime;
  }

  /**
   * Gets the turnaround time of the process.
   *
   * @return the turnaround time
   */
  public int getTurnaroundTime() {
    return turnaroundTime;
  }

  /**
   * Sets the value of a register.
   *
   * @param register the register to set
   * @param value    the value to set
   */
  public void setRegister(Register register, int value) {
    registers[register.ordinal()] = value;
  }

  /**
   * Gets the value of a register.
   *
   * @param register the register to get
   * @return the value of the register
   */
  public int getRegister(Register register) {
    return registers[register.ordinal()];
  }

  /**
   * Gets the ID of the CPU running the process.
   *
   * @return the CPU ID
   */
  public int getCpuId() {
    return cpuId;
  }

  /**
   * Sets the ID of the CPU running the process.
   *
   * @param cpuId the CPU ID to set
   */
  public void setCpuId(int cpuId) {
    this.cpuId = cpuId;
  }

  /**
   * InstantAdapter class for serializing and deserializing Instant objects to and
   * from JSON.
   */
  private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toEpochMilli());
    }
  }
  
}