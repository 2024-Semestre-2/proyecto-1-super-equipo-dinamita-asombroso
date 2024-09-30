package itcr.model;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.EnumMap;
import java.util.function.BiConsumer;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;

/**
 * CPU class represents the central processing unit with multiple cores.
 * It manages the execution of processes and handles various instructions.
 */
public class CPU {
  private static final int NUM_CORES = 5;
  public Process[] runningProcesses;
  private String[] instructionRegisters;
  private EnumMap<Register, Integer>[] registers;
  public MemoryManager memory;
  public Map<String, String> statsForProcesses = new HashMap<>();
  private int cpuId;
  private Scheduler scheduler;

  // Flags
  private boolean zeroFlag = false;

  public CPU(int cpuId, Scheduler scheduler, MemoryManager memoryManager) {
    this.cpuId = cpuId;
    this.scheduler = scheduler;
    this.memory = memoryManager;
    runningProcesses = new Process[NUM_CORES];
    instructionRegisters = new String[NUM_CORES];
    registers = new EnumMap[NUM_CORES];
    initializeRegisters();
  }

  /**
   * Initializes the registers for each core.
   */
  private void initializeRegisters() {
    for (int i = 0; i < NUM_CORES; i++) {
      registers[i] = new EnumMap<>(Register.class);
      for (Register reg : Register.values()) {
        registers[i].put(reg, 0);
      }
    }
  }

  /**
   * Assigns a process to a specific core.
   *
   * @param process the process to assign
   * @param coreId  the ID of the core
   */
  public void assignProcessToCore(Process process, int coreId) {
    if (coreId >= 0 && coreId < NUM_CORES) {
      runningProcesses[coreId] = process;
      loadProcessContext(coreId);
    }
  }

  public void executeInstructionOnAllCores() throws Exception {
    for (int coreId = 0; coreId < NUM_CORES; coreId++) {
      if (runningProcesses[coreId] != null) {
        executeInstruction(coreId);
      }
    }
  }

  /**
   * Executes the next instruction for a specific core.
   *
   * @param coreId the ID of the core
   * @throws Exception if an error occurs during execution
   */
  public void executeInstruction(int coreId) throws Exception {
    Process process = runningProcesses[coreId];
    if (process == null)
      return;

    String instruction = getNextInstruction(coreId);
    instructionRegisters[coreId] = instruction;
    if (instruction == null) {
      dispatcher(coreId);
      return;
    }

    String[] parts = instruction.split(" ");

    InstructionType type = InstructionType.valueOf(parts[0]);
    if (!instructionHandlers.containsKey(type)) {
      String message = "Instruction type not recognized: " + type;
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      return;
    }
    instructionHandlers.get(type).accept(coreId, parts);

    ProcessControlBlock pcb = process.getPCB();
    pcb.incrementProgramCounter();
    if (pcb.getStartTime() == null) {
      pcb.setStartTime(Instant.now());
    }
    pcb.setlastStateChangeTime();
    pcb.updateCpuTimeUsed();
    pcb.setCpuId(this.cpuId);

    memory.updateBCP("P" + process.getProcessId(), process.getPCB().toJsonString());
    saveProcessContext(coreId);
  }

  private String getNextInstruction(int coreId) {
    Process CurrentProcess = runningProcesses[coreId];
    String id = "P" + CurrentProcess.getProcessId();
    String res = memory.getInstruction(id, CurrentProcess.getCurrentInstructionIndex());
    CurrentProcess.setCurrentInstructionIndex(CurrentProcess.getCurrentInstructionIndex() + 1);
    return res;
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

  /**
   * Terminates the process running on the specified core.
   * 
   * @param index the index of the core
   */
  public void dispatcher(int index) {
    if (index >= 0 && index < NUM_CORES) {
      Process currentProcess = runningProcesses[index];

      currentProcess.getPCB().updateState(ProcessState.TERMINATED);
      updateProcessBCP(currentProcess);

      String id = "P" + currentProcess.getProcessId();

      // Error handling for deallocation of memory
      if (!memory.deallocateMemory(id)) {
        sendInterruptMessage(index, InterruptCode._10H,
            "Error deallocating memory for process " + id, currentProcess.getProcessId());
      }
      if (!memory.deallocateStack(id)) {
        sendInterruptMessage(index, InterruptCode._10H,
            "Error deallocating stack for process " + id, currentProcess.getProcessId());
      }
      if (!memory.freeBCPFromOS(id)) {
        sendInterruptMessage(index, InterruptCode._10H,
            "Error freeing BCP from OS for process " + id, currentProcess.getProcessId());
      }

      // Save the stats for the process
      // sendInterruptMessage(index, InterruptCode._10H, getStats(index),
      // currentProcess.getProcessId());

      JsonObject stats = getStats(index);
      scheduler.updateProcessStats(this.cpuId, id, stats);

      runningProcesses[index] = null;
      resetRegister(index);
    } else {
      String message = "Index out of bounds: " + index;
      System.out.println(">> [Error / Not recognized core] " + message);
    }
  }

  public JsonObject getStats(int index) {
    Process currentProcess = runningProcesses[index];
    if (currentProcess == null)
      return new JsonObject();

    JsonObject stats = new JsonObject();
    stats.addProperty("cpuId", this.cpuId);
    stats.addProperty("coreId", index);
    stats.addProperty("processId", currentProcess.getProcessId());

    Instant start = currentProcess.getPCB().getStartTime();
    Instant nowUtc = Instant.now();

    LocalDateTime localDateTime = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
    LocalDateTime localDateTimeN = LocalDateTime.ofInstant(nowUtc, ZoneId.systemDefault());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
    stats.addProperty("startTime", localDateTime.format(formatter));
    stats.addProperty("finishTime", localDateTimeN.format(formatter));

    long diffInMillis = Duration.between(start, nowUtc).toMillis();
    stats.addProperty("totalCoreUsageTime", String.format("%d.%d", diffInMillis / 1000, diffInMillis % 1000));

    return stats;
  }

  /**
   * Updates the BCP of the process in memory. With whatever changes were made.
   * 
   * @param process the process to update
   */
  private void updateProcessBCP(Process process) {
    memory.updateBCP("P" + process.getProcessId(), process.getPCB().toJsonString());
  }

  /**
   * Updates the flags based on the result of an operation.
   * 
   * @param result the result of the operation
   */
  private void updateFlags(int result) {
    zeroFlag = result == 0;
    // signFlag = result < 0;
  }

  public boolean isCoreAvailable(int coreId) {
    return runningProcesses[coreId] == null;
  }

  public int getNumCores() {
    return NUM_CORES;
  }

  public Process getRunningProcess(int coreId) {
    return runningProcesses[coreId];
  }

  public String getRegisters(int coreId) {
    StringBuilder sb = new StringBuilder();
    for (Register reg : Register.values()) {
      sb.append(reg).append(": ").append(registers[coreId].get(reg)).append("\n");
    }
    return sb.toString();
  }

  /**
   * Terminates all processes and resets the CPU state with the memory.
   */
  public void fullReset() {
    // Reset all cores, processes, and registers
    for (int i = 0; i < NUM_CORES; i++) {
      runningProcesses[i] = null;
      resetRegister(i);
    }

    // Reset flags
    zeroFlag = false;
    // signFlag = false;
    // carryFlag = false;
    // overflowFlag = false;

    // Reset interrupt queue
    InterruptQueue.clear();

    // Reset user input handler
    UserInputHandler.reset();

    // Reset process stats
    this.statsForProcesses.clear();
  }

  public int getCpuId() {
    return cpuId;
  }

  // -------------------------------------------------------------
  // All instruction handlers are defined below this comment block
  // -------------------------------------------------------------

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

  /**
   * Handles the PUSH instruction.
   * Pushes the value in the AX register onto the stack.
   */
  private void handlePush(int coreId, String[] parts) {
    Process process = runningProcesses[coreId];
    String processId = "P" + process.getProcessId();
    ProcessControlBlock pcb = process.getPCB();
    int currentSP = pcb.getStackPointer();

    if (currentSP >= 4) {
      String message = "Stack overflow: maximum stack size is 5.";
      InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
      return;
    }

    int value = registers[coreId].get(Register.AX);

    if (memory.writeToStack(processId, currentSP + 1, value)) {
      pcb.setStackPointer(currentSP + 1);
    } else {
      String message = "Failed to push value to stack for process " + processId;
      InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
    }
  }

  /**
   * Handles the POP instruction.
   * Pops the top value from the stack into the specified register.
   */
  private void handlePop(int coreId, String[] parts) {
    Process process = runningProcesses[coreId];

    if (parts.length != 2) {
      String message = "POP instruction requires a register argument.";
      InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
    }

    Register targetRegister = null;
    try {
      targetRegister = Register.valueOf(parts[1].toUpperCase());
    } catch (IllegalArgumentException e) {
      String message = ("Invalid register: " + parts[1]);
      InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
      return; // Exit the method if the register is invalid
    }

    String processId = "P" + process.getProcessId();
    ProcessControlBlock pcb = process.getPCB();
    int currentSP = pcb.getStackPointer();

    if (currentSP < 0) {
      String message = ("Stack underflow: stack is empty.");
      InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
      return; // Exit the method if the stack is empty
    }

    int value = memory.popFromStack(processId, currentSP);
    registers[coreId].put(targetRegister, value);
    pcb.setStackPointer(currentSP - 1);
  }

  /**
   * Handles the PARAM instruction.
   * Pushes one to three parameters onto the stack.
   */
  private void handleParam(int coreId, String[] parts) {
    Process process = runningProcesses[coreId];
    String[] cleanParts = Arrays.stream(parts)
        .map(part -> part.replace(",", "").trim())
        .toArray(String[]::new);

    if (cleanParts.length < 2 || cleanParts.length > 4) {
      String message = ("PARAM instruction requires 1 to 3 parameters.");
      InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
    }

    String processId = "P" + process.getProcessId();
    ProcessControlBlock pcb = process.getPCB();
    int currentSP = pcb.getStackPointer();

    for (int i = 1; i < cleanParts.length; i++) {
      try {
        int value = Integer.parseInt(cleanParts[i]);
        if (currentSP >= 4) {
          String message = ("Stack overflow: maximum stack size is 5.");
          InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
        }
        if (memory.writeToStack(processId, currentSP + 1, value)) {
          currentSP++;
          pcb.setStackPointer(currentSP);
        } else {
          String message = ("Failed to write parameter to stack for process " + processId);
          InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
        }
      } catch (NumberFormatException e) {
        String message = ("Parameter must be a valid 32-bit integer");
        InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId()));
      }
    }
  }

  /**
   * Handles the JE (Jump if Equal) instruction.
   * Jumps to the specified instruction if the zero flag is set.
   */
  private void handleJe(int coreId, String[] parts) {
    if (zeroFlag) {
      handleJmp(coreId, parts);
    }
  }

  /**
   * Handles the JNE (Jump if Not Equal) instruction.
   * Jumps to the specified instruction if the zero flag is not set.
   */
  private void handleJne(int coreId, String[] parts) {
    if (!zeroFlag) {
      handleJmp(coreId, parts);
    }
  }

  /**
   * Handles the CMP (Compare) instruction.
   * Compares the values of two registers and sets the zero flag accordingly.
   */
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

  /**
   * Handles the JMP (Jump) instruction.
   * Jumps to the specified instruction index.
   */
  private void handleJmp(int coreId, String[] parts) {
    Process currentP = runningProcesses[coreId];

    int topIndex = currentP.getQtyInstructions();
    int currentIndex = currentP.getCurrentInstructionIndex() - 1;
    int val = Integer.valueOf(parts[1]);

    if (val > 0) {
      if (val + currentIndex >= topIndex) {
        String message = ("Desplazamiento invalido.");
        InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, currentP.getProcessId()));
      }
    } else {
      if (currentIndex + val < 0) {
        String message = ("Desplazamiento invalido.");
        InterruptQueue.addMessage(new InterruptMessage(coreId, InterruptCode._10H, message, currentP.getProcessId()));
      }
    }
    runningProcesses[coreId].setCurrentInstructionIndex(currentIndex + val);
  }

  /**
   * Handles the SWAP instruction.
   * Swaps the values of two registers.
   */
  private void handleSwap(int coreId, String[] parts) {
    Register reg1 = Register.valueOf(parts[1].replace(",", ""));
    Register reg2 = Register.valueOf(parts[2]);
    int temp = registers[coreId].get(reg1);
    registers[coreId].put(reg1, registers[coreId].get(reg2));
    registers[coreId].put(reg2, temp);
  }

  /**
   * Handles the DEC (Decrement) instruction.
   * Decrements the value of the specified register or the AC register if no
   * register is specified.
   */
  private void handleDec(int coreId, String[] parts) {
    if (parts.length == 1) {
      int result = registers[coreId].get(Register.AC) - 1;
      registers[coreId].put(Register.AC, result);
      updateFlags(result);
    } else {
      Register reg = Register.valueOf(parts[1]);
      int result = registers[coreId].get(reg) - 1;
      registers[coreId].put(reg, result);
      updateFlags(result);
    }
  }

  /**
   * Handles the INC (Increment) instruction.
   * Increments the value of the specified register or the AC register if no
   * register is specified.
   */
  private void handleInc(int coreId, String[] parts) {
    if (parts.length == 1) {
      int result = registers[coreId].get(Register.AC) + 1;
      registers[coreId].put(Register.AC, result);
      updateFlags(result);
    } else {
      Register reg = Register.valueOf(parts[1]);
      int result = registers[coreId].get(reg) + 1;
      registers[coreId].put(reg, result);
      updateFlags(result);
    }
  }

  /**
   * Handles the LOAD instruction.
   * Loads the value of the specified register into the AC register.
   */
  private void handleLoad(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    registers[coreId].put(Register.AC, registers[coreId].get(reg));
  }

  /**
   * Handles the STORE instruction.
   * Stores the value of the AC register into the specified register.
   */
  private void handleStore(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    registers[coreId].put(reg, registers[coreId].get(Register.AC));
  }

  /**
   * Handles the MOV (Move) instruction.
   * Moves the value from one register to another or sets a register to a
   * specified value.
   */
  private void handleMov(int coreId, String[] parts) {
    Register destReg = Register.valueOf(parts[1].replace(",", ""));
    if (parts.length == 3 && !isNumeric(parts[2])) {
      Register sourceReg = Register.valueOf(parts[2]);
      registers[coreId].put(destReg, registers[coreId].get(sourceReg));
    } else {
      registers[coreId].put(destReg, Integer.parseInt(parts[2]));
    }
  }

  /**
   * Handles the ADD instruction.
   * Adds the value of the specified register to the AC register.
   */
  private void handleAdd(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    int result = registers[coreId].get(Register.AC) + registers[coreId].get(reg);
    registers[coreId].put(Register.AC, result);
    updateFlags(result);
  }

  /**
   * Handles the SUB (Subtract) instruction.
   * Subtracts the value of the specified register from the AC register.
   */
  private void handleSub(int coreId, String[] parts) {
    Register reg = Register.valueOf(parts[1]);
    int result = registers[coreId].get(Register.AC) - registers[coreId].get(reg);
    registers[coreId].put(Register.AC, result);
    updateFlags(result);
  }

  // ------------------------------
  // Interrupt handling functions
  // ------------------------------

  /**
   * Handles an interrupt for a specific core.
   *
   * @param coreId the ID of the core
   * @param parts  the parts of the interrupt instruction
   */
  public void handleInterrupt(int coreId, String[] parts) {
    InterruptCode code = InterruptCode.valueOf(parts[1]);
    Process process = runningProcesses[coreId];
    String prefixMsg = "[ Core " + coreId + " ] >> ";

    switch (code) {
      case _21H:
        handleFileManagement(coreId, process, prefixMsg);
        break;
      case _20H:
        terminateProcess(coreId, process);
        break;
      case _10H:
        printDxValue(coreId, process);
        break;
      case _09H:
        handleNumericInput(coreId, process, prefixMsg);
        break;
      case _08H:
        handleStringInput(coreId, process, prefixMsg);
        break;
    }
  }

  /**
   * Handles file management operations for the specified core.
   *
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param prefixMsg the prefix message for the interrupt
   */
  private void handleFileManagement(int coreId, Process process, String prefixMsg) {
    int axValue = registers[coreId].get(Register.AX);
    int bxValue = registers[coreId].get(Register.BX);
    int cxValue = registers[coreId].get(Register.CX);

    String fileName = memory.getString(bxValue);
    String content = memory.getString(cxValue);

    switch (axValue) {
      case 0:
        createFile(coreId, process, fileName, prefixMsg);
        break;
      case 1:
        openFile(coreId, process, fileName, prefixMsg);
        break;
      case 2:
        readFile(coreId, process, fileName, prefixMsg);
        break;
      case 3:
        writeFile(coreId, process, fileName, content, prefixMsg);
        break;
      case 4:
        closeFile(coreId, process, fileName, prefixMsg);
        break;
      case 5:
        deleteFile(coreId, process, fileName, prefixMsg);
        break;
    }
  }

  /**
   * Creates a file with the specified name.
   * Sends an interrupt message with the result of the operation.
   * If the file already exists, sends an error message.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param fileName  the name of the file to create
   * @param prefixMsg the prefix message for the interrupt
   */
  private void createFile(int coreId, Process process, String fileName, String prefixMsg) {
    memory.createFile(fileName);
    String message = prefixMsg + "Archivo " + fileName + " creado";
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  /**
   * Opens a file with the specified name.
   * Sends an interrupt message with the result of the operation.
   * If the file is already opened by another process, sends an error message.
   * If the file does not exist, sends an error message.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param fileName  the name of the file to open
   * @param prefixMsg the prefix message for the interrupt
   */
  private void openFile(int coreId, Process process, String fileName, String prefixMsg) {
    if (fileOpenedByOtherProcess(fileName, process.getProcessId())) {
      String message = prefixMsg + "El archivo " + fileName + " ya está abierto por otro proceso";
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      return;
    }

    String existingFile = memory.getFile(fileName);
    if (existingFile == null) {
      String message = prefixMsg + "El archivo " + fileName + " no existe";
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      return;
    }

    process.getPCB().getOpenFiles().add(fileName);
    updateProcessBCP(process);
    String message = prefixMsg + "Archivo " + fileName + " abierto";
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  /**
   * Reads the content of a file with the specified name.
   * Sends an interrupt message with the result of the operation.
   * If the file is already opened by another process, sends an error message.
   * If the file does not exist, sends an error message.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param fileName  the name of the file to read
   * @param prefixMsg the prefix message for the interrupt
   */
  private void readFile(int coreId, Process process, String fileName, String prefixMsg) {
    if (fileOpenedByOtherProcess(fileName, process.getProcessId())) {
      String message = prefixMsg + "El archivo " + fileName + " ya está abierto por otro proceso";
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      return;
    }

    String fileContent = memory.getFile(fileName);
    if (fileContent == null) {
      String message = prefixMsg + "El archivo " + fileName + " no existe";
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      return;
    }

    String message = prefixMsg + "Contenido del archivo " + fileName + ": " + fileContent;
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  /**
   * Writes content to a file with the specified name.
   * Sends an interrupt message with the result of the operation.
   * If the file is already opened by another process, sends an error message.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param fileName  the name of the file to write
   * @param content   the content to write to the file
   * @param prefixMsg the prefix message for the interrupt
   */
  private void writeFile(int coreId, Process process, String fileName, String content, String prefixMsg) {
    if (fileOpenedByOtherProcess(fileName, process.getProcessId())) {
      String message = prefixMsg + "El archivo " + fileName + " ya está abierto por otro proceso";
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      return;
    }

    String message = memory.storeFile(fileName, content)
        ? prefixMsg + "Archivo " + fileName + " escrito"
        : prefixMsg + "Archivo " + fileName + " no almacenado";
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  /**
   * Closes a file with the specified name.
   * Sends an interrupt message with the result of the operation.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param fileName  the name of the file to close
   * @param prefixMsg the prefix message for the interrupt
   */
  private void closeFile(int coreId, Process process, String fileName, String prefixMsg) {
    process.getPCB().getOpenFiles().remove(fileName);
    updateProcessBCP(process);
    String message = prefixMsg + "Archivo " + fileName + " cerrado";
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  /**
   * Deletes a file with the specified name.
   * Sends an interrupt message with the result of the operation.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param fileName  the name of the file to delete
   * @param prefixMsg the prefix message for the interrupt
   */
  private void deleteFile(int coreId, Process process, String fileName, String prefixMsg) {
    process.getPCB().getOpenFiles().remove(fileName);
    updateProcessBCP(process);
    memory.freeFile(fileName);
    String message = prefixMsg + "Archivo " + fileName + " eliminado";
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  // ------------------------------
  // Utility functions

  /**
   * Just terminates the process.
   * And sends an interrupt message with the stats of the process.
   * 
   * @param coreId  the ID of the core
   * @param process the process to terminate
   */
  private void terminateProcess(int coreId, Process process) {
    process.updateState(ProcessState.TERMINATED);
    updateProcessBCP(process);
  }

  /**
   * prints the value of the DX register. From the interrupt.
   * 
   * @param coreId  the ID of the core
   * @param process the process to terminate
   */
  private void printDxValue(int coreId, Process process) {
    int dx = registers[coreId].get(Register.DX);
    String message = "[ Core " + coreId + " ] >> DX = " + dx;
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
  }

  /**
   * Handles the numeric input for the process.
   * Sends an interrupt message requesting input and then processes the input.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param prefixMsg the prefix message for the interrupt
   */
  private void handleNumericInput(int coreId, Process process, String prefixMsg) {
    String requestMessage = prefixMsg + "Entrada numérica solicitada";
    sendInterruptMessage(coreId, InterruptCode._10H, requestMessage, process.getProcessId());
    sendInterruptMessage(coreId, InterruptCode._09H, "Entrada numérica solicitada", process.getProcessId());

    process.updateState(ProcessState.WAITING);
    CompletableFuture<String> inputFuture = UserInputHandler.requestInput(process.getProcessId());
    inputFuture.thenAccept(input -> processNumericInput(coreId, process, input, prefixMsg));
  }

  /**
   * Processes the numeric input for the process.
   * Sends an interrupt message with the result of the operation.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param input     the input value
   * @param prefixMsg the prefix message for the interrupt
   */
  private void processNumericInput(int coreId, Process process, String input, String prefixMsg) {
    try {
      int inputInt = Integer.parseInt(input);
      if (inputInt < 0 || inputInt > 255) {
        String message = prefixMsg + "La entrada debe ser un número entre 0 y 255";
        sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
      } else {
        String message = prefixMsg + "Entrada recibida: " + input;
        sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
        registers[coreId].put(Register.DX, inputInt);
      }
    } catch (NumberFormatException e) {
      String message = prefixMsg + "Entrada inválida";
      sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());
    }
    process.updateState(ProcessState.RUNNING);
    updateProcessBCP(process);
  }

  /**
   * Handles the string input for the process.
   * Sends an interrupt message requesting input and then processes the input.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param prefixMsg the prefix message for the interrupt
   */
  private void handleStringInput(int coreId, Process process, String prefixMsg) {
    String requestMessage = prefixMsg + "Entrada solicitada";
    sendInterruptMessage(coreId, InterruptCode._10H, requestMessage, process.getProcessId());
    sendInterruptMessage(coreId, InterruptCode._08H, "Entrada solicitada", process.getProcessId());

    process.updateState(ProcessState.WAITING);
    CompletableFuture<String> inputFuture = UserInputHandler.requestInput(process.getProcessId());
    inputFuture.thenAccept(input -> processStringInput(coreId, process, input, prefixMsg));
  }

  /**
   * Processes the string input for the process.
   * Sends an interrupt message with the result of the operation.
   * 
   * @param coreId    the ID of the core
   * @param process   the process associated with the core
   * @param input     the input value
   * @param prefixMsg the prefix message for the interrupt
   */
  private void processStringInput(int coreId, Process process, String input, String prefixMsg) {
    String message = prefixMsg + "Entrada recibida: " + input;
    sendInterruptMessage(coreId, InterruptCode._10H, message, process.getProcessId());

    int storedAddr = memory.storeString(input);
    registers[coreId].put(Register.BX, storedAddr);
    process.updateState(ProcessState.RUNNING);
    updateProcessBCP(process);
  }

  /**
   * Sends an interrupt message with the specified code, message, and process ID.
   * 
   * @param coreId    the ID of the core
   * @param code      the interrupt code
   * @param message   the message to send
   * @param processId the ID of the process
   */
  private void sendInterruptMessage(int coreId, InterruptCode code, String message, int processId) {
    InterruptQueue.addMessage(new InterruptMessage(coreId, code, message, processId));
  }

  /**
   * Check if a file name is already opened by another process.
   * 
   * @param fileName  the name of the file
   * @param processId the ID of the process to exclude
   * @return true if the file is opened by another process, false otherwise
   */
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

  /**
   * Resets the registers for a specific core.
   *
   * @param index the index of the core
   */
  private void resetRegister(int index) {
    registers[index] = new EnumMap<>(Register.class);
    for (Register reg : Register.values()) {
      registers[index].put(reg, 0);
    }
  }

  private boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");
  }

  private enum InstructionType {
    LOAD, STORE, MOV, ADD, SUB, INT, INC, DEC, SWAP, JMP, CMP, JE, JNE, PARAM, PUSH, POP
  }
}