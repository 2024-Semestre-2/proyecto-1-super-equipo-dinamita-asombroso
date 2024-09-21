package itcr.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;

import itcr.models.MemoryManager;

public class CPU {
  private static final int NUM_CORES = 5;
  public Process[] runningProcesses;
  private String[] instructionRegisters;
  private EnumMap<Register, Integer>[] registers;
  public MemoryManager memory;

  // Flags
  private boolean zeroFlag = false;
  // private boolean signFlag = false;
  // private boolean carryFlag = false;
  // private boolean overflowFlag = false;

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
    if (process == null) {
      System.out.println("[" + coreId + "] " + "No process assigned to this core");
      return;
    }

    String instruction = getNextInstruction(coreId);

    System.out.println("[" + coreId + "] " + "Executing > Instruction: " + instruction);

    instructionRegisters[coreId] = instruction;

    if (instruction == null) {
      process.updateState(ProcessState.TERMINATED);
      memory.freeBCP("P" + process.getProcessId());
      dispatcher(coreId);
      return;
    }

    String[] parts = instruction.split(" ");
    InstructionType type = InstructionType.valueOf(parts[0]);

    instructionHandlers.get(type).accept(coreId, parts);
    process.getPCB().incrementProgramCounter();
    saveProcessContext(coreId);
  }

  private final Map<InstructionType, BiConsumer<Integer, String[]>> instructionHandlers = new HashMap<>();

  {
    instructionHandlers.put(InstructionType.LOAD, this::handleLoad);
    instructionHandlers.put(InstructionType.STORE, this::handleStore);
    instructionHandlers.put(InstructionType.MOV, this::handleMov);
    instructionHandlers.put(InstructionType.ADD, this::handleAdd);
    instructionHandlers.put(InstructionType.SUB, this::handleSub);
    instructionHandlers.put(InstructionType.INT, this::handleInterrupt);
    instructionHandlers.put(InstructionType.INC, this::handleInc);
    instructionHandlers.put(InstructionType.DEC, this::handleDec);
    instructionHandlers.put(InstructionType.SWAP, this::handleSwap);
    instructionHandlers.put(InstructionType.JMP, this::handleJmp);
    instructionHandlers.put(InstructionType.CMP, this::handleCmp);
    instructionHandlers.put(InstructionType.JE, this::handleJe);
    instructionHandlers.put(InstructionType.JNE, this::handleJne);
  }

  private void handleJe(int coreId, String[] parts) {
    if (zeroFlag) {
      handleJmp(coreId, parts);
    }
  }

  private void handleJne(int coreId, String[] parts) {
    if (!zeroFlag) {
      handleJmp(coreId, parts);
    }
  }

  private void handleCmp(int coreId, String[] parts) {
    Register reg1 = Register.valueOf(parts[1].replace(",", ""));
    Register reg2 = Register.valueOf(parts[2]);
    int val1 = registers[coreId].get(reg1);
    int val2 = registers[coreId].get(reg2);
    zeroFlag = false;
    if (val1 == val2) {zeroFlag = true;}
    else {zeroFlag = false;}
  }

  private void handleJmp(int coreId, String[] parts) {
    Process currentP = runningProcesses[coreId];

    int topIndex = currentP.getInstructions().size();
    int currentIndex = currentP.getCurrentInstructionIndex() - 1;
    int val = Integer.valueOf(parts[1]);

    if (val > 0) {
      if (val + currentIndex >= topIndex) {
        throw new Error("Desplazamiento invalido.");
      }
    } else {
      if (currentIndex + val < 0) {
        throw new Error("Desplazamiento invalido.");
      }
    }
    runningProcesses[coreId].setCurrentInstructionIndex(currentIndex + val);
  }

  private void handleSwap(int coreId, String[] parts) {
    Register reg1 = Register.valueOf(parts[1].replace(",", ""));
    Register reg2 = Register.valueOf(parts[2]);
    int temp = registers[coreId].get(reg1);
    registers[coreId].put(reg1, registers[coreId].get(reg2));
    registers[coreId].put(reg2, temp);
  }

  private void handleDec(int coreId, String[] parts) {
    if (parts.length == 1) {
      int result = registers[coreId].get(Register.AC) - 1;
      registers[coreId].put(Register.AC, result);
      updateFlags(result);
    } else {
      Register reg = Register.valueOf(parts[1]);
      int result = registers[coreId].get(Register.AC) - registers[coreId].get(reg);
      registers[coreId].put(Register.AC, result);
      updateFlags(result);
    }
  }

  private void handleInc(int coreId, String[] parts) {
    if (parts.length == 1) {
      int result = registers[coreId].get(Register.AC) + 1;
      registers[coreId].put(Register.AC, result);
      updateFlags(result);
    } else {
      Register reg = Register.valueOf(parts[1]);
      int result = registers[coreId].get(Register.AC) + registers[coreId].get(reg);
      registers[coreId].put(Register.AC, result);
      updateFlags(result);
    }
  }

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
      case _20H:
        process.updateState(ProcessState.TERMINATED);
        memory.freeBCP("P" + process.getProcessId());
        break;
      case _10H:
        System.out.println("10H interrupt called");
    }
  }

  private void updateFlags(int result) {
    zeroFlag = result == 0;
    // signFlag = result < 0;
  }

  public void handleIOCompletion(Process process) {
    process.updateState(ProcessState.READY);
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
      System.out.println("Dispatcher: Terminating process " + id);
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
    LOAD, STORE, MOV, ADD, SUB, INT, INC, DEC, SWAP, JMP, CMP, JE, JNE
  }

  private enum InterruptCode {
    _20H, _10H
  }

  private String getNextInstruction(int coreId) {
    Process CurrentProcess = runningProcesses[coreId];
    String id = "P" + CurrentProcess.getProcessId();
    String res = memory.getInstruction(id, CurrentProcess.getCurrentInstructionIndex());
    CurrentProcess.setCurrentInstructionIndex(CurrentProcess.getCurrentInstructionIndex() + 1);
    return res;
  }
}