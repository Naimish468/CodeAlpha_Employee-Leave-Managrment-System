package com.example.leavemanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminDashboard extends JFrame {
    private DataStorage dataStorage;
    private JTable leaveTable;
    private LeaveTableModel tableModel;

    public AdminDashboard(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Table
        tableModel = new LeaveTableModel();
        leaveTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(leaveTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton approveButton = new JButton("Approve");
        approveButton.addActionListener(new ApproveActionListener());
        buttonPanel.add(approveButton);

        JButton rejectButton = new JButton("Reject");
        rejectButton.addActionListener(new RejectActionListener());
        buttonPanel.add(rejectButton);

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
        tableModel.setLeaves(leaves);
    }

    private class ApproveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = leaveTable.getSelectedRow();
            if (selectedRow >= 0) {
                LeaveApplication leave = tableModel.getLeaveAt(selectedRow);
                leave.setStatus("Approved");
                dataStorage.saveLeaves(tableModel.getLeaves());
                loadData();
            }
        }
    }

    private class RejectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = leaveTable.getSelectedRow();
            if (selectedRow >= 0) {
                LeaveApplication leave = tableModel.getLeaveAt(selectedRow);
                leave.setStatus("Rejected");
                dataStorage.saveLeaves(tableModel.getLeaves());
                loadData();
            }
        }
    }

    private static class LeaveTableModel extends javax.swing.table.AbstractTableModel {
        private List<LeaveApplication> leaves;
        private String[] columnNames = {"ID", "Employee ID", "Start Date", "End Date", "Reason", "Status"};

        public void setLeaves(List<LeaveApplication> leaves) {
            this.leaves = leaves;
            fireTableDataChanged();
        }

        public List<LeaveApplication> getLeaves() {
            return leaves;
        }

        public LeaveApplication getLeaveAt(int row) {
            return leaves.get(row);
        }

        @Override
        public int getRowCount() {
            return leaves == null ? 0 : leaves.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            LeaveApplication leave = leaves.get(rowIndex);
            switch (columnIndex) {
                case 0: return leave.getId();
                case 1: return leave.getEmployeeId();
                case 2: return leave.getStartDate();
                case 3: return leave.getEndDate();
                case 4: return leave.getReason();
                case 5: return leave.getStatus();
                default: return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
    }
}