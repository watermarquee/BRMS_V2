/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import brms_v2.LogIn;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author www.luv2code.com
 *
 */
public class ReadBlob {

    Connection myConn = null;
    Statement myStmt = null;
    ResultSet myRs = null;

    InputStream input = null;
    FileOutputStream output = null;
    String formName;
    String app_path = "";

    public ReadBlob() {
    }

    public void setFileName(String fileName) {
        this.formName = fileName;
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
    
    
    public void read() {
        try {
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb2", "root", "");

            myStmt = myConn.createStatement();
            String sql = "select thisPDF from form where (formName = \"" + formName + "\" OR formTitle = \"" + formName + "\")";
            myRs = myStmt.executeQuery(sql);

            File theFile = new File(app_path+"\\templates\\" + formName + ".pdf");
            output = new FileOutputStream(theFile);

            if (myRs.next()) {

                input = myRs.getBinaryStream("thisPDF");
                System.out.println("\n*********************************************");
                System.out.println("Reading form from database...");
                System.out.println(sql);

                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    output.write(buffer);
                }

                System.out.println("\nSaved to file: " + theFile.getAbsolutePath());

                System.out.println("\nCompleted successfully!");
                System.out.println("*********************************************\n");

            }

        } catch (SQLException | IOException exc) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                close(myConn, myStmt);
            } catch (SQLException ex) {
                Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void readForRequestedFormDisplay(String formID) {
        try {
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb2", "root", "");

            myStmt = myConn.createStatement();
            //String sql = "select information from formentry where formentryid = \"" + formID + "\" ";
            String sql = "select thisPDF from form where formID = \"" + formID + "\" ";
            myRs = myStmt.executeQuery(sql);

            File theFile = new File(app_path+"\\templates\\" + formName + ".pdf");
            output = new FileOutputStream(theFile);

            if (myRs.next()) {

                input = myRs.getBinaryStream("thisPDF");
                System.out.println("\n*********************************************");
                System.out.println("Reading form from database...");
                System.out.println(sql);

                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    output.write(buffer);
                }

                System.out.println("\nSaved to file: " + theFile.getAbsolutePath());

                System.out.println("\nCompleted successfully!");
                System.out.println("*********************************************\n");

            }

        } catch (SQLException | IOException exc) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                close(myConn, myStmt);
            } catch (SQLException ex) {
                Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void readForFormDisplay(String formID) {
        try {
            myConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/brgydb2", "root", "");

            myStmt = myConn.createStatement();
            String sql = "select thisPDF from form where formid = \"" + formID + "\" ";
            myRs = myStmt.executeQuery(sql);

            File theFile = new File(app_path+"\\templates\\" + formName + ".pdf");
            output = new FileOutputStream(theFile);

            if (myRs.next()) {

                input = myRs.getBinaryStream("thisPDF");
                System.out.println("\n*********************************************");
                System.out.println("Reading resume from database...");
                System.out.println(sql);

                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    output.write(buffer);
                }

                System.out.println("\nSaved to file: " + theFile.getAbsolutePath());

                System.out.println("\nCompleted successfully!");
                System.out.println("*********************************************\n");

            }

        } catch (SQLException | IOException exc) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                close(myConn, myStmt);
            } catch (SQLException ex) {
                Logger.getLogger(ReadBlob.class.getName()).log(Level.SEVERE, null, ex);
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
