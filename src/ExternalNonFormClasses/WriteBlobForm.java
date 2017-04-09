/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import brms_v2.LogIn;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author www.luv2code.com
 *
 */
public class WriteBlobForm {

    String fname = "", fvarcount = "", formID = "", userID = "", newDate = "", avail = "";
    String app_path = "";

    public WriteBlobForm() {
    }

    public void callClass(String formID, String fname, String fvar, String avail, String userID) {
        this.formID = formID;
        this.fname = fname;
        this.fvarcount = fvar;
        this.userID = userID;
        this.avail = avail;

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
        newDate = sdf.format(new Date());
        
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
        app_path = jarDir;
        
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

    public void write() { //file edited was original file
        Connection myConn = null;
        PreparedStatement myStmt = null;

        FileInputStream input = null;
        try {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb2", "root", "");

            // 2. Prepare statement
            String sql = "UPDATE form SET thisPDF = ?, formName = \"" + fname + "\" , form_vars = \"" + fvarcount + "\" , dateModified = \"" + newDate + "\", status = \"" + avail + "\", userID = \"" + userID + "\" where formID = \"" + formID + "\"";
            System.out.println(sql);
            myStmt = myConn.prepareStatement(sql);

            // 3. Set parameter for resume file name
            File theFile = new File(app_path+"\\templates\\" + fname + ".pdf");
            input = new FileInputStream(theFile);
            myStmt.setBinaryStream(1, input);

            System.out.println("Reading input file: " + theFile.getAbsolutePath());

            // 4. Execute statement
            System.out.println("\nStoring resume in database: " + theFile);
            System.out.println(sql);

            myStmt.executeUpdate();

            System.out.println("\nCompleted successfully!");

        } catch (SQLException | FileNotFoundException exc) {
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

    public void writeReplace(String path) { //file edited was changed
        Connection myConn = null;
        PreparedStatement myStmt = null;

        FileInputStream input = null;
        try {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb2", "root", "");

            // 2. Prepare statement
            String sql = "UPDATE form SET thisPDF = ?, formName = \"" + fname + "\" , form_vars = \"" + fvarcount + "\", dateModified = \"" + newDate + "\", status = \"" + avail + "\",  userID = \"" + userID + "\" where formID = \"" + formID + "\"";
            myStmt = myConn.prepareStatement(sql);

            // 3. Set parameter for resume file name
            File theFile = new File(path);
            input = new FileInputStream(theFile);
            myStmt.setBinaryStream(1, input);

            System.out.println("Reading input file: " + theFile.getAbsolutePath());

            // 4. Execute statement
            System.out.println("\nStoring resume in database: " + theFile);
            System.out.println(sql);

            myStmt.executeUpdate();

            System.out.println("\nCompleted successfully!");

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

    public void writeNew(String path) { //file expected to be anywhere

        Connection myConn = null;
        PreparedStatement myStmt = null;

        FileInputStream input = null;
        try {
            // 1. Get a connection to database
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb2", "root", "");

            // 2. Prepare statement
            String sql = "insert into form (thisPDF, formName, form_vars, dateAdded, dateModified, status, userID) values(?, \"" + fname + "\", \"" + fvarcount + "\", \"" + newDate + "\", \"" + newDate + "\", \"" + avail + "\", \"" + userID + "\")";
            myStmt = myConn.prepareStatement(sql);

            // 3. Set parameter for resume file name
            File theFile = new File(path);
            input = new FileInputStream(theFile);
            myStmt.setBinaryStream(1, input);

            System.out.println("Reading input file: " + theFile.getAbsolutePath());

            // 4. Execute statement
            System.out.println("\nStoring resume in database: " + theFile);
            System.out.println(sql);

            myStmt.executeUpdate();

            System.out.println("\nCompleted successfully!");

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
