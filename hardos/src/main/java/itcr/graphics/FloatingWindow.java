package itcr.graphics;

import javax.swing.*;
import java.awt.*;

import itcr.controllers.DesktopScreenController;

public abstract class FloatingWindow extends JDialog {
  protected DesktopScreenController controller;

  public FloatingWindow(JFrame parent, String title, DesktopScreenController controller) {
    super(parent, title, true);
    this.controller = controller;
    setResizable(false);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());
    initComponents();
    pack();
    setLocationRelativeTo(parent);
  }

  protected abstract void initComponents();
}