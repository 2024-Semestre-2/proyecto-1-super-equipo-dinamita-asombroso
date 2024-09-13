package itcr.model;

import java.util.LinkedList;
import java.util.Queue;

public class Scheduler {
  private Queue<Process> readyQueue;
  private Queue<Process> waitingQueue;
  private CPU cpu;

  public Scheduler(CPU cpu) {
    this.readyQueue = new LinkedList<>();
    this.waitingQueue = new LinkedList<>();
    this.cpu = cpu;
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

  private boolean processCanResume(Process process) {
    return true;
  }

  public boolean hasProcessesRunning() {
    return !(readyQueue.isEmpty() && waitingQueue.isEmpty());
  }

}