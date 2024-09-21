package itcr.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import itcr.models.MemoryManager;

public class Scheduler {
  private Queue<Integer> readyQueue;
  private Queue<Integer> waitingQueue;
  public CPU cpu;
  private MemoryManager memoryManager;

  public Scheduler(CPU cpu, MemoryManager memoryManager) {
    this.readyQueue = new LinkedList<>();
    this.waitingQueue = new LinkedList<>();
    this.cpu = cpu;
    this.memoryManager = memoryManager;
    this.cpu.memory = memoryManager;
  }

  public String getRegisters(int coreId) {
    return cpu.getRegisters(coreId);
  }

  public void addProcess(Process process) {
    int processId = process.getPCB().getProcessId();
    readyQueue.offer(processId);
    ProcessControlBlock pcb = process.getPCB();
    pcb.setState(ProcessState.READY);
    memoryManager.storeBCP("P" + processId, pcb.toJsonString());
  }

  public void scheduleNextProcess() {
    for (int i = 0; i < cpu.getNumCores(); i++) {
      if (cpu.isCoreAvailable(i) && !readyQueue.isEmpty()) {
        int nextProcessId = readyQueue.poll();
        String bcpJson = memoryManager.getBCP("P" + nextProcessId);
        ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
        pcb.setState(ProcessState.RUNNING);
        memoryManager.storeBCP("P" + nextProcessId, pcb.toJsonString());
        System.out.println("Running process P" + nextProcessId);
        memoryManager.printAllInstructions("P" + nextProcessId);

        Process nextProcess = new itcr.model.Process(pcb);
        cpu.assignProcessToCore(nextProcess, i);
      }
    }
  }

  public void executeNextInstruction() throws Exception {
    cpu.executeInstruction(0);
  }

  public void handleProcessCompletion(Process process) {
    int processId = process.getPCB().getProcessId();
    String bcpJson = memoryManager.getBCP("P" + processId);
    ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
    pcb.setState(ProcessState.TERMINATED);
    memoryManager.storeBCP("P" + processId, pcb.toJsonString());
    // Liberate resources, update stats, etc.
  }

  public void moveToWaiting(Process process) {
    int processId = process.getPCB().getProcessId();
    waitingQueue.offer(processId);
    String bcpJson = memoryManager.getBCP("P" + processId);
    ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
    pcb.setState(ProcessState.WAITING);
    memoryManager.storeBCP("P" + processId, pcb.toJsonString());
  }

  public void checkWaitingProcesses() {
    Queue<Integer> tempQueue = new LinkedList<>();
    while (!waitingQueue.isEmpty()) {
      int processId = waitingQueue.poll();
      if (processCanResume(processId)) {
        readyQueue.offer(processId);
        String bcpJson = memoryManager.getBCP("P" + processId);
        ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
        pcb.setState(ProcessState.READY);
        memoryManager.storeBCP("P" + processId, pcb.toJsonString());
      } else {
        tempQueue.offer(processId);
      }
    }
    waitingQueue = tempQueue;
  }

  public void executeInstruction() throws Exception {
    Thread[] threads = new Thread[cpu.getNumCores()];

    for (int i = 0; i < cpu.getNumCores(); i++) {
      final int coreId = i;
      threads[i] = new Thread(() -> {
        try {
          Process process = cpu.getRunningProcess(coreId);
          System.out.println("Core " + coreId + " running process: " + process);
          if (process != null) {
            String bcpJson = memoryManager.getBCP("P" + process.getPCB().getProcessId());
            ProcessControlBlock pcb = ProcessControlBlock.fromJsonString(bcpJson);
            if (pcb.getState() == ProcessState.RUNNING) {
              cpu.executeInstruction(coreId);
              pcb.incrementProgramCounter();
              memoryManager.storeBCP("P" + process.getPCB().getProcessId(), pcb.toJsonString());
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

    checkWaitingProcesses();
    scheduleNextProcess();
  }

  public List<String> getCoreRegisters() {
    List<String> registers = new ArrayList<>();
    for (int i = 0; i < cpu.getNumCores(); i++) {
      registers.add("Core " + i + ": " + cpu.getRegisters(i));
    }
    return registers;
  }

  private boolean processCanResume(int processId) {
    // Implement logic to determine if a process can resume
    return true;
  }

  public boolean hasProcessesRunning() {
    return !(readyQueue.isEmpty() && waitingQueue.isEmpty());
  }
}