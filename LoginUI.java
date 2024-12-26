package com.njit;

import db.db;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class LoginUI extends JFrame {
    private JTextField username;
    private JPasswordField password;
    private JButton login, register;
    private JRadioButton adminButton;
    private JRadioButton userButton; // 单选按钮
    private ButtonGroup group; // 单选按钮组

    public LoginUI() {
        super();
        this.setSize(300, 200);
        this.setTitle("登录");
        this.setLocationRelativeTo(getOwner()); // 居中

        // 设置布局
        Container cont = getContentPane();
        cont.setLayout(new GridLayout(4, 2)); // 4 行 2 列布局

        // 添加用户名和密码字段
        cont.add(new JLabel("用户名:"));
        username = new JTextField(10);
        cont.add(username);

        cont.add(new JLabel("密码:"));
        password = new JPasswordField(10);
        cont.add(password);

        // 添加角色选择单选按钮
        cont.add(new JLabel("选择角色:"));
        JPanel radioPanel = new JPanel();
        adminButton = new JRadioButton("管理员");
        userButton = new JRadioButton("读者", true); // 默认选中 "读者"
        group = new ButtonGroup();
        group.add(adminButton);
        group.add(userButton);
        radioPanel.add(adminButton);
        radioPanel.add(userButton);
        cont.add(radioPanel);

        // 添加登录和注册按钮
        login = new JButton("登录");
        register = new JButton("注册");
        cont.add(login);
        cont.add(register);

        // 登录按钮监听器
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                handleLogin();
            }
        });

        // 注册按钮监听器
        register.addActionListener(e -> {
            RegisterUI registerFrame = new RegisterUI();
            registerFrame.setVisible(true);
        });
    }

    // 登录逻辑
    private void handleLogin() {
        String enteredUsername = username.getText();
        String enteredPassword = new String(password.getPassword());
        String role = userButton.isSelected() ? "读者" : "管理员";

        db database = new db();
        String sql = "SELECT * FROM Users WHERE AdminName = '" + enteredUsername + "' AND AdminPassword = '" + enteredPassword + "'";
        try {
            ResultSet rs = database.executeQuery(sql);
            if (rs.next()) {
                int userPower = rs.getInt("Upower");
                if ((role.equals("管理员") && userPower == 1) || (role.equals("读者") && userPower == 0)) {
                    JOptionPane.showMessageDialog(this, "登录成功，欢迎" + role);
                    // 根据角色打开不同的 UI
                    if (role.equals("管理员")) {
                        // 如果是管理员，打开 AdminUI
                        AdminUI adminUI = new AdminUI(enteredUsername);
                        adminUI.setVisible(true);
                    } else {
                        // 如果是读者，打开 ReaderUI，并传递用户名
                        ReaderUI readerUI = new ReaderUI(enteredUsername);
                        readerUI.setVisible(true);
                    }
                    database.closeConn(); // 关闭数据库连接
                    dispose(); // 关闭登录窗口
                } else {
                    JOptionPane.showMessageDialog(this, "角色权限错误！");
                }
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
