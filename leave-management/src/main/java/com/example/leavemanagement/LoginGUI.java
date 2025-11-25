package com.example.leavemanagement;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private DataStorage dataStorage;

    public LoginGUI() {
        dataStorage = new DataStorage();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginActionListener());
        panel.add(loginButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton);

        add(panel);
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            List<Employee> employees = dataStorage.loadEmployees();
            for (Employee emp : employees) {
                if (emp.getName().equals(username) && emp.getPassword().equals(password)) {
                    dispose();
                    if (emp.getId() == 0) { // Admin
                        new AdminDashboard(dataStorage).setVisible(true);
                    } else {
                        new EmployeeDashboard(emp, dataStorage).setVisible(true);
                    }
                    return;
                }
            }
            JOptionPane.showMessageDialog(LoginGUI.this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}