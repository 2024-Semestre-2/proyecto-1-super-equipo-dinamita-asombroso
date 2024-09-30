package itcr.graphics;

import itcr.controllers.MyPcConfigController;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * MyPcConfig class represents a custom floating window for configuring PC
 * settings.
 * It allows users to set various memory sizes and load configurations from a
 * file.
 */
public class MyPcConfig extends FloatingWindow<MyPcConfigController> {
  private JTextField kernelSizeField;
  private JTextField osSizeField;
  private JTextField mainMemorySizeField;
  private JTextField secondaryMemorySizeField;
  private JTextField virtualMemorySizeField;
  private JTextField numCPUsField;
  private JButton loadConfigButton;

  /**
   * Constructor for MyPcConfig.
   * Initializes the configuration window with the given parent frame and
   * controller.
   *
   * @param parent     the parent frame
   * @param controller the controller to manage the configuration
   */
  public MyPcConfig(JFrame parent, MyPcConfigController controller) {
    super(parent, "Configuración de Mi PC", controller);
  }

  /**
   * Initializes the components of the configuration window.
   */
  @Override
  protected void initComponents() {
    JPanel mainPanel = new JPanel(new GridLayout(7, 2));

    mainPanel.add(new JLabel("Tamaño del Kernel:"));
    kernelSizeField = new JTextField(String.valueOf(controller.getKernelSize()));
    mainPanel.add(kernelSizeField);

    mainPanel.add(new JLabel("Tamaño del Sistema Operativo:"));
    osSizeField = new JTextField(String.valueOf(controller.getOsSize()));
    mainPanel.add(osSizeField);

    mainPanel.add(new JLabel("Tamaño de Memoria Principal:"));
    mainMemorySizeField = new JTextField(String.valueOf(controller.getMainMemorySize()));
    mainPanel.add(mainMemorySizeField);

    mainPanel.add(new JLabel("Tamaño de Memoria Secundaria:"));
    secondaryMemorySizeField = new JTextField(String.valueOf(controller.getSecondaryMemorySize()));
    mainPanel.add(secondaryMemorySizeField);

    mainPanel.add(new JLabel("Tamaño de Memoria Virtual:"));
    virtualMemorySizeField = new JTextField(String.valueOf(controller.getVirtualMemorySize()));
    mainPanel.add(virtualMemorySizeField);

    mainPanel.add(new JLabel("Número de CPUs:"));
    numCPUsField = new JTextField(String.valueOf(controller.getNumCPUs()));
    mainPanel.add(numCPUsField);

    JButton saveButton = new JButton("Guardar");
    saveButton.addActionListener(e -> saveChanges());
    mainPanel.add(saveButton);

    loadConfigButton = new JButton("Cargar configuración");
    loadConfigButton.addActionListener(e -> loadConfigurationFromFile());
    mainPanel.add(loadConfigButton);

    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Saves the changes made in the configuration window.
   */
  private void saveChanges() {
    int kernelSize = Integer.parseInt(kernelSizeField.getText());
    int osSize = Integer.parseInt(osSizeField.getText());
    int mainMemorySize = Integer.parseInt(mainMemorySizeField.getText());
    int secondaryMemorySize = Integer.parseInt(secondaryMemorySizeField.getText());
    int virtualMemorySize = Integer.parseInt(virtualMemorySizeField.getText());
    int numCPUs = Integer.parseInt(numCPUsField.getText());

    String errorMsg = controller.validateConfiguration(kernelSize, osSize, mainMemorySize, secondaryMemorySize,
        virtualMemorySize, numCPUs);

    if (errorMsg != null) {
      JOptionPane.showMessageDialog(this, errorMsg);
      return;
    }

    controller.setKernelSize(kernelSize);
    controller.setOsSize(osSize);
    controller.setMainMemorySize(mainMemorySize);
    controller.setSecondaryMemorySize(secondaryMemorySize);
    controller.setVirtualMemorySize(virtualMemorySize);
    controller.setNumCpus(numCPUs);

    dispose();
  }

  /**
   * Loads the configuration from a file.
   */
  private void loadConfigurationFromFile() {
    JFileChooser fileChooser = new JFileChooser();
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      String filePath = selectedFile.getAbsolutePath();
      String fileType = getFileType(selectedFile);

      try {
        controller.loadConfigurationFromFile(filePath, fileType);
        updateFields();
      } catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }
  }

  /**
   * Gets the file type from the file name.
   *
   * @param file the file
   * @return the file type
   */
  private String getFileType(File file) {
    String fileName = file.getName();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
      return fileName.substring(dotIndex + 1).toLowerCase();
    }
    return "";
  }

  /**
   * Updates the fields in the configuration window with the current values from
   * the controller.
   */
  private void updateFields() {
    kernelSizeField.setText(String.valueOf(controller.getKernelSize()));
    osSizeField.setText(String.valueOf(controller.getOsSize()));
    mainMemorySizeField.setText(String.valueOf(controller.getMainMemorySize()));
    secondaryMemorySizeField.setText(String.valueOf(controller.getSecondaryMemorySize()));
    virtualMemorySizeField.setText(String.valueOf(controller.getVirtualMemorySize()));
  }
}