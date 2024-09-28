package itcr.controllers;

import itcr.model.MemoryManager;

/**
 * The NotepadController class manages file operations for a notepad application,
 * including saving files and retrieving file content.
 */
public class NotepadController {
  private final MemoryManager memoryManager;
  private final Runnable refreshFunction;

  /**
   * Constructs a NotepadController with the specified MemoryManager and refresh function.
   *
   * @param memoryManager the memory manager to be used for file operations
   * @param refreshFunction a function to refresh the UI after file operations
   */
  public NotepadController(MemoryManager memoryManager, Runnable refreshFunction) {
    this.memoryManager = memoryManager;
    this.refreshFunction = refreshFunction;
  }

  /**
   * Saves a file with the specified name and content.
   *
   * @param fileName the name of the file
   * @param content the content to save in the file
   */
  public void saveFile(String fileName, String content) {
    memoryManager.storeFile(fileName, content);
    if (refreshFunction != null) {
      refreshFunction.run();
    }
  }

  /**
   * Retrieves the content of a file with the specified name.
   *
   * @param fileName the name of the file
   * @return the content of the file as a String
   */
  public String getFileContent(String fileName) {
    return memoryManager.getFile(fileName);
  }
}