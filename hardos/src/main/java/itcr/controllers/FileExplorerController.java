package itcr.controllers;

import itcr.graphics.FileExplorer;
import itcr.model.FileInfo;
import itcr.model.MemoryManager;

import java.util.List;

/**
 * The FileExplorerController class manages the interactions between the file explorer UI
 * and the memory manager, including file operations such as retrieving, saving, and deleting files.
 */
public class FileExplorerController {
  private final MemoryManager memoryManager;
  private FileExplorer fileExplorer;

  /**
   * Constructs a FileExplorerController with the specified MemoryManager.
   *
   * @param memoryManager the memory manager to be used for file operations
   */
  public FileExplorerController(MemoryManager memoryManager) {
    this.memoryManager = memoryManager;
  }

  /**
   * Sets the FileExplorer instance for this controller.
   *
   * @param fileExplorer the FileExplorer instance
   */
  public void setFileExplorer(FileExplorer fileExplorer) {
    this.fileExplorer = fileExplorer;
  }

  /**
   * Retrieves the list of files from the memory manager.
   *
   * @return a list of FileInfo objects representing the files
   */
  public List<FileInfo> getFileList() {
    return memoryManager.getFileList();
  }

  /**
   * Deletes a file with the specified name.
   *
   * @param fileName the name of the file to delete
   * @return true if the file was successfully deleted, false otherwise
   */
  public boolean deleteFile(String fileName) {
    memoryManager.freeFile(fileName);

    if (memoryManager.getFile(fileName) == null) {
      if (fileExplorer != null) {
        fileExplorer.updateFileList();
      }
      return true;
    }
    return false;
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

  /**
   * Saves a file with the specified name and content.
   *
   * @param fileName the name of the file
   * @param content the content to save in the file
   * @return true if the file was successfully saved, false otherwise
   */
  public boolean saveFile(String fileName, String content) {
    if (memoryManager.storeFile(fileName, content)) {
      if (fileExplorer != null) {
        fileExplorer.updateFileList();
      }
      return true;
    }
    return false;
  }

  /**
   * Retrieves the MemoryManager instance.
   *
   * @return the MemoryManager instance
   */
  public MemoryManager getMemoryManager() {
    return memoryManager;
  }
}