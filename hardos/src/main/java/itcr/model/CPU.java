package itcr.model;

import java.io.IOException;

public class CPU {
  private static final int NUM_CORES = 5;
  private Process[] runningProcesses;
  private String[] instructionRegisters;
  private static final int NUM_REGISTERS = 5; // AC, AX, BX, CX, DX
  private int[] registers;

  // flags
  private boolean zeroFlag = false;
  private boolean signFlag = false;
  private boolean carryFlag = false;
  private boolean overflowFlag = false;

  public CPU() {
    runningProcesses = new Process[NUM_CORES];
    instructionRegisters = new String[NUM_CORES];
    registers = new int[NUM_REGISTERS];
    initializeRegisters();
  }

  private void initializeRegisters() {
    registers[0] = 0; // AC
    registers[1] = 0; // AX
    registers[2] = 0; // BX
    registers[3] = 0; // CX
    registers[4] = 0; // DX
  }

  public void assignProcessToCore(Process process, int coreId) {
    if (coreId >= 0 && coreId < NUM_CORES) {
      runningProcesses[coreId] = process;
    } else {
      throw new IllegalArgumentException("Invalid core ID");
    }
  }

  public void executeInstruction(int coreId) throws Exception {
    Process process = runningProcesses[coreId];
    if (process == null)
      return;

    String instruction = process.getNextInstruction();
    instructionRegisters[coreId] = instruction;

    if (instruction == null) {
      process.updateState(ProcessState.TERMINATED);
      return;
    }

    String[] parts = instruction.split(" ");
    switch (parts[0]) {
      case "LOAD": // LOAD [register] (e.g. LOAD AX)
        int registerIndex = getRegisterIndex(parts[1]);
        registers[0] = process.getRegister(registerIndex);
        break;
      case "STORE": // STORE [register] (STORE THE VALUE OF AC to the register)
        registerIndex = getRegisterIndex(parts[1]);
        process.setRegister(registerIndex, registers[0]);
        break;
      case "MOV":
        // first case: MOV [register] [register] (e.g. MOV AX BX)
        if (parts.length == 3) {
          int sourceRegisterIndex = getRegisterIndex(parts[2]);
          int destinationRegisterIndex = getRegisterIndex(parts[1]);
          process.setRegister(destinationRegisterIndex, process.getRegister(sourceRegisterIndex));
        } else {
          // second case: MOV [register] [value] (e.g. MOV AX 10)
          registerIndex = getRegisterIndex(parts[1]);
          process.setRegister(registerIndex, Integer.parseInt(parts[2]));
        }
      case "ADD": // only ADD [register] (sums the value of AC to the register)
        registerIndex = getRegisterIndex(parts[1]);
        process.setRegister(registerIndex, process.getRegister(registerIndex) + registers[0]);
        break;
      case "SUB": // only SUB [register] (subtracts the value of AC from the register)
        registerIndex = getRegisterIndex(parts[1]);
        process.setRegister(registerIndex, process.getRegister(registerIndex) - registers[0]);
        break;
      case "INC":
        // First case: INC [register] (e.g. INC AX) Just adds 1 to the register
        if (parts.length == 2) {
          registerIndex = getRegisterIndex(parts[1]);
          process.setRegister(registerIndex, process.getRegister(registerIndex) + 1);
        } else {
          // Second case: INC (only adds 1 to the AC)
          registers[0]++;
        }
      case "DEC":
        // First case: DEC [register] (e.g. DEC AX) Just subtracts 1 from the register
        if (parts.length == 2) {
          registerIndex = getRegisterIndex(parts[1]);
          process.setRegister(registerIndex, process.getRegister(registerIndex) - 1);
        } else {
          // Second case: DEC (only subtracts 1 from the AC)
          registers[0]--;
        }
      case "SWAP": // SWAP [register1] [register2] (e.g. SWAP AX BX)
        int registerIndex1 = getRegisterIndex(parts[1]);
        int registerIndex2 = getRegisterIndex(parts[2]);
        int temp = process.getRegister(registerIndex1);
        process.setRegister(registerIndex1, process.getRegister(registerIndex2));
        process.setRegister(registerIndex2, temp);
        break;
      case "INT": // INT [interruptCode] (e.g. INT 20H)
        handleInterrupt(parts[1], process);
        break;
      case "JMP": // Jumps to an instruction JMP [+/-][value] (e.g. JMP +2)
        int jumpValue = Integer.parseInt(parts[1]);
        process.setCurrentInstructionIndex(process.getCurrentInstructionIndex() + jumpValue);
        break;
      case "CMP": // CMP [register1] [register2] (e.g. CMP AX BX)
        registerIndex1 = getRegisterIndex(parts[1]);
        registerIndex2 = getRegisterIndex(parts[2]);
        int value1 = process.getRegister(registerIndex1);
        int value2 = process.getRegister(registerIndex2);
        if (value1 == value2) {
          zeroFlag = true;
          signFlag = false;
        } else if (value1 > value2) {
          zeroFlag = false;
          signFlag = false;
        } else {
          zeroFlag = false;
          signFlag = true;
        }
        break;
      case "JE": // JE [-/+][value] (e.g. JE +2)
        if (zeroFlag) {
          jumpValue = Integer.parseInt(parts[1]);
          process.setCurrentInstructionIndex(process.getCurrentInstructionIndex() + jumpValue);
        }
        break;
      case "JNE": // JNE [-/+][value] (e.g. JNE +2)
        if (!zeroFlag) {
          jumpValue = Integer.parseInt(parts[1]);
          process.setCurrentInstructionIndex(process.getCurrentInstructionIndex() + jumpValue);
        }
        break;
      case "PARAM": // PARAM [num1] [num2] [num3]
        // From 1 to 3 parameters can be passed, just store them in the Stack
        for (int i = 1; i < parts.length; i++) {
          process.pushToStack(Integer.parseInt(parts[i]));
        }
      case "PUSH": // PUSH [register] (Ej PUSH AX)
        registerIndex = getRegisterIndex(parts[1]);
        process.pushToStack(process.getRegister(registerIndex));
        break;

      case "POP": // POP [register] (Ej POP AX)
        registerIndex = getRegisterIndex(parts[1]);
        process.setRegister(registerIndex, process.popFromStack());
        break;

      default:
        throw new IllegalStateException("Unknown instruction: " + instruction);
    }

    process.getPCB().incrementProgramCounter();
  }

  private void handleInterrupt(String interruptCode, Process process) throws IOException {
    switch (interruptCode) {
      case "20H":
        process.updateState(ProcessState.TERMINATED);
        break;
      case "10H": //  Prints the value of the AC register
        // TODO
        break;
      case "09H":
        // keboard input (only number 0-255)
        // TODO
      case "21H":
        // File management
        // TODO
    }
  }

  public void handleIOInterrupt(int coreId, String deviceType, Scheduler scheduler) {
    Process process = runningProcesses[coreId];
    if (process != null) {
      process.updateState(ProcessState.WAITING);
      runningProcesses[coreId] = null;
      // notif scheduler
      scheduler.moveToWaiting(process);
    }
  }

  private int getRegisterIndex(String registerName) {
    switch (registerName) {
      case "AX":
        return 0;
      case "BX":
        return 1;
      case "CX":
        return 2;
      case "DX":
        return 3;
      default:
        throw new IllegalArgumentException("Unknown register: " + registerName);
    }
  }

  public boolean isCoreAvailable(int coreId) {
    if (coreId >= 0 && coreId < NUM_CORES) {
      return runningProcesses[coreId] == null;
    } else {
      throw new IllegalArgumentException("Invalid core ID");
    }
  }

  public int getNumCores() {
    return NUM_CORES;
  }

  public Process getRunningProcess(int coreId) {
    return runningProcesses[coreId];
  }

  public String getInstructionRegister(int coreId) {
    return instructionRegisters[coreId];
  }

  public void saveProcessContext(int coreId) {
    Process process = runningProcesses[coreId];
    if (process != null) {
      ProcessControlBlock pcb = process.getPCB();
      System.arraycopy(registers, 0, pcb.getRegisters(), 0, NUM_REGISTERS);
      pcb.setProgramCounter(process.getCurrentInstructionIndex());
    }
  }

  public void loadProcessContext(int coreId) {
    Process process = runningProcesses[coreId];
    if (process != null) {
      ProcessControlBlock pcb = process.getPCB();
      System.arraycopy(pcb.getRegisters(), 0, registers, 0, NUM_REGISTERS);
      process.setCurrentInstructionIndex(pcb.getProgramCounter());
    }
  }

}