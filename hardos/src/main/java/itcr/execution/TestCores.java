
package itcr.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import itcr.model.*;
import itcr.models.*;
import itcr.model.Process;

/**
 *
 * @author truez
 */

/*
 *      List<String> instructions  = new ArrayList<String>();
        instructions.add("LOAD AX");
        instructions.add("STORE BX");
        instructions.add("MOV BX, AX");
        instructions.add("MOV BX, 5");
        instructions.add("ADD BX");
        instructions.add("SUB BX");
 */

public class TestCores {

    public static void main(String[] args) throws Exception {
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
        List<String> instructions4  = new ArrayList<String>();
        instructions4.add("MOV BX, 23");
        instructions4.add("MOV DX, 91");
        instructions4.add("INC");
        instructions4.add("INC BX");
        instructions4.add("DEC DX");
        List<String> instructions5  = new ArrayList<String>();
        instructions5.add("MOV AX, 17");
        instructions5.add("MOV BX, 18");
        instructions5.add("SWAP AX, BX");
        
        MemoryManager mm = new MemoryManager(500, 1000);
        mm.storeInstruction("process0", "LOAD AX");

        Process p1 = new Process(0, 0, 0, instructions1);
        Process p2 = new Process(0, 0, 0, instructions2);
        Process p3 = new Process(0, 0, 0, instructions3);
        Process p4 = new Process(0, 0, 0, instructions4);
        Process p5 = new Process(0, 0, 0, instructions5);

        CPU cpu = new CPU();
        Scheduler scheduler = new Scheduler(cpu);
        scheduler.addProcess(p1);
        scheduler.addProcess(p2);
        scheduler.addProcess(p3);
        scheduler.addProcess(p4);
        scheduler.addProcess(p5);
        scheduler.scheduleNextProcess();

        scheduler.executeInstruction();
        scheduler.executeInstruction();
        scheduler.executeInstruction();
        scheduler.executeInstruction();
        scheduler.executeInstruction();
        scheduler.executeInstruction();


        for (String register : scheduler.getCoreRegisters()) {
            System.out.println("Registro: " + register);
        }

    }
}
