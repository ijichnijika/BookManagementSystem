package com.njit;

import javax.swing.*;

public class MainApplication extends LoginUI{
    public static void main(String[] args) {
        LoginUI ui = new LoginUI();
        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.setVisible(true);
    }
}
