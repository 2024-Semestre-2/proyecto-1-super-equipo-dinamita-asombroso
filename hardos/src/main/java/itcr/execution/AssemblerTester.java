package itcr.execution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import itcr.model.Assembler;

public class AssemblerTester {
  private static final String PATH_TO_TESTFILES_FOLDER = "C:\\Users\\rostr\\Documents\\GitHub\\proyecto-1-super-equipo-dinamita-asombroso\\test_files";

  public static void main(String[] args) {
    File folder = new File(PATH_TO_TESTFILES_FOLDER);
    File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".asm"));

    if (listOfFiles == null) {
      System.out.println("No se encontraron archivos .asm en la carpeta especificada.");
      return;
    }

    for (File file : listOfFiles) {
      System.out.println("Probando archivo: " + file.getName());
      try {
        String content = readFileContent(file.toPath());
        String validationResult = Assembler.validateFormat(content);

        if (validationResult == null) {
          System.out.println("El archivo es válido.");
        } else {
          System.out.println("Se encontraron errores en el archivo:");
          System.out.println(validationResult);
        }
      } catch (IOException e) {
        System.out.println("Error al leer el archivo: " + e.getMessage());
      }
      System.out.println("--------------------");
    }
  }

  private static String readFileContent(Path filePath) throws IOException {
    StringBuilder contentBuilder = new StringBuilder();
    try (Stream<String> stream = Files.lines(filePath)) {
      stream.forEach(s -> contentBuilder.append(s).append("\n"));
    }
    return contentBuilder.toString();
  }
}