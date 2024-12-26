package com.njit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import com.njit.service.BookService;
import db.db;

public class ReaderUI extends JFrame {
    private JTextField name, Authority, gender, phoneNumber; // 个人信息字段
    private JButton editButton, saveButton, logoutButton; // 修改和注销按钮
    private db database;
    private String loggedInUsername;
    private JButton payFineButton;
    private JTextField fineIDField, fineAmountField;

    public ReaderUI(String username) {
        super("图书管理系统 - 用户界面");
        this.setSize(900, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.database = new db();
        this.loggedInUsername = username;  // 存储传递过来的用户名
        initUI();
        loadPersonalInfo();  // 加载个人信息
    }

    private BookService createBookManagementPanel() {
        // 传递数据库连接对象和当前登录的用户名
        BookService panel = new BookService(database, loggedInUsername);
        return panel;
    }

    public void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 添加图书查询和借阅管理面板
        tabbedPane.add("图书查询与借阅管理", createBookManagementPanel());

        // 个人信息管理面板
        tabbedPane.add("个人信息", createPersonalInfoPanel());

        // 罚金缴纳面板
        JPanel finePanel = createFinePaymentPanel();
        tabbedPane.addTab("罚金缴纳", finePanel);

        // 借阅记录按钮
        JButton viewBorrowRecordsButton = new JButton("查看借阅记录");

        // 添加按钮到面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewBorrowRecordsButton);
        viewBorrowRecordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBorrowRecords();  // 点击按钮时显示借阅记录
            }
        });

        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);  // 添加到下方

        // 监听“编辑”按钮
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableEditing(true);  // 允许编辑
            }
        });

        // 监听“保存”按钮
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePersonalInfo();  // 保存个人信息
            }
        });

        // 监听“注销账户”按钮
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();  // 注销账户
            }
        });
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = createFormPanel();  // 提取个人信息表单为一个方法
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);  // 提取按钮面板为方法
        return panel;
    }

    // 加载并显示个人信息
    private void loadPersonalInfo() {
        String sql = "SELECT * FROM Reader WHERE ReaderName = ?";
        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            pstmt.setString(1, loggedInUsername);  // 使用登录的用户名查询数据库
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                name.setText(rs.getString("ReaderName"));
                gender.setText(rs.getString("sex"));
                phoneNumber.setText(rs.getString("phoneNumber"));
                Authority.setText(rs.getString("Authority"));
            } else {
                JOptionPane.showMessageDialog(this, "未找到个人信息！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载个人信息失败！");
        }
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        formPanel.add(new JLabel("姓名:"));
        name = new JTextField();
        name.setEditable(false);
        formPanel.add(name);

        formPanel.add(new JLabel("权限:"));
        Authority = new JTextField();
        Authority.setEditable(false);
        formPanel.add(Authority);

        formPanel.add(new JLabel("性别:"));
        gender = new JTextField();
        gender.setEditable(false);
        formPanel.add(gender);

        formPanel.add(new JLabel("电话号码:"));
        phoneNumber = new JTextField();
        phoneNumber.setEditable(false);
        formPanel.add(phoneNumber);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        editButton = new JButton("编辑");
        saveButton = new JButton("保存");
        logoutButton = new JButton("注销账户");

        buttonPanel.add(editButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(logoutButton);

        return buttonPanel;
    }

    private void enableEditing(boolean isEditable) {
        name.setEditable(!isEditable);
        Authority.setEditable(!isEditable);
        gender.setEditable(isEditable);
        phoneNumber.setEditable(isEditable);
        saveButton.setEnabled(isEditable);  // 启用保存按钮
        editButton.setEnabled(!isEditable);  // 禁用编辑按钮
    }

    //保存按钮
    private void savePersonalInfo() {
        String sql = "UPDATE Reader SET ReaderName = ?, sex = ?, phoneNumber = ? WHERE ReaderName = ?";
        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            pstmt.setString(1, name.getText());
            pstmt.setString(2, gender.getText());
            pstmt.setString(3, phoneNumber.getText());
            pstmt.setString(4, loggedInUsername);  // 使用当前登录的用户名

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "个人信息更新成功！");
                enableEditing(false);  // 保存后禁用编辑
            } else {
                JOptionPane.showMessageDialog(this, "更新失败，请重试！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "保存个人信息失败！");
        }
    }

    //注销按钮
    private void logout() {
        // 关闭当前窗口并返回到登录界面
        int confirm = JOptionPane.showConfirmDialog(this, "确认注销账户吗？", "注销", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();  // 关闭当前窗口
            LoginUI loginUI = new LoginUI();  // 返回登录界面
            loginUI.setVisible(true);
        }
    }

    private JPanel createFinePaymentPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        panel.add(new JLabel("罚金ID:"));
        fineIDField = new JTextField();
        panel.add(fineIDField);

        panel.add(new JLabel("罚金金额:"));
        fineAmountField = new JTextField();
        panel.add(fineAmountField);

        payFineButton = new JButton("缴纳罚金");
        panel.add(payFineButton);

        // 按钮事件监听
        initFinePaymentListener();

        return panel;
    }

    // 罚金缴纳按钮事件监听
    private void initFinePaymentListener() {
        payFineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                payFine();
            }
        });
    }

    // 罚金缴纳
    private void payFine() {
        String sql = "{CALL PayFineProcedure(?, ?, ?)}";  // 调用存储过程
        try (CallableStatement cstmt = database.cstmt(sql)) {
            cstmt.setString(1, loggedInUsername);  // 设置借书人用户名
            cstmt.setInt(2, Integer.parseInt(fineIDField.getText()));  // 设置罚金ID
            cstmt.setDouble(3, Double.parseDouble(fineAmountField.getText()));  // 设置罚金金额
            cstmt.execute();
            JOptionPane.showMessageDialog(this, "罚金缴纳成功！");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "罚金缴纳失败！");
            ex.printStackTrace();
        }
    }

    // 查询借阅记录
    private void showBorrowRecords() {
        String sql = "SELECT r.RecordId, bc.BookName, r.ReaderName, r.LoanTime, r.ReturnTime " +
                "FROM Record r " +
                "JOIN BookCopyright bc ON r.isbn = bc.isbn " +
                "WHERE r.ReaderName = ?";

        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            pstmt.setString(1, loggedInUsername);  // 假设你已经保存了当前登录用户名
            ResultSet rs = pstmt.executeQuery();

            // 创建表格显示借阅记录
            DefaultTableModel model = new DefaultTableModel(new String[]{"借阅ID", "书籍名称", "借阅人", "借阅日期", "归还日期"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("RecordId"),
                        rs.getString("BookName"),
                        rs.getString("ReaderName"),
                        rs.getDate("LoanTime"),
                        rs.getDate("ReturnTime")
                });
            }

            // 创建一个JTable展示借阅记录
            JTable borrowTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(borrowTable);
            JOptionPane.showMessageDialog(this, scrollPane, "借阅记录", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询借阅记录失败！");
        }
    }
}
