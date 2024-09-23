package itcr.controllers;

import itcr.graphics.FileExplorer;
import itcr.model.FileInfo;
import itcr.model.MemoryManager;

import java.util.List;

public class FileExplorerController {
  private MemoryManager memoryManager;
  private FileExplorer fileExplorer;

  public FileExplorerController(MemoryManager memoryManager) {
    this.memoryManager = memoryManager;
  }

  public void setFileExplorer(FileExplorer fileExplorer) {
    this.fileExplorer = fileExplorer;
  }

  public List<FileInfo> getFileList() {
    return memoryManager.getFileList();
  }

  public boolean deleteFile(String fileName) {
    memoryManager.freeFile(fileName);

    String supposedDeletedFile = memoryManager.getFile(fileName);
    if (supposedDeletedFile == null) {
      if (fileExplorer != null) {
        fileExplorer.updateFileList();
      }
      return true;
    }
    return false;
  }

  public String getFileContent(String fileName) {
    return memoryManager.getFile(fileName);
  }

  public boolean saveFile(String fileName, String content) {
    if (memoryManager.storeFile(fileName, content)) {
      if (fileExplorer != null) {
        fileExplorer.updateFileList();
      }
      return true;
    } else {
      return false;
    }
  }

  public MemoryManager getMemoryManager() {
    return memoryManager;
  }

}
