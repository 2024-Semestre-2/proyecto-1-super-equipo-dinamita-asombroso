package itcr.execution;

import itcr.models.MemoryManager;

public class MemoryManagementTest {

  private MemoryManager memoryManager;

  public void setUp() {
    memoryManager = new MemoryManager(64, 128);
  }

  public void testStoreAndRetrieveInstruction() {
    String processName = "process1";
    String instruction = "MOV AX, 5";

    boolean stored = memoryManager.storeInstruction(processName, instruction);
    String retrievedInstruction = memoryManager.getInstruction(processName, 0);

    System.out.println("Test Store and Retrieve Instruction:");
    System.out.println("Stored: " + stored);
    System.out.println("Retrieved: " + retrievedInstruction);
    System.out.println("Test Passed: " + (stored && instruction.equals(retrievedInstruction)));
  }

  public void testStoreAndRetrieveNumber() {
    String processName = "process1";
    int number = 42;

    boolean stored = memoryManager.storeNumber(processName, number);
    Integer retrievedNumber = memoryManager.getNumber(processName, 0);

    System.out.println("\nTest Store and Retrieve Number:");
    System.out.println("Stored: " + stored);
    System.out.println("Retrieved: " + retrievedNumber);
    System.out.println("Test Passed: " + (stored && number == retrievedNumber));
  }

  public void testStoreAndRetrieveBCP() {
    String processName = "process1";
    String bcpString = "bcp(process1){estado:listo,tiempo_llegada:0,tiempo_cpu:5}";

    boolean stored = memoryManager.storeBCP(processName, bcpString);
    String retrievedBCP = memoryManager.getBCP(processName);

    System.out.println("\nTest Store and Retrieve BCP:");
    System.out.println("Stored: " + stored);
    System.out.println("Retrieved: " + retrievedBCP);
    System.out.println("Test Passed: " + (stored && bcpString.equals(retrievedBCP)));
  }

  public void testStoreAndRetrieveFile() {
    String fileName = "test.txt";
    String fileContent = "Este es un archivo de prueba.";

    boolean stored = memoryManager.storeFile(fileName, fileContent);
    String retrievedFile = memoryManager.getFile(fileName);

    System.out.println("\nTest Store and Retrieve File:");
    System.out.println("Stored: " + stored);
    System.out.println("Retrieved: " + retrievedFile);
    System.out.println("Test Passed: " + (stored && fileContent.equals(retrievedFile)));
  }

  public void testMemoryOverflow() {
    String processName = "largeProcess";
    StringBuilder largeInstruction = new StringBuilder();
    for (int i = 0; i < 70 * 1024; i++) {
      largeInstruction.append("A");
    }

    boolean stored = memoryManager.storeInstruction(processName, largeInstruction.toString());

    System.out.println("\nTest Memory Overflow:");
    System.out.println("Attempt to store large instruction: " + stored);
    System.out.println("Test Passed: " + !stored);
  }

  public void testMultipleInstructions() {
    String processName = "process1";
    String[] instructions = { "MOV AX, 5", "ADD AX, 10", "PUSH AX" };

    System.out.println("\nTest Multiple Instructions:");
    for (int i = 0; i < instructions.length; i++) {
      boolean stored = memoryManager.storeInstruction(processName, instructions[i]);
      String retrievedInstruction = memoryManager.getInstruction(processName, i);

      System.out
          .println("Stored: " + stored + "| Instruction: " + instructions[i] + "| Retrieved: " + retrievedInstruction);
      System.out.println("Test Passed: " + (stored && instructions[i].equals(retrievedInstruction)));
    }

    System.out.println("\nAll instructions after storing:");
    memoryManager.printAllInstructions(processName);
  }

  public void testFileOperations() {
    String fileName = "test.asm";
    String fileContent = "section .text\n    global _start\n_start:\n    mov eax, 1\n    int 0x80";

    boolean stored = memoryManager.storeFile(fileName, fileContent);
    String retrievedFile = memoryManager.getFile(fileName);

    System.out.println("\nTest File Operations:");
    System.out.println("Stored: " + stored);
    System.out.println("Retrieved: " + retrievedFile);

    memoryManager.freeFile(fileName);
    retrievedFile = memoryManager.getFile(fileName);

    System.out.println("File freed. Retrieved after free: " + retrievedFile);
    System.out.println("Test Passed: " + (stored && fileContent.trim().equals(retrievedFile) && retrievedFile == null));
  }

  public static void main(String[] args) {
    MemoryManagementTest test = new MemoryManagementTest();

    // comentario == passed

    test.setUp();
    test.testStoreAndRetrieveInstruction();
    test.testStoreAndRetrieveNumber();
    test.testStoreAndRetrieveBCP();
    test.testStoreAndRetrieveFile();
    test.testMemoryOverflow();
    test.testMultipleInstructions();
    test.testFileOperations();
  }
}