/*
Project: Employee Leave Management System (Java Swing + File Persistence)
Files included below (copy each into its own .java file under package folders, or compile from root):

- src/Main.java
- src/model/Employee.java
- src/model/LeaveRequest.java
- src/storage/LeaveStorage.java
- src/ui/MainFrame.java

This is a single-file representation for convenience. Save each class into corresponding files and compile.
*/

// ==========================
// src/Main.java
// ==========================
package com.elms;

import com.elms.ui.MainFrame;
import com.elms.storage.LeaveStorage;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Ensure UI looks native
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // Load or create demo data
        LeaveStorage storage = LeaveStorage.getInstance();
        storage.load();
        if (storage.getEmployees().isEmpty()) {
            storage.createDemoData();
            storage.save();
        }

        EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

// ==========================
// src/model/Employee.java
// ==========================
package com.elms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String email;
    private String password; // simple for demo, don't store plain text in real apps
    private Role role;

    // leave balances per type
    private int casualBalance;
    private int sickBalance;
    private int paidBalance;

    private List<Integer> leaveRequestIds = new ArrayList<>();

    public enum Role { EMPLOYEE, ADMIN }

    public Employee(int id, String name, String email, String password, Role role, int casual, int sick, int paid) {
        this.id = id; this.name = name; this.email = email; this.password = password; this.role = role;
        this.casualBalance = casual; this.sickBalance = sick; this.paidBalance = paid;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public int getCasualBalance() { return casualBalance; }
    public int getSickBalance() { return sickBalance; }
    public int getPaidBalance() { return paidBalance; }

    public void deductBalance(LeaveRequest.LeaveType type, int days) {
        switch (type) {
            case CASUAL -> casualBalance -= days;
            case SICK -> sickBalance -= days;
            case PAID -> paidBalance -= days;
        }
    }

    public void addLeaveRequestId(int reqId) { leaveRequestIds.add(reqId); }
    public List<Integer> getLeaveRequestIds() { return leaveRequestIds; }
}

// ==========================
// src/model/LeaveRequest.java
// ==========================
package com.elms.model;

import java.io.Serializable;
import java.time.LocalDate;

public class LeaveRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { PENDING, APPROVED, REJECTED }
    public enum LeaveType { CASUAL, SICK, PAID }

    private int id;
    private int empId;
    private LeaveType type;
    private LocalDate start;
    private LocalDate end;
    private String reason;
    private Status status;
    private String adminRemark;

    public LeaveRequest(int id, int empId, LeaveType type, LocalDate start, LocalDate end, String reason) {
        this.id = id; this.empId = empId; this.type = type; this.start = start; this.end = end; this.reason = reason;
        this.status = Status.PENDING;
        this.adminRemark = "";
    }

    public int getId() { return id; }
    public int getEmpId() { return empId; }
    public LeaveType getType() { return type; }
    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
    public String getAdminRemark() { return adminRemark; }

    public int daysCount() {
        return (int) (end.toEpochDay() - start.toEpochDay()) + 1;
    }

    public void approve(String remark) { this.status = Status.APPROVED; this.adminRemark = remark; }
    public void reject(String remark) { this.status = Status.REJECTED; this.adminRemark = remark; }
}

// ==========================
// src/storage/LeaveStorage.java
// ==========================
package com.elms.storage;

import com.elms.model.Employee;
import com.elms.model.LeaveRequest;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class LeaveStorage implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final String DATA_FILE = "elms_data.ser";

    private Map<Integer, Employee> employees = new HashMap<>();
    private Map<Integer, LeaveRequest> requests = new HashMap<>();
    private int nextEmpId = 1;
    private int nextReqId = 1;

    private static LeaveStorage instance;

    private LeaveStorage() {}

    public static LeaveStorage getInstance() {
        if (instance == null) instance = new LeaveStorage();
        return instance;
    }

    public Map<Integer, Employee> getEmployees() { return employees; }
    public Map<Integer, LeaveRequest> getRequests() { return requests; }

    public synchronized Employee addEmployee(String name, String email, String password, Employee.Role role,
                                             int casual, int sick, int paid) {
        int id = nextEmpId++;
        Employee e = new Employee(id, name, email, password, role, casual, sick, paid);
        employees.put(id, e);
        return e;
    }

    public synchronized LeaveRequest addRequest(int empId, LeaveRequest.LeaveType type, LocalDate start, LocalDate end, String reason) {
        int id = nextReqId++;
        LeaveRequest r = new LeaveRequest(id, empId, type, start, end, reason);
        requests.put(id, r);
        Employee e = employees.get(empId);
        if (e != null) e.addLeaveRequestId(id);
        return r;
    }

    public Optional<Employee> findEmployeeByCredentials(int id, String password) {
        Employee e = employees.get(id);
        if (e != null && e.getPassword().equals(password)) return Optional.of(e);
        return Optional.empty();
    }

    public Optional<Employee> findEmployeeById(int id) { return Optional.ofNullable(employees.get(id)); }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(this);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            LeaveStorage loaded = (LeaveStorage) ois.readObject();
            this.employees = loaded.employees;
            this.requests = loaded.requests;
            this.nextEmpId = loaded.nextEmpId;
            this.nextReqId = loaded.nextReqId;
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void createDemoData() {
        Employee admin = addEmployee("Admin User","admin@example.com","admin", Employee.Role.ADMIN, 0,0,0);
        Employee alice = addEmployee("Alice","alice@example.com","alice", Employee.Role.EMPLOYEE, 8,8,10);
        Employee bob = addEmployee("Bob","bob@example.com","bob", Employee.Role.EMPLOYEE, 8,8,10);

        addRequest(alice.getId(), LeaveRequest.LeaveType.CASUAL, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), "Personal work");
        addRequest(bob.getId(), LeaveRequest.LeaveType.SICK, LocalDate.now().minusDays(3), LocalDate.now().minusDays(3), "Fever");
    }
}

// ==========================
// src/ui/MainFrame.java
// ==========================
package com.elms.ui;

import com.elms.model.Employee;
import com.elms.model.LeaveRequest;
import com.elms.storage.LeaveStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Vector;

public class MainFrame extends JFrame {
    private CardLayout cards = new CardLayout();
    private JPanel root = new JPanel(cards);
    private LeaveStorage storage = LeaveStorage.getInstance();

    private Employee currentUser = null;

    public MainFrame() {
        setTitle("Employee Leave Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);

        root.add(buildLoginPanel(), "login");
        root.add(buildEmployeePanel(), "employee");
        root.add(buildAdminPanel(), "admin");

        add(root);
        cards.show(root, "login");
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);

        JLabel lblTitle = new JLabel("ELMS - Login"); lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        c.gridx=0;c.gridy=0;c.gridwidth=2; p.add(lblTitle,c);

        c.gridwidth=1;
        c.gridy=1; p.add(new JLabel("Employee ID:"), c);
        JTextField txtId = new JTextField(10); c.gridx=1; p.add(txtId,c);

        c.gridx=0;c.gridy=2; p.add(new JLabel("Password:"), c);
        JPasswordField txtPass = new JPasswordField(10); c.gridx=1; p.add(txtPass,c);

        c.gridx=0;c.gridy=3;c.gridwidth=2;
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> doLogin(txtId.getText(), new String(txtPass.getPassword())));
        p.add(btnLogin,c);

        c.gridy=4; JLabel hint = new JLabel("Demo admin -> ID:1 pass:admin  |  Alice ID:2 pass:alice  | Bob ID:3 pass:bob"); p.add(hint,c);

        return p;
    }

    private void doLogin(String idText, String pass) {
        try {
            int id = Integer.parseInt(idText.trim());
            var opt = storage.findEmployeeByCredentials(id, pass);
            if (opt.isPresent()) {
                currentUser = opt.get();
                if (currentUser.getRole() == Employee.Role.ADMIN) cards.show(root, "admin"); else cards.show(root, "employee");
                setTitle("ELMS - Logged in: " + currentUser.getName());
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials","Error",JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter numeric ID","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // Employee view
    private JPanel empPanel;
    private JTable empHistoryTable;
    private JLabel lblBalances;

    private JPanel buildEmployeePanel() {
        empPanel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLogout = new JButton("Logout"); btnLogout.addActionListener(e -> { currentUser = null; setTitle("Employee Leave Management System"); cards.show(root, "login"); });
        top.add(btnLogout);
        lblBalances = new JLabel(); top.add(lblBalances);
        empPanel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());

        // Apply form
        JPanel apply = new JPanel(new GridBagLayout());
        apply.setBorder(BorderFactory.createTitledBorder("Apply for Leave"));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6);
        c.gridx=0;c.gridy=0; apply.add(new JLabel("Leave Type:"),c);
        JComboBox<LeaveRequest.LeaveType> cbType = new JComboBox<>(LeaveRequest.LeaveType.values()); c.gridx=1; apply.add(cbType,c);

        c.gridx=0;c.gridy=1; apply.add(new JLabel("Start Date (YYYY-MM-DD):"),c);
        JTextField txtStart = new JTextField(10); c.gridx=1; apply.add(txtStart,c);

        c.gridx=0;c.gridy=2; apply.add(new JLabel("End Date (YYYY-MM-DD):"),c);
        JTextField txtEnd = new JTextField(10); c.gridx=1; apply.add(txtEnd,c);

        c.gridx=0;c.gridy=3; apply.add(new JLabel("Reason:"),c);
        JTextField txtReason = new JTextField(20); c.gridx=1; apply.add(txtReason,c);

        c.gridx=0;c.gridy=4;c.gridwidth=2; JButton btnApply = new JButton("Apply"); apply.add(btnApply,c);
        btnApply.addActionListener((ActionEvent e) -> {
            try {
                LeaveRequest.LeaveType type = (LeaveRequest.LeaveType) cbType.getSelectedItem();
                LocalDate s = LocalDate.parse(txtStart.getText().trim());
                LocalDate en = LocalDate.parse(txtEnd.getText().trim());
                if (en.isBefore(s)) { JOptionPane.showMessageDialog(this,"End date must be after start date"); return; }
                int days = (int)(en.toEpochDay()-s.toEpochDay())+1;
                // check balance
                boolean ok=true; String msg="";
                switch (type) {
                    case CASUAL -> { if (currentUser.getCasualBalance()<days) { ok=false; msg="Not enough casual leave balance."; } }
                    case SICK -> { if (currentUser.getSickBalance()<days) { ok=false; msg="Not enough sick leave balance."; } }
                    case PAID -> { if (currentUser.getPaidBalance()<days) { ok=false; msg="Not enough paid leave balance."; } }
                }
                if (!ok) { JOptionPane.showMessageDialog(this,msg); return; }
                var req = storage.addRequest(currentUser.getId(), type, s, en, txtReason.getText().trim());
                storage.save();
                JOptionPane.showMessageDialog(this,"Leave applied (Request ID: " + req.getId() + ")");
                refreshEmployeeView();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input: " + ex.getMessage()); }
        });

        center.add(apply, BorderLayout.NORTH);

        // History table
        empHistoryTable = new JTable(new DefaultTableModel(new Object[]{"ID","Type","Start","End","Days","Status","Admin Remark"},0));
        center.add(new JScrollPane(empHistoryTable), BorderLayout.CENTER);

        empPanel.add(center, BorderLayout.CENTER);
        return empPanel;
    }

    private void refreshEmployeeView() {
        if (currentUser==null) return;
        lblBalances.setText(String.format("Balances - Casual: %d, Sick: %d, Paid: %d", currentUser.getCasualBalance(), currentUser.getSickBalance(), currentUser.getPaidBalance()));
        DefaultTableModel model = (DefaultTableModel) empHistoryTable.getModel();
        model.setRowCount(0);
        for (Integer id : currentUser.getLeaveRequestIds()) {
            LeaveRequest r = storage.getRequests().get(id);
            if (r==null) continue;
            model.addRow(new Object[]{r.getId(), r.getType(), r.getStart(), r.getEnd(), r.daysCount(), r.getStatus(), r.getAdminRemark()});
        }
    }

    // Admin view
    private JTable adminTable;

    private JPanel buildAdminPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLogout = new JButton("Logout"); btnLogout.addActionListener(e -> { currentUser = null; setTitle("Employee Leave Management System"); cards.show(root, "login"); });
        top.add(btnLogout);
        p.add(top, BorderLayout.NORTH);

        adminTable = new JTable(new DefaultTableModel(new Object[]{"ReqID","EmpID","Name","Type","Start","End","Days","Status"},0));
        p.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton btnRefresh = new JButton("Refresh"); btnRefresh.addActionListener(e -> refreshAdminView());
        JButton btnApprove = new JButton("Approve");
        JButton btnReject = new JButton("Reject");
        actions.add(btnRefresh); actions.add(btnApprove); actions.add(btnReject);

        btnApprove.addActionListener(e -> handleAdminAction(true));
        btnReject.addActionListener(e -> handleAdminAction(false));

        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void handleAdminAction(boolean approve) {
        int sel = adminTable.getSelectedRow();
        if (sel<0) { JOptionPane.showMessageDialog(this,"Select a request first"); return; }
        int reqId = (int) adminTable.getValueAt(sel,0);
        LeaveRequest r = storage.getRequests().get(reqId);
        if (r==null) return;
        if (r.getStatus()!= LeaveRequest.Status.PENDING) { JOptionPane.showMessageDialog(this,"Request already processed"); return; }
        String remark = JOptionPane.showInputDialog(this, "Admin remark:", "", JOptionPane.PLAIN_MESSAGE);
        if (remark==null) remark="";
        if (approve) {
            // deduct balance
            var empOpt = storage.findEmployeeById(r.getEmpId());
            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                int days = r.daysCount();
                emp.deductBalance(r.getType(), days);
            }
            r.approve(remark);
        } else {
            r.reject(remark);
        }
        storage.save();
        refreshAdminView();
    }

    private void refreshAdminView() {
        DefaultTableModel model = (DefaultTableModel) adminTable.getModel();
        model.setRowCount(0);
        for (LeaveRequest r : storage.getRequests().values()) {
            Employee e = storage.getEmployees().get(r.getEmpId());
            model.addRow(new Object[]{r.getId(), r.getEmpId(), e!=null?e.getName():"-", r.getType(), r.getStart(), r.getEnd(), r.daysCount(), r.getStatus()});
        }
    }

    // Show panels appropriately when switched
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        // when the frame becomes visible, attach a timer to refresh views each time the card shows
        Timer t = new Timer(300, e -> {
            if (currentUser!=null) {
                if (currentUser.getRole()==Employee.Role.EMPLOYEE) refreshEmployeeView(); else refreshAdminView();
            }
        });
        t.setRepeats(false); t.start();
    }
}

/*
Instructions:
1. Save classes into files respecting package paths (e.g., com/elms/Main.java etc.).
2. Compile: from project root (where 'com' folder exists):
   javac com/elms/Main.java
   Then run:
   java com.elms.Main

3. The program persists data in 'elms_data.ser' in the working directory.

How to switch to MySQL (brief):
- Replace LeaveStorage persistence methods (save/load) with JDBC connections.
- Create tables employees and leave_requests and map fields.
- Use prepared statements for CRUD.

This demo uses basic password-in-cleartext for simplicity. For production, use hashing and proper authentication.
*/

# README

## Employee Leave Management System (Java Swing)

### 📌 Overview
This project is a **Java-based Employee Leave Management System** built using **Swing GUI** and **file handling** for data storage. It allows employees to apply for leave, view leave balance, and check leave history. Admins can approve or reject leave requests.

### 🧩 Features
- Java Swing GUI
- Employee login
- Apply for leave
- View leave balance & history
- Admin login
- Admin approval/rejection panel
- File-based persistent storage

### 📁 Project Structure
```
src/
 ├── model/
 │     Employee.java
 │     LeaveRequest.java
 │
 ├── dao/
 │     EmployeeDAO.java
 │     LeaveDAO.java
 │
 ├── ui/
 │     LoginFrame.java
 │     EmployeeDashboard.java
 │     ApplyLeaveFrame.java
 │     AdminDashboard.java
 │
 └── Main.java
```

### ⚙️ How to Run
1. Install JDK 8 or above.
2. Open project in any IDE (NetBeans, Eclipse, IntelliJ) or compile manually:
   ```bash
   javac -d bin src/**/*.java
   java -cp bin Main
   ```
3. Default users:
   - **Admin** → username: `admin`, password: `admin123`
   - **Employee** → stored in `employees.txt`

### 📂 Data Files
The system stores data in plain text files:
```
data/
 ├── employees.txt
 └── leave_requests.txt
```

### 🔧 Future Improvements
- Replace file handling with MySQL + JDBC
- Add password hashing
- Add email notifications
- Add charts & PDF reports

### 📜 License
Free to use for academic projects.

