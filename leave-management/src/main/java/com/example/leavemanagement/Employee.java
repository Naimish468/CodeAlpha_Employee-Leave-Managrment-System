package com.example.leavemanagement;

import java.util.List;

public class Employee {
    private int id;
    private String name;
    private String password;
    private int leaveBalance;
    private List<LeaveApplication> leaveHistory;

    public Employee() {}

    public Employee(int id, String name, String password, int leaveBalance) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.leaveBalance = leaveBalance;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(int leaveBalance) { this.leaveBalance = leaveBalance; }

    public List<LeaveApplication> getLeaveHistory() { return leaveHistory; }
    public void setLeaveHistory(List<LeaveApplication> leaveHistory) { this.leaveHistory = leaveHistory; }
}