package itcr.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Scheduler class manages the scheduling of processes, including ready and
 * waiting queues, and interaction with the CPU and MemoryManager.
 */
public class Scheduler {
  private Queue<Integer> readyQueue;
  private Queue<Integer> waitingQueue;
  public CPU cpu;
  private MemoryManager memoryManager;

  /**
   * Constructor for Scheduler.
   *
   * @param cpu           the CPU instance
   * @param memoryManager the MemoryManager instance
   */
  public Scheduler(CPU cpu, MemoryManager memoryManager) {
    this.readyQueue = new LinkedList<>();
    this.waitingQueue = new LinkedList<>();
    this.cpu = cpu;
    this.memoryManager = memoryManager;
    this.cpu.memory = memoryManager;
  }

  /**
   * Gets the registers of a specific core.
   *
   * @param coreId the ID of the core
   * @return a string representation of the registers
   */
  public String getRegisters(int coreId) {
    return cpu.getRegisters(coreId);
  }

  /**
   * Gets the registers of a specific process running on a core.
   *
   * @param index  the index of the register
   * @param coreId the ID of the core
   * @return a string representation of the registers
   */
  public String getRegisters(int index, int coreId) {
    itcr.model.Process process = cpu.getRunningProcess(coreId);
    if (process == null) {
      return "";
    }
    if (process.getPCB() == null) {
      return "";
    }

    int address = process.getPCB().getStackPointer();
    int currentInstrIndex = cpu.getRunningProcess(coreId).getCurrentInstructionIndex() - 1;

    int pcReg = memoryManager.getAddressFromInstruction("P" + process.getProcessId(), currentInstrIndex + 1);
    int irReg = memoryManager.getAddressFromInstruction("P" + process.getProcessId(), currentInstrIndex);

    process.getPCB().setProgramCounter(pcReg);
    memoryManager.updateBCP("P" + process.getProcessId(), process.getPCB().toJsonString());

    return "PC: " + pcReg + "\nStackPointer: " + address + "\nIR: " + irReg;
  }

  /**
   * Adds a process to the ready queue.
   *
   * @param process the process to add
   */
  public void addProcess(Process process) {
    int processId = process.getPCB().getProcessId();
    readyQueue.offer(processId);
    ProcessControlBlock pcb = process.getPCB();
    pcb.setState(ProcessState.READY);
    // memoryManager.storeBCP("P" + processId, pcb.toJsonString()); // 0
  }

  /**
   * Schedules the next process to run on available cores.
   */
  public void scheduleNextProcess() {
    for (int i = 0; i < cpu.getNumCores(); i++) {
      if (cpu.isCoreAvailable(i) && !readyQueue.isEmpty()) {
        int nextProcessId = readyQueue.poll();
        String bcpJson = memoryManager.getBCP("P" + nextProcessId);
        ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
        pcb.setState(ProcessState.RUNNING);
        memoryManager.updateBCP("P" + nextProcessId, pcb.toJsonString()); // 1

        Process nextProcess = new itcr.model.Process(pcb);
        nextProcess.setQtyInstructions(memoryManager.getQtyInstructions("P" + nextProcessId));
        cpu.assignProcessToCore(nextProcess, i);
      }
    }
  }

  /**
   * Executes the next instruction on the first core.
   *
   * @throws Exception if an error occurs during execution
   */
  public void executeNextInstruction() throws Exception {
    cpu.executeInstruction(0);
  }

  /**
   * Gets the status of a specific core.
   *
   * @param id the ID of the core
   * @return a string representation of the core status
   */
  public String getCoreStatus(int id) {
    return cpu.getCoreStatus(id);
  }

  /**
   * Executes instructions on all cores.
   *
   * @throws Exception if an error occurs during execution
   */
  public void executeInstruction() throws Exception {
    Thread[] threads = new Thread[cpu.getNumCores()];

    for (int i = 0; i < cpu.getNumCores(); i++) {
      final int coreId = i;
      threads[i] = new Thread(() -> {
        try {
          Process process = cpu.getRunningProcess(coreId);
          if (process != null) {
            String bcpJson = memoryManager.getBCP("P" + process.getPCB().getProcessId());

            ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
            if (pcb != null) {
              if (pcb.getState() == ProcessState.RUNNING) {
                cpu.executeInstruction(coreId);
                if (pcb.getState() != ProcessState.WAITING) {
                  pcb.incrementProgramCounter();
                  // memoryManager.storeBCP("P" + process.getPCB().getProcessId(),
                  // pcb.toJsonString()); 6
                }
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      threads[i].start();
    }

    // Wait for all threads to complete
    for (Thread thread : threads) {
      thread.join();
    }

    // checkWaitingProcesses();
    scheduleNextProcess();
  }

  /**
   * Gets the registers of all cores.
   *
   * @return a list of string representations of the registers for each core
   */
  public List<String> getCoreRegisters() {
    List<String> registers = new ArrayList<>();
    for (int i = 0; i < cpu.getNumCores(); i++) {
      registers.add("Core " + i + ": " + cpu.getRegisters(i));
    }
    return registers;
  }

  /**
   * Gets the content of a file from the memory manager.
   *
   * @param filename the name of the file
   * @return the content of the file
   */
  public String getFileContent(String filename) {
    return memoryManager.getFile(filename);
  }

  /**
   * Gets the memory manager.
   *
   * @return the memory manager
   */
  public MemoryManager getMemoryManager() {
    return memoryManager;
  }

  /**
   * Resets the scheduler, clearing queues and resetting the CPU and memory
   * manager.
   */
  public void reset() {
    readyQueue.clear();
    waitingQueue.clear();
    cpu.fullReset();

    for (String filename : memoryManager.getFiles()) {
      String fileContent = memoryManager.getFile(filename);
      if (cpu.memory.storeFile(filename, fileContent)) {

      } else {

      }
    }

    this.memoryManager = cpu.memory;
  }
}