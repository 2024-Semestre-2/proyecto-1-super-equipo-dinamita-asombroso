package itcr.graphics;

import javax.swing.*;
import java.awt.*;

public abstract class FloatingWindow<T> extends JDialog {
  protected T controller;

  public FloatingWindow(JFrame parent, String title, T controller) {
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