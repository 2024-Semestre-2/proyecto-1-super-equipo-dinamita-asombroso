package itcr.controllers;

import itcr.models.MemoryManager;
import itcr.models.FileInfo;
import itcr.graphics.FileExplorer;
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
      System.out.println("Archivo guardado exitosamente");
      return true;
    } else {
      System.out.println("No se pudo guardar el archivo");
      return false;
    }
  }

  public MemoryManager getMemoryManager() {
    return memoryManager;
  }

}
