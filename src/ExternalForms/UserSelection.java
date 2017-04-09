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
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author mrRNBean
 */
public class UserSelection extends JDialog {

    Main main;
    CreateUser createUser;
    DefaultTableModel model;
    String currentOfficialViewedID, adminID;
    LogHandler logHandler;

    /**
     * Creates new form OfficialSelection
     *
     * @param main
     */
    public UserSelection(Main main) {
        super(main);
        initComponents();

        this.main = main;
        createUser = new CreateUser(main);

        setLocationRelativeTo(null);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                enableMainHideThis();
            }
        });
    }
    public void setLogHandler(LogHandler logHandler){
        this.logHandler = logHandler;
    }
    public void callClass(String adminID) {
        this.adminID = adminID;

        this.setVisible(true);
        this.main.setEnabled(false);

        createUser.setLogHandler(logHandler);
        setViewOfficialsData();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        citizens = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        select = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        viewPanel.setBackground(new java.awt.Color(0, 102, 153));

        citizens.setBackground(new java.awt.Color(189, 195, 198));
        citizens.setForeground(new java.awt.Color(0, 0, 0));
        citizens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Official ID", "Position", "Last Name", "First Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(citizens);
        if (citizens.getColumnModel().getColumnCount() > 0) {
            citizens.getColumnModel().getColumn(0).setPreferredWidth(20);
        }

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 102, 153));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Select New User");
        jLabel1.setOpaque(true);

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE))
                .addContainerGap())
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        select.setBackground(new java.awt.Color(189, 195, 198));
        select.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        select.setForeground(new java.awt.Color(0, 102, 153));
        select.setText("Select");
        select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectActionPerformed(evt);
            }
        });

        cancel.setBackground(new java.awt.Color(189, 195, 198));
        cancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cancel.setForeground(new java.awt.Color(0, 102, 153));
        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(353, Short.MAX_VALUE)
                .addComponent(select)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancel)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(429, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(select)
                    .addComponent(cancel))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(viewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(50, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        enableMainHideThis();
    }//GEN-LAST:event_cancelActionPerformed

    private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
        //FIRST BUTTON BELOW VIEW CITIZENS PANEL
        if (citizens.getSelectedRow() >= 0) { //IF ITEM IS SELECTED FROM TABLE
            currentOfficialViewedID = String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 0));
            //setLogDetails
            //(String adminID adminID
            //String position, String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 1))
            //String personID, getPersonIDByOffID(String oID)
            //String personName)

            createUser.callClassAdd(currentOfficialViewedID, "create");
            String personName = String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 3)) + " "+ String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 2));

            SQLConnect tc = new SQLConnect();
            SQLConnect tc1;
            ResultSet rs = tc.getPersonIDByOffID(currentOfficialViewedID);
            try {
                if (rs.next()) {
                    createUser.setLogDetails(adminID, String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 1)), String.valueOf(rs.getInt("personID")), personName);
                }
            } catch (SQLException ex) {
                Logger.getLogger(UserSelection.class.getName()).log(Level.SEVERE, null, ex);
            }
            enableMainHideThis();

        }
    }//GEN-LAST:event_selectActionPerformed

    public void setModelAndClearModelItems(JTable tb) {
        //ERASES ALL DATA FROM A TABLE 
        model = (DefaultTableModel) tb.getModel();
        while (tb.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    public void enableMainHideThis() {
        main.setEnabled(true);
        main.setVisible(true);
        dispose();
    }

    private void setViewOfficialsData() {
        //DISPLAY ALL CITIZEN RECORDS AT VIEW CITIZENS PANEL'S TABLE
        SQLConnect tc = new SQLConnect();
        SQLConnect tc1;
        ResultSet rs = tc.getOfficialsInAdmin(adminID);
        setModelAndClearModelItems(citizens);

        try {
            for (int x = 0; rs.next();) {
                tc = new SQLConnect();
                if (tc.isOfficial(String.valueOf(rs.getInt("personID")))) {
                    tc1 = new SQLConnect();
                    String offID = String.valueOf(rs.getInt("officialID"));
                    if (!tc1.isUser(offID)) {
                        String pId = "";
                        SQLConnect tc2 = new SQLConnect();
                        ResultSet cr = tc2.getPersonIDByOffID(offID);
                        if (cr.next()) {
                            pId = String.valueOf(cr.getInt("personID"));
                        }
                        SQLConnect tc3 = new SQLConnect();
                        if (!tc3.isInactiveOfficialUser(pId)) {
                            tc1 = new SQLConnect();
                            cr = tc1.getPerson(pId);
                            if (cr.next()) {
                                model.insertRow(x, new String[]{String.valueOf(rs.getInt("officialID")), rs.getString("position"), cr.getString("lname"), cr.getString("fname")});
                                x++;
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JTable citizens;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton select;
    private javax.swing.JPanel viewPanel;
    // End of variables declaration//GEN-END:variables
}