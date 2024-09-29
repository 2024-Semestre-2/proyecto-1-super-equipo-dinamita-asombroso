package itcr.model;

/**
 * FileInfo class represents information about a file, including its name, start index, and size.
 */
public class FileInfo {
  String fileName;
  int startIndex;
  int size;

  /**
   * Constructor for FileInfo with start index and size.
   *
   * @param startIndex the start index of the file
   * @param size the size of the file
   */
  FileInfo(int startIndex, int size) {
    this.startIndex = startIndex;
    this.size = size;
  }

  /**
   * Constructor for FileInfo with file name and size.
   *
   * @param fileName the name of the file
   * @param size the size of the file
   */
  FileInfo(String fileName, int size) {
    this.fileName = fileName;
    this.size = size;
  }

  /**
   * Gets the name of the file.
   *
   * @return the name of the file
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the size of the file.
   *
   * @return the size of the file
   */
  public int getSize() {
    return size;
  }
}