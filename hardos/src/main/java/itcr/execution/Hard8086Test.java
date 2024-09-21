package itcr.execution;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

import itcr.model.*;
import itcr.models.*;

public class Hard8086Test {
  public static void main(String[] args) {
    List<String> instructions1 = new ArrayList<String>();
    instructions1.add("MOV AX, 555");
    instructions1.add("MOV BX, 999");
    instructions1.add("CMP BX, AX");
    instructions1.add("JNE 3");
    instructions1.add("MOV AX, 0");
    instructions1.add("STORE BX");
    instructions1.add("INC");
    instructions1.add("INC");
    instructions1.add("JMP -1");
    List<String> instructions2 = new ArrayList<String>();
    instructions2.add("MOV AX, 10");
    instructions2.add("MOV BX, 10");
    instructions2.add("CMP AX, BX");
    instructions2.add("JNE 3");
    instructions2.add("MOV CX, 666");
    instructions2.add("MOV DX, 666");
    instructions2.add("INC");
    instructions2.add("INT _20H");

    SwingUtilities.invokeLater(() -> {
      CPU cpu = new CPU();
      MemoryManager memoryManager = new MemoryManager(512, 256, 64, 128);
      Scheduler scheduler = new Scheduler(cpu, memoryManager);

      int processSize = instructions1.stream().mapToInt(String::length).sum();
      int processSize1 = instructions2.stream().mapToInt(String::length).sum();

      int baseAddress = memoryManager.allocateMemory("P0", processSize);
      int baseAddress1 = memoryManager.allocateMemory("P1", processSize1);

      for (int i = 0; i < instructions1.size(); i++) {
        memoryManager.storeInstruction("P0", instructions1.get(i));
      }

      for (int i = 0; i < instructions2.size(); i++) {
        memoryManager.storeInstruction("P1", instructions2.get(i));
      }

      memoryManager.printMemoryMap();
      // memoryManager.printAllInstructions("P0");
      // memoryManager.printAllInstructions("P1");

      itcr.model.Process process = new itcr.model.Process(instructions1);
      ProcessControlBlock pcb = new ProcessControlBlock(itcr.model.Process.processCounter++, baseAddress, processSize, 1);
      memoryManager.storeBCP("P0", pcb.toJsonString());
      process.setPCB(pcb);

      itcr.model.Process process1 = new itcr.model.Process(instructions2);
      ProcessControlBlock pcb1 = new ProcessControlBlock(itcr.model.Process.processCounter++, baseAddress1, processSize1, 1);
      memoryManager.storeBCP("P1", pcb1.toJsonString());
      process1.setPCB(pcb1);

      memoryManager.printAllBCPs();

      System.out.println("DELETE -----------");
      memoryManager.freeBCP("P1");
      memoryManager.printAllBCPs();
      System.out.println("RESTORE -----------");
      memoryManager.storeBCP("P1", pcb1.toJsonString());
      memoryManager.printAllBCPs();

      scheduler.addProcess(process);
      scheduler.addProcess(process1);

      new Hard8086(scheduler).setVisible(true);
    });
  }
}