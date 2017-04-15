/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalForms;

import ExternalNonFormClasses.LogHandler;
import brms_v2.Main;
import ExternalNonFormClasses.SQLConnect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author mrRNBean
 */
public class CreateUser extends JDialog {

    Main main;
    String officialID, storeMethod = "", userID = ""; //create; update
    String adminID, position, personID, personName, userNamePrim, passwordPrim;
    int comPrim;
    LogHandler logHandler;

    public CreateUser(Main main) {
        super(main);
        initComponents();
        this.main = main;

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                enableMainHideThis();
            }
        });

    }

    public void callClassAdd(String officialID, String method) {
        this.storeMethod = method;
        this.officialID = officialID;

        this.setVisible(true);
        this.main.setEnabled(false);

        title.setText("Create New User");

        if (method.equals("update")) {
            placeData();
        }
        save.setEnabled(false);
    }

    public void callClassEdit(String userID, String method) {
        this.storeMethod = method;
        this.userID = userID;

        this.setVisible(true);
        this.main.setEnabled(false);

        title.setText("Edit User");

        if (method.equals("update")) {
            placeData();
        }

        save.setEnabled(false);
    }

    public void setLogHandler(LogHandler logHandler) {
        this.logHandler = logHandler;
    }

    public void setLogDetails(String adminID, String position, String personID, String personName) {
        this.adminID = adminID;
        this.position = position;
        this.personID = personID;
        this.personName = personName;
    }

    public void setLogDetails(String personID, String personName) {
        this.personID = personID;
        this.personName = personName;
    }

    public void writeLogAdded() {
        // User ID; Official ID ; Administration ID, Term Dates; Person ID, Name
        SQLConnect tc = new SQLConnect();
        ResultSet rs = tc.getLatestUserID();
        try {
            logHandler.saveLog("Added New User with"
                    + " -New User ID: " + rs.getInt("userID")
                    + " -New User Name: " + user.getText()
                    + " -New User Access Type: " + String.valueOf(combo1.getSelectedItem())
                    + " -Official Administration ID: " + adminID
                    + " -Official ID: " + officialID
                    + " -Official Position: " + position
                    + " -Official Person ID: " + personID
                    + " -Official Person Name: " + personName
            );
        } catch (SQLException ex) {
            Logger.getLogger(OfficialSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeLogEdited() {
        // User ID; Official ID ; Administration ID, Term Dates; Person ID, Name
        SQLConnect tc = new SQLConnect();
        ResultSet rs = tc.getOffIDByUserID(userID);

        try {
            logHandler.saveLog("Edited User with"
                    + " -User ID: " + userID
                    + " -New User Name: " + user.getText()
                    + " -New User Access Type: " + String.valueOf(combo1.getSelectedItem())
                    + " -Official ID: " + rs.getInt("officialID")
                    + " -Official Person ID: " + personID
                    + " -Official Person Name: " + personName
            );
        } catch (SQLException ex) {
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
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

        save = new javax.swing.JButton();
        back = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        user = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pass = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        combo1 = new javax.swing.JComboBox<>();
        title = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        save.setBackground(new java.awt.Color(189, 195, 198));
        save.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        save.setForeground(new java.awt.Color(0, 102, 153));
        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        back.setBackground(new java.awt.Color(189, 195, 198));
        back.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        back.setForeground(new java.awt.Color(0, 102, 153));
        back.setText("Cancel");
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 102, 153));

        user.setBackground(new java.awt.Color(255, 255, 255));
        user.setForeground(new java.awt.Color(0, 0, 0));
        user.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                userKeyReleased(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(0, 102, 153));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Username:");
        jLabel1.setOpaque(true);

        jLabel2.setBackground(new java.awt.Color(0, 102, 153));
        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Password:");
        jLabel2.setOpaque(true);

        pass.setBackground(new java.awt.Color(255, 255, 255));
        pass.setForeground(new java.awt.Color(0, 0, 0));
        pass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passKeyReleased(evt);
            }
        });

        jLabel3.setBackground(new java.awt.Color(0, 102, 153));
        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Admin Type:");
        jLabel3.setOpaque(true);

        combo1.setBackground(new java.awt.Color(255, 255, 255));
        combo1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        combo1.setForeground(new java.awt.Color(0, 102, 153));
        combo1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Level 1", "Level 2", "Level 3" }));
        combo1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                combo1MouseClicked(evt);
            }
        });

        title.setBackground(new java.awt.Color(255, 255, 255));
        title.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        title.setForeground(new java.awt.Color(0, 102, 153));
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText("Create User");
        title.setOpaque(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(combo1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(pass, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(user, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(user, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(combo1)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(back, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(save)
                    .addComponent(back))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backActionPerformed
        this.setVisible(false);
        enableMainHideThis();
    }//GEN-LAST:event_backActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        String msg = "";
        SQLConnect tc = new SQLConnect();
        if (!isUserTaken()) {
            if (storeMethod.equals("create")) {
                tc.storeUser(officialID, user.getText(), pass.getText(), String.valueOf(combo1.getSelectedItem()));
                msg = "Succesfully Added User!";

                writeLogAdded();
            } else if (storeMethod.equals("update")) {
                tc.updateUser(userID, user.getText(), pass.getText(), String.valueOf(combo1.getSelectedItem()));
                msg = "Succesfully Updated User!";

                writeLogEdited();
            }

            //Added User ID; Official ID; Administration ID, Term Dates; Person ID, Name
            JOptionPane.showMessageDialog(this, msg);

            main.loadUsers();
            this.setVisible(false);
            enableMainHideThis();
        } else {
            JOptionPane.showMessageDialog(this, "Username Already Taken.");
        }
    }//GEN-LAST:event_saveActionPerformed

    private void userKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_userKeyReleased
        if (storeMethod.equals("create") && hasInputs()) {
            save.setEnabled(true);
        } else if (storeMethod.equals("update") && hasInputs() && isInputUpdated()) {
            save.setEnabled(true);
        } else {
            save.setEnabled(false);
        }
    }//GEN-LAST:event_userKeyReleased

    private void passKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passKeyReleased
        if (storeMethod.equals("create") && hasInputs()) {
            save.setEnabled(true);
        } else if (storeMethod.equals("update") && hasInputs() && isInputUpdated()) {
            save.setEnabled(true);
        } else {
            save.setEnabled(false);
        }
    }//GEN-LAST:event_passKeyReleased

    private void combo1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_combo1MouseClicked
        if (storeMethod.equals("create") && hasInputs()) {
            save.setEnabled(true);
        } else if (storeMethod.equals("update") && hasInputs() && isInputUpdated()) {
            save.setEnabled(true);
        } else {
            save.setEnabled(false);
        }
    }//GEN-LAST:event_combo1MouseClicked

    public void enableMainHideThis() {
        main.setEnabled(true);
        main.setVisible(true);
        dispose();
    }

    public void placeData() {
        try {
            SQLConnect tc = new SQLConnect();
            ResultSet rx = tc.getUser(userID);
            if (rx.next()) {
                user.setText(rx.getString("username"));
                pass.setText(rx.getString("password"));
                combo1.setSelectedItem(rx.getString("adminType"));

                userNamePrim = user.getText();
                passwordPrim = pass.getText();
                comPrim = combo1.getSelectedIndex();
            }
        } catch (SQLException ex) {
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean isUserTaken() {
        try {
            SQLConnect tc = new SQLConnect();
            ResultSet rx = tc.getUsers();
            while (rx.next()) {
                if (user.getText().equals(rx.getString("username"))) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean isInputUpdated() {
        return !user.getText().equals(userNamePrim)
                || !pass.getText().equals(passwordPrim);
    }

    public boolean hasInputs() {
        return user.getText().length() > 0
                && pass.getText().length() > 0;
    }
    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back;
    private javax.swing.JComboBox<String> combo1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField pass;
    private javax.swing.JButton save;
    private javax.swing.JLabel title;
    private javax.swing.JTextField user;
    // End of variables declaration//GEN-END:variables
}
