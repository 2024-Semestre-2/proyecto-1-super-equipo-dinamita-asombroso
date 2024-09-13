package itcr.graphics;

import itcr.controllers.NotepadController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Notepad extends FloatingWindow<NotepadController> {
  private JTextArea textArea;
  private String fileName;

  public Notepad(JFrame parent, NotepadController controller, String fileName, String fileContent) {
    super(parent, fileName == null ? "Nuevo archivo" : "Editar archivo: " + fileName, controller);
    this.fileName = fileName;

    if (fileContent != null) {
      textArea.setText(fileContent);
    }
  }

  @Override
  protected void initComponents() {
    textArea = new JTextArea(20, 40);
    JScrollPane scrollPane = new JScrollPane(textArea);
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton saveButton = new JButton("Guardar");
    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveFile();
      }
    });
    buttonPanel.add(saveButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private void saveFile() {
    String content = textArea.getText();

    if (fileName == null) {
      // Try to get a file name from the user, if is new file
      fileName = JOptionPane.showInputDialog(this, "Ingrese el nombre del archivo:");
    }

    if (fileName != null && !fileName.trim().isEmpty()) {
      controller.saveFile(fileName, content);
      JOptionPane.showMessageDialog(this, "Archivo guardado correctamente.");
      dispose();
    } else {
      JOptionPane.showMessageDialog(this, "El nombre del archivo no puede estar vacío.");
    }
  }
}
