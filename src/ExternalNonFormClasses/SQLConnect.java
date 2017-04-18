/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import ExternalForms.Browser;
import brms_v2.LogIn;
import brms_v2.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author mrRNBean
 */
public class SQLConnect {

    private Connection con;
    private Statement st;
    private ResultSet rs;
    private ArrayList<String> details;
    private String ip, db, uname, pword;
    private boolean disp = true;

    public SQLConnect() {

        //details = new ArrayList<>();
        getIPAddress();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //change user and password in last two params below
            //con = DriverManager.getConnection("jdbc:mysql://localhost:3306/brgydb2", "root", "");
            System.out.println("jdbc:mysql://" + ip + ":3306/" + db + "," + uname + "," + pword);
            con = DriverManager.getConnection("jdbc:mysql://" + ip + ":3306/" + db, uname, pword);
            st = con.createStatement();

        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Error in constructor: " + ex);
            JOptionPane.showMessageDialog(null, "Database Connection Error. Please recheck \'Advanced Options\' configuration.");
            disp = false;
        }
    }
    
    public void createDBUser(){
        try {
            String query = "CREATE USER IF NOT EXISTS 'root2'@'localhost' IDENTIFIED BY 'pass';";
            st.execute(query);
            
            query = "GRANT ALL ON my_db.* TO 'root2'@'localhost';";
            st.execute(query);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "CANNOT CREATE USER");
        }
    }

    public boolean toDispose() {
        return disp;
    }

    public void closeCon() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {

            }
        }
    }

    public ResultSet getAllPersons() {
        try {
            String query = "SELECT * FROM person;";
            return st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllCitizenPersons: " + ex);
        }

        return null;
    }

    public ResultSet getAllGuestPersons() {
        try {
            String query = "SELECT * FROM person WHERE personType = \"guest\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllCitizenPersons: " + ex);
        }

        return rs;
    }

    public ResultSet getAllCitizenPersons() {
        try {
            String query = "SELECT * FROM person WHERE personType = \"citizen\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllCitizenPersons: " + ex);
        }

        return rs;
    }

    public void storeNewPerson(String personType, String values) {

        String query = "INSERT INTO person (personType, lname, fname, mname, nameEx, dob, gender, address) VALUES (\"" + personType + "\", " + values + ");";

        try {
            st.executeUpdate(query);
        } catch (Exception ex) {
            System.out.println("Error storeNewPerson: " + ex);
        }
    }

    public void storeNewPersonAddInfo(String queryType, String values, String personID) {
        String query = "";
        switch (queryType) {
            case "personAddInfo":
                query = "INSERT INTO personaddinfo (age, pob, status, contact, zipCode, precint, occupation, email, religion, personID) VALUES (" + values + ", \"" + personID + "\");";
                break;
            case "personFamily":
                query = "INSERT INTO personfamily (lname, fname, mname, dob, relativeType, personID) VALUES (" + values + ", \"" + personID + "\");";
                break;
            case "person_education":
                query = "INSERT INTO person_education (level, school_name, year_graduated, personID) VALUES (" + values + ", \"" + personID + "\");";
                break;
        }
        try {
            st.executeUpdate(query);
        } catch (Exception ex) {
            System.out.println("Error in storeNewPersonAddInfo for " + queryType + ": " + ex);
        }
    }

    public void updatePerson(String pID, String personType, String table, String values, String addParam) {
        String query = "";

        if (addParam.equals("none") || addParam.equals("")) {
            query = "UPDATE " + table + " SET " + values + " WHERE personID = \"" + pID + "\";";
        } else {
            switch (addParam) {
                case "FATHER":
                case "MOTHER":
                case "SIBLING":
                case "CHILD":
                    query = "UPDATE " + table + " SET " + values + " WHERE personID = \"" + pID + "\" AND relativeType = \"" + addParam + "\";";
                    break;
                case "ELEMENTARY":
                case "HIGH SCHOOL":
                case "COLLEGE":
                case "VOCATIONAL":
                case "GRADUATE":
                    query = "UPDATE " + table + " SET " + values + " WHERE personID = \"" + pID + "\" AND level = \"" + addParam + "\";";
                    break;
            }
        }

        System.out.println("Update Person Query: " + query);
        try {
            st.executeUpdate(query);
        } catch (Exception ex) {
            System.out.println("Error updatePerson for " + addParam + ": " + ex);
        }
    }

    public ResultSet getPersonTables(String table) {

        try {
            String query = "SELECT * FROM " + table + ";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPersonTables: " + ex);
        }

        return rs;
    }

    public boolean personHas(String pID, String table) {

        try {
            rs = getPersonTables(table);

            while (rs.next()) {
                if (pID.equals(String.valueOf(rs.getInt("personID")))) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public boolean personHas(String pID, String table, String addParam, String addParamField) {

        try {
            rs = getPersonTables(table);

            while (rs.next()) {
                if (pID.equals(String.valueOf(rs.getInt("personID"))) && addParam.equals(rs.getString(addParamField))) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public ResultSet getPerson(String pID) {

        try {
            String query = "SELECT * FROM person WHERE personID = \"" + pID + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPerson: " + ex);
        }

        return rs;
    }

    public ResultSet getPersonBday(String personType, String search, String searchType) {
        try {
            String query = null;

            if (!searchType.equals("MY")) { //MDY, MD, DY
                query = "SELECT * FROM person WHERE personType = \"" + personType + "\" AND dob LIKE \"%" + search + "%\";";
            } else if (searchType.equals("MY")) {
                String x[] = search.split("/", 2);
                query = "SELECT * FROM person WHERE personType = \"" + personType + "\" AND (dob LIKE \"%" + x[0] + "%\" AND dob LIKE \"%" + x[1] + "%\");";
            }
            System.out.println(query);
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPerson: " + ex);
        }

        return rs;
    }

    //SELECT field1, field2 FROM table WHERE NOT columnA = 'x' AND NOT columbB = 'y'
    public ResultSet getClaimables() {
        try {
            String query = null;

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
            String dateFormat = sdf.format(new Date());

            query = "SELECT * FROM formentry WHERE date_claimed = \"N/A\" AND  date_to_claim = \"" + dateFormat + "\";";

            //System.out.println(query);
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPerson: " + ex);
        }

        return rs;
    }

    public ResultSet getPerson(String personType, String search) {
        try {
            String query;

            if (personType.equals("citizen")) {
                query = "SELECT * FROM person WHERE personType = \"citizen\" AND (lname LIKE \"%" + search + "%\" OR fname LIKE \"%" + search + "%\") ;";
            } else if (personType.equals("guest")) {
                query = "SELECT * FROM person WHERE personType = \"guest\" AND (lname LIKE \"%" + search + "%\" OR fname LIKE \"%" + search + "%\") ;";
            } else {
                query = "SELECT * FROM person WHERE lname LIKE \"%" + search + "%\" OR fname LIKE \"%" + search + "%\" ;";
            }
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPerson: " + ex);
        }

        return rs;
    }

    public ResultSet getPersonCs(String personID, String cs) {
        try {
            String query = "SELECT * FROM personaddinfo WHERE personID = \"" + personID + "\" AND status = \"" + cs + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPerson: " + ex);
        }

        return rs;
    }

    public ResultSet getPersonViaGender(String personType, String gender) {
        try {
            String query = "SELECT * FROM person WHERE personType = \"" + personType + "\" AND gender = \"" + gender + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getPerson: " + ex);
        }

        return rs;
    }

    public ResultSet getPersonInfo(String queryType, String personID, String addParam) {
        String query = "";
        switch (queryType) {
            case "personAddInfo":
                query = "SELECT * FROM personaddinfo WHERE personID = \"" + personID + "\";";
                break;
            case "personFamily":
                query = "SELECT * FROM personfamily WHERE personID = \"" + personID + "\" AND relativeType = \"" + addParam + "\";";
                break;
            case "person_education":
                query = "SELECT * FROM person_education WHERE personID = \"" + personID + "\" AND level = \"" + addParam + "\";";
                break;
        }

        try {
            rs = st.executeQuery(query);
        } catch (Exception ex) {
            System.out.println("Error in getPersonInfo for " + queryType + " : " + ex);
        }

        return rs;
    }

    public ResultSet getActiveAdminOfficials(String adminID) {
        try {
            String query = "SELECT * FROM brgy_admin WHERE adminID = \"" + adminID + "\" AND status = \"active\"";
            rs = st.executeQuery(query);

        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public ResultSet getAdmin(String adminID) {
        try {
            String query = "SELECT * FROM brgy_admin WHERE adminID = \"" + adminID + "\";";
            rs = st.executeQuery(query);

        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public int getAllAdminsCount() {
        try {
            String query = "SELECT COUNT(*) FROM brgy_admin;";
            rs = st.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    public ResultSet getOfficialsInAdminViaPosition(String aId, String position, String status) {
        try {
            String query = "SELECT * FROM official WHERE adminId = \"" + aId + "\" AND position = \"" + position + "\" AND status = \"" + status + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public ResultSet getOfficials() {
        try {
            String query = "SELECT * FROM official;";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public int wasOfficial(String pID, String adminID) { //if status is replaced
        try {
            rs = getOfficials();
            while (rs.next()) {
                if (pID.equals(String.valueOf(rs.getInt("personID"))) && rs.getString("status").equals("replaced") && String.valueOf(rs.getInt("adminID")).equals(adminID)) {
                    return rs.getInt("officialID");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    public void changeOfficialStatus(String offId, String newStatus) {
        try {
            String query = "UPDATE official SET status = \"" + newStatus + "\" where officialID = \"" + offId + "\";";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isOfficial(String pID) { //identify if record exists to proceed making request; if not, create
        try {
            rs = getOfficials();
            while (rs.next()) {
                if (pID.equals(String.valueOf(rs.getInt("personID"))) && rs.getString("status").equals("active")) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public void storeOfficial(String personId, String position, String adminID) {
        try {
            String query = "INSERT INTO official (personID, position, status, adminID) VALUES(\"" + personId + "\",\"" + position + "\", \"active\", \"" + adminID + "\");";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ResultSet getPersonIDByOffID(String oID) { //view request history
        try {
            String query = "SELECT personID FROM official WHERE officialID = \"" + oID + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public ResultSet getOffIDByUserID(String oID) { //view request history
        try {
            String query = "SELECT officialID FROM user WHERE userID = \"" + oID + "\";";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public void createNewAdmin(String sdate, String edate, String stats) {
        try {
            String query = "INSERT INTO brgy_admin (dateStart, dateEnd, status) VALUES(\"" + sdate + "\",\"" + edate + "\",\"" + stats + "\")";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void changeAdminStatus(String adminId, String newStatus) {
        try {
            String query = "UPDATE brgy_admin SET status = \"" + newStatus + "\" where adminID = \"" + adminId + "\";";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deactivateAdminOfficials(String adminId, String pId) {
        String query = "UPDATE officials SET status = \"inactive\" WHERE officialID = \"" + pId + "\" AND adminID = \"" + adminId + "\";";
        try {
            st.executeUpdate(query);
        } catch (Exception ex) {

        }
    }

    public ResultSet getOfficialsInAdmin(String aId) {
        try {
            String query = "SELECT * FROM official WHERE adminID = \"" + aId + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public void changeUserStatus(String userId, String newStatus) {
        try {
            String query = "UPDATE user SET status = \"" + newStatus + "\" WHERE userID = \"" + userId + "\";";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void storeUser(String oID, String username, String password, String aType) {
        try {
            String query = "INSERT INTO user (officialID, username, password, adminType, status) VALUES(\"" + oID + "\",\"" + username + "\", \"" + password + "\", \"" + aType + "\", \"active\");";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateUser(String uId, String uname, String pword, String aType) {
        try {
            String query = "UPDATE user SET username = \"" + uname + "\", password = \"" + pword + "\", adminType = \"" + aType + "\" WHERE userID  = \"" + uId + "\";";
            System.out.println(query);
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ResultSet getUser(String uId) {
        try {
            String query = "SELECT * FROM user WHERE userID = \"" + uId + "\"";
            rs = st.executeQuery(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public ResultSet getUser(String username, String password) {
        try {
            String query = "SELECT * FROM user WHERE username = \"" + username + "\" AND password = \"" + password + "\"; ";
            rs = st.executeQuery(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public ResultSet getUsers() {
        try {
            String query = "SELECT * FROM user;";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public boolean isUser(String offID) { //identify if record exists to proceed making request; if not, create
        try {
            rs = getUsers();
            while (rs.next()) {
                if (offID.equals(String.valueOf(rs.getInt("officialID"))) && rs.getString("status").equals("active")) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean isInactiveOfficialUser(String candidatePID) { //if still 1 person was in past admin and is still user and now also in current admin
        try {
            rs = getUsers();
            while (rs.next()) {
                String uid = String.valueOf(rs.getInt("userID"));
                String oid = String.valueOf(rs.getInt("officialID"));
                boolean u = isUser(oid);
                ResultSet x = getPersonIdByUserId(uid);
                if (x.next() && candidatePID.equals(String.valueOf(x.getInt("personID"))) && u) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public ResultSet getPersonIdByUserId(String userId) {
        ResultSet rx = null;
        try {

            String query = "SELECT officialID FROM user WHERE userID = \"" + userId + "\";";
            rx = st.executeQuery(query);

            if (rx.next()) {
                rs = getPersonIDByOffID(String.valueOf(rx.getInt("officialID")));
            }

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    //Calendar Panel -- Event
    public void storeEvent(String m, String d, String y, String etit, String ev, String etim, String erem, String userId) {
        try {
            String query = "INSERT INTO event (month, day, year, title, venue, time, remarks, userID, date_added, date_mod) VALUES(\"" + m + "\",\"" + d + "\",\"" + y + "\",\"" + etit + "\",\"" + ev + "\",\"" + etim + "\",\"" + erem + "\",\"" + userId + "\", CURDATE(), CURDATE())";
            System.out.println("" + query);
            st.executeUpdate(query);

        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateEvent(String eID, String etit, String ev, String etim, String erem) {
        try {
            String query = "UPDATE event SET title =  \"" + etit + "\", venue = \"" + ev + "\", time =\"" + etim + "\" , remarks=\"" + erem + "\" ,  date_mod = CURDATE() WHERE eventID = " + eID + ";";
            System.out.println("" + query);
            st.executeUpdate(query);

        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteEvent(String eID) {
        try {
            String query = "DELETE FROM event WHERE eventID = " + eID + ";";
            System.out.println("" + query);
            st.executeUpdate(query);
            JOptionPane.showMessageDialog(null, "Successfully Deleted Event!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Unsuccessfully Deleted Event!");
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> hasDuplicateEventTitle(String title) {
        ArrayList<String> data = new ArrayList<>();
        try {
            String query = "SELECT * FROM event WHERE title = \"" + title + "\";";
            System.out.println("" + query);
            ResultSet rz = st.executeQuery(query);
            while (rz.next()) {
                data.add(rz.getString("month")+" "+rz.getString("day")+", "+rz.getString("year"));
                System.out.println(rz.getString("month")+" "+rz.getString("day")+", "+rz.getString("year"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data;
    }

    public ResultSet getAllEvents(String month, String day) {
        try {
            String query = "SELECT * FROM event WHERE month = \"" + month + "\" AND day = \"" + day + "\";";
            System.out.println("" + query);
            rs = st.executeQuery(query);

        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rs;
    }

    //Transactions
    public ResultSet getAllTransactions() {

        try {
            String query = "SELECT * FROM transaction;";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllTrasnaction: " + ex);
        }

        return rs;
    }

    public boolean hasTrans(String pID) {
        try {
            ResultSet y = getAllTransactions();
            while (y.next()) {
                try {
                    if (y.getInt("personID") == Integer.parseInt(pID)) {
                        return true;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public ResultSet getTransactionViaPersonID(String pID) {
        try {
            String query = "SELECT * FROM transaction WHERE personID = \"" + pID + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllTrasnaction: " + ex);
        }

        return rs;

    }

    public int getPersonIDViaTransID(String tID) {
        try {
            String query = "SELECT personID FROM transaction WHERE transID = \"" + tID + "\";";
            rs = st.executeQuery(query);
            if (rs.next()) {
                return rs.getInt("personID");
            }

        } catch (Exception ex) {
            System.out.println("Error getAllTrasnaction: " + ex);
        }

        return 0;

    }

    public ResultSet getPersonFormsByTransID(String tID) { //view request history
        try {
            String query = "SELECT * FROM formentry WHERE transID = \"" + tID + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public ResultSet getForm(String fID) { //get PDF details
        try {
            String query = "SELECT * FROM form WHERE formID = \"" + fID + "\";";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }

        return rs;
    }

    public void claimForm(String transId, String reqID, String claimTime, String claimDate) {
        try {
            String query = "UPDATE formentry SET date_claimed = \"" + claimDate + "\", time_claimed = \"" + claimTime + "\" WHERE (transID = \"" + transId + "\" AND formEntryID = \"" + reqID + "\");";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTransaction(String transID, String newInput) {
        try {
            String query = "UPDATE transaction SET date_last_trans = \"" + newInput + "\" WHERE transID = \"" + transID + "\";";

            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //storeFormEntry --- set date_latest_request 
    public void storeTrans(String personID) { //create record to request forms
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy");
            String formattedDate = dateFormat.format(new Date());

            String query = "INSERT INTO transaction (personID, date_last_trans) VALUES(\"" + personID + "\",\"" + formattedDate + "\")";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getpTransID(String pID) { //get ID in order to makeRequest
        String found = "none";
        try {
            rs = getAllTransactions();
            while (rs.next()) {
                if (pID.equals(found = String.valueOf(rs.getInt("personID")))) {
                    return String.valueOf(rs.getInt("transID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return found;
    }

    public String getLatestPersonId() {
        ResultSet rx;
        String currentPersonID = "";
        try {
            String query = "SELECT * FROM person;";
            rx = st.executeQuery(query);
            if (rx.last()) { //getLatestSaved
                currentPersonID = String.valueOf(rx.getInt("personID"));
                rx.close();
                System.out.println("Current Person ID obtained: " + currentPersonID);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentPersonID;
    }

    //forms
    public ResultSet getAllForms() {
        try {
            String query = "SELECT * FROM form;";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllForms: " + ex);
        }

        return rs;
    }

    //new for forms
    public ArrayList<ArrayList<String>> getAllFormsArrayList() {
        ArrayList<ArrayList<String>> dataAll = new ArrayList<>();
        try {
            String query = "SELECT * FROM form;";
            ResultSet rx = st.executeQuery(query);

            while (rx.next()) {
                ArrayList<String> data = new ArrayList<>();

                data.add(rx.getString("formID"));
                data.add(rx.getString("formName"));
                data.add(rx.getString("status"));
                data.add(rx.getString("dateAdded"));
                data.add(rx.getString("dateModified"));
                data.add(rx.getString("userID"));

                dataAll.add(data);
            }
            rx.close();
        } catch (Exception ex) {
            System.out.println("Error getAllForms: " + ex);
        }

        return dataAll;
    }

    public void createLog(String action, String userID) {
        System.out.println("\n" + action + "\n");

        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("HH:mm:ss");
            String timeClaimed = sdf.format(c.getTime());

            sdf = new SimpleDateFormat("MM-dd-yy");
            String dateClaimed = sdf.format(new Date());

            String query = "INSERT INTO log(userID, action, date_created, time_created) VALUES(\"" + userID + "\",\"" + action + "\",\"" + dateClaimed + "\",\"" + timeClaimed + "\")";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ResultSet getLatestRequestID() {
        try {
            String query = "select formentryID from formentry order by formentryID desc limit 1";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public ResultSet getLatestTransID() {
        try {
            String query = "select transID from transaction order by transID desc limit 1";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public ResultSet getLatestEventID() {
        try {
            String query = "select eventID from event order by eventID desc limit 1";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public ResultSet getLatestFormID() {
        try {
            String query = "select formID from form order by formID desc limit 1";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public ResultSet getLatestUserID() {
        try {
            String query = "select userID from user order by userID desc limit 1";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public ResultSet getLatestOfficialID() {
        try {
            String query = "select officialID from official order by officialID desc limit 1";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public int getActiveAdminId() {
        try {
            String query = "SELECT * FROM brgy_admin WHERE status = \"active\"";
            rs = st.executeQuery(query);
            if (rs.next()) {
                return rs.getInt("adminID");
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public ResultSet getEvent(String eventID) {
        try {
            String query = "select * from event where eventID = \"" + eventID + "\"";
            rs = st.executeQuery(query);
            rs.next();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
        return rs;
    }

    public ResultSet getAllLogs() {
        try {
            String query = "SELECT * FROM log;";
            rs = st.executeQuery(query);

        } catch (Exception ex) {
            System.out.println("Error getAllLogs: " + ex);
        }

        return rs;
    }

    String app_path;

    public final void checkReportsFolder() {
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
        File file3 = new File(jarDir + "\\CONFIG.txt");
        if (!file3.exists()) {
            try {
                if (file3.createNewFile()) {
                    System.out.println("temp is created!");
                } else {
                    System.out.println("Failed to create temp!");
                }
            } catch (IOException ex) {
                Logger.getLogger(Browser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void getIPAddress() {
        String line;
        checkReportsFolder();
        File file = new File(app_path + "\\CONFIG.txt");
        BufferedReader fileReader = null;

        try {
            // Create the file reader
            fileReader = new BufferedReader(new FileReader(file));
            int x = 0;
            // Read the file line by line starting from the second line
            while ((line = fileReader.readLine()) != null) {
                System.out.println(line);
                switch (x) {
                    case 0:
                        ip = line;
                        break;
                    case 1:
                        db = line;
                        break;
                    case 2:
                        uname = line;
                        break;
                    case 3:
                        pword = line;
                        break;
                    default:
                        break;
                }
                x++;
            }

        } catch (Exception e) {
            System.out.println("Error reading IP ADDRESS !!!");
        }
    }

    //-NEW----------------------------------------------------------------------
    public int getFormVarCount(String id) {
        int formVarCount = 0;
        try {
            String query = "SELECT form_vars FROM form where formID = " + id + ";";
            ResultSet rx = st.executeQuery(query);

            if (rx.next()) {
                formVarCount = rx.getInt("form_vars");
                rx.close();
            }

        } catch (Exception ex) {
            System.out.println("Error getFormVars: " + ex);
        }

        return formVarCount;
    }

    public void storeFormRequestData(String values) {
        try {
            String sql = "INSERT INTO formentry (formData, transID, formID, date_requested, date_to_claim, time_requested) \nVALUES(" + values + ")";
            System.out.println("STORE NEW REQUEST: " + sql);
            st.executeUpdate(sql);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getFormEntryFormID(String fomEntID) {
        int formID = 0;
        try {
            String query = "SELECT formID FROM formentry where formEntryID = " + fomEntID + ";";
            ResultSet rx = st.executeQuery(query);

            if (rx.next()) {
                formID = rx.getInt("formID");
                rx.close();
            }

        } catch (Exception ex) {
            System.out.println("Error getFormVars: " + ex);
        }

        return formID;
    }

    public String getFormEntryFormData(String fomEntID) {
        String data = "";
        try {
            String query = "SELECT formData FROM formentry where formEntryID = " + fomEntID + ";";
            ResultSet rx = st.executeQuery(query);

            if (rx.next()) {
                data = rx.getString("formData");
                rx.close();
            }

        } catch (Exception ex) {
            System.out.println("Error getFormData: " + ex);
        }

        return data;
    }

//VISIT NOTE-------------------------------------------------------------------------------------------------------------
    public void storeNote(String values) {
        try {
            String sql = "INSERT INTO visit_note (visit_purpose, visit_details, visit_date, visit_time, visit_datetime_added, visit_datetime_mod, transID) \nVALUES(" + values + ")";
            System.out.println("STORE NEW VISIT: " + sql);
            st.executeUpdate(sql);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateNote(String vpurp, String vdet, String vdate, String vtime, String visit_id) {
        try {
            String query = "UPDATE visit_note SET visit_purpose = \"" + vpurp + "\", visit_details = \"" + vdet + "\", visit_date = \"" + vdate + "\", visit_time = \"" + vtime + "\", visit_datetime_mod = NOW() WHERE visit_id = \"" + visit_id + "\";";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> getNote(String visit_id) {
        ArrayList<String> data = new ArrayList();
        try {
            String query = "SELECT * FROM visit_note where visit_id = " + visit_id + ";";
            ResultSet rx = st.executeQuery(query);
            if (rx.next()) {
                data.add(rx.getString("visit_purpose"));
                data.add(rx.getString("visit_details"));
                data.add(rx.getString("visit_date"));
                data.add(rx.getString("visit_time"));
                rx.close();
            }
        } catch (Exception ex) {
            System.out.println("Error getFormData: " + ex);
        }

        return data;
    }

    public ArrayList<ArrayList<String>> getAllNotes() {
        ArrayList<ArrayList<String>> dataAll = new ArrayList();
        try {
            String query = "SELECT * FROM visit_note;";
            ResultSet rx = st.executeQuery(query);
            if (rx.next()) {
                do {
                    ArrayList<String> data = new ArrayList();
                    data.add(rx.getString("visit_id"));
                    data.add(rx.getString("visit_purpose"));
                    data.add(rx.getString("visit_details"));
                    data.add(rx.getString("visit_date"));
                    data.add(rx.getString("visit_time"));
                    data.add(rx.getString("visit_datetime_added"));
                    data.add(rx.getString("visit_datetime_mod"));
                    dataAll.add(data);
                } while (rx.next());
                rx.close();
            }
        } catch (Exception ex) {
            System.out.println("Error getFormData: " + ex);
        }
        return dataAll;
    }

    public ArrayList<ArrayList<String>> searchPersonViaKeyword(String key) {
        ArrayList<ArrayList<String>> dataAll = new ArrayList();
        try {
            Set<String> hitTransIDs = new TreeSet<>();

            //get transID of all forms with keyword search in formData
            String query1 = "SELECT transID FROM formentry WHERE formData LIKE \"%" + key + "%\"";
            ResultSet rx = st.executeQuery(query1);
            if (rx.next()) {
                do {
                    hitTransIDs.add(rx.getString("transID"));
                } while (rx.next());
                rx.close();
            }
            //get transID of all notes with keyword search in in notes
            String query2 = "SELECT transID FROM visit_note WHERE (visit_purpose LIKE \"%" + key + "%\" AND visit_details LIKE \"%" + key + "%\")";
            ResultSet ry = st.executeQuery(query2);
            if (ry.next()) {
                do {
                    hitTransIDs.add(ry.getString("transID"));
                } while (ry.next());
                ry.close();
            }

            //sort ids
            ArrayList<String> hitTransIDs2 = new ArrayList<>();
            hitTransIDs2.addAll(hitTransIDs);
            Collections.sort(hitTransIDs2);

            //get personID of all transIDs
            ArrayList<String> personIDs = new ArrayList<>();
            for (int x = 0; x < hitTransIDs2.size(); x++) {
                String query3 = "SELECT personID FROM transaction WHERE transID = \"" + hitTransIDs2.get(x) + "\"";
                ResultSet ry1 = st.executeQuery(query3);
                if (ry1.next()) {
                    personIDs.add(ry1.getString("personID"));
                    ry1.close();
                }
            }
            //sort personIDs
            Collections.sort(personIDs);
            System.out.println(personIDs);

            //id lname, fname, address
            for (int x = 0; x < personIDs.size(); x++) {
                String query4 = "SELECT personID, lname, fname, address FROM person WHERE personID = \"" + personIDs.get(x) + "\";";
                ResultSet rz = st.executeQuery(query4);
                if (rz.next()) {
                    do {
                        ArrayList<String> data = new ArrayList();
                        data.add(rz.getString("personID"));
                        data.add(rz.getString("lname"));
                        data.add(rz.getString("fname"));
                        data.add(rz.getString("address"));
                        dataAll.add(data);
                    } while (rz.next());
                    rx.close();
                }
            }

        } catch (Exception ex) {
            System.out.println("Error getFormData: " + ex);
        }

        return dataAll;
    }

    public ArrayList<ArrayList<String>> searchTransactionViaWhere(String key, String searchType) {
        ArrayList<ArrayList<String>> dataAll = new ArrayList();

        String whereClause = "";
        if (searchType.equals("date")) {
            whereClause = "date_requested";
        } else if (searchType.equals("keyword")) {
            whereClause = "formData";
        } else if (searchType.equals("form")) {
            whereClause = "formID";
        }

        try {
            ArrayList<String> hitTransIDs = new ArrayList<>();
            String query1 = "SELECT transID FROM formentry WHERE " + whereClause + " LIKE \"%" + key + "%\"";
            System.out.println(query1);
            ResultSet rx = st.executeQuery(query1);
            if (rx.next()) {
                do {
                    hitTransIDs.add(rx.getString("transID"));
                } while (rx.next());
                rx.close();
            }
            System.out.println("");
            //get personID of all transIDs
            ArrayList<String> personIDs = new ArrayList<>();
            for (int x = 0; x < hitTransIDs.size(); x++) {
                String query3 = "SELECT personID FROM transaction WHERE transID = \"" + hitTransIDs.get(x) + "\"";
                System.out.println(query3);
                ResultSet ry1 = st.executeQuery(query3);
                if (ry1.next()) {
                    personIDs.add(ry1.getString("personID"));
                    ry1.close();
                }
            }
            //sort personIDs
            Collections.sort(personIDs);

            //id lname, fname, address
            for (int x = 0; x < personIDs.size(); x++) {
                String query4 = "select transaction.transID, person.lname, person.fname, transaction.date_last_trans "
                        + "from person "
                        + "inner join transaction "
                        + "on transaction.personID = person.personID "
                        + "where transaction.personID = " + personIDs.get(x) + " "
                        + "AND person.personID = " + personIDs.get(x) + ";";
                System.out.println(query4);
                ResultSet rz = st.executeQuery(query4);
                if (rz.next()) {
                    do {
                        ArrayList<String> data = new ArrayList();
                        data.add(rz.getString("transID"));
                        data.add(rz.getString("lname"));
                        data.add(rz.getString("fname"));
                        data.add(rz.getString("date_last_trans"));
                        dataAll.add(data);
                    } while (rz.next());
                    rx.close();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataAll;
    }

    public ArrayList<String> getEventData(int id) {
        ArrayList<String> data = new ArrayList<>();
        try {
            String query4 = "select * from event where eventID = " + id + ";";
            System.out.println(query4);
            ResultSet rz = st.executeQuery(query4);
            if (rz.next()) {
                do {
                    data.add(rz.getString("title"));
                    data.add(rz.getString("venue"));
                    data.add(rz.getString("time"));
                    data.add(rz.getString("remarks"));
                } while (rz.next());
                rz.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
}
