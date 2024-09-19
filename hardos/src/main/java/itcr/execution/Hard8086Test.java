package itcr.execution;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

import itcr.model.*;
import itcr.models.*;

public class Hard8086Test {
  public static void main(String[] args) {
    List<String> instructions = new ArrayList<>();
    instructions.add("MOV DX, 18");
    instructions.add("MOV AX, 3");
    instructions.add("LOAD DX");
    instructions.add("STORE BX");
    instructions.add("MOV DX, 343");
    instructions.add("MOV CX, 982");

    SwingUtilities.invokeLater(() -> {
      CPU cpu = new CPU();
      MemoryManager memoryManager = new MemoryManager();
      Scheduler scheduler = new Scheduler(cpu, memoryManager);

      int processSize = instructions.stream().mapToInt(String::length).sum();
      int baseAddress = memoryManager.allocateMemory("P1", processSize);

      itcr.model.Process process = new itcr.model.Process(baseAddress, processSize, 1, instructions);
      scheduler.addProcess(process);

      new Hard8086(cpu, scheduler, memoryManager).setVisible(true);
    });
  }
}