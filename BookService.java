package com.njit.service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import com.njit.ReaderUI;
import db.db;

public class BookService extends JPanel {

    private JTextField searchField;
    private JButton searchButton, borrowButton, returnButton;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private db database;
    private String loggedInUsername;  // 存储当前登录的用户名

    // 接受 db 实例和 loggedInUsername
    public BookService(db database, String loggedInUsername) {
        this.database = database;
        this.loggedInUsername = loggedInUsername;
        this.setLayout(new BorderLayout());

        // 初始化界面
        initUI();
    }

    public void initUI() {
        // 顶部搜索框和按钮
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("查询");
        searchPanel.add(new JLabel("搜索书名或作者:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 中间的表格
        String[] columnNames = {"ISBN", "书名", "作者", "价格", "出版时间", "图书状态"};
        tableModel = new DefaultTableModel(columnNames, 0);
        bookTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(bookTable);

        // 底部按钮
        JPanel buttonPanel = new JPanel();
        borrowButton = new JButton("借书");
        returnButton = new JButton("还书");
        buttonPanel.add(borrowButton);
        buttonPanel.add(returnButton);

        // 添加到主窗口
        this.add(searchPanel, BorderLayout.NORTH);
        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // 添加事件监听
        initListeners();

        // 加载数据库中的所有书籍信息
        loadBooksData();
    }

    private void loadBooksData() {
        if (database == null) {
            JOptionPane.showMessageDialog(this, "数据库连接未初始化！");
            return;
        }

        String sql = "SELECT b.BookId,b.isbn, bc.BookName, bc.AuthorName, bc.PublisherName, bc.PublishTime, b.BookStatus, b.BookLocation, bc.Price " +
                "FROM Book b " +
                "JOIN BookCopyright bc ON b.ISBN = bc.ISBN";
        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            // 清空表格
            tableModel.setRowCount(0);

            // 填充数据
            while (rs.next()) {
                Object[] row = {
                        rs.getString("ISBN"),
                        rs.getString("BookName"),
                        rs.getString("AuthorName"),
                        rs.getDouble("Price"),
                        rs.getDate("PublishTime") != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("PublishTime")) : "",
                        rs.getString("BookStatus")
                };
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "当前没有书籍数据！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载书籍数据失败，请检查数据库连接！");
        }
    }

    private void initListeners() {
        searchButton.addActionListener(evt -> searchBooks());
        borrowButton.addActionListener(evt -> borrowBook());
        returnButton.addActionListener(evt -> returnBook());
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();  // 获取用户输入的关键词
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词！");
            return;
        }

        // SQL 查询语句，使用 LIKE 来模糊查询书名和作者
        String sql = "SELECT b.ISBN, bc.BookName, bc.AuthorName, bc.PublisherName, bc.PublishTime, b.BookStatus, b.BookLocation, bc.Price " +
                "FROM Book b " +
                "JOIN BookCopyright bc ON b.ISBN = bc.ISBN " +
                "WHERE bc.BookName LIKE ? OR bc.AuthorName LIKE ?";

        try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
            // 设置查询参数，使用 % 进行模糊匹配
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");

            ResultSet rs = pstmt.executeQuery();

            // 清空表格
            tableModel.setRowCount(0);

            // 填充数据到表格
            while (rs.next()) {
                Object[] row = {
                        rs.getString("ISBN"),
                        rs.getString("BookName"),
                        rs.getString("AuthorName"),
                        rs.getDouble("Price"),
                        rs.getDate("PublishTime") != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("PublishTime")) : "",
                        rs.getString("BookStatus")
                };
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "未找到匹配的书籍！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询失败，请检查数据库连接！");
        }
    }

    private void borrowBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一本书！");
            return;
        }

        String isbn = (String) tableModel.getValueAt(selectedRow, 0);  // 获取ISBN
        String bookStatus = (String) tableModel.getValueAt(selectedRow, 5);  // 获取当前图书状态

        // 获取当前登录用户的用户名作为借阅人
        String readerName = loggedInUsername;

        if (bookStatus.equals("已借出")) {
            JOptionPane.showMessageDialog(this, "该书已被借出，无法借阅！");
            return;
        }

        // 向临时表插入 ReaderName 和 ISBN
        String insertTempSql = "INSERT INTO TempReaderInfo (ISBN, ReaderName) VALUES (?, ?)";
        try (PreparedStatement pstmt = database.PreparedStatement(insertTempSql)) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, readerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "无法插入临时记录，借书失败！");
            return;
        }

        // 更新 Book 表的 BookStatus 为 '已借出'，此操作会触发触发器
        String updateSql = "UPDATE Book SET BookStatus = '已借出' WHERE isbn = ?";
        try (PreparedStatement pstmt = database.PreparedStatement(updateSql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();

            // 提示借书成功
            JOptionPane.showMessageDialog(this, "借书成功！");
            tableModel.setValueAt("已借出", selectedRow, 5);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "借书失败，请检查数据库连接！");
        }
    }


    private void returnBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一本书！");
            return;
        }

        String isbn = (String) tableModel.getValueAt(selectedRow, 0);
        String bookStatus = (String) tableModel.getValueAt(selectedRow, 5); // 获取图书状态列

        // 只有在图书状态为"已借出"时才能还书
        if ("已借出".equals(bookStatus)) {
            String sql = "UPDATE Book SET BookStatus = '未借出' WHERE ISBN = ?";
            try (PreparedStatement pstmt = database.PreparedStatement(sql)) {
                pstmt.setString(1, isbn);
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "还书成功！");
                    tableModel.setValueAt("未借出", selectedRow, 5); // 更新图书状态
                } else {
                    JOptionPane.showMessageDialog(this, "还书失败！");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "还书失败，请检查数据库连接！");
            }
        } else {
            JOptionPane.showMessageDialog(this, "该书未被借出，无需归还！");
        }
    }
}
