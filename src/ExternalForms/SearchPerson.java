/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalForms;

import ExternalNonFormClasses.LogHandler;
import ExternalNonFormClasses.SQLConnect;
import brms_v2.Main;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author mrRNBean
 */
public class SearchPerson extends JDialog {

    Main main;
    SQLConnect connect;
    String personType;
    DefaultTableModel model;
    LogHandler logHandler;

    public SearchPerson(Main main) {
        super(main);
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        connect = new SQLConnect();
        this.main = main;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                enableMainHideThis();
            }
        });
    }

    public void callClass(String personType, LogHandler logHandler) {
        this.personType = personType;
        this.logHandler = logHandler;
        this.setVisible(true);
        this.main.setEnabled(false);

        setData();
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
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        citizens = new javax.swing.JTable();
        searchText = new javax.swing.JTextField();
        search = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cancel = new javax.swing.JButton();
        select = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        viewPanel.setBackground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        citizens.setBackground(new java.awt.Color(189, 195, 198));
        citizens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Last Name", "First Name", "Address"
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

        search.setBackground(new java.awt.Color(189, 195, 198));
        search.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        search.setForeground(new java.awt.Color(0, 102, 153));
        search.setText("Search");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 102, 153));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Select Person Name");
        jLabel1.setOpaque(true);

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(viewPanelLayout.createSequentialGroup()
                        .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(viewPanelLayout.createSequentialGroup()
                                .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(356, 356, 356))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
        );

        cancel.setBackground(new java.awt.Color(189, 195, 198));
        cancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        cancel.setForeground(new java.awt.Color(0, 102, 153));
        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        select.setBackground(new java.awt.Color(189, 195, 198));
        select.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        select.setForeground(new java.awt.Color(0, 102, 153));
        select.setText("Select");
        select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(328, Short.MAX_VALUE)
                .addComponent(select)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancel)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(16, Short.MAX_VALUE)
                    .addComponent(viewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(16, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(417, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel)
                    .addComponent(select))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(viewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(47, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        //TOP BUTTON AT VIEW CITIZEN'S PANEL
        if (!searchText.getText().equals("")) {
            cancel.setText("Back");
            setModelAndClearModelItems(citizens);
            connect = new SQLConnect();
            ResultSet rs = connect.getPerson(personType, searchText.getText());
            try {
                int x = 0;
                while (rs.next()) {
                    model.insertRow(x++, new String[]{String.valueOf(rs.getInt("personID")), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
                }

                if (x == 0) {
                    JOptionPane.showMessageDialog(null, "No Results Found!");
                    searchText.setText("");
                    cancel.setText("Cancel");
                    setData();
                }

            } catch (SQLException ex) {
                System.out.println("Error: " + ex);
            }
        }
    }//GEN-LAST:event_searchActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        if (cancel.getText().equals("Back")) {
            searchText.setText("");
            cancel.setText("Cancel");
            setData();
        } else {
            enableMainHideThis();
        }
    }//GEN-LAST:event_cancelActionPerformed

    private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
        if (citizens.getSelectedRow() >= 0) {
            String tid = "";
            connect.closeCon();
            connect = new SQLConnect();
            String personID = String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 0));
            connect.closeCon();
            connect = new SQLConnect();
            if (!connect.hasTrans(personID)) { //for citizens since guests do have transIDs
                connect.closeCon();
                connect = new SQLConnect();
                connect.storeTrans(personID);
                
                connect.closeCon();
                connect = new SQLConnect();
                tid = connect.getpTransID(personID);
                String tempName = String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 2)) +" "+ String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 1));
                logHandler.saveLog("Created New Transaction with -Transaction ID: " + tid + " -Person ID: " + personID + " -Person Name: " + tempName); //Viewed Transaction ID; Person ID, Name	
            } else {
                tid = connect.getpTransID(personID);
            }
            
            System.out.println("Searched Table. Making a Request with TransID: " + tid + " -> " + String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 2)) + " " + String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 1)));
            main.makeRequest(tid, String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 2)) + " " + String.valueOf(citizens.getValueAt(citizens.getSelectedRow(), 1)));
            enableMainHideThis();
        }
    }//GEN-LAST:event_selectActionPerformed

    public void enableMainHideThis() {
        main.setEnabled(true);
        main.setVisible(true);
        dispose();
    }

    public void setModelAndClearModelItems(JTable tb) {
        //ERASES ALL DATA FROM A TABLE 
        model = (DefaultTableModel) tb.getModel();
        while (tb.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    private void setData() {
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = null;

        if (personType.equals("citizen")) {
            rs = connect.getAllCitizenPersons();
        } else if (personType.equals("guest")) {
            rs = connect.getAllGuestPersons();
        }

        setModelAndClearModelItems(citizens);
        int rowCount = 0;
        try {
            while (rs.next()) {
                model.insertRow(rowCount++, new String[]{String.valueOf(rs.getInt("personID")), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchPerson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JTable citizens;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton search;
    private javax.swing.JTextField searchText;
    private javax.swing.JButton select;
    private javax.swing.JPanel viewPanel;
    // End of variables declaration//GEN-END:variables
}
