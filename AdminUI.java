package com.njit;

import db.db;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminUI extends JFrame {
    private db database;
    private JTextField readerIDField, readerNameField, readerGenderField, readerPhoneField, readerAuthorityField, readerPasswordField;
    private JButton addReaderButton, deleteReaderButton, updateReaderButton;
    private JTable readerTable;
    private DefaultTableModel tableModel;

    public AdminUI(String username) {
        super("管理员界面");
        this.database = new db(); // 初始化数据库连接
        initUI();
        this.setSize(900, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    // 初始化UI
    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 读者信息管理面板
        JPanel readerPanel = createReaderManagementPanel();
        tabbedPane.addTab("读者信息管理", readerPanel);
        JPanel bookPanel = createBookManagementPanel();
        tabbedPane.addTab("图书信息管理", bookPanel);
        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);
    }
    //  图书信息管理
    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("图书信息"));
        return panel;
    }
    // 读者信息管理面板
    private JPanel createReaderManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("读者信息"));

        // 设置表格模型
        tableModel = new DefaultTableModel(new String[]{
                "读者ID", "姓名", "性别", "电话", "权限", "密码"}, 0);
        readerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(readerTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 加载读者信息到表格
        loadReaderData();

        // 控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 每一项字段都放到独立的面板中
        controlPanel.add(createFieldPanel("读者ID:", readerIDField = new JTextField(20)));
        readerIDField.setEditable(false); // 设置 ID 不可编辑
        controlPanel.add(createFieldPanel("姓名:", readerNameField = new JTextField(20)));
        controlPanel.add(createFieldPanel("性别:", readerGenderField = new JTextField(20)));
        controlPanel.add(createFieldPanel("电话:", readerPhoneField = new JTextField(20)));
        controlPanel.add(createFieldPanel("权限:", readerAuthorityField = new JTextField(20)));
        controlPanel.add(createFieldPanel("密码:", readerPasswordField = new JTextField(20)));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        addReaderButton = new JButton("添加读者");
        deleteReaderButton = new JButton("删除读者");
        updateReaderButton = new JButton("修改信息");

        buttonPanel.add(addReaderButton);
        buttonPanel.add(deleteReaderButton);
        buttonPanel.add(updateReaderButton);

        controlPanel.add(buttonPanel); // 添加按钮面板到控制面板

        // 按钮事件监听
        initReaderManagementListeners();

        // 表格行选择监听
        readerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = readerTable.getSelectedRow();
                    if (selectedRow != -1) {
                        // 获取选中行的数据并填充到文本框
                        readerIDField.setText((String) tableModel.getValueAt(selectedRow, 0));
                        readerNameField.setText((String) tableModel.getValueAt(selectedRow, 1));
                        readerGenderField.setText((String) tableModel.getValueAt(selectedRow, 2));
                        readerPhoneField.setText((String) tableModel.getValueAt(selectedRow, 3));
                        readerAuthorityField.setText((String) tableModel.getValueAt(selectedRow, 4));
                        readerPasswordField.setText((String) tableModel.getValueAt(selectedRow, 5));
                    }
                }
            }
        });

        // 使用 JScrollPane 包装控制面板
        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        controlScrollPane.setPreferredSize(new Dimension(900, 500));
        panel.add(tablePanel, BorderLayout.NORTH);
        panel.add(controlScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fieldPanel.add(new JLabel(labelText));
        fieldPanel.add(textField);
        return fieldPanel;
    }

    // 按钮事件监听
    private void initReaderManagementListeners() {
        addReaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addReader();
            }
        });
        deleteReaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteReader();
            }
        });
        updateReaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateReader();
            }
        });
    }

    // 增加读者
    private void addReader() {
        String sql = "INSERT INTO Reader (ReaderId, ReaderName, Sex, PhoneNumber, Authority, PassWd) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            pstmt.setString(1, readerIDField.getText());
            pstmt.setString(2, readerNameField.getText());
            pstmt.setString(3, readerGenderField.getText());
            pstmt.setString(4, readerPhoneField.getText());
            pstmt.setString(5, readerAuthorityField.getText());
            pstmt.setString(6, readerPasswordField.getText());
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "读者添加成功！");
            loadReaderData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "添加失败：" + e.getMessage());
        }
    }

    // 删除读者
    private void deleteReader() {
        String sql = "DELETE FROM Reader WHERE ReaderId = ?";
        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            pstmt.setString(1, readerIDField.getText());
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "读者删除成功！");
            loadReaderData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage());
        }
    }

    // 修改读者信息
    private void updateReader() {
        String sql = "UPDATE Reader SET ReaderName = ?, Sex = ?, PhoneNumber = ?, Authority = ?, PassWd = ? " +
                "WHERE ReaderId = ?";
        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            pstmt.setString(1, readerNameField.getText());
            pstmt.setString(2, readerGenderField.getText());
            pstmt.setString(3, readerPhoneField.getText());
            pstmt.setString(4, readerAuthorityField.getText());
            pstmt.setString(5, readerPasswordField.getText());
            pstmt.setString(6, readerIDField.getText());
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "读者信息更新成功！");
            loadReaderData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "更新失败：" + e.getMessage());
        }
    }

    // 加载所有读者信息到表格
    private void loadReaderData() {
        String sql = "SELECT ReaderId, ReaderName, Sex, PhoneNumber, Authority, PassWd FROM Reader";
        try (ResultSet rs = database.executeQuery(sql)) {
            // 清空表格数据
            tableModel.setRowCount(0);

            // 添加新数据到表格
            while (rs.next()) {
                Object[] row = {
                        rs.getString("ReaderId"),
                        rs.getString("ReaderName"),
                        rs.getString("Sex"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Authority"),
                        rs.getString("PassWd")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载读者信息失败：" + e.getMessage());
        }
    }
}
