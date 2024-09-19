package itcr.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import itcr.models.MemoryManager;

public class Scheduler {
  private Queue<Process> readyQueue;
  private Queue<Process> waitingQueue;
  public CPU cpu;
  private MemoryManager memoryManager;

  public Scheduler(CPU cpu, MemoryManager memoryManager) {
    this.readyQueue = new LinkedList<>();
    this.waitingQueue = new LinkedList<>();
    this.cpu = cpu;
    this.memoryManager = memoryManager;
  }

  public void addProcess(Process process) {
    readyQueue.offer(process);
    process.updateState(ProcessState.READY);
  }

  public void scheduleNextProcess() {
    for (int i = 0; i < cpu.getNumCores(); i++) {
      if (cpu.isCoreAvailable(i) && !readyQueue.isEmpty()) {
        Process nextProcess = readyQueue.poll();
        cpu.assignProcessToCore(nextProcess, i);
        nextProcess.updateState(ProcessState.RUNNING);
      }
    }
  }

  public void executeNextInstruction() throws Exception {
    cpu.executeInstruction(0);
  }

  public void handleProcessCompletion(Process process) {
    process.updateState(ProcessState.TERMINATED);
    // Liberate resources, stats, etc TODO
  }

  public void moveToWaiting(Process process) {
    waitingQueue.offer(process);
    process.updateState(ProcessState.WAITING);
  }

  public void checkWaitingProcesses() {
    Queue<Process> tempQueue = new LinkedList<>();
    while (!waitingQueue.isEmpty()) {
      Process process = waitingQueue.poll();
      if (processCanResume(process)) {
        readyQueue.offer(process);
        process.updateState(ProcessState.READY);
      } else {
        tempQueue.offer(process);
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
          if (process != null && process.getState() == ProcessState.RUNNING) {
            cpu.executeInstruction(coreId);
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

  private boolean processCanResume(Process process) {
    return true;
  }

  public boolean hasProcessesRunning() {
    return !(readyQueue.isEmpty() && waitingQueue.isEmpty());
  }

}