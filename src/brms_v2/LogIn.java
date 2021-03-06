/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brms_v2;

import ExternalForms.Network;
import ExternalNonFormClasses.SQLConnect;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author mrRNBean
 */
public final class LogIn extends javax.swing.JFrame {

    String username, password, userID;

    /**
     * Creates new form LogIn
     */
    boolean focused1 = false, focused2 = false;

    Main main;
    Network network;
    SQLConnect connect;

    public LogIn() {
        initComponents();
        main = new Main(this); //param this
        network = new Network(this);
        setLocationRelativeTo(null);
        this.setTitle("Barangay Records Management System");
        this.setResizable(false);
        userField.setForeground(Color.decode("0x8C8C8C"));
        userField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                if (!focused1) {
                    userField.setText("");
                    focused1 = true;
                    userField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent fe) {

            }
        });

        passField.getParent().requestFocusInWindow();
        passField.setForeground(Color.decode("0x8C8C8C"));
        passField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                if (!focused2) {
                    passField.setText("");
                    focused2 = true;
                    passField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent fe) {

            }
        });

        jButton1.setVisible(false);
        logInButton.setEnabled(false);
        checkReportsFolder();

    }

    public void checkReportsFolder() {
        //get file path
        CodeSource codeSource = LogIn.class.getProtectionDomain().getCodeSource();
        File jarFile = null;
        try {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        String jarDir = jarFile.getParentFile().getPath();

        System.out.println("PATH: " + jarDir);

        //create folder in app path
        File file = new File(jarDir + "\\templates");
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory templates is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        //create folder in app path
        File file2 = new File(jarDir + "\\printables");
        if (!file2.exists()) {
            if (file2.mkdir()) {
                System.out.println("Directory printables is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
    }

    public void callClass() {
        userField.setForeground(Color.decode("0x8C8C8C"));
        passField.setForeground(Color.decode("0x8C8C8C"));
        userField.setText("Username");
        passField.setText("Password");
        focused1 = false;
        focused2 = false;
        logInButton.setEnabled(false);
        jButton1.setVisible(false);
        this.setVisible(true);
        this.setEnabled(true);
    }

    public void saveFunc() {
        connect = new SQLConnect();
        if (focused1 && focused2) {
            username = userField.getText();
            password = passField.getText();

            if (username != null && !username.equals("") && password != null && !password.equals("")) {
                boolean proceed = false;
                connect.closeCon();
                connect = new SQLConnect();
                ResultSet rs = connect.getUser(username, password);

                try {
                    while (rs.next()) {
                        if (rs.getString("username").equals(username) && rs.getString("password").equals(password) && rs.getString("status").equals("active")) {
                            userID = String.valueOf(rs.getString("userID"));
                            enableMainHideThis();
                            main.callClass(username, password, userID, rs.getString("adminType"));
                            proceed = true;
                            break;
                        }
                    }

                    if (!proceed) {
                        JOptionPane.showMessageDialog(null, "Username and/or Password Invalid!");
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Username and/or Password Invalid!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Username and/or Password Invalid!");
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userField = new javax.swing.JTextField();
        logInButton = new javax.swing.JButton();
        passField = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        userField.setText("Username");
        userField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userFieldActionPerformed(evt);
            }
        });
        userField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                userFieldKeyReleased(evt);
            }
        });

        logInButton.setText("Log In");
        logInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logInButtonActionPerformed(evt);
            }
        });

        passField.setText("password");
        passField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passFieldKeyReleased(evt);
            }
        });

        jButton1.setText("Advanced Options");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(87, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(userField)
                            .addComponent(passField, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                        .addGap(85, 85, 85))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(logInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(133, 133, 133))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(logInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logInButtonActionPerformed
        saveFunc();
    }//GEN-LAST:event_logInButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        network.callClass();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void userFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userFieldActionPerformed

    private void userFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userFieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveFunc();
        } else if ((this.userField.getText().length() > 0) && focused2 && (this.passField.getText().length() > 0)) {
            //if naay text sa userfield && na-click na ang password field which would mean na naerase na ang default text
            logInButton.setEnabled(true);
        } else {
            logInButton.setEnabled(false);
        }
    }//GEN-LAST:event_userFieldKeyReleased

    private void passFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passFieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            saveFunc();
        } else if (focused2 && (this.passField.getText().length() > 0)) {
            logInButton.setEnabled(true);
        } else {
            logInButton.setEnabled(false);
        }
    }//GEN-LAST:event_passFieldKeyReleased

    public void enableMainHideThis() {
        main.setEnabled(true);
        main.setVisible(true);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LogIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            NativeInterface.open();
            SwingUtilities.invokeLater(() -> {
                new LogIn().setVisible(true);
            });
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton logInButton;
    private javax.swing.JPasswordField passField;
    private javax.swing.JTextField userField;
    // End of variables declaration//GEN-END:variables
}
