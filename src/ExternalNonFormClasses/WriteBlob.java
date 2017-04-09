/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mrRNBean
 */
public class WriteBlob {
    
    String values;
    String fileName;
    
    public WriteBlob() {
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void createQueryValues(String transId, String formId, String dateReq, String dateClaim, String timeReq){
        values = "\""+transId+"\",\""+formId+"\",\""+dateReq+"\",\""+dateClaim+"\",\""+timeReq+"\"";
        System.out.println("\n*************************************\nVALUES to save to formentry.tbl: "+values+"\n*************************************\n");
    }
    
    public void write(){
        Connection myConn = null;
        PreparedStatement myStmt = null;

        FileInputStream input = null;
        try {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb", "root", "baguioclan");

            // 2. Prepare statement
            String sql = "INSERT INTO formentry (information, transID, formID, date_requested, date_to_claim, time_requested) VALUES(?, "+values+")";
            myStmt = myConn.prepareStatement(sql);

            // 3. Set parameter for resume file name
            File theFile = new File("printables/"+fileName+".pdf");
            input = new FileInputStream(theFile);
            myStmt.setBinaryStream(1, input);
            
            System.out.println("\n*********************************************");
            System.out.println("Reading input file: " + theFile.getAbsolutePath());

            // 4. Execute statement
            System.out.println("\nStoring form in database: " + theFile);
            System.out.println(sql);

            myStmt.executeUpdate();

            System.out.println("\nCompleted successfully!");
            System.out.println("*********************************************\n");

        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(WriteBlobForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                close(myConn, myStmt);
            } catch (SQLException ex) {
                Logger.getLogger(WriteBlobForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void close(Connection myConn, Statement myStmt) throws SQLException {
        if (myStmt != null) {
            myStmt.close();
        }

        if (myConn != null) {
            myConn.close();
        }
    }
}
