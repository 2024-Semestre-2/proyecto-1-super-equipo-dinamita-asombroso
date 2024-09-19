package itcr.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

import itcr.models.MemoryManager;

public class CPU {
  private static final int NUM_CORES = 5;
  public Process[] runningProcesses;
  private String[] instructionRegisters;
  private EnumMap<Register, Integer>[] registers;
  public MemoryManager memory;

  // Flags
  private boolean zeroFlag = false;
  private boolean signFlag = false;
  private boolean carryFlag = false;
  private boolean overflowFlag = false;

  public CPU() {
    // this.memory = memory;
    runningProcesses = new Process[NUM_CORES];
    instructionRegisters = new String[NUM_CORES];
    registers = new EnumMap[NUM_CORES];
    initializeRegisters();
  }

  private void initializeRegisters() {
    for (int i = 0; i < NUM_CORES; i++) {
      registers[i] = new EnumMap<>(Register.class);
      for (Register reg : Register.values()) {
        registers[i].put(reg, 0);
      }
    }
  }

  private void resetRegister(int index) {
      registers[index] = new EnumMap<>(Register.class);
      for (Register reg : Register.values()) {
        registers[index].put(reg, 0);
      }
  }

  public void assignProcessToCore(Process process, int coreId) {
    if (coreId >= 0 && coreId < NUM_CORES) {
      runningProcesses[coreId] = process;
      loadProcessContext(coreId);
    } else {
      throw new IllegalArgumentException("Invalid core ID");
    }
  }

  public void executeInstruction(int coreId) throws Exception {
    Process process = runningProcesses[coreId];
    if (process == null)
      return;
    //
    //String instruction = process.getNextInstruction();

    String instruction = getNextInstruction(coreId);

    instructionRegisters[coreId] = instruction;

    if (instruction == null) {
      process.updateState(ProcessState.TERMINATED);
      System.out.println("ready for put in terminate state core: " +  coreId);
      dispatcher(coreId);
      return;
    }

    String[] parts = instruction.split(" ");
    InstructionType type = InstructionType.valueOf(parts[0]);

    instructionHandlers.get(type).accept(coreId, parts);
    process.getPCB().incrementProgramCounter();
    saveProcessContext(coreId);
  }

  private final Map<InstructionType, BiConsumer<Integer, String[]>> instructionHandlers = Map.of(
      InstructionType.LOAD, this::handleLoad,
      InstructionType.STORE, this::handleStore,
      InstructionType.MOV, this::handleMov,
      InstructionType.ADD, this::handleAdd,
      InstructionType.SUB, this::handleSub,
      InstructionType.INT, this::handleInterrupt);
      

  private void handleLoad(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    registers[coreId].put(Register.AC, registers[coreId].get(reg));
  }

  private void handleStore(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    registers[coreId].put(reg, registers[coreId].get(Register.AC));
  }

  private void handleMov(int coreId, String[] parts) {
    Register destReg = Register.valueOf(parts[1].replace(",", ""));
    if (parts.length == 3 && !isNumeric(parts[2])) {
      Register sourceReg = Register.valueOf(parts[2]);
      registers[coreId].put(destReg, registers[coreId].get(sourceReg));
    } else {
      registers[coreId].put(destReg, Integer.parseInt(parts[2]));
    }
  }

  private void handleAdd(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    int result = registers[coreId].get(Register.AC) + registers[coreId].get(reg);
    registers[coreId].put(Register.AC, result);
    updateFlags(result);
  }

  private void handleSub(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    int result = registers[coreId].get(Register.AC) - registers[coreId].get(reg);
    registers[coreId].put(Register.AC, result);
    updateFlags(result);
  }

  private void handleInterrupt(int coreId, String[] parts) {
    InterruptCode code = InterruptCode.valueOf(parts[1]);
    Process process = runningProcesses[coreId];
    switch (code) {
      case X20H:
        process.updateState(ProcessState.TERMINATED);
        break;
      case X10H:
        // Handle I/O interrupt
        // ioHandler.handleIORequest(process, parts[2]);
        break;
      // Add more interrupt handlers
    }
  }

  private void updateFlags(int result) {
    zeroFlag = result == 0;
    signFlag = result < 0;
    // Update carry and overflow flags as needed
  }

  public void handleIOCompletion(Process process) {
    process.updateState(ProcessState.READY);
    // Notify scheduler that the process is ready
  }

  public boolean isCoreAvailable(int coreId) {
    return runningProcesses[coreId] == null;
  }

  public int getNumCores() {
    return NUM_CORES;
  }

  public void loadProcessContext(int coreId) {
    Process process = runningProcesses[coreId];
    for (Register reg : Register.values()) {
      registers[coreId].put(reg, process.getPCB().getRegister(reg));
    }
  }

  public void saveProcessContext(int coreId) {
    Process process = runningProcesses[coreId];
    for (Register reg : Register.values()) {
      process.getPCB().setRegister(reg, registers[coreId].get(reg));
    }
  }

  public Process getRunningProcess(int coreId) {
    return runningProcesses[coreId];
  }

  public int getCoreRegister(int coreId, Register reg) {
    return registers[coreId].get(reg);
  }

  public String getRegisters(int coreId) {
    StringBuilder sb = new StringBuilder();
    for (Register reg : Register.values()) {
      sb.append(reg).append(": ").append(registers[coreId].get(reg)).append("\n");
    }
    return sb.toString();
  }

  public void dispatcher(int index) {
    if (index >= 0 && index < NUM_CORES) {
        Process currentProcess = runningProcesses[index];
        String id = "P" + currentProcess.getProcessId();
        memory.deallocateMemory(id);
        runningProcesses[index] = null;
        resetRegister(index);
    } else {
        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }
  }

  private boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");
  }

  private enum InstructionType {
    LOAD, STORE, MOV, ADD, SUB, INT
  }

  private enum InterruptCode {
    X20H, X10H
  }

  private String getNextInstruction(int coreId) {
    Process CurrentProcess = runningProcesses[coreId];
    String id = "P" +  CurrentProcess.getProcessId();
    String res = memory.getInstruction(id, CurrentProcess.currentInstructionIndex++);
    return res;
  }
}