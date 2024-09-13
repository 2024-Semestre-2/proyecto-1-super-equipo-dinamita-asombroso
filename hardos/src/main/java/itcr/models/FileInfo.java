package itcr.models;

public class FileInfo {
  String fileName;
  int startIndex;
  int size;

  FileInfo(int startIndex, int size) {
    this.startIndex = startIndex;
    this.size = size;
  }

  FileInfo(String fileName, int size) {
    this.fileName = fileName;
    this.size = size;
  }

  public String getFileName() {
    return fileName;
  }

  public int getSize() {
    return size;
  }
}