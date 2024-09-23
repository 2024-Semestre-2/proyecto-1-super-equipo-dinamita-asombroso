package itcr.controllers;

import itcr.model.MemoryManager;

public class NotepadController {
  private MemoryManager memoryManager;
  private Runnable refreshFunction;

  public NotepadController(MemoryManager memoryManager, Runnable function) {
    this.memoryManager = memoryManager;
    this.refreshFunction = function;
  }

  public void saveFile(String fileName, String content) {
    memoryManager.storeFile(fileName, content);
    if (refreshFunction != null) {
      refreshFunction.run();
    }
  }

  public String getFileContent(String fileName) {
    return memoryManager.getFile(fileName);
  }
}
