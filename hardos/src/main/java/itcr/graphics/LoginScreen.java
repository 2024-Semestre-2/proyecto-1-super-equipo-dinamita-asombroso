package itcr.graphics;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;

import itcr.controllers.LoginScreenController;

class LoginScreen extends JPanel {
  private JPasswordField passwordField;
  private JLabel backgroundLabel;
  private LoginScreenController controller;

  public LoginScreen(OSSystem osSystem) {
    this.controller = new LoginScreenController(osSystem);

    setLayout(new GridBagLayout());
    setPreferredSize(new Dimension(1024, 768));
    backgroundLabel = new JLabel(new ImageIcon("xp_login_background.jpg"));
    backgroundLabel.setLayout(new GridBagLayout());
    add(backgroundLabel);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.gridx = 0;
    gbc.gridy = 0;

    ImageIcon profileIcon = new ImageIcon("xp_user_icon.jpg");
    Image profileImg = profileIcon.getImage();
    Image scaledProfileImg = profileImg.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
    JLabel userIcon = new JLabel(new ImageIcon(scaledProfileImg));

    Border border = BorderFactory.createLineBorder(Color.BLUE, 5);
    userIcon.setBorder(border);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    backgroundLabel.add(userIcon, gbc);

    JLabel adminLabel = new JLabel("Admin");
    adminLabel.setForeground(Color.WHITE);
    adminLabel.setFont(new Font("Arial", Font.BOLD, 16));

    gbc.gridy = 1;
    gbc.gridwidth = 2;
    backgroundLabel.add(adminLabel, gbc);

    JLabel passwordLabel = new JLabel("Contraseña:");
    passwordLabel.setForeground(Color.WHITE);
    passwordField = new JPasswordField(15);
    passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    backgroundLabel.add(passwordLabel, gbc);

    gbc.gridx = 1;
    backgroundLabel.add(passwordField, gbc);

    ImageIcon buttonIcon = new ImageIcon("xp_login_button.png");
    Image buttonImg = buttonIcon.getImage();
    Image scaledButtonImg = buttonImg.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
    JButton loginButton = new JButton(new ImageIcon(scaledButtonImg));
    loginButton.setBorder(BorderFactory.createEmptyBorder());
    loginButton.setContentAreaFilled(false);

    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.CENTER;
    backgroundLabel.add(loginButton, gbc);

    loginButton.addActionListener(e -> {
      String password = new String(passwordField.getPassword());
      controller.attemptLogin("admin", password);
    });
  }
}