package com.njit;

import db.db;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterUI extends JFrame {

    private JTextField username, name, phoneNumber;  // 添加电话号码字段
    private JPasswordField password, passwordagain;
    private JRadioButton sexmale, sexfemale;
    private JPanel sex, birth, fav;
    private JTextField year;
    private JComboBox<String> month, day;
    private JCheckBox f1, f2, f3, f4;
    private JButton register, cancel;
    private db database;
    private JComboBox<String> authority;  // 权限选择框
    private JTextField readerTypeNumber;  // ReaderTypeNumber字段

    public RegisterUI() {
        super();
        this.setSize(750, 450);
        this.setTitle("读者注册");
        this.setLocationRelativeTo(getOwner()); // 居中
        // 设置组件布局
        Container contain = getContentPane();
        contain.setLayout(new BoxLayout(contain, BoxLayout.Y_AXIS));

        // 添加组件
        JPanel cont = new JPanel(new GridLayout(8, 2, 10, 10));  // 更新GridLayout的行数

        // 用户名
        cont.add(new JLabel("用户名"));
        username = new JTextField(15);
        cont.add(username);

        // 电话号码
        cont.add(new JLabel("电话号码"));
        phoneNumber = new JTextField(11);
        cont.add(phoneNumber);

        // 密码
        cont.add(new JLabel("密码"));
        password = new JPasswordField(15);
        cont.add(password);

        // 再输一次密码
        cont.add(new JLabel("再输一次密码"));
        passwordagain = new JPasswordField(15);
        cont.add(passwordagain);

        // 性别
        cont.add(new JLabel("性别"));
        sexmale = new JRadioButton("男", true);
        sexfemale = new JRadioButton("女");
        ButtonGroup bg = new ButtonGroup();
        bg.add(sexmale);
        bg.add(sexfemale);
        sex = new JPanel();
        sex.add(sexmale);
        sex.add(sexfemale);
        cont.add(sex);

        // 权限选择
        cont.add(new JLabel("权限"));
        authority = new JComboBox<>();
        authority.addItem("学生");
        authority.addItem("群众");
        cont.add(authority);


        // 按钮
        JPanel cont2 = new JPanel(new GridLayout(1, 2, 20, 20));
        register = new JButton("注册");
        cancel = new JButton("重新输入");
        cont2.add(register);
        cont2.add(cancel);

        // 加入列内容面板
        contain.add(cont);
        contain.add(cont2);

        // 初始化数据库连接
        database = new db();

        // 注册监听器
        register.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // 获取密码
                String pass = new String(password.getPassword());                       // 获取密码
                String passagain = new String(passwordagain.getPassword());             // 获取再次输入的密码
                if (pass.equals(passagain)) {                                           // 判断密码是否一致
                    // 获取用户输入的信息
                    String nameText = username.getText(); // 获取姓名
                    String phoneText = phoneNumber.getText();  // 获取电话号码
                    String gender = sexmale.isSelected() ? "男" : "女";
                    String authorityText = (String) authority.getSelectedItem();  // 获取权限

                    // 插入数据到数据库（去掉 ReaderId 字段，数据库自动生成）
                    String sql = "INSERT INTO Reader (ReaderName, Sex, PhoneNumber, PassWd, Authority) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
                        pstmt.setString(1, nameText);             // 设置ReaderName为姓名
                        pstmt.setString(2, gender);               // 设置性别
                        pstmt.setString(3, phoneText);            // 设置电话
                        pstmt.setString(4, pass);                 // 设置密码
                        pstmt.setString(5, authorityText);        // 设置权限

                        int result = pstmt.executeUpdate();
                        if (result > 0) {
                            JOptionPane.showMessageDialog(null, "注册成功！");
                        } else {
                            JOptionPane.showMessageDialog(null, "注册失败，请重试！");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "注册失败，重复的用户名或其他错误，请重试！");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "密码不一致!");
                }
            }
        });

        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // 取消注册，清空输入框
                name.setText("");
                username.setText("");
                phoneNumber.setText("");
                password.setText("");
                passwordagain.setText("");
                year.setText("");
                month.setSelectedIndex(0);
                day.setSelectedIndex(0);
                f1.setSelected(false);
                f2.setSelected(false);
                f3.setSelected(false);
                f4.setSelected(false);
                sexmale.setSelected(true);
                authority.setSelectedIndex(0);
                readerTypeNumber.setText("");
            }
        });
    }
}
