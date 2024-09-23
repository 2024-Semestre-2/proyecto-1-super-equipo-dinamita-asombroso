package itcr.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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

    String instruction = getNextInstruction(coreId);

    instructionRegisters[coreId] = instruction;

    if (instruction == null) {
      process.updateState(ProcessState.TERMINATED);
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
    instructionHandlers.put(InstructionType.PARAM, this::handleParam);
    instructionHandlers.put(InstructionType.PUSH, this::handlePush);
    instructionHandlers.put(InstructionType.POP, this::handlePop);
  }

  // Instruction handlers (push, pop, mov, etc.)

  private void handlePush(int coreId, String[] parts) {
    Process process = runningProcesses[coreId];
    String processId = "P" + process.getProcessId();
    ProcessControlBlock pcb = process.getPCB();
    int currentSP = pcb.getStackPointer();

    if (currentSP >= 4) {
      throw new IllegalArgumentException("Stack overflow: maximum stack size is 5.");
    }

    int value = registers[coreId].get(Register.AX);

    if (memory.writeToStack(processId, currentSP + 1, value)) {
      pcb.setStackPointer(currentSP + 1);
    } else {
      throw new IllegalArgumentException("Failed to push value to stack for process " + processId);
    }
  }

  private void handlePop(int coreId, String[] parts) {
    if (parts.length != 2) {
      throw new IllegalArgumentException("POP instruction requires a register argument.");
    }

    Register targetRegister;
    try {
      targetRegister = Register.valueOf(parts[1].toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid register: " + parts[1]);
    }

    Process process = runningProcesses[coreId];
    String processId = "P" + process.getProcessId();
    ProcessControlBlock pcb = process.getPCB();
    int currentSP = pcb.getStackPointer();

    if (currentSP < 0) {
      throw new IllegalArgumentException("Stack underflow: stack is empty.");
    }

    int value = memory.popFromStack(processId, currentSP);
    registers[coreId].put(targetRegister, value);
    pcb.setStackPointer(currentSP - 1);
  }

  private void handleParam(int coreId, String[] parts) {
    String[] cleanParts = Arrays.stream(parts)
        .map(part -> part.replace(",", "").trim())
        .toArray(String[]::new);

    if (cleanParts.length < 2 || cleanParts.length > 4) {
      throw new IllegalArgumentException("PARAM instruction requires 1 to 3 parameters.");
    }

    Process process = runningProcesses[coreId];
    String processId = "P" + process.getProcessId();
    ProcessControlBlock pcb = process.getPCB();
    int currentSP = pcb.getStackPointer();

    for (int i = 1; i < cleanParts.length; i++) {
      try {
        int value = Integer.parseInt(cleanParts[i]);
        if (currentSP >= 4) {
          throw new IllegalArgumentException("Stack overflow: maximum stack size is 5.");
        }
        if (memory.writeToStack(processId, currentSP + 1, value)) {
          currentSP++;
          pcb.setStackPointer(currentSP);
        } else {
          throw new IllegalArgumentException("Failed to write parameter to stack for process " + processId);
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Parameter must be a valid 32-bit integer", e);
      }
    }
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
    if (val1 == val2) {
      zeroFlag = true;
    } else {
      zeroFlag = false;
    }
  }

  private void handleJmp(int coreId, String[] parts) {
    Process currentP = runningProcesses[coreId];

    int topIndex = currentP.getQtyInstructions();
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
    String prefixMsg = "[ Core " + coreId + " ] >> ";
    String message = "";
    switch (code) {
      case _21H: // file management
        int ax_value = registers[coreId].get(Register.AX);
        int bx_value = registers[coreId].get(Register.BX);
        int cx_value = registers[coreId].get(Register.CX);

        String fileName = memory.getString(bx_value);
        String content = memory.getString(cx_value);

        // depends on the value of ax the type of file management
        // 0: create file, 1: open file, 2: read file, 3: write file, 4: close file

        // on the value of bx, is the name of the file (used for all the operations)

        // on the value of cx, is the content of the file (used for write file)

        switch (ax_value) {
          case 0:
            // create file
            message = prefixMsg + "File " + fileName + " created";

            memory.createFile(fileName);
            break;

          case 1:

            if (fileOpenedByOtherProcess(fileName, process.getProcessId())) {
              message = prefixMsg + "File " + fileName + " is already opened by another process";
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
              break;
            }

            // open file
            message = prefixMsg + "File " + fileName + " opened";
            String existingFile = memory.getFile(fileName);
            if (existingFile == null) {
              message = prefixMsg + "File " + fileName + " does not exist";
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
              break;
            }
            process.getPCB().getOpenFiles().add(fileName);
            break;
          case 2:

            if (fileOpenedByOtherProcess(fileName, process.getProcessId())) {
              message = prefixMsg + "File " + fileName + " is already opened by another process";
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
              break;
            }

            // read file
            message = prefixMsg + "File " + fileName + " read";
            // just print the content of the file
            String fileContent = memory.getFile(fileName);
            if (fileContent == null) {
              message = prefixMsg + "File " + fileName + " does not exist";
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
              break;
            }
            message = prefixMsg + "File " + fileName + " content: " + fileContent;
            InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
          case 3:
            
            if (fileOpenedByOtherProcess(fileName, process.getProcessId())) {
              message = prefixMsg + "File " + fileName + " is already opened by another process";
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
              break;
            }

            // write file
            message = prefixMsg + "File " + fileName + " written";
            if (memory.storeFile(fileName, content)) {
              message = prefixMsg + "File " + fileName + " written";
            } else {
              message = prefixMsg + "File " + fileName + " not stored";
            }

            break;
          case 4:
            // close file
            message = prefixMsg + "File " + fileName + " closed";
            process.getPCB().getOpenFiles().remove(fileName);
            break;

          default:
            break;
        }
        break;
      case _20H: // terminates process
        process.updateState(ProcessState.TERMINATED);
        message = prefixMsg + "Process " + process.getProcessId() + " terminated";
        InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
        break;
      case _10H: // prints dx
        int dx = registers[coreId].get(Register.DX);
        message = prefixMsg + "DX = " + dx;
        InterruptQueue.addMessage(new InterruptMessage(coreId, code, message, process.getProcessId()));
        break;
      case _09H: // input of a number between 0 and 255

        // send message to console
        message = prefixMsg + "Input requested";
        InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));

        InterruptQueue.addMessage(new InterruptMessage(coreId, code, "Input requested", process.getProcessId()));
        process.updateState(ProcessState.WAITING);
        CompletableFuture<String> inputFuture = UserInputHandler.requestInput(process.getProcessId());
        inputFuture.thenAccept(input -> {
          
          // prints that the input was received
          try {
            int inputInt = Integer.parseInt(input);
            if (inputInt < 0 || inputInt > 255) {
              String msg = prefixMsg + "The input must be a number between 0 and 255";
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, msg, process.getProcessId()));
            }
            else {
              String msg = prefixMsg + "Input received: " + input;
              InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, msg, process.getProcessId()));
              registers[coreId].put(Register.DX, inputInt);
            }
          } catch (NumberFormatException e) {
            String msg = prefixMsg + "Invalid input";
            InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, msg, process.getProcessId()));
          }
          process.updateState(ProcessState.READY);
        });
        break;
      
      case _08H:
        // input of a string
        message = prefixMsg + "Input requested";
        InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));

        InterruptQueue.addMessage(new InterruptMessage(coreId, code, "Input requested", process.getProcessId()));
        process.updateState(ProcessState.WAITING);
        CompletableFuture<String> inputFutureString = UserInputHandler.requestInput(process.getProcessId());
        inputFutureString.thenAccept(input -> {
          String msg = prefixMsg + "Input received: " + input;
          InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, msg, process.getProcessId()));

          int storedAddr = memory.storeString(input);
          registers[coreId].put(Register.BX, storedAddr);
          process.updateState(ProcessState.READY);
        });
        break;
    }
  }

  private boolean fileOpenedByOtherProcess(String fileName, int processId) {
    for (int i = 0; i < NUM_CORES; i++) {
      if (runningProcesses[i] != null && runningProcesses[i].getProcessId() != processId) {
        ProcessControlBlock pcb = runningProcesses[i].getPCB();
        if (pcb.getOpenFiles().contains(fileName)) {
          return true;
        }
      }
    }
    return false;
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
      if (!memory.deallocateMemory(id)) {
        System.out.println("Error deallocating memory for process " + id);
      }
      if (!memory.deleteBCP(id + "_bcp")) {
        System.out.println("Error freeing BCP for process " + id);
      }
      if (!memory.deallocateStack(id)) {
        System.out.println("Error deallocating stack for process " + id);
      }
      runningProcesses[index] = null;
      resetRegister(index);
    } else {
      throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }
  }

  private boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");
  }

  private String getNextInstruction(int coreId) {
    Process CurrentProcess = runningProcesses[coreId];
    String id = "P" + CurrentProcess.getProcessId();
    String res = memory.getInstruction(id, CurrentProcess.getCurrentInstructionIndex());
    CurrentProcess.setCurrentInstructionIndex(CurrentProcess.getCurrentInstructionIndex() + 1);
    return res;
  }

  private enum InstructionType {
    LOAD, STORE, MOV, ADD, SUB, INT, INC, DEC, SWAP, JMP, CMP, JE, JNE, PARAM, PUSH, POP
  }
}