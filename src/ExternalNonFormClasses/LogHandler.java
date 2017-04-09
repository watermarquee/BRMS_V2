/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import brms_v2.Main;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author mrRNBean
 */
public class LogHandler {

    Main main;
    String userID;
    JTable logTable;
    SQLConnect connect;
    DefaultTableModel model;

    public LogHandler(Main main, JTable j) {
        this.main = main;
        this.logTable = j;
        connect = new SQLConnect();
    }

    public void setUserId(String userID) {
        this.userID = userID;
    }

    public void saveLog(String action) {
        connect.closeCon();
        connect = new SQLConnect();
        connect.createLog(action, userID); //param 1 is Action
    }
    
    
    public void setLogHistory() {
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAllLogs();
        setModelAndClearModelItems();

        try {
            for (int x = 0; rs.next(); x++) {
                model.insertRow(x, new String[]{String.valueOf(rs.getInt("logID")), String.valueOf(rs.getInt("userID")), rs.getString("action"), rs.getString("date_created") + "; "+rs.getString("time_created") });
            }
        } catch (SQLException ex) {
            System.out.println("Error Set loghistory Data: " + ex);
        }
    }

    public void displayActionDialog(String logID) { //displays a readable version of log action
        
    }

    public void setModelAndClearModelItems() {
        //ERASES ALL DATA FROM A TABLE 
        model = (DefaultTableModel) logTable.getModel();
        while (logTable.getRowCount() > 0) {
            model.removeRow(0);
        }
    }
}
