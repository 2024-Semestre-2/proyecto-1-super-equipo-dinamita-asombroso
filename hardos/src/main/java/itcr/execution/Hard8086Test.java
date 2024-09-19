package itcr.execution;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import itcr.model.*;
import itcr.models.*;

public class Hard8086Test {
  public static void main(String[] args) {
    List<String> instructions1  = new ArrayList<String>();
    instructions1.add("MOV DX, 18");
    instructions1.add("MOV AX, 3");
    instructions1.add("LOAD DX");
    instructions1.add("STORE BX");
    instructions1.add("MOV DX, 343");
    instructions1.add("MOV CX, 982");
    List<String> instructions2  = new ArrayList<String>();
    instructions2.add("MOV AX, 187");
    instructions2.add("ADD AX");
    instructions2.add("ADD AX");
    instructions2.add("MOV BX, 153");
    List<String> instructions3  = new ArrayList<String>();
    instructions3.add("MOV CX, 98");
    instructions3.add("MOV BX, 7");
    instructions3.add("LOAD CX");
    instructions3.add("SUB BX");
    instructions3.add("SUB BX");
    instructions3.add("MOV AX, 187");
    instructions3.add("ADD AX");
    instructions3.add("ADD AX");
    instructions3.add("MOV BX, 153");
    List<String> instructions4  = new ArrayList<String>();
    instructions4.add("MOV BX, 23");
    instructions4.add("MOV DX, 91");
    instructions4.add("ADD AX");
    instructions4.add("ADD AX");
    List<String> instructions5  = new ArrayList<String>();
    instructions5.add("MOV AX, 17");
    instructions5.add("MOV BX, 18");
    instructions5.add("MOV CX, BX");
    instructions5.add("MOV CX, BX");

    SwingUtilities.invokeLater(() -> {
      CPU cpu = new CPU();
      MemoryManager memoryManager = new MemoryManager();
      Scheduler scheduler = new Scheduler(cpu, memoryManager);

      int processSize = instructions1.stream().mapToInt(String::length).sum();
      int processSize1 = instructions2.stream().mapToInt(String::length).sum();
      int processSize2 = instructions3.stream().mapToInt(String::length).sum();
      int processSize3 = instructions4.stream().mapToInt(String::length).sum();
      int processSize4 = instructions5.stream().mapToInt(String::length).sum();

      int baseAddress = memoryManager.allocateMemory("P0", processSize);
      int baseAddress1 = memoryManager.allocateMemory("P1", processSize1);
      int baseAddress2 = memoryManager.allocateMemory("P2", processSize2);
      int baseAddress3 = memoryManager.allocateMemory("P3", processSize3);
      int baseAddress4 = memoryManager.allocateMemory("P4", processSize4);

        for (int i = 0; i < instructions1.size(); i++) {
          memoryManager.storeInstruction("P0", instructions1.get(i));
        } 
      

      cpu.memory = memoryManager;

      itcr.model.Process process = new itcr.model.Process(baseAddress, processSize, 1, instructions1);
      itcr.model.Process process1 = new itcr.model.Process(baseAddress1, processSize1, 1, instructions2);
      itcr.model.Process process2 = new itcr.model.Process(baseAddress2, processSize2, 1, instructions3);
      itcr.model.Process process3 = new itcr.model.Process(baseAddress3, processSize3, 1, instructions4);
      itcr.model.Process process4 = new itcr.model.Process(baseAddress4, processSize4, 1, instructions5);
      scheduler.addProcess(process);
      scheduler.addProcess(process1);
      scheduler.addProcess(process2);
      scheduler.addProcess(process3);
      scheduler.addProcess(process4);

      List<String> instructions6  = new ArrayList<String>();
      instructions6.add("MOV AX, 666");
      instructions6.add("MOV BX, 666");
      instructions6.add("MOV CX, 666");
      instructions6.add("MOV CX, 666");
      int processSize6 = instructions6.stream().mapToInt(String::length).sum();
      int baseAddress6 = memoryManager.allocateMemory("P6", processSize6);
      itcr.model.Process process6 = new itcr.model.Process(baseAddress6, processSize6, 1, instructions6);

      List<String> instructions7  = new ArrayList<String>();
      instructions7.add("MOV AX, 766");
      instructions7.add("MOV BX, 766");
      instructions7.add("MOV CX, 766");
      int processSize7 = instructions7.stream().mapToInt(String::length).sum();
      int baseAddress7 = memoryManager.allocateMemory("P7", processSize7);
      itcr.model.Process process7 = new itcr.model.Process(baseAddress7, processSize7, 1, instructions7);
      
      scheduler.addProcess(process6);
      scheduler.addProcess(process7);


      memoryManager.printAllInstructions("P0");
    

      new Hard8086(cpu, scheduler, memoryManager).setVisible(true);
    });
  }
}