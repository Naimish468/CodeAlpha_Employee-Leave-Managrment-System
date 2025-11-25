package com.example.leavemanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

public class EmployeeDashboard extends JFrame {
    private Employee employee;
    private DataStorage dataStorage;
    private JTextArea historyArea;
    private JLabel balanceLabel;

    public EmployeeDashboard(Employee employee, DataStorage dataStorage) {
        this.employee = employee;
        this.dataStorage = dataStorage;
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Employee Dashboard - " + employee.getName());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Balance
        balanceLabel = new JLabel("Leave Balance: " + employee.getLeaveBalance());
        panel.add(balanceLabel, BorderLayout.NORTH);

        // History
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton applyLeaveButton = new JButton("Apply for Leave");
        applyLeaveButton.addActionListener(new ApplyLeaveActionListener());
        buttonPanel.add(applyLeaveButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginGUI().setVisible(true);
            dispose();
        });
        buttonPanel.add(logoutButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void loadData() {
        List<LeaveApplication> leaves = dataStorage.loadLeaves();
        StringBuilder sb = new StringBuilder();
        for (LeaveApplication leave : leaves) {
            if (leave.getEmployeeId() == employee.getId()) {
                sb.append("From: ").append(leave.getStartDate()).append(" To: ").append(leave.getEndDate())
                        .append(" Status: ").append(leave.getStatus()).append("\n");
            }
        }
        historyArea.setText(sb.toString());
    }

    private class ApplyLeaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField startField = new JTextField();
            JTextField endField = new JTextField();
            JTextField reasonField = new JTextField();

            Object[] message = {
                "Start Date (YYYY-MM-DD):", startField,
                "End Date (YYYY-MM-DD):", endField,
                "Reason:", reasonField
            };

            int option = JOptionPane.showConfirmDialog(EmployeeDashboard.this, message, "Apply for Leave", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    LocalDate start = LocalDate.parse(startField.getText());
                    LocalDate end = LocalDate.parse(endField.getText());
                    String reason = reasonField.getText();

                    LeaveApplication leave = new LeaveApplication(employee.getId(), start, end, reason, "Pending");
                    List<LeaveApplication> leaves = dataStorage.loadLeaves();
                    leaves.add(leave);
                    dataStorage.saveLeaves(leaves);

                    JOptionPane.showMessageDialog(EmployeeDashboard.this, "Leave application submitted");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeDashboard.this, "Invalid date format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}