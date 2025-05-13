import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class StudentPaymentSystem extends JFrame {
    private List<Student> students = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable studentTable;

    private JTextField txtName;
    private JTextField txtId;
    private JTextField txtAmount;

    public StudentPaymentSystem() {
        setTitle("Student Payment System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        setMinimumSize(new Dimension(400, 400));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel for student registration
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2), "Register Student", 0, 0, new Font("Arial", Font.BOLD, 14), new Color(30, 144, 255)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        txtId = new JTextField(10);
        inputPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(15);
        inputPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Payment Amount:"), gbc);
        gbc.gridx = 1;
        txtAmount = new JTextField(10);
        inputPanel.add(txtAmount, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JButton btnAddStudent = new JButton("Add Student");
        inputPanel.add(btnAddStudent, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Table for displaying students
        String[] columns = {"Student ID", "Name", "Amount Paid"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing
            }
        };
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2), "Students Payment List", 0, 0, new Font("Arial", Font.BOLD, 14), new Color(30, 144, 255)));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBackground(new Color(230, 240, 255));
        JButton btnAddPayment = new JButton("Add Payment");
        JButton btnGenerateReceipt = new JButton("Generate Receipt");
        btnAddPayment.setBackground(new Color(100, 149, 237));
        btnAddPayment.setForeground(Color.WHITE);
        btnAddPayment.setFocusPainted(false);
        btnGenerateReceipt.setBackground(new Color(100, 149, 237));
        btnGenerateReceipt.setForeground(Color.WHITE);
        btnGenerateReceipt.setFocusPainted(false);
        actionPanel.add(btnAddPayment);
        actionPanel.add(btnGenerateReceipt);
        add(actionPanel, BorderLayout.SOUTH);

        // Button Actions
        btnAddStudent.addActionListener(e -> addStudent());
        btnAddPayment.addActionListener(e -> addPayment());
        btnGenerateReceipt.addActionListener(e -> generateReceipt());

        // Tooltips
        txtId.setToolTipText("Enter unique student ID");
        txtName.setToolTipText("Enter student full name");
        txtAmount.setToolTipText("Enter initial payment amount");
        btnAddStudent.setToolTipText("Click to add a new student");
        btnAddPayment.setToolTipText("Add additional payment for selected student");
        btnGenerateReceipt.setToolTipText("Generate payment receipt for selected student");

        // Pressing Enter in payment adds the student
        txtAmount.addActionListener(e -> addStudent());
    }

    private void addStudent() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String amountStr = txtAmount.getText().trim();

        if (id.isEmpty() || name.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (getStudentById(id) != null) {
            JOptionPane.showMessageDialog(this, "Student ID already exists!", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a positive number!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student student = new Student(id, name, amount);
        students.add(student);
        tableModel.addRow(new Object[]{id, name, String.format("%.2f", amount)});

        txtId.setText("");
        txtName.setText("");
        txtAmount.setText("");
    }

    private Student getStudentById(String id) {
        for (Student s : students) {
            if (s.getId().equalsIgnoreCase(id)) {
                return s;
            }
        }
        return null;
    }

    private void addPayment() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a student first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentId = (String) tableModel.getValueAt(selectedRow, 0);

        String paymentStr = JOptionPane.showInputDialog(this, "Enter additional payment amount:", "Add Payment", JOptionPane.PLAIN_MESSAGE);
        if (paymentStr == null) {
            return; // User cancelled input dialog
        }

        double payment;
        try {
            payment = Double.parseDouble(paymentStr);
            if (payment <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a positive number for payment!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student student = getStudentById(studentId);
        if (student != null) {
            student.addPayment(payment);
            tableModel.setValueAt(String.format("%.2f", student.getAmountPaid()), selectedRow, 2);
            JOptionPane.showMessageDialog(this, "Payment added successfully.");
        }
    }

    private void generateReceipt() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to generate receipt!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student student = students.get(selectedRow);
        StringBuilder receipt = new StringBuilder();
        receipt.append("--------- Payment Receipt ---------\n");
        receipt.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        receipt.append("Student ID: ").append(student.getId()).append("\n");
        receipt.append("Student Name: ").append(student.getName()).append("\n");
        receipt.append("Total Amount Paid: $").append(String.format("%.2f", student.getAmountPaid())).append("\n");
        receipt.append("-----------------------------------\n");
        JOptionPane.showMessageDialog(this, receipt.toString(), "Payment Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // Set a modern look and feel UI
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            StudentPaymentSystem app = new StudentPaymentSystem();
            app.setVisible(true);
        });
    }


    static class Student {
        private String id;
        private String name;
        private double amountPaid;

        public Student(String id, String name, double amountPaid) {
            this.id = id;
            this.name = name;
            this.amountPaid = amountPaid;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getAmountPaid() { return amountPaid; }
        public void addPayment(double amount) { this.amountPaid += amount; }
    }
}
