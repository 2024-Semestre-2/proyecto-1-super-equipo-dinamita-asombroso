package itcr.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

  public void executeInstruction() throws Exception {
    Thread Thread1 = new Thread(() -> { try {this.cpu.executeInstruction(0);} catch (Exception e) {e.printStackTrace();} });
    Thread Thread2 = new Thread(() -> { try {this.cpu.executeInstruction(1);} catch (Exception e) {e.printStackTrace();} });
    Thread Thread3 = new Thread(() -> { try {this.cpu.executeInstruction(2);} catch (Exception e) {e.printStackTrace();} });
    Thread Thread4 = new Thread(() -> { try {this.cpu.executeInstruction(3);} catch (Exception e) {e.printStackTrace();} });
    Thread Thread5 = new Thread(() -> { try {this.cpu.executeInstruction(4);} catch (Exception e) {e.printStackTrace();} });
    Thread1.start();
    Thread2.start();
    Thread3.start();
    Thread4.start();
    Thread5.start();
    Thread1.join();
    Thread2.join();
    Thread3.join();
    Thread4.join();
    Thread5.join();
    /*
    for (String register : getCoreRegisters()) {
      System.out.println("Registro: " + register);
    }*/
  } 

  public List<String> getCoreRegisters() {
    List<String> res = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      res.add(cpu.getCoreRegister(i));
    }
    return res;
  } 
  private boolean processCanResume(Process process) {
    return true;
  }

  public boolean hasProcessesRunning() {
    return !(readyQueue.isEmpty() && waitingQueue.isEmpty());
  }

}