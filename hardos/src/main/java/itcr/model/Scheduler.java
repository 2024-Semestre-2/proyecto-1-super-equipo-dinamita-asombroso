package itcr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.gson.JsonObject;

public class Scheduler {
  private Queue<Integer> readyQueue;
  private Queue<Integer> waitingQueue;
  private List<CPU> cpus;
  public MemoryManager memoryManager;
  private Map<Integer, Map<String, JsonObject>> cpuStats;
  private int numCPUs = 1;

  /**
   * Constructor for Scheduler with multiple CPUs.
   *
   * @param numCPUs       the number of CPUs to use
   * @param memoryManager the MemoryManager instance
   */
  public Scheduler(int numCPUs, MemoryManager memoryManager) {
    this.readyQueue = new LinkedList<>();
    this.waitingQueue = new LinkedList<>();
    this.cpus = new ArrayList<>(numCPUs);
    this.memoryManager = memoryManager;
    this.cpuStats = new HashMap<>();
    this.numCPUs = numCPUs;
    for (int i = 0; i < numCPUs; i++) {
      CPU cpu = new CPU(i, this, memoryManager);
      this.cpus.add(cpu);
      this.cpuStats.put(i, new HashMap<>());
    }
  }

  public void changeNumberCPUs(int numCPUs) {
    this.numCPUs = numCPUs;
    if (numCPUs < cpus.size()) {
      for (int i = cpus.size() - 1; i >= numCPUs; i--) {
        cpus.remove(i);
        cpuStats.remove(i);
      }
    } else if (numCPUs > cpus.size()) {
      for (int i = cpus.size(); i < numCPUs; i++) {
        CPU cpu = new CPU(i, this, memoryManager);
        cpus.add(cpu);
        cpuStats.put(i, new HashMap<>());
      }
    }
  }

  public int getNumCPUs() {
    return numCPUs;
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
    memoryManager.updateBCP("P" + processId, pcb.toJsonString());
  }

  /**
   * Schedules the next process to run on available cores.
   */
  public void scheduleNextProcess() {
    for (CPU cpu : cpus) {
      for (int coreId = 0; coreId < cpu.getNumCores(); coreId++) {
        if (cpu.isCoreAvailable(coreId) && !readyQueue.isEmpty()) {
          int nextProcessId = readyQueue.poll();
          String bcpJson = memoryManager.getBCP("P" + nextProcessId);
          ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
          pcb.setState(ProcessState.RUNNING);
          pcb.setCpuId(cpu.getCpuId());
          memoryManager.updateBCP("P" + nextProcessId, pcb.toJsonString());

          Process nextProcess = new Process(pcb);
          nextProcess.setQtyInstructions(memoryManager.getQtyInstructions("P" + nextProcessId));
          cpu.assignProcessToCore(nextProcess, coreId);

          // Set the next process reference
          if (!readyQueue.isEmpty()) {
            int nextInQueueId = readyQueue.peek();
            String nextBcpJson = memoryManager.getBCP("P" + nextInQueueId);
            ProcessControlBlock nextPcb = ProcessControlBlock.fromJsonString(nextBcpJson);
            pcb.setNextProcess(nextPcb);
            memoryManager.updateBCP("P" + nextProcessId, pcb.toJsonString());
          }
        }
      }
    }
  }

  /**
   * Executes instructions on all CPUs and cores.
   *
   * @throws Exception if an error occurs during execution
   */
  public void executeInstruction() throws Exception {
    for (CPU cpu : cpus) {
      cpu.executeInstructionOnAllCores();
    }

    checkWaitingProcesses();
    scheduleNextProcess();
  }

  private void checkWaitingProcesses() {
    List<Integer> readyProcessIds = new ArrayList<>();
    for (Integer processId : waitingQueue) {
      String bcpJson = memoryManager.getBCP("P" + processId);
      ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
      if (pcb.isReadyToRun()) {
        pcb.setState(ProcessState.READY);
        memoryManager.updateBCP("P" + processId, pcb.toJsonString());
        readyProcessIds.add(processId);
      }
    }
    waitingQueue.removeAll(readyProcessIds);
    readyQueue.addAll(readyProcessIds);
  }

  public Queue<Integer> getReadyQueue() {
    return new LinkedList<>(readyQueue);
  }

  /**
   * Moves a process to the waiting queue.
   *
   * @param processId the ID of the process to move
   */
  public void moveToWaiting(int processId) {
    String bcpJson = memoryManager.getBCP("P" + processId);
    ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
    pcb.setState(ProcessState.WAITING);
    memoryManager.updateBCP("P" + processId, pcb.toJsonString());
    waitingQueue.offer(processId);
  }

  // ----------------------------------------------
  // Stats related methods
  // ----------------------------------------------
  public void updateProcessStats(int cpuId, String processId, JsonObject stats) {
    cpuStats.computeIfAbsent(cpuId, k -> new HashMap<>()).put(processId, stats);
  }

  public Map<Integer, Map<String, JsonObject>> getAllCPUStats() {
    return new HashMap<>(cpuStats);
  }

  public boolean hasProcessesToExecute() {
    if (!readyQueue.isEmpty()) {
      return true;
    }

    // Verify if there are processes running on any core
    for (CPU cpu : cpus) {
      for (int i = 0; i < cpu.getNumCores(); i++) {
        if (cpu.getRunningProcess(i) != null) {
          return true;
        }
      }
    }

    return false;
  }

  // ----------------------------------------------
  // Not that important for the project
  // ----------------------------------------------

  /**
   * Gets the extra registers of a specific process running on a core of a
   * specific CPU.
   *
   * @param cpuId  the ID of the CPU
   * @param coreId the ID of the core
   * @return a string representation of the extra registers
   */
  public String getExtraRegisters(int cpuId, int coreId) {
    CPU cpu = cpus.get(cpuId);
    Process process = cpu.getRunningProcess(coreId);
    if (process == null || process.getPCB() == null) {
      return "";
    }

    int address = process.getPCB().getStackPointer();
    int currentInstrIndex = process.getCurrentInstructionIndex() - 1;

    int pcReg = memoryManager.getAddressFromInstruction("P" + process.getProcessId(), currentInstrIndex + 1);
    int irReg = memoryManager.getAddressFromInstruction("P" + process.getProcessId(), currentInstrIndex);

    process.getPCB().setProgramCounter(pcReg);
    memoryManager.updateBCP("P" + process.getProcessId(), process.getPCB().toJsonString());

    return "PC: " + pcReg + "\nSP: " + address + "\nIR: " + irReg;
  }

  /**
   * Gets the registers of a specific core of a specific CPU.
   *
   * @param cpuId  the ID of the CPU
   * @param coreId the ID of the core
   * @return a string representation of the registers
   */
  public String getRegisters(int cpuId, int coreId) {
    return cpus.get(cpuId).getRegisters(coreId);
  }

  /**
   * Resets the scheduler, clearing queues and resetting all CPUs and memory
   * manager.
   */
  public void reset() {
    readyQueue.clear();
    waitingQueue.clear();
    for (CPU cpu : cpus) {
      cpu.fullReset();
    }

    // Reset memory
    int mainMemorySize = memoryManager.getMainMemorySize();
    int secondaryMemorySize = memoryManager.getSecondaryMemorySize();
    int kernelSize = memoryManager.getKernelSize();
    int osSize = memoryManager.getOsSize();

    MemoryManager freshMemory = new MemoryManager(
        mainMemorySize,
        secondaryMemorySize,
        kernelSize,
        osSize);

    // Copy files from old memory to new memory
    for (String filename : memoryManager.getFiles()) {
      String fileContent = memoryManager.getFile(filename);
      if (!freshMemory.storeFile(filename, fileContent))
        System.out.println("Error storing file: " + filename);
    }

    // Free up memory
    this.memoryManager = freshMemory;
    for (CPU cpu : cpus) {
      cpu.memory = memoryManager;
    }
  }

  public int getTotalCores() {
    int totalCores = 0;
    for (CPU cpu : cpus) {
      totalCores += cpu.getNumCores();
    }
    return totalCores;
  }

}