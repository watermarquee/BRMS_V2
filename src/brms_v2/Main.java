/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brms_v2;

import ExternalNonFormClasses.SQLConnect;
import ExternalNonFormClasses.ReadBlob;
import ExternalForms.AddNewRelative;
import ExternalForms.Birthdays;
import ExternalNonFormClasses.BRMS_Calendar;
import ExternalForms.Browser;
import ExternalForms.Claimables;
import ExternalForms.CreateUser;
import ExternalForms.FormEditor;
import ExternalForms.FormOptions;
import ExternalForms.Import;
import ExternalForms.NewAdminCreate;
import ExternalForms.NewEvent;
import ExternalForms.OfficialSelection;
import ExternalForms.SearchPerson;
import ExternalForms.UserSelection;
import ExternalForms.VisitNote;
import ExternalNonFormClasses.LogHandler;
import ExternalNonFormClasses.PDFEnator;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
//imp
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.plaf.basic.CalendarHeaderHandler;
import org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler;

/**
 *
 * @author mrRNBean
 */
public class Main extends JFrame {

    int siblingCount = 0, childrenCount = 0; //relative count
    int activeAdminId; //for baranggay officials
    int prevnextcounter; //for barangay administration
    int brgyadmins; //count of admins
    int requestingPanel = 0; //panels labeled by numbers 

    boolean requesting = false; //turns false when (1) you press back (cancel) at the form select panel (f_backActionPerformed)
    // (2) you cancel creating an account (via Request Form Menu Button) before being able to request a form

    String logged_username, logged_password, logged_userID, logged_userType; //know system user
    String relativeType = ""; //needed to determine which table ANT will add on
    String profileViewType = ""; //set where view Profile is from [vcView = view citizens, rhview = request history, oview = officers, uview = users]
    String addFunctionType = ""; //citizen functions => [addc, editc, updatec]; guest functions => [addg, editg, updateg]; 
    String currentPersonID = "";
    String requestedFormID = "";
    String requestedFormName = "";
    String currentPersonName = "";
    String currentPersonTransID = "";
    String currentPersonTransType = "";
    String calendarDaySelected = "null";

    ArrayList<String> officialIDs;
    ArrayList<String> usersPersonIDs;
    ArrayList<String> officialPersonIDs;
    ArrayList<String> newSiblings, newChildren; //for "person" 

    String[] sibAndChildEntries; //split values to createAddQuery
    String positionValues[] = {"Captain", "Secretary", "Treasurer", "Kagawad 1", "Kagawad 2", "Kagawad 3", "Kagawad 4", "Kagawad 5", "Kagawad 6", "Kagawad 7", "Staff 1", "Staff 2", "Staff 3"}; //default values for baranggay officials

    //Import Class Declaration
    DefaultTableModel model;
    LogIn logIn; //to call the log in frame when logging out

    //External Classes Declarations
    OfficialSelection officialSelection;
    NewAdminCreate newAdminCreate;
    UserSelection userSelection;
    SearchPerson searchPerson;
    Claimables claimables;
    LogHandler logHandler;
    FormEditor formEditor;
    CreateUser createUser;
    Birthdays birthday;
    ReadBlob readBlob;
    AddNewRelative ant;
    SQLConnect connect;
    BRMS_Calendar cal;
    PDFEnator pdf;
    NewEvent nev;
    Browser browser;
    VisitNote vnote;
    FormOptions formOp;
    Import imprt;
    //imp
    JXMonthView monthView;
    SpinningCalendarHeaderHandler spinningHandler;

    public Main(LogIn j) { //must accept user,pass,Id
        initComponents();

        newSiblings = new ArrayList<>();
        newChildren = new ArrayList<>();

        this.logIn = j; //the log-in frame used to log in
        this.setTitle("Barangay Records Management System");
        this.addWindowListener(new WindowAdapter() { //when closed via X button
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("\n*********************************************");
                System.out.println("Application has been closed. Closing browser. Deleting files..");
                System.out.println("*********************************************\n");
                browser.deactivateBrowser(); //closes access to Adobe PDF Reader and delete all files created assuming they have all been stored to DB

                System.out.println("\n*********************************************");
                System.out.println("APPLICATION PROCESS DONE");
                System.out.println("*********************************************\n");
                NativeInterface.close();
            }
        });

        //UIManager.put("JFrame.background", new ColorUIResource(255, 255, 255));
        this.getContentPane().setBackground(new ColorUIResource(255, 255, 255));

        Border border = rh_TID.getBorder();
        Border margin = new EmptyBorder(10, 10, 10, 10);
        rh_TID.setBorder(new CompoundBorder(border, margin));

        border = rh_name.getBorder();
        rh_name.setBorder(new CompoundBorder(border, margin));

        border = rh_type.getBorder();
        rh_type.setBorder(new CompoundBorder(border, margin));

        setResizable(false);
        setLayoutProperties();
    }

    public void init() {
        //external classes instantiate
        pdf = new PDFEnator(this);
        nev = new NewEvent(this);
        vnote = new VisitNote(this);
        imprt = new Import(this);
        browser = new Browser(this);
        ant = new AddNewRelative(this);
        model = new DefaultTableModel();
        readBlob = new ReadBlob();
        formOp = new FormOptions(this);
        claimables = new Claimables(this);
        birthday = new Birthdays(this);
        formEditor = new FormEditor(this);
        createUser = new CreateUser(this);
        searchPerson = new SearchPerson(this);
        userSelection = new UserSelection(this);
        newAdminCreate = new NewAdminCreate(this);
        officialSelection = new OfficialSelection(this);
        logHandler = new LogHandler(this, logHistoryTable);
        //imp
        UIManager.put(CalendarHeaderHandler.uiControllerID, "org.jdesktop.swingx.plaf.basic.SpinningCalendarHeaderHandler");
        UIManager.put(SpinningCalendarHeaderHandler.ARROWS_SURROUND_MONTH, Boolean.TRUE);
    }

    public void callClass(String uname, String pword, String uid, String userType) {
        connect = new SQLConnect();
        init();
        logged_username = uname;
        logged_password = pword;
        logged_userID = uid;
        logged_userType = userType;

        this.setVisible(true);
        this.setEnabled(true);

        logHandler.setUserId(logged_userID);
        nev.setUserID(logged_userID);
        formEditor.setLogHandler(logHandler);
        officialSelection.setHandler(logHandler);
        userSelection.setLogHandler(logHandler);
        createUser.setLogHandler(logHandler);
        birthday.setLogHandler(logHandler);
        claimables.setLogHandler(logHandler);

        logHandler.saveLog("Logged In with -User ID: " + logged_userID + " -User Name: " + logged_username);

        setHomePanelData();
        setCalendarComponentsArray();
        setLocationRelativeTo(null);
        setViewCitizensData();
        setTransactionsData();
        hideAllPanels();
        homePanel.setVisible(true);
    }

    Dimension screenSize;
    int SCREEN_WIDTH, SCREEN_HEIGHT;

    private void setLayoutProperties() {
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        //screen size
        Dimension screenSizeOrig = Toolkit.getDefaultToolkit().getScreenSize();
        //height of the task bar
        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int taskBarSize = scnMax.bottom;
        //screen X and Y
        SCREEN_WIDTH = (screenSizeOrig.width);//screenSizeOrig.width;
        SCREEN_HEIGHT = (screenSizeOrig.height - taskBarSize);//screenSizeOrig.height;
        screenSize = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        //available size of the screen 
        setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        //System.out.println("" + SCREEN_WIDTH + "," + SCREEN_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    public void logOutHideThis() {
        logIn.setEnabled(true);
        logIn.setVisible(true);
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonsPanel = new javax.swing.JPanel();
        home = new javax.swing.JButton();
        viewCitizens = new javax.swing.JButton();
        transactions = new javax.swing.JButton();
        addCitizen = new javax.swing.JButton();
        requestForm = new javax.swing.JButton();
        calendar = new javax.swing.JButton();
        options = new javax.swing.JButton();
        logout = new javax.swing.JButton();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        viewCitizensPanel = new javax.swing.JPanel();
        v_viewCitizensScrollPane = new javax.swing.JScrollPane();
        viewCitizensTable = new javax.swing.JTable();
        vc_viewProfileButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        vc_searchCitizen = new javax.swing.JButton();
        vc_filter = new javax.swing.JButton();
        vc_back = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        profilePanel = new javax.swing.JPanel();
        addTab = new javax.swing.JTabbedPane();
        personalInfo = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lname = new javax.swing.JTextField();
        fname = new javax.swing.JTextField();
        mname = new javax.swing.JTextField();
        nameEx = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        sex = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        status = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        pob = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        religion = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        address = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        zipCode = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        precint = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        occupation = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        email = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        age = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        pID = new javax.swing.JLabel();
        dob = new org.jdesktop.swingx.JXDatePicker();
        printButtProfile = new javax.swing.JButton();
        new_note = new javax.swing.JLabel();
        new_s1 = new javax.swing.JLabel();
        new_s2 = new javax.swing.JLabel();
        new_s3 = new javax.swing.JLabel();
        telNum = new javax.swing.JTextField();
        famback = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        mlname = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        flname = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        slname = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        fmname = new javax.swing.JTextField();
        ffname = new javax.swing.JTextField();
        mmname = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        sfname = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        mfname = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        child = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        sibling = new javax.swing.JTable();
        jLabel29 = new javax.swing.JLabel();
        siblingAdd = new javax.swing.JButton();
        childAdd = new javax.swing.JButton();
        smname = new javax.swing.JTextField();
        educ = new javax.swing.JPanel();
        cSchool = new javax.swing.JTextField();
        eSchool = new javax.swing.JTextField();
        gYear = new javax.swing.JTextField();
        gSchool = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        hsSchool = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        hsYear = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        vYear = new javax.swing.JTextField();
        cYear = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        eYear = new javax.swing.JTextField();
        vSchool = new javax.swing.JTextField();
        rhtab = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        t_transactionsScrollPane3 = new javax.swing.JScrollPane();
        profile_rhTable = new javax.swing.JTable();
        profile_requestForm = new javax.swing.JButton();
        profile_claimForm = new javax.swing.JButton();
        profile_viewFrom = new javax.swing.JButton();
        profile_claimForm1 = new javax.swing.JButton();
        notetab = new javax.swing.JPanel();
        jLabel54 = new javax.swing.JLabel();
        t_transactionsScrollPane4 = new javax.swing.JScrollPane();
        profile_notesTable = new javax.swing.JTable();
        profile_addNote = new javax.swing.JButton();
        profile_viewNote = new javax.swing.JButton();
        profileSave = new javax.swing.JButton();
        profileCancel = new javax.swing.JButton();
        settingsPanel = new javax.swing.JPanel();
        set1 = new javax.swing.JPanel();
        set_viewForms = new javax.swing.JButton();
        set3 = new javax.swing.JPanel();
        set_viewOfficials = new javax.swing.JButton();
        set4 = new javax.swing.JPanel();
        set_viewUsers = new javax.swing.JButton();
        set5 = new javax.swing.JPanel();
        set_viewLog = new javax.swing.JButton();
        set6 = new javax.swing.JPanel();
        set_import = new javax.swing.JButton();
        officialsPanel = new javax.swing.JPanel();
        officialsPane = new javax.swing.JScrollPane();
        tableOfficials = new javax.swing.JTable();
        off_adminYear = new javax.swing.JLabel();
        off_createNewAdminButton = new javax.swing.JButton();
        off_viewProfileButton = new javax.swing.JButton();
        off_editOfficial = new javax.swing.JButton();
        off_backButton = new javax.swing.JButton();
        jLabel43 = new javax.swing.JLabel();
        off_next = new javax.swing.JButton();
        off_prev = new javax.swing.JButton();
        adminIDLabel = new javax.swing.JLabel();
        usersPanel = new javax.swing.JPanel();
        usersPane = new javax.swing.JScrollPane();
        tableUsers = new javax.swing.JTable();
        use_adminYear1 = new javax.swing.JLabel();
        use_deact = new javax.swing.JButton();
        use_viewProf = new javax.swing.JButton();
        use_edit = new javax.swing.JButton();
        use_back = new javax.swing.JButton();
        use_newUser = new javax.swing.JButton();
        calendarPanel = new javax.swing.JPanel();
        monthChoice = new javax.swing.JComboBox();
        yearChoice = new javax.swing.JComboBox();
        Day = new javax.swing.JPanel();
        DayName = new javax.swing.JPanel();
        sun = new javax.swing.JButton();
        mon = new javax.swing.JButton();
        wed = new javax.swing.JButton();
        thu = new javax.swing.JButton();
        fri = new javax.swing.JButton();
        sat = new javax.swing.JButton();
        tue = new javax.swing.JButton();
        Weeks = new javax.swing.JPanel();
        week1 = new javax.swing.JPanel();
        day0 = new javax.swing.JButton();
        day1 = new javax.swing.JButton();
        day3 = new javax.swing.JButton();
        day4 = new javax.swing.JButton();
        day5 = new javax.swing.JButton();
        day6 = new javax.swing.JButton();
        day2 = new javax.swing.JButton();
        week2 = new javax.swing.JPanel();
        day7 = new javax.swing.JButton();
        day8 = new javax.swing.JButton();
        day10 = new javax.swing.JButton();
        day11 = new javax.swing.JButton();
        day12 = new javax.swing.JButton();
        day13 = new javax.swing.JButton();
        day9 = new javax.swing.JButton();
        week3 = new javax.swing.JPanel();
        day14 = new javax.swing.JButton();
        day15 = new javax.swing.JButton();
        day17 = new javax.swing.JButton();
        day18 = new javax.swing.JButton();
        day19 = new javax.swing.JButton();
        day20 = new javax.swing.JButton();
        day16 = new javax.swing.JButton();
        week5 = new javax.swing.JPanel();
        day21 = new javax.swing.JButton();
        day22 = new javax.swing.JButton();
        day24 = new javax.swing.JButton();
        day25 = new javax.swing.JButton();
        day26 = new javax.swing.JButton();
        day27 = new javax.swing.JButton();
        day23 = new javax.swing.JButton();
        week6 = new javax.swing.JPanel();
        day28 = new javax.swing.JButton();
        day29 = new javax.swing.JButton();
        day31 = new javax.swing.JButton();
        day32 = new javax.swing.JButton();
        day33 = new javax.swing.JButton();
        day34 = new javax.swing.JButton();
        day30 = new javax.swing.JButton();
        week7 = new javax.swing.JPanel();
        day35 = new javax.swing.JButton();
        day36 = new javax.swing.JButton();
        day38 = new javax.swing.JButton();
        day39 = new javax.swing.JButton();
        day40 = new javax.swing.JButton();
        day41 = new javax.swing.JButton();
        day37 = new javax.swing.JButton();
        viewDayEvent = new javax.swing.JButton();
        eventPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        eventsTable = new javax.swing.JTable();
        backToCalendar = new javax.swing.JButton();
        dateSelected = new javax.swing.JLabel();
        addNewEvent = new javax.swing.JButton();
        transactionsPanel = new javax.swing.JPanel();
        t_transactionsScrollPane1 = new javax.swing.JScrollPane();
        transactionTable = new javax.swing.JTable();
        t_viewRequestHistory = new javax.swing.JButton();
        t_searchField = new javax.swing.JTextField();
        t_searchTrans = new javax.swing.JButton();
        t_filter = new javax.swing.JButton();
        t_back = new javax.swing.JButton();
        t_viewRequestHistory1 = new javax.swing.JButton();
        requestHistoryPanel = new javax.swing.JPanel();
        t_transactionsScrollPane2 = new javax.swing.JScrollPane();
        requestHistoryTable = new javax.swing.JTable();
        rh_viewProfile = new javax.swing.JButton();
        rh_back = new javax.swing.JButton();
        rh_requestForm = new javax.swing.JButton();
        rh_claimForm = new javax.swing.JButton();
        rh_TID = new javax.swing.JLabel();
        rh_name = new javax.swing.JLabel();
        rh_type = new javax.swing.JLabel();
        rh_viewFrom = new javax.swing.JButton();
        profile_claimForm2 = new javax.swing.JButton();
        formSelectPanel = new javax.swing.JPanel();
        formSelectPane = new javax.swing.JScrollPane();
        formsSelectTable = new javax.swing.JTable();
        prompt = new javax.swing.JLabel();
        f_Select = new javax.swing.JButton();
        f_back = new javax.swing.JButton();
        logHistoryPanel = new javax.swing.JPanel();
        v_viewCitizensScrollPane1 = new javax.swing.JScrollPane();
        logHistoryTable = new javax.swing.JTable();
        lh_back = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        formListPanel = new javax.swing.JPanel();
        v_viewCitizensScrollPane2 = new javax.swing.JScrollPane();
        formListTable = new javax.swing.JTable();
        fl_viewForm = new javax.swing.JButton();
        fl_back = new javax.swing.JButton();
        fl_editForm = new javax.swing.JButton();
        fl_addForm = new javax.swing.JButton();
        jLabel44 = new javax.swing.JLabel();
        fl_formSetts = new javax.swing.JButton();
        homePanel = new javax.swing.JPanel();
        aboutPanel = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        claimableFormsPanel = new javax.swing.JPanel();
        home_viewClaimables = new javax.swing.JButton();
        claimableFormsPanel1 = new javax.swing.JPanel();
        home_viewBday = new javax.swing.JButton();
        claimableFormsPanel3 = new javax.swing.JPanel();
        home_userManual = new javax.swing.JButton();
        claimableFormsPanel4 = new javax.swing.JPanel();
        home_aboutBRMS = new javax.swing.JButton();
        claimableFormsPanel12 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        homeBrgyTable = new javax.swing.JTable();
        jLabel46 = new javax.swing.JLabel();
        home_brgyTerm = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        buttonsPanel.setBackground(new java.awt.Color(0, 102, 153));

        home.setBackground(new java.awt.Color(255, 255, 255));
        home.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        home.setForeground(new java.awt.Color(0, 102, 153));
        home.setText("HOME");
        home.setPreferredSize(new java.awt.Dimension(155, 72));
        home.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeActionPerformed(evt);
            }
        });

        viewCitizens.setBackground(new java.awt.Color(255, 255, 255));
        viewCitizens.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        viewCitizens.setForeground(new java.awt.Color(0, 102, 153));
        viewCitizens.setText("View Citizens");
        viewCitizens.setPreferredSize(new java.awt.Dimension(155, 72));
        viewCitizens.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewCitizensActionPerformed(evt);
            }
        });

        transactions.setBackground(new java.awt.Color(255, 255, 255));
        transactions.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        transactions.setForeground(new java.awt.Color(0, 102, 153));
        transactions.setText("Transactions");
        transactions.setPreferredSize(new java.awt.Dimension(155, 72));
        transactions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transactionsActionPerformed(evt);
            }
        });

        addCitizen.setBackground(new java.awt.Color(255, 255, 255));
        addCitizen.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        addCitizen.setForeground(new java.awt.Color(0, 102, 153));
        addCitizen.setText("Add Citizen");
        addCitizen.setPreferredSize(new java.awt.Dimension(155, 72));
        addCitizen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCitizenActionPerformed(evt);
            }
        });

        requestForm.setBackground(new java.awt.Color(255, 255, 255));
        requestForm.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        requestForm.setForeground(new java.awt.Color(0, 102, 153));
        requestForm.setText("Request Forms");
        requestForm.setPreferredSize(new java.awt.Dimension(155, 72));
        requestForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestFormActionPerformed(evt);
            }
        });

        calendar.setBackground(new java.awt.Color(255, 255, 255));
        calendar.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        calendar.setForeground(new java.awt.Color(0, 102, 153));
        calendar.setText("Calendar");
        calendar.setPreferredSize(new java.awt.Dimension(155, 72));
        calendar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calendarActionPerformed(evt);
            }
        });

        options.setBackground(new java.awt.Color(255, 255, 255));
        options.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        options.setForeground(new java.awt.Color(0, 102, 153));
        options.setText("Options");
        options.setPreferredSize(new java.awt.Dimension(155, 72));
        options.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsActionPerformed(evt);
            }
        });

        logout.setBackground(new java.awt.Color(255, 255, 255));
        logout.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        logout.setForeground(new java.awt.Color(0, 102, 153));
        logout.setText("Log Out");
        logout.setPreferredSize(new java.awt.Dimension(155, 72));
        logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(home, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(viewCitizens, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addComponent(transactions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addCitizen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(requestForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(calendar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(options, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(viewCitizens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(transactions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addCitizen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(requestForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(options, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(103, Short.MAX_VALUE))
        );

        jLayeredPane1.setBackground(new java.awt.Color(255, 255, 255));

        viewCitizensPanel.setBackground(new java.awt.Color(0, 102, 153));
        viewCitizensPanel.setPreferredSize(new java.awt.Dimension(904, 675));

        viewCitizensTable.setBackground(new java.awt.Color(189, 195, 198));
        viewCitizensTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Citizen ID", "Last Name", "First Name", "Address"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        v_viewCitizensScrollPane.setViewportView(viewCitizensTable);
        if (viewCitizensTable.getColumnModel().getColumnCount() > 0) {
            viewCitizensTable.getColumnModel().getColumn(0).setMaxWidth(60);
            viewCitizensTable.getColumnModel().getColumn(1).setMaxWidth(400);
            viewCitizensTable.getColumnModel().getColumn(2).setMaxWidth(1000);
            viewCitizensTable.getColumnModel().getColumn(3).setMaxWidth(300);
        }

        vc_viewProfileButton.setBackground(new java.awt.Color(189, 195, 198));
        vc_viewProfileButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        vc_viewProfileButton.setForeground(new java.awt.Color(0, 102, 153));
        vc_viewProfileButton.setText("View Profile");
        vc_viewProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vc_viewProfileButtonActionPerformed(evt);
            }
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchFieldKeyReleased(evt);
            }
        });

        vc_searchCitizen.setBackground(new java.awt.Color(189, 195, 198));
        vc_searchCitizen.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        vc_searchCitizen.setForeground(new java.awt.Color(0, 102, 153));
        vc_searchCitizen.setText("Search");
        vc_searchCitizen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vc_searchCitizenActionPerformed(evt);
            }
        });
        vc_searchCitizen.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                vc_searchCitizenKeyReleased(evt);
            }
        });

        vc_filter.setBackground(new java.awt.Color(189, 195, 198));
        vc_filter.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        vc_filter.setForeground(new java.awt.Color(0, 102, 153));
        vc_filter.setText("Filter");
        vc_filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vc_filterActionPerformed(evt);
            }
        });

        vc_back.setBackground(new java.awt.Color(189, 195, 198));
        vc_back.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        vc_back.setForeground(new java.awt.Color(0, 102, 153));
        vc_back.setText("Back");
        vc_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vc_backActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(189, 195, 198));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(0, 102, 153));
        jButton1.setText("Print");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout viewCitizensPanelLayout = new javax.swing.GroupLayout(viewCitizensPanel);
        viewCitizensPanel.setLayout(viewCitizensPanelLayout);
        viewCitizensPanelLayout.setHorizontalGroup(
            viewCitizensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewCitizensPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewCitizensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(v_viewCitizensScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewCitizensPanelLayout.createSequentialGroup()
                        .addComponent(vc_filter, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(vc_searchCitizen, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewCitizensPanelLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(vc_viewProfileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(vc_back, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        viewCitizensPanelLayout.setVerticalGroup(
            viewCitizensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewCitizensPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewCitizensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vc_searchCitizen, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vc_filter, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(v_viewCitizensScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(viewCitizensPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vc_viewProfileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vc_back, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        profilePanel.setBackground(new java.awt.Color(0, 102, 153));
        profilePanel.setPreferredSize(new java.awt.Dimension(894, 693));

        addTab.setBackground(new java.awt.Color(189, 195, 198));

        personalInfo.setBackground(new java.awt.Color(255, 255, 255));
        personalInfo.setForeground(new java.awt.Color(51, 51, 55));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Personal Information");

        jLabel3.setText("Last Name:");

        jLabel4.setText("First Name:");

        jLabel5.setText("Middle Name:");

        lname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lnameActionPerformed(evt);
            }
        });

        nameEx.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<none>", "Senior", "Junior", "III", "IV", "V" }));

        jLabel7.setText("Sex:");

        sex.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Male", "Female" }));

        jLabel8.setText("Civil Status:");

        status.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Single", "Married", "Separated", "Widowed", "Annuled" }));
        status.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusActionPerformed(evt);
            }
        });

        jLabel9.setText("Date of Birth:");

        jLabel10.setText("Place of Birth:");

        jLabel11.setText("Religion:");

        jLabel12.setText("Residential Address:");

        jLabel13.setText("Telephone Number:");

        jLabel14.setText("Zip Code:");

        jLabel15.setText("Email Address:");

        jLabel16.setText("Ocupation:");

        jLabel17.setText("Precint Number:");

        jLabel6.setText("Age:");

        jLabel41.setText("Extension:");

        jLabel42.setText("Citizen ID:");

        dob.setForeground(new java.awt.Color(0, 0, 0));
        dob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dobActionPerformed(evt);
            }
        });

        printButtProfile.setBackground(new java.awt.Color(189, 195, 198));
        printButtProfile.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        printButtProfile.setForeground(new java.awt.Color(0, 102, 153));
        printButtProfile.setText("Print");
        printButtProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtProfileActionPerformed(evt);
            }
        });

        new_note.setFont(new java.awt.Font("Dialog", 3, 12)); // NOI18N
        new_note.setForeground(new java.awt.Color(255, 0, 0));
        new_note.setText("NOTE: Labels with * are required fields.");

        new_s1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        new_s1.setForeground(new java.awt.Color(255, 0, 0));
        new_s1.setText("*");

        new_s2.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        new_s2.setForeground(new java.awt.Color(255, 0, 0));
        new_s2.setText("*");

        new_s3.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        new_s3.setForeground(new java.awt.Color(255, 0, 0));
        new_s3.setText("*");

        javax.swing.GroupLayout personalInfoLayout = new javax.swing.GroupLayout(personalInfo);
        personalInfo.setLayout(personalInfoLayout);
        personalInfoLayout.setHorizontalGroup(
            personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(personalInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(personalInfoLayout.createSequentialGroup()
                        .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addGroup(personalInfoLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(new_s2))
                            .addComponent(jLabel5)
                            .addGroup(personalInfoLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(new_s1)))
                        .addGap(47, 47, 47)
                        .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(personalInfoLayout.createSequentialGroup()
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lname, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(mname, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(fname, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(10, 10, 10)
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7)
                                    .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel42)
                                        .addComponent(jLabel41)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(status, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(nameEx, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(sex, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(pID)))
                            .addGroup(personalInfoLayout.createSequentialGroup()
                                .addComponent(dob, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, personalInfoLayout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addGap(29, 29, 29))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, personalInfoLayout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(religion, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                                    .addComponent(pob)))))
                    .addGroup(personalInfoLayout.createSequentialGroup()
                        .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(printButtProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(new_note, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(personalInfoLayout.createSequentialGroup()
                        .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel16)
                            .addComponent(jLabel15)
                            .addComponent(jLabel6))
                        .addGap(6, 6, 6)
                        .addComponent(new_s3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(personalInfoLayout.createSequentialGroup()
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(telNum, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(occupation, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(24, 24, 24)
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel14))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(zipCode)
                                    .addComponent(precint)))
                            .addGroup(personalInfoLayout.createSequentialGroup()
                                .addComponent(age, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(address)
                            .addComponent(email))))
                .addContainerGap())
        );
        personalInfoLayout.setVerticalGroup(
            personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(personalInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel42)
                    .addComponent(pID))
                .addGap(18, 18, 18)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameEx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41)
                    .addComponent(new_s1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(fname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(new_s2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(mname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(pob, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dob, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(religion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(age, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(new_s3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(zipCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(telNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(occupation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(precint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel17)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(personalInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(67, 67, 67)
                .addComponent(new_note)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
                .addComponent(printButtProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        addTab.addTab("Personal Information", personalInfo);

        famback.setBackground(new java.awt.Color(189, 195, 198));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel20.setText("Father's Last Name:");

        jLabel26.setText("Spouse's Last Name:");

        jLabel23.setText("Mother's First Name:");

        jLabel19.setText("Father's First Name:");

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("Siblings");

        jLabel24.setText("Mother's Middle Name:");

        jLabel22.setText("Mother's Last Name:");

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel18.setText("Family Background");

        jLabel27.setText("Spouse's First Name:");

        jLabel21.setText("Father's Middle Name:");

        jLabel28.setText("Spouse's Middle Name:");

        child.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Last Name", "First Name", "Middle Name", "Date of Birth"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(child);

        sibling.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Last Name", "First Name", "Middle Name", "Date of Birth"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(sibling);

        jLabel29.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Children");

        siblingAdd.setBackground(new java.awt.Color(189, 195, 198));
        siblingAdd.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        siblingAdd.setForeground(new java.awt.Color(0, 102, 153));
        siblingAdd.setText("Add");
        siblingAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siblingAddActionPerformed(evt);
            }
        });

        childAdd.setBackground(new java.awt.Color(189, 195, 198));
        childAdd.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        childAdd.setForeground(new java.awt.Color(0, 102, 153));
        childAdd.setText("Add");
        childAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3)
                    .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(siblingAdd, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel27)
                                    .addComponent(jLabel28)
                                    .addComponent(jLabel26))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sfname)
                                    .addComponent(smname)
                                    .addComponent(slname, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 125, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(childAdd)
                            .addComponent(mfname, javax.swing.GroupLayout.PREFERRED_SIZE, 675, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(flname, javax.swing.GroupLayout.PREFERRED_SIZE, 675, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel22)
                                .addComponent(jLabel23))
                            .addComponent(jLabel21)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(mlname, javax.swing.GroupLayout.DEFAULT_SIZE, 675, Short.MAX_VALUE)
                                    .addComponent(fmname)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(ffname))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(18, 18, 18)
                        .addComponent(mmname)))
                .addGap(650, 650, 650))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(7, 7, 7)
                    .addComponent(jLabel18)
                    .addContainerGap(1322, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(flname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ffname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fmname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(mlname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(mfname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(mmname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel25)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(siblingAdd)
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(slname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sfname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(smname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28))
                .addGap(13, 13, 13)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(childAdd)
                .addContainerGap(130, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel18)
                    .addContainerGap(691, Short.MAX_VALUE)))
        );

        famback.setViewportView(jPanel2);

        addTab.addTab("Family Background", famback);

        educ.setBackground(new java.awt.Color(255, 255, 255));

        jLabel37.setText("Year Graduated:");

        jLabel38.setText("Year Graduated:");

        jLabel40.setText("Year Graduated:");

        jLabel34.setText("Vocational School Graduated:");

        jLabel36.setText("Year Graduated:");

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel30.setText("Educational Background");

        jLabel32.setText("High School Graduated:");

        jLabel33.setText("College Graduated:");

        jLabel39.setText("Year Graduated:");

        jLabel35.setText("Graduate School Graduated:");

        jLabel31.setText("Elementary School Graduated:");

        javax.swing.GroupLayout educLayout = new javax.swing.GroupLayout(educ);
        educ.setLayout(educLayout);
        educLayout.setHorizontalGroup(
            educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(educLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(educLayout.createSequentialGroup()
                        .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel34)
                            .addComponent(jLabel33)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39)
                            .addComponent(jLabel40))
                        .addGap(15, 15, 15)
                        .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gSchool, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cSchool, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(vSchool)
                            .addGroup(educLayout.createSequentialGroup()
                                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gYear, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(vYear, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cYear, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 471, Short.MAX_VALUE))))
                    .addComponent(jLabel30)
                    .addComponent(jLabel35)
                    .addGroup(educLayout.createSequentialGroup()
                        .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31)
                            .addComponent(jLabel32)
                            .addComponent(jLabel36)
                            .addComponent(jLabel37))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(eSchool)
                            .addComponent(hsSchool)
                            .addGroup(educLayout.createSequentialGroup()
                                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(eYear, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(hsYear, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        educLayout.setVerticalGroup(
            educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(educLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30)
                .addGap(18, 18, 18)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(eSchool, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(eYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(hsSchool, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(hsYear, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(vSchool, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38))
                .addGap(5, 5, 5)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(cSchool, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(cYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel35)
                    .addComponent(gSchool, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(educLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel40)
                    .addComponent(gYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(260, Short.MAX_VALUE))
        );

        addTab.addTab("Educational Background", educ);

        rhtab.setBackground(new java.awt.Color(255, 255, 255));

        jLabel53.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel53.setText("Request History");

        profile_rhTable.setBackground(new java.awt.Color(189, 195, 198));
        profile_rhTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Req ID", "Form Name", "Date and Time Requested", "Date Available", "Date and Time Claimed"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        profile_rhTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                profile_rhTableMouseReleased(evt);
            }
        });
        t_transactionsScrollPane3.setViewportView(profile_rhTable);
        if (profile_rhTable.getColumnModel().getColumnCount() > 0) {
            profile_rhTable.getColumnModel().getColumn(0).setMaxWidth(60);
            profile_rhTable.getColumnModel().getColumn(1).setMaxWidth(515);
            profile_rhTable.getColumnModel().getColumn(2).setMaxWidth(435);
            profile_rhTable.getColumnModel().getColumn(3).setMaxWidth(335);
            profile_rhTable.getColumnModel().getColumn(4).setMaxWidth(435);
        }

        profile_requestForm.setBackground(new java.awt.Color(189, 195, 198));
        profile_requestForm.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_requestForm.setForeground(new java.awt.Color(0, 102, 153));
        profile_requestForm.setText("Request Form");
        profile_requestForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_requestFormActionPerformed(evt);
            }
        });

        profile_claimForm.setBackground(new java.awt.Color(189, 195, 198));
        profile_claimForm.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_claimForm.setForeground(new java.awt.Color(0, 102, 153));
        profile_claimForm.setText("Claim Form");
        profile_claimForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_claimFormActionPerformed(evt);
            }
        });

        profile_viewFrom.setBackground(new java.awt.Color(189, 195, 198));
        profile_viewFrom.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_viewFrom.setForeground(new java.awt.Color(0, 102, 153));
        profile_viewFrom.setText("View Form");
        profile_viewFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_viewFromActionPerformed(evt);
            }
        });

        profile_claimForm1.setBackground(new java.awt.Color(189, 195, 198));
        profile_claimForm1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_claimForm1.setForeground(new java.awt.Color(0, 102, 153));
        profile_claimForm1.setText("Print");
        profile_claimForm1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_claimForm1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rhtabLayout = new javax.swing.GroupLayout(rhtab);
        rhtab.setLayout(rhtabLayout);
        rhtabLayout.setHorizontalGroup(
            rhtabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rhtabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rhtabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(t_transactionsScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rhtabLayout.createSequentialGroup()
                        .addGroup(rhtabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel53)
                            .addGroup(rhtabLayout.createSequentialGroup()
                                .addComponent(profile_requestForm, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(profile_claimForm, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(profile_claimForm1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(profile_viewFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        rhtabLayout.setVerticalGroup(
            rhtabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rhtabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel53)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(t_transactionsScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rhtabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profile_requestForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profile_claimForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profile_viewFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profile_claimForm1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(93, Short.MAX_VALUE))
        );

        addTab.addTab("Request History", rhtab);

        notetab.setBackground(new java.awt.Color(255, 255, 255));

        jLabel54.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel54.setText("Visit Notes");

        profile_notesTable.setBackground(new java.awt.Color(189, 195, 198));
        profile_notesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Note ID", "Note Title", "Date and Time Added", "Date and Time Modified"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        t_transactionsScrollPane4.setViewportView(profile_notesTable);
        if (profile_notesTable.getColumnModel().getColumnCount() > 0) {
            profile_notesTable.getColumnModel().getColumn(0).setMaxWidth(60);
            profile_notesTable.getColumnModel().getColumn(1).setMaxWidth(620);
            profile_notesTable.getColumnModel().getColumn(2).setMaxWidth(300);
            profile_notesTable.getColumnModel().getColumn(3).setMaxWidth(300);
        }

        profile_addNote.setBackground(new java.awt.Color(189, 195, 198));
        profile_addNote.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_addNote.setForeground(new java.awt.Color(0, 102, 153));
        profile_addNote.setText("Add Note");
        profile_addNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_addNoteActionPerformed(evt);
            }
        });

        profile_viewNote.setBackground(new java.awt.Color(189, 195, 198));
        profile_viewNote.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_viewNote.setForeground(new java.awt.Color(0, 102, 153));
        profile_viewNote.setText("View Note");
        profile_viewNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_viewNoteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout notetabLayout = new javax.swing.GroupLayout(notetab);
        notetab.setLayout(notetabLayout);
        notetabLayout.setHorizontalGroup(
            notetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notetabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(notetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(t_transactionsScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
                    .addGroup(notetabLayout.createSequentialGroup()
                        .addComponent(jLabel54)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(notetabLayout.createSequentialGroup()
                        .addComponent(profile_addNote, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(profile_viewNote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        notetabLayout.setVerticalGroup(
            notetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notetabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel54)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(t_transactionsScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(notetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profile_addNote, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profile_viewNote, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(93, Short.MAX_VALUE))
        );

        addTab.addTab("Visit Notes", notetab);

        profileSave.setBackground(new java.awt.Color(189, 195, 198));
        profileSave.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        profileSave.setForeground(new java.awt.Color(0, 102, 153));
        profileSave.setText("Save");
        profileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileSaveActionPerformed(evt);
            }
        });

        profileCancel.setBackground(new java.awt.Color(189, 195, 198));
        profileCancel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        profileCancel.setForeground(new java.awt.Color(0, 102, 153));
        profileCancel.setText("Cancel");
        profileCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout profilePanelLayout = new javax.swing.GroupLayout(profilePanel);
        profilePanel.setLayout(profilePanelLayout);
        profilePanelLayout.setHorizontalGroup(
            profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addTab, javax.swing.GroupLayout.DEFAULT_SIZE, 870, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, profilePanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(profileSave, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(profileCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        profilePanelLayout.setVerticalGroup(
            profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addTab, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(profilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profileCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profileSave, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        settingsPanel.setBackground(new java.awt.Color(0, 102, 153));
        settingsPanel.setPreferredSize(new java.awt.Dimension(918, 705));

        set1.setBackground(new java.awt.Color(255, 255, 255));

        set_viewForms.setBackground(new java.awt.Color(189, 195, 198));
        set_viewForms.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        set_viewForms.setForeground(new java.awt.Color(0, 102, 153));
        set_viewForms.setText("View Forms");
        set_viewForms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_viewFormsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout set1Layout = new javax.swing.GroupLayout(set1);
        set1.setLayout(set1Layout);
        set1Layout.setHorizontalGroup(
            set1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewForms, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        set1Layout.setVerticalGroup(
            set1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewForms, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        set3.setBackground(new java.awt.Color(255, 255, 255));

        set_viewOfficials.setBackground(new java.awt.Color(189, 195, 198));
        set_viewOfficials.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        set_viewOfficials.setForeground(new java.awt.Color(0, 102, 153));
        set_viewOfficials.setText("View Officials");
        set_viewOfficials.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_viewOfficialsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout set3Layout = new javax.swing.GroupLayout(set3);
        set3.setLayout(set3Layout);
        set3Layout.setHorizontalGroup(
            set3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewOfficials, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        set3Layout.setVerticalGroup(
            set3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewOfficials, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        set4.setBackground(new java.awt.Color(255, 255, 255));

        set_viewUsers.setBackground(new java.awt.Color(189, 195, 198));
        set_viewUsers.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        set_viewUsers.setForeground(new java.awt.Color(0, 102, 153));
        set_viewUsers.setText("View Users");
        set_viewUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_viewUsersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout set4Layout = new javax.swing.GroupLayout(set4);
        set4.setLayout(set4Layout);
        set4Layout.setHorizontalGroup(
            set4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewUsers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        set4Layout.setVerticalGroup(
            set4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        set5.setBackground(new java.awt.Color(255, 255, 255));

        set_viewLog.setBackground(new java.awt.Color(189, 195, 198));
        set_viewLog.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        set_viewLog.setForeground(new java.awt.Color(0, 102, 153));
        set_viewLog.setText("View Logs");
        set_viewLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_viewLogActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout set5Layout = new javax.swing.GroupLayout(set5);
        set5.setLayout(set5Layout);
        set5Layout.setHorizontalGroup(
            set5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        set5Layout.setVerticalGroup(
            set5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_viewLog, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        set6.setBackground(new java.awt.Color(255, 255, 255));

        set_import.setBackground(new java.awt.Color(189, 195, 198));
        set_import.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        set_import.setForeground(new java.awt.Color(0, 102, 153));
        set_import.setText("Import Citizens");
        set_import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_importActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout set6Layout = new javax.swing.GroupLayout(set6);
        set6.setLayout(set6Layout);
        set6Layout.setHorizontalGroup(
            set6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_import, javax.swing.GroupLayout.DEFAULT_SIZE, 871, Short.MAX_VALUE)
                .addContainerGap())
        );
        set6Layout.setVerticalGroup(
            set6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(set6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set_import, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(set1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(set3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(set4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(set5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(set6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(set1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(set3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(set4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(set5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(set6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        officialsPanel.setBackground(new java.awt.Color(0, 102, 153));

        officialsPane.setBorder(null);

        tableOfficials.setAutoCreateRowSorter(true);
        tableOfficials.setBackground(new java.awt.Color(189, 195, 198));
        tableOfficials.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        tableOfficials.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Position", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableOfficials.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tableOfficials.setGridColor(new java.awt.Color(204, 204, 204));
        tableOfficials.setOpaque(false);
        tableOfficials.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableOfficials.setShowVerticalLines(false);
        tableOfficials.getTableHeader().setReorderingAllowed(false);
        tableOfficials.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableOfficialsMouseReleased(evt);
            }
        });
        officialsPane.setViewportView(tableOfficials);

        off_adminYear.setBackground(new java.awt.Color(255, 255, 255));
        off_adminYear.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_adminYear.setForeground(new java.awt.Color(0, 102, 153));
        off_adminYear.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        off_adminYear.setText("jLabel1");
        off_adminYear.setOpaque(true);

        off_createNewAdminButton.setBackground(new java.awt.Color(189, 195, 198));
        off_createNewAdminButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_createNewAdminButton.setForeground(new java.awt.Color(0, 102, 153));
        off_createNewAdminButton.setText("Create New Administration");
        off_createNewAdminButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                off_createNewAdminButtonActionPerformed(evt);
            }
        });

        off_viewProfileButton.setBackground(new java.awt.Color(189, 195, 198));
        off_viewProfileButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_viewProfileButton.setForeground(new java.awt.Color(0, 102, 153));
        off_viewProfileButton.setText("View Profile");
        off_viewProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                off_viewProfileButtonActionPerformed(evt);
            }
        });

        off_editOfficial.setBackground(new java.awt.Color(189, 195, 198));
        off_editOfficial.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_editOfficial.setForeground(new java.awt.Color(0, 102, 153));
        off_editOfficial.setText("Edit");
        off_editOfficial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                off_editOfficialActionPerformed(evt);
            }
        });

        off_backButton.setBackground(new java.awt.Color(189, 195, 198));
        off_backButton.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_backButton.setForeground(new java.awt.Color(0, 102, 153));
        off_backButton.setText("Back");
        off_backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                off_backButtonActionPerformed(evt);
            }
        });

        jLabel43.setBackground(new java.awt.Color(255, 255, 255));
        jLabel43.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(0, 102, 153));
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("Barangay Officials");
        jLabel43.setOpaque(true);

        off_next.setBackground(new java.awt.Color(189, 195, 198));
        off_next.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_next.setForeground(new java.awt.Color(0, 102, 153));
        off_next.setText("Next");
        off_next.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                off_nextMouseClicked(evt);
            }
        });
        off_next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                off_nextActionPerformed(evt);
            }
        });

        off_prev.setBackground(new java.awt.Color(189, 195, 198));
        off_prev.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        off_prev.setForeground(new java.awt.Color(0, 102, 153));
        off_prev.setText("Prev");
        off_prev.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                off_prevMouseClicked(evt);
            }
        });
        off_prev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                off_prevActionPerformed(evt);
            }
        });

        adminIDLabel.setText("Administartion ID:");

        javax.swing.GroupLayout officialsPanelLayout = new javax.swing.GroupLayout(officialsPanel);
        officialsPanel.setLayout(officialsPanelLayout);
        officialsPanelLayout.setHorizontalGroup(
            officialsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(officialsPanelLayout.createSequentialGroup()
                .addGroup(officialsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(officialsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(officialsPanelLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addGroup(officialsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, officialsPanelLayout.createSequentialGroup()
                                .addComponent(off_prev)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(off_adminYear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(off_next))
                            .addComponent(officialsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
                            .addComponent(off_createNewAdminButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(adminIDLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(officialsPanelLayout.createSequentialGroup()
                                .addComponent(off_viewProfileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(off_editOfficial, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(off_backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(104, 104, 104)))
                .addContainerGap())
        );
        officialsPanelLayout.setVerticalGroup(
            officialsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(officialsPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(officialsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(off_prev, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(off_adminYear, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(off_next, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(adminIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(officialsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(off_createNewAdminButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(officialsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(off_backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(off_editOfficial, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(off_viewProfileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(85, Short.MAX_VALUE))
        );

        usersPanel.setBackground(new java.awt.Color(0, 102, 153));

        usersPane.setBorder(null);

        tableUsers.setAutoCreateRowSorter(true);
        tableUsers.setBackground(new java.awt.Color(189, 195, 198));
        tableUsers.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        tableUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "User ID", "Name", "Username", "User Type", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableUsers.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tableUsers.setGridColor(new java.awt.Color(204, 204, 204));
        tableUsers.setOpaque(false);
        tableUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableUsers.setShowVerticalLines(false);
        tableUsers.getTableHeader().setReorderingAllowed(false);
        tableUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableUsersMouseReleased(evt);
            }
        });
        usersPane.setViewportView(tableUsers);

        use_adminYear1.setBackground(new java.awt.Color(255, 255, 255));
        use_adminYear1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        use_adminYear1.setForeground(new java.awt.Color(0, 102, 153));
        use_adminYear1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        use_adminYear1.setText("BRMS Administrators");
        use_adminYear1.setOpaque(true);

        use_deact.setBackground(new java.awt.Color(189, 195, 198));
        use_deact.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        use_deact.setForeground(new java.awt.Color(0, 102, 153));
        use_deact.setText("Deactivate User");
        use_deact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_deactActionPerformed(evt);
            }
        });

        use_viewProf.setBackground(new java.awt.Color(189, 195, 198));
        use_viewProf.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        use_viewProf.setForeground(new java.awt.Color(0, 102, 153));
        use_viewProf.setText("View Profile");
        use_viewProf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_viewProfActionPerformed(evt);
            }
        });

        use_edit.setBackground(new java.awt.Color(189, 195, 198));
        use_edit.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        use_edit.setForeground(new java.awt.Color(0, 102, 153));
        use_edit.setText("Edit");
        use_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_editActionPerformed(evt);
            }
        });

        use_back.setBackground(new java.awt.Color(189, 195, 198));
        use_back.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        use_back.setForeground(new java.awt.Color(0, 102, 153));
        use_back.setText("Back");
        use_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_backActionPerformed(evt);
            }
        });

        use_newUser.setBackground(new java.awt.Color(189, 195, 198));
        use_newUser.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        use_newUser.setForeground(new java.awt.Color(0, 102, 153));
        use_newUser.setText("New User");
        use_newUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                use_newUserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout usersPanelLayout = new javax.swing.GroupLayout(usersPanel);
        usersPanel.setLayout(usersPanelLayout);
        usersPanelLayout.setHorizontalGroup(
            usersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(usersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(use_adminYear1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(usersPanelLayout.createSequentialGroup()
                .addGap(167, 167, 167)
                .addGroup(usersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(usersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(usersPanelLayout.createSequentialGroup()
                            .addComponent(use_newUser, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(use_deact, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(usersPanelLayout.createSequentialGroup()
                            .addComponent(use_viewProf, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(use_edit, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(use_back, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(usersPane, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(164, Short.MAX_VALUE))
        );
        usersPanelLayout.setVerticalGroup(
            usersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(usersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(use_adminYear1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(usersPane, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(usersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(use_deact, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                    .addComponent(use_newUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(11, 11, 11)
                .addGroup(usersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(use_viewProf, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(use_edit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(use_back, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        calendarPanel.setBackground(new java.awt.Color(0, 102, 153));
        calendarPanel.setForeground(new java.awt.Color(255, 255, 255));

        monthChoice.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        monthChoice.setForeground(new java.awt.Color(0, 102, 153));

        yearChoice.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        yearChoice.setForeground(new java.awt.Color(0, 102, 153));

        Day.setBackground(new java.awt.Color(0, 102, 153));

        DayName.setBackground(new java.awt.Color(0, 102, 153));

        sun.setBackground(new java.awt.Color(255, 255, 255));
        sun.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        sun.setForeground(new java.awt.Color(0, 102, 153));
        sun.setText("Sun");

        mon.setBackground(new java.awt.Color(255, 255, 255));
        mon.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        mon.setForeground(new java.awt.Color(0, 102, 153));
        mon.setText("Mon");

        wed.setBackground(new java.awt.Color(255, 255, 255));
        wed.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        wed.setForeground(new java.awt.Color(0, 102, 153));
        wed.setText("Wed");

        thu.setBackground(new java.awt.Color(255, 255, 255));
        thu.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        thu.setForeground(new java.awt.Color(0, 102, 153));
        thu.setText("Thu");

        fri.setBackground(new java.awt.Color(255, 255, 255));
        fri.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        fri.setForeground(new java.awt.Color(0, 102, 153));
        fri.setText("Fri");

        sat.setBackground(new java.awt.Color(255, 255, 255));
        sat.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        sat.setForeground(new java.awt.Color(0, 102, 153));
        sat.setText("Sat");

        tue.setBackground(new java.awt.Color(255, 255, 255));
        tue.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        tue.setForeground(new java.awt.Color(0, 102, 153));
        tue.setText("Tue");

        javax.swing.GroupLayout DayNameLayout = new javax.swing.GroupLayout(DayName);
        DayName.setLayout(DayNameLayout);
        DayNameLayout.setHorizontalGroup(
            DayNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DayNameLayout.createSequentialGroup()
                .addGap(86, 86, 86)
                .addComponent(sun, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(mon, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tue, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(wed, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(thu, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(fri, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(sat, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        DayNameLayout.setVerticalGroup(
            DayNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DayNameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DayNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sun, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mon, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wed, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(thu, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fri, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sat, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tue, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
        );

        Weeks.setBackground(new java.awt.Color(0, 102, 153));

        week1.setBackground(new java.awt.Color(0, 102, 153));

        day0.setBackground(new java.awt.Color(255, 255, 255));
        day0.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day0.setForeground(new java.awt.Color(0, 102, 153));

        day1.setBackground(new java.awt.Color(255, 255, 255));
        day1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day1.setForeground(new java.awt.Color(0, 102, 153));

        day3.setBackground(new java.awt.Color(255, 255, 255));
        day3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day3.setForeground(new java.awt.Color(0, 102, 153));

        day4.setBackground(new java.awt.Color(255, 255, 255));
        day4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day4.setForeground(new java.awt.Color(0, 102, 153));

        day5.setBackground(new java.awt.Color(255, 255, 255));
        day5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day5.setForeground(new java.awt.Color(0, 102, 153));

        day6.setBackground(new java.awt.Color(255, 255, 255));
        day6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day6.setForeground(new java.awt.Color(0, 102, 153));

        day2.setBackground(new java.awt.Color(255, 255, 255));
        day2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day2.setForeground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout week1Layout = new javax.swing.GroupLayout(week1);
        week1.setLayout(week1Layout);
        week1Layout.setHorizontalGroup(
            week1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week1Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(day0, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day3, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day4, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day5, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day6, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        week1Layout.setVerticalGroup(
            week1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(week1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(day0, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day4, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        week2.setBackground(new java.awt.Color(0, 102, 153));

        day7.setBackground(new java.awt.Color(255, 255, 255));
        day7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day7.setForeground(new java.awt.Color(0, 102, 153));

        day8.setBackground(new java.awt.Color(255, 255, 255));
        day8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day8.setForeground(new java.awt.Color(0, 102, 153));

        day10.setBackground(new java.awt.Color(255, 255, 255));
        day10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day10.setForeground(new java.awt.Color(0, 102, 153));

        day11.setBackground(new java.awt.Color(255, 255, 255));
        day11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day11.setForeground(new java.awt.Color(0, 102, 153));

        day12.setBackground(new java.awt.Color(255, 255, 255));
        day12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day12.setForeground(new java.awt.Color(0, 102, 153));

        day13.setBackground(new java.awt.Color(255, 255, 255));
        day13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day13.setForeground(new java.awt.Color(0, 102, 153));

        day9.setBackground(new java.awt.Color(255, 255, 255));
        day9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day9.setForeground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout week2Layout = new javax.swing.GroupLayout(week2);
        week2.setLayout(week2Layout);
        week2Layout.setHorizontalGroup(
            week2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week2Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(day7, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day8, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day9, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day10, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day11, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day12, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day13, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        week2Layout.setVerticalGroup(
            week2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(week2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(day7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day10, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day11, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day12, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day13, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day9, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        week3.setBackground(new java.awt.Color(0, 102, 153));

        day14.setBackground(new java.awt.Color(255, 255, 255));
        day14.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day14.setForeground(new java.awt.Color(0, 102, 153));

        day15.setBackground(new java.awt.Color(255, 255, 255));
        day15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day15.setForeground(new java.awt.Color(0, 102, 153));

        day17.setBackground(new java.awt.Color(255, 255, 255));
        day17.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day17.setForeground(new java.awt.Color(0, 102, 153));

        day18.setBackground(new java.awt.Color(255, 255, 255));
        day18.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day18.setForeground(new java.awt.Color(0, 102, 153));

        day19.setBackground(new java.awt.Color(255, 255, 255));
        day19.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day19.setForeground(new java.awt.Color(0, 102, 153));

        day20.setBackground(new java.awt.Color(255, 255, 255));
        day20.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day20.setForeground(new java.awt.Color(0, 102, 153));

        day16.setBackground(new java.awt.Color(255, 255, 255));
        day16.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day16.setForeground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout week3Layout = new javax.swing.GroupLayout(week3);
        week3.setLayout(week3Layout);
        week3Layout.setHorizontalGroup(
            week3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week3Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(day14, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day15, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day16, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day17, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day18, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day19, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day20, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        week3Layout.setVerticalGroup(
            week3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(week3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(day14, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day15, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day17, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day18, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day19, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day20, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day16, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        week5.setBackground(new java.awt.Color(0, 102, 153));

        day21.setBackground(new java.awt.Color(255, 255, 255));
        day21.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day21.setForeground(new java.awt.Color(0, 102, 153));

        day22.setBackground(new java.awt.Color(255, 255, 255));
        day22.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day22.setForeground(new java.awt.Color(0, 102, 153));

        day24.setBackground(new java.awt.Color(255, 255, 255));
        day24.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day24.setForeground(new java.awt.Color(0, 102, 153));

        day25.setBackground(new java.awt.Color(255, 255, 255));
        day25.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day25.setForeground(new java.awt.Color(0, 102, 153));

        day26.setBackground(new java.awt.Color(255, 255, 255));
        day26.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day26.setForeground(new java.awt.Color(0, 102, 153));

        day27.setBackground(new java.awt.Color(255, 255, 255));
        day27.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day27.setForeground(new java.awt.Color(0, 102, 153));

        day23.setBackground(new java.awt.Color(255, 255, 255));
        day23.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day23.setForeground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout week5Layout = new javax.swing.GroupLayout(week5);
        week5.setLayout(week5Layout);
        week5Layout.setHorizontalGroup(
            week5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week5Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(day21, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day22, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day23, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day24, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day25, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day26, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day27, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        week5Layout.setVerticalGroup(
            week5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(week5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(day23, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .addComponent(day24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(day25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(day26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(day27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(day22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(day21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        week6.setBackground(new java.awt.Color(0, 102, 153));

        day28.setBackground(new java.awt.Color(255, 255, 255));
        day28.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day28.setForeground(new java.awt.Color(0, 102, 153));

        day29.setBackground(new java.awt.Color(255, 255, 255));
        day29.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day29.setForeground(new java.awt.Color(0, 102, 153));

        day31.setBackground(new java.awt.Color(255, 255, 255));
        day31.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day31.setForeground(new java.awt.Color(0, 102, 153));

        day32.setBackground(new java.awt.Color(255, 255, 255));
        day32.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day32.setForeground(new java.awt.Color(0, 102, 153));

        day33.setBackground(new java.awt.Color(255, 255, 255));
        day33.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day33.setForeground(new java.awt.Color(0, 102, 153));

        day34.setBackground(new java.awt.Color(255, 255, 255));
        day34.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day34.setForeground(new java.awt.Color(0, 102, 153));

        day30.setBackground(new java.awt.Color(255, 255, 255));
        day30.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day30.setForeground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout week6Layout = new javax.swing.GroupLayout(week6);
        week6.setLayout(week6Layout);
        week6Layout.setHorizontalGroup(
            week6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week6Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(day28, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day29, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day30, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day31, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day32, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day33, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day34, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        week6Layout.setVerticalGroup(
            week6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(week6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(day28, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day29, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day31, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day32, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day33, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day34, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day30, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11))
        );

        week7.setBackground(new java.awt.Color(0, 102, 153));

        day35.setBackground(new java.awt.Color(255, 255, 255));
        day35.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day35.setForeground(new java.awt.Color(0, 102, 153));

        day36.setBackground(new java.awt.Color(255, 255, 255));
        day36.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day36.setForeground(new java.awt.Color(0, 102, 153));

        day38.setBackground(new java.awt.Color(255, 255, 255));
        day38.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day38.setForeground(new java.awt.Color(0, 102, 153));

        day39.setBackground(new java.awt.Color(255, 255, 255));
        day39.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day39.setForeground(new java.awt.Color(0, 102, 153));

        day40.setBackground(new java.awt.Color(255, 255, 255));
        day40.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day40.setForeground(new java.awt.Color(0, 102, 153));

        day41.setBackground(new java.awt.Color(255, 255, 255));
        day41.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day41.setForeground(new java.awt.Color(0, 102, 153));

        day37.setBackground(new java.awt.Color(255, 255, 255));
        day37.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        day37.setForeground(new java.awt.Color(0, 102, 153));

        javax.swing.GroupLayout week7Layout = new javax.swing.GroupLayout(week7);
        week7.setLayout(week7Layout);
        week7Layout.setHorizontalGroup(
            week7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week7Layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(day35, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day36, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day37, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day38, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day39, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day40, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(day41, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        week7Layout.setVerticalGroup(
            week7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(week7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(week7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(day35, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day36, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day38, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day39, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day40, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day41, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(day37, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        viewDayEvent.setBackground(new java.awt.Color(189, 195, 198));
        viewDayEvent.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        viewDayEvent.setForeground(new java.awt.Color(0, 102, 153));
        viewDayEvent.setText("View Event");
        viewDayEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDayEventActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout WeeksLayout = new javax.swing.GroupLayout(Weeks);
        Weeks.setLayout(WeeksLayout);
        WeeksLayout.setHorizontalGroup(
            WeeksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(WeeksLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(WeeksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(week1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(week2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(week3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(week5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(week6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(week7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, WeeksLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(viewDayEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(86, 86, 86))
        );
        WeeksLayout.setVerticalGroup(
            WeeksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(WeeksLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(week1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(week2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(week3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(week5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(week6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(week7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(viewDayEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout DayLayout = new javax.swing.GroupLayout(Day);
        Day.setLayout(DayLayout);
        DayLayout.setHorizontalGroup(
            DayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Weeks, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(DayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(DayName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        DayLayout.setVerticalGroup(
            DayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DayLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(DayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Weeks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout calendarPanelLayout = new javax.swing.GroupLayout(calendarPanel);
        calendarPanel.setLayout(calendarPanelLayout);
        calendarPanelLayout.setHorizontalGroup(
            calendarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calendarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Day, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, calendarPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(monthChoice, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(yearChoice, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(312, 312, 312))
        );
        calendarPanelLayout.setVerticalGroup(
            calendarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(calendarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(calendarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(yearChoice, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(monthChoice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        eventPanel.setBackground(new java.awt.Color(0, 102, 153));

        eventsTable.setAutoCreateRowSorter(true);
        eventsTable.setBackground(new java.awt.Color(189, 195, 198));
        eventsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Year", "Title", "Venue", "Time", "Remarks", "Author ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        eventsTable.setMinimumSize(new java.awt.Dimension(15, 30));
        eventsTable.setRowHeight(60);
        eventsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(eventsTable);

        backToCalendar.setBackground(new java.awt.Color(189, 195, 198));
        backToCalendar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        backToCalendar.setForeground(new java.awt.Color(0, 102, 153));
        backToCalendar.setText("Back");
        backToCalendar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToCalendarActionPerformed(evt);
            }
        });

        dateSelected.setBackground(new java.awt.Color(255, 255, 255));
        dateSelected.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        dateSelected.setForeground(new java.awt.Color(0, 102, 153));
        dateSelected.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dateSelected.setOpaque(true);

        addNewEvent.setBackground(new java.awt.Color(189, 195, 198));
        addNewEvent.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        addNewEvent.setForeground(new java.awt.Color(0, 102, 153));
        addNewEvent.setText("Add New Event");
        addNewEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewEventActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout eventPanelLayout = new javax.swing.GroupLayout(eventPanel);
        eventPanel.setLayout(eventPanelLayout);
        eventPanelLayout.setHorizontalGroup(
            eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventPanelLayout.createSequentialGroup()
                .addContainerGap(758, Short.MAX_VALUE)
                .addComponent(addNewEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(backToCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(eventPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(dateSelected, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        eventPanelLayout.setVerticalGroup(
            eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, eventPanelLayout.createSequentialGroup()
                .addContainerGap(714, Short.MAX_VALUE)
                .addGroup(eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(backToCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(eventPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(eventPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(dateSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 546, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(94, Short.MAX_VALUE)))
        );

        transactionsPanel.setBackground(new java.awt.Color(0, 102, 153));
        transactionsPanel.setPreferredSize(new java.awt.Dimension(904, 675));

        transactionTable.setBackground(new java.awt.Color(189, 195, 198));
        transactionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Trans ID", "Last Name", "First Name", "Date of Latest Transaction"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        t_transactionsScrollPane1.setViewportView(transactionTable);
        if (transactionTable.getColumnModel().getColumnCount() > 0) {
            transactionTable.getColumnModel().getColumn(0).setMaxWidth(60);
            transactionTable.getColumnModel().getColumn(1).setMaxWidth(700);
            transactionTable.getColumnModel().getColumn(2).setMaxWidth(700);
            transactionTable.getColumnModel().getColumn(3).setMaxWidth(300);
        }

        t_viewRequestHistory.setBackground(new java.awt.Color(189, 195, 198));
        t_viewRequestHistory.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        t_viewRequestHistory.setForeground(new java.awt.Color(0, 102, 153));
        t_viewRequestHistory.setText("View Request History");
        t_viewRequestHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_viewRequestHistoryActionPerformed(evt);
            }
        });

        t_searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                t_searchFieldKeyReleased(evt);
            }
        });

        t_searchTrans.setBackground(new java.awt.Color(189, 195, 198));
        t_searchTrans.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        t_searchTrans.setForeground(new java.awt.Color(0, 102, 153));
        t_searchTrans.setText("Search");
        t_searchTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_searchTransActionPerformed(evt);
            }
        });

        t_filter.setBackground(new java.awt.Color(189, 195, 198));
        t_filter.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        t_filter.setForeground(new java.awt.Color(0, 102, 153));
        t_filter.setText("Filter");
        t_filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_filterActionPerformed(evt);
            }
        });

        t_back.setBackground(new java.awt.Color(189, 195, 198));
        t_back.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        t_back.setForeground(new java.awt.Color(0, 102, 153));
        t_back.setText("Back");
        t_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_backActionPerformed(evt);
            }
        });

        t_viewRequestHistory1.setBackground(new java.awt.Color(189, 195, 198));
        t_viewRequestHistory1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        t_viewRequestHistory1.setForeground(new java.awt.Color(0, 102, 153));
        t_viewRequestHistory1.setText("Print");
        t_viewRequestHistory1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                t_viewRequestHistory1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout transactionsPanelLayout = new javax.swing.GroupLayout(transactionsPanel);
        transactionsPanel.setLayout(transactionsPanelLayout);
        transactionsPanelLayout.setHorizontalGroup(
            transactionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(transactionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transactionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(t_transactionsScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(transactionsPanelLayout.createSequentialGroup()
                        .addComponent(t_filter, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(t_searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 611, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(t_searchTrans, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, transactionsPanelLayout.createSequentialGroup()
                        .addComponent(t_viewRequestHistory1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(t_viewRequestHistory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(t_back, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        transactionsPanelLayout.setVerticalGroup(
            transactionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, transactionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transactionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(t_searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(t_searchTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(t_filter, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(t_transactionsScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(transactionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(t_back, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(t_viewRequestHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(t_viewRequestHistory1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(141, Short.MAX_VALUE))
        );

        requestHistoryPanel.setBackground(new java.awt.Color(0, 102, 153));
        requestHistoryPanel.setPreferredSize(new java.awt.Dimension(904, 675));

        requestHistoryTable.setBackground(new java.awt.Color(189, 195, 198));
        requestHistoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Req ID", "Form Name", "Date and Time Requested", "Date Available", "Date and Time Claimed"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        requestHistoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                requestHistoryTableMouseReleased(evt);
            }
        });
        t_transactionsScrollPane2.setViewportView(requestHistoryTable);
        if (requestHistoryTable.getColumnModel().getColumnCount() > 0) {
            requestHistoryTable.getColumnModel().getColumn(0).setMaxWidth(60);
            requestHistoryTable.getColumnModel().getColumn(1).setMaxWidth(800);
            requestHistoryTable.getColumnModel().getColumn(2).setMaxWidth(340);
            requestHistoryTable.getColumnModel().getColumn(3).setMaxWidth(240);
            requestHistoryTable.getColumnModel().getColumn(4).setMaxWidth(340);
        }

        rh_viewProfile.setBackground(new java.awt.Color(189, 195, 198));
        rh_viewProfile.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_viewProfile.setForeground(new java.awt.Color(0, 102, 153));
        rh_viewProfile.setText("View Profile");
        rh_viewProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rh_viewProfileActionPerformed(evt);
            }
        });

        rh_back.setBackground(new java.awt.Color(189, 195, 198));
        rh_back.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_back.setForeground(new java.awt.Color(0, 102, 153));
        rh_back.setText("Back");
        rh_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rh_backActionPerformed(evt);
            }
        });

        rh_requestForm.setBackground(new java.awt.Color(189, 195, 198));
        rh_requestForm.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_requestForm.setForeground(new java.awt.Color(0, 102, 153));
        rh_requestForm.setText("Request Form");
        rh_requestForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rh_requestFormActionPerformed(evt);
            }
        });

        rh_claimForm.setBackground(new java.awt.Color(189, 195, 198));
        rh_claimForm.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_claimForm.setForeground(new java.awt.Color(0, 102, 153));
        rh_claimForm.setText("Claim Form");
        rh_claimForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rh_claimFormActionPerformed(evt);
            }
        });

        rh_TID.setBackground(new java.awt.Color(255, 255, 255));
        rh_TID.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_TID.setForeground(new java.awt.Color(0, 102, 153));
        rh_TID.setText("TID");
        rh_TID.setOpaque(true);

        rh_name.setBackground(new java.awt.Color(255, 255, 255));
        rh_name.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_name.setForeground(new java.awt.Color(0, 102, 153));
        rh_name.setText("TNAMe");
        rh_name.setOpaque(true);

        rh_type.setBackground(new java.awt.Color(255, 255, 255));
        rh_type.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_type.setForeground(new java.awt.Color(0, 102, 153));
        rh_type.setText("TTYPE");
        rh_type.setOpaque(true);

        rh_viewFrom.setBackground(new java.awt.Color(189, 195, 198));
        rh_viewFrom.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        rh_viewFrom.setForeground(new java.awt.Color(0, 102, 153));
        rh_viewFrom.setText("View Form");
        rh_viewFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rh_viewFromActionPerformed(evt);
            }
        });

        profile_claimForm2.setBackground(new java.awt.Color(189, 195, 198));
        profile_claimForm2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        profile_claimForm2.setForeground(new java.awt.Color(0, 102, 153));
        profile_claimForm2.setText("Print");
        profile_claimForm2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profile_claimForm2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout requestHistoryPanelLayout = new javax.swing.GroupLayout(requestHistoryPanel);
        requestHistoryPanel.setLayout(requestHistoryPanelLayout);
        requestHistoryPanelLayout.setHorizontalGroup(
            requestHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(requestHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(requestHistoryPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(rh_TID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rh_name, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rh_type, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(t_transactionsScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(requestHistoryPanelLayout.createSequentialGroup()
                        .addComponent(rh_requestForm, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rh_claimForm, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rh_viewFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rh_viewProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(profile_claimForm2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rh_back, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        requestHistoryPanelLayout.setVerticalGroup(
            requestHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, requestHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(requestHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(rh_TID, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(rh_name, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rh_type, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(t_transactionsScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rh_requestForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rh_claimForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rh_viewProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rh_viewFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rh_back, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profile_claimForm2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(145, Short.MAX_VALUE))
        );

        formSelectPanel.setBackground(new java.awt.Color(0, 102, 153));
        formSelectPanel.setForeground(new java.awt.Color(255, 255, 255));

        formSelectPane.setBorder(null);

        formsSelectTable.setAutoCreateRowSorter(true);
        formsSelectTable.setBackground(new java.awt.Color(189, 195, 198));
        formsSelectTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        formsSelectTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Form ID", "Form Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        formsSelectTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        formsSelectTable.setGridColor(new java.awt.Color(204, 204, 204));
        formsSelectTable.setOpaque(false);
        formsSelectTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        formsSelectTable.setShowVerticalLines(false);
        formsSelectTable.getTableHeader().setReorderingAllowed(false);
        formSelectPane.setViewportView(formsSelectTable);
        if (formsSelectTable.getColumnModel().getColumnCount() > 0) {
            formsSelectTable.getColumnModel().getColumn(0).setMaxWidth(170);
            formsSelectTable.getColumnModel().getColumn(1).setMaxWidth(610);
        }

        prompt.setBackground(new java.awt.Color(255, 255, 255));
        prompt.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        prompt.setForeground(new java.awt.Color(0, 102, 153));
        prompt.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        prompt.setText("Select Form to Request");
        prompt.setOpaque(true);

        f_Select.setBackground(new java.awt.Color(255, 255, 255));
        f_Select.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        f_Select.setForeground(new java.awt.Color(0, 102, 153));
        f_Select.setText("Select");
        f_Select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                f_SelectActionPerformed(evt);
            }
        });

        f_back.setBackground(new java.awt.Color(255, 255, 255));
        f_back.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        f_back.setForeground(new java.awt.Color(0, 102, 153));
        f_back.setText("Back");
        f_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                f_backActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout formSelectPanelLayout = new javax.swing.GroupLayout(formSelectPanel);
        formSelectPanel.setLayout(formSelectPanelLayout);
        formSelectPanelLayout.setHorizontalGroup(
            formSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formSelectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formSelectPanelLayout.createSequentialGroup()
                        .addComponent(prompt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, formSelectPanelLayout.createSequentialGroup()
                        .addGap(0, 169, Short.MAX_VALUE)
                        .addGroup(formSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(formSelectPanelLayout.createSequentialGroup()
                                .addComponent(f_Select, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(f_back, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(formSelectPane, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(152, 152, 152))))
        );
        formSelectPanelLayout.setVerticalGroup(
            formSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formSelectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(prompt, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(formSelectPane, javax.swing.GroupLayout.PREFERRED_SIZE, 534, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(formSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(f_Select, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f_back, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(100, Short.MAX_VALUE))
        );

        logHistoryPanel.setBackground(new java.awt.Color(0, 102, 153));
        logHistoryPanel.setForeground(new java.awt.Color(255, 255, 255));
        logHistoryPanel.setPreferredSize(new java.awt.Dimension(904, 675));

        logHistoryTable.setBackground(new java.awt.Color(189, 195, 198));
        logHistoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Log ID", "Author ID", "Action", "Date and Time Authored"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        v_viewCitizensScrollPane1.setViewportView(logHistoryTable);
        if (logHistoryTable.getColumnModel().getColumnCount() > 0) {
            logHistoryTable.getColumnModel().getColumn(0).setMaxWidth(60);
            logHistoryTable.getColumnModel().getColumn(1).setMaxWidth(60);
            logHistoryTable.getColumnModel().getColumn(2).setMaxWidth(1000);
            logHistoryTable.getColumnModel().getColumn(3).setMaxWidth(300);
        }

        lh_back.setBackground(new java.awt.Color(255, 255, 255));
        lh_back.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lh_back.setForeground(new java.awt.Color(0, 102, 153));
        lh_back.setText("Back");
        lh_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lh_backActionPerformed(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 102, 153));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("LOG HISTORY");
        jLabel1.setOpaque(true);

        javax.swing.GroupLayout logHistoryPanelLayout = new javax.swing.GroupLayout(logHistoryPanel);
        logHistoryPanel.setLayout(logHistoryPanelLayout);
        logHistoryPanelLayout.setHorizontalGroup(
            logHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(v_viewCitizensScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 963, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logHistoryPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lh_back, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        logHistoryPanelLayout.setVerticalGroup(
            logHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(v_viewCitizensScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lh_back, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(100, Short.MAX_VALUE))
        );

        formListPanel.setBackground(new java.awt.Color(0, 102, 153));
        formListPanel.setPreferredSize(new java.awt.Dimension(904, 675));

        formListTable.setBackground(new java.awt.Color(189, 195, 198));
        formListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Form ID", "Form Name", "Status", "Date Added", "Date Modified", "Last Modified By"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        v_viewCitizensScrollPane2.setViewportView(formListTable);
        if (formListTable.getColumnModel().getColumnCount() > 0) {
            formListTable.getColumnModel().getColumn(0).setMaxWidth(60);
            formListTable.getColumnModel().getColumn(1).setMaxWidth(600);
            formListTable.getColumnModel().getColumn(2).setMaxWidth(300);
            formListTable.getColumnModel().getColumn(3).setMaxWidth(300);
            formListTable.getColumnModel().getColumn(4).setMaxWidth(300);
            formListTable.getColumnModel().getColumn(5).setMaxWidth(200);
        }

        fl_viewForm.setBackground(new java.awt.Color(255, 255, 255));
        fl_viewForm.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        fl_viewForm.setForeground(new java.awt.Color(0, 102, 153));
        fl_viewForm.setText("View Form");
        fl_viewForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fl_viewFormActionPerformed(evt);
            }
        });

        fl_back.setBackground(new java.awt.Color(255, 255, 255));
        fl_back.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        fl_back.setForeground(new java.awt.Color(0, 102, 153));
        fl_back.setText("Back");
        fl_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fl_backActionPerformed(evt);
            }
        });

        fl_editForm.setBackground(new java.awt.Color(255, 255, 255));
        fl_editForm.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        fl_editForm.setForeground(new java.awt.Color(0, 102, 153));
        fl_editForm.setText("Edit Form");
        fl_editForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fl_editFormActionPerformed(evt);
            }
        });

        fl_addForm.setBackground(new java.awt.Color(255, 255, 255));
        fl_addForm.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        fl_addForm.setForeground(new java.awt.Color(0, 102, 153));
        fl_addForm.setText("Add Form");
        fl_addForm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fl_addFormActionPerformed(evt);
            }
        });

        jLabel44.setBackground(new java.awt.Color(255, 255, 255));
        jLabel44.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(0, 102, 153));
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setText("AVAILABLE FORMS TO REQUEST IN BARANGAY RECORDS MANAGEMENT SYSTEM");
        jLabel44.setOpaque(true);

        fl_formSetts.setBackground(new java.awt.Color(255, 255, 255));
        fl_formSetts.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        fl_formSetts.setForeground(new java.awt.Color(0, 102, 153));
        fl_formSetts.setText("Form Settings");
        fl_formSetts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fl_formSettsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout formListPanelLayout = new javax.swing.GroupLayout(formListPanel);
        formListPanel.setLayout(formListPanelLayout);
        formListPanelLayout.setHorizontalGroup(
            formListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, formListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(v_viewCitizensScrollPane2)
                    .addGroup(formListPanelLayout.createSequentialGroup()
                        .addComponent(fl_addForm, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fl_editForm, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fl_viewForm, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fl_formSetts, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fl_back, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)))
                .addContainerGap())
        );
        formListPanelLayout.setVerticalGroup(
            formListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(v_viewCitizensScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(formListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fl_viewForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fl_back, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fl_editForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fl_addForm, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fl_formSetts, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(173, Short.MAX_VALUE))
        );

        homePanel.setBackground(new java.awt.Color(0, 102, 153));
        homePanel.setPreferredSize(new java.awt.Dimension(918, 705));

        aboutPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel45.setBackground(new java.awt.Color(0, 102, 153));
        jLabel45.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(0, 102, 153));
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel45.setText("Barangay Records Management System");

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel45, javax.swing.GroupLayout.DEFAULT_SIZE, 917, Short.MAX_VALUE)
                .addContainerGap())
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel45, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addContainerGap())
        );

        claimableFormsPanel.setBackground(new java.awt.Color(255, 255, 255));

        home_viewClaimables.setBackground(new java.awt.Color(189, 195, 198));
        home_viewClaimables.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        home_viewClaimables.setForeground(new java.awt.Color(0, 102, 153));
        home_viewClaimables.setText("Claimable Forms");
        home_viewClaimables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                home_viewClaimablesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout claimableFormsPanelLayout = new javax.swing.GroupLayout(claimableFormsPanel);
        claimableFormsPanel.setLayout(claimableFormsPanelLayout);
        claimableFormsPanelLayout.setHorizontalGroup(
            claimableFormsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_viewClaimables, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                .addContainerGap())
        );
        claimableFormsPanelLayout.setVerticalGroup(
            claimableFormsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_viewClaimables, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                .addContainerGap())
        );

        claimableFormsPanel1.setBackground(new java.awt.Color(255, 255, 255));

        home_viewBday.setBackground(new java.awt.Color(189, 195, 198));
        home_viewBday.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        home_viewBday.setForeground(new java.awt.Color(0, 102, 153));
        home_viewBday.setText("Birthdays ");
        home_viewBday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                home_viewBdayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout claimableFormsPanel1Layout = new javax.swing.GroupLayout(claimableFormsPanel1);
        claimableFormsPanel1.setLayout(claimableFormsPanel1Layout);
        claimableFormsPanel1Layout.setHorizontalGroup(
            claimableFormsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_viewBday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        claimableFormsPanel1Layout.setVerticalGroup(
            claimableFormsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_viewBday, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                .addContainerGap())
        );

        claimableFormsPanel3.setBackground(new java.awt.Color(255, 255, 255));

        home_userManual.setBackground(new java.awt.Color(189, 195, 198));
        home_userManual.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        home_userManual.setForeground(new java.awt.Color(0, 102, 153));
        home_userManual.setText("User Manual");
        home_userManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                home_userManualActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout claimableFormsPanel3Layout = new javax.swing.GroupLayout(claimableFormsPanel3);
        claimableFormsPanel3.setLayout(claimableFormsPanel3Layout);
        claimableFormsPanel3Layout.setHorizontalGroup(
            claimableFormsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_userManual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        claimableFormsPanel3Layout.setVerticalGroup(
            claimableFormsPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_userManual, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                .addContainerGap())
        );

        claimableFormsPanel4.setBackground(new java.awt.Color(255, 255, 255));

        home_aboutBRMS.setBackground(new java.awt.Color(189, 195, 198));
        home_aboutBRMS.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        home_aboutBRMS.setForeground(new java.awt.Color(0, 102, 153));
        home_aboutBRMS.setText("About");
        home_aboutBRMS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                home_aboutBRMSActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout claimableFormsPanel4Layout = new javax.swing.GroupLayout(claimableFormsPanel4);
        claimableFormsPanel4.setLayout(claimableFormsPanel4Layout);
        claimableFormsPanel4Layout.setHorizontalGroup(
            claimableFormsPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_aboutBRMS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        claimableFormsPanel4Layout.setVerticalGroup(
            claimableFormsPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_aboutBRMS, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addContainerGap())
        );

        claimableFormsPanel12.setBackground(new java.awt.Color(255, 255, 255));

        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel57.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ic-logo.png"))); // NOI18N
        jLabel57.setToolTipText("");

        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Barangay_League_Logo.png"))); // NOI18N
        jLabel58.setToolTipText("");

        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Davao_City_Ph_official_seal.png"))); // NOI18N
        jLabel47.setToolTipText("");

        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel59.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo.png"))); // NOI18N
        jLabel59.setToolTipText("");

        javax.swing.GroupLayout claimableFormsPanel12Layout = new javax.swing.GroupLayout(claimableFormsPanel12);
        claimableFormsPanel12.setLayout(claimableFormsPanel12Layout);
        claimableFormsPanel12Layout.setHorizontalGroup(
            claimableFormsPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(claimableFormsPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel58, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel59, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        claimableFormsPanel12Layout.setVerticalGroup(
            claimableFormsPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, claimableFormsPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(claimableFormsPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel58, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                    .addComponent(jLabel57, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel59, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        homeBrgyTable.setBackground(new java.awt.Color(189, 195, 198));
        homeBrgyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Position", "Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        homeBrgyTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(homeBrgyTable);
        if (homeBrgyTable.getColumnModel().getColumnCount() > 0) {
            homeBrgyTable.getColumnModel().getColumn(0).setMaxWidth(400);
            homeBrgyTable.getColumnModel().getColumn(1).setMaxWidth(600);
        }

        jLabel46.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(0, 102, 153));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setText("Baranggay Officials");
        jLabel46.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        home_brgyTerm.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        home_brgyTerm.setForeground(new java.awt.Color(0, 102, 153));
        home_brgyTerm.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_brgyTerm.setText("2016-2019");
        home_brgyTerm.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel46, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(home_brgyTerm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel46)
                .addGap(3, 3, 3)
                .addComponent(home_brgyTerm)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout homePanelLayout = new javax.swing.GroupLayout(homePanel);
        homePanel.setLayout(homePanelLayout);
        homePanelLayout.setHorizontalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aboutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(homePanelLayout.createSequentialGroup()
                        .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(claimableFormsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(claimableFormsPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(claimableFormsPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(claimableFormsPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(claimableFormsPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        homePanelLayout.setVerticalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aboutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(claimableFormsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(claimableFormsPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(homePanelLayout.createSequentialGroup()
                        .addComponent(claimableFormsPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(claimableFormsPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(claimableFormsPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        jLayeredPane1.setLayer(viewCitizensPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(profilePanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(settingsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(officialsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(usersPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(calendarPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(eventPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(transactionsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(requestHistoryPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(formSelectPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(logHistoryPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(formListPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(homePanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(viewCitizensPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(profilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(officialsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(usersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(calendarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(eventPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(transactionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(requestHistoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(formSelectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap(27, Short.MAX_VALUE)
                    .addComponent(logHistoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 987, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap(26, Short.MAX_VALUE)
                    .addComponent(formListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 988, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(homePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(viewCitizensPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(profilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(officialsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(usersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(calendarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(eventPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(transactionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(requestHistoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(formSelectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addGap(22, 22, 22)
                    .addComponent(logHistoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(formListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(homePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void statusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusActionPerformed
        //LISTERNER FOR STATUS COMPONENT WHEN ITS VALUE CHANGE
        statusXspouse();
    }//GEN-LAST:event_statusActionPerformed

    private void homeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeActionPerformed
        hideAllPanels();
        homePanel.setVisible(true);
    }//GEN-LAST:event_homeActionPerformed

    private void viewCitizensActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewCitizensActionPerformed
        hideAllPanels();
        setViewCitizensData();
        vc_back.setEnabled(false);
        viewCitizensPanel.setVisible(true);
    }//GEN-LAST:event_viewCitizensActionPerformed

    private void transactionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transactionsActionPerformed
        hideAllPanels();
        transactionsPanel.setVisible(true);
        setTransactionsData();
        t_back.setEnabled(false);
    }//GEN-LAST:event_transactionsActionPerformed

    private void addCitizenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCitizenActionPerformed

        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            newSiblings = new ArrayList<>();
            newChildren = new ArrayList<>();

            siblingCount = 0;
            childrenCount = 0;
            addFunctionType = "addc";

            emptyFields();
            enableFields();
            hideAllPanels();
            statusXspouse();

            if (addTab.getTabCount() > 3) {
                addTab.removeTabAt(4); //notes
                addTab.removeTabAt(3); //request history
            }

            profileSave.setText("Save");
            dob.setDate(new Date());
            profilePanel.setVisible(true);
            jLabel42.setVisible(false);

            System.out.println(""); //new Line in log
        }


    }//GEN-LAST:event_addCitizenActionPerformed

    private void requestFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestFormActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            requestingPanel = 0; //reset to get new value
            String currName = "";
            if (requestHistoryPanel.isVisible()) { //if user is viewing a transaction and transaction owner is requestor
                hideAllPanels();
                formSelectPanel.setVisible(true);
                setFormSelectionListData(formsSelectTable);
            } else if (formSelectPanel.isVisible()) {
                JOptionPane.showMessageDialog(null, "Already Requiesting a Form!");
            } else {

                JPanel rPanel = new JPanel();
                //rPanel.setBackground(new ColorUIResource(0, 102, 153));
                JRadioButton c = new JRadioButton("Citizen");
                //c.setBackground(new ColorUIResource(0, 102, 153));
                JRadioButton g = new JRadioButton("Guest");
                //g.setBackground(new ColorUIResource(0, 102, 153));
                JRadioButton curr = null;
                ButtonGroup x = new ButtonGroup();
                x.add(c);
                x.add(g);

                if (profilePanel.isVisible() && addFunctionType.equals("editc")) { //if user is viewing profile and profile owner is requestor
                    curr = new JRadioButton("Current: " + (currName = fname.getText() + " " + lname.getText()));
                    //curr.setBackground(new ColorUIResource(0, 102, 153));
                    x.add(curr);
                    curr.setSelected(true);
                    rPanel.add(curr);
                    requestingPanel = 1;
                } else if (viewCitizensPanel.isVisible() && viewCitizensTable.getSelectedRow() > -1) { //if user is viewing citizens and selected name owner is requestor
                    curr = new JRadioButton("Current: " + (currName = viewCitizensTable.getValueAt(viewCitizensTable.getSelectedRow(), 2) + " " + viewCitizensTable.getValueAt(viewCitizensTable.getSelectedRow(), 1)));
                    //curr.setBackground(new ColorUIResource(0, 102, 153));
                    x.add(curr);
                    curr.setSelected(true);
                    rPanel.add(curr);
                    requestingPanel = 2;
                } else if (transactionsPanel.isVisible() && transactionTable.getSelectedRow() > -1) { //if user is viewing citizens and selected name owner is requestor
                    curr = new JRadioButton("Current: " + (currName = transactionTable.getValueAt(transactionTable.getSelectedRow(), 2) + " " + transactionTable.getValueAt(transactionTable.getSelectedRow(), 1)));
                    //curr.setBackground(new ColorUIResource(0, 102, 153));
                    x.add(curr);
                    curr.setSelected(true);
                    rPanel.add(curr);
                    requestingPanel = 3;
                } else {
                    c.setSelected(true);
                }

                rPanel.add(c);
                rPanel.add(g);

                while (true) {
                    int result = JOptionPane.showConfirmDialog(null, rPanel,
                            "Request Form as:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    //-----------------------SELECTED CITIZEN---------------------
                    if (result == JOptionPane.OK_OPTION) {
                        if (c.isSelected()) {
                            JPanel cpanel = new JPanel();
                            cpanel.setBackground(new ColorUIResource(0, 102, 153));
                            JRadioButton newC = new JRadioButton("New Citizen");
                            JRadioButton exC = new JRadioButton("Existing");
                            ButtonGroup y = new ButtonGroup();
                            y.add(newC);
                            y.add(exC);

                            cpanel.add(newC);
                            cpanel.add(exC);
                            newC.setSelected(true);
                            int value2 = JOptionPane.showConfirmDialog(null, cpanel,
                                    "Select Citizen Type:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                            if (value2 == JOptionPane.OK_OPTION) {
                                if (newC.isSelected()) {
                                    int v = JOptionPane.showConfirmDialog(null,
                                            "Need to create profile first! \nProceed with transaction?", null, JOptionPane.OK_CANCEL_OPTION);

                                    if (v == JOptionPane.OK_OPTION) {
                                        hideAllPanels();
                                        requesting = true;
                                        addFunctionType = "addc"; //redirect
                                        profilePanel.setVisible(true);
                                        jLabel42.setVisible(false);
                                        break;
                                    }
                                } else if (exC.isSelected()) {
                                    searchPerson.callClass("citizen", logHandler);
                                    break;
                                }
                            } else {
                                break;
                            }

                        } //-----------------------SELECTED GUEST---------------------
                        else if (g.isSelected()) { //if guest
                            JPanel gpanel = new JPanel();
                            gpanel.setBackground(new ColorUIResource(0, 102, 153));
                            JRadioButton newG = new JRadioButton("New Guest");
                            final JRadioButton exG = new JRadioButton("Existing");
                            ButtonGroup y = new ButtonGroup();
                            y.add(newG);
                            y.add(exG);

                            gpanel.add(newG);
                            gpanel.add(exG);
                            newG.setSelected(true);
                            dob.setDate(new Date());

                            int value2 = JOptionPane.showConfirmDialog(null, gpanel,
                                    "Select Guest Type:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                            if (value2 == JOptionPane.OK_OPTION) {
                                if (newG.isSelected()) { //new guest
                                    int v = JOptionPane.showConfirmDialog(null,
                                            "Need to create profile first! \nProceed with transaction?", null, JOptionPane.OK_CANCEL_OPTION);
                                    if (v == 0) {
                                        hideAllPanels();
                                        requesting = true; //to redirect to form select panel after save
                                        addFunctionType = "addg"; //save new guest record
                                        enableFields();
                                        viewGuest();
                                        jLabel42.setVisible(false);
                                        profilePanel.setVisible(true);
                                        break;
                                    }
                                } else if (exG.isSelected()) { //existing guest
                                    searchPerson.callClass("guest", logHandler);
                                    break;
                                }
                            } else {
                                break;
                            }
                        } //-----------------------SELECTED CHOSEN NAME FROM A TABLE IN A PANEL---------------------
                        else if (curr.isSelected()) {
                            currentPersonName = currName;

                            switch (requestingPanel) {
                                case 3: { //transactionPanel 
                                    currentPersonTransID = String.valueOf(transactionTable.getValueAt(transactionTable.getSelectedRow(), 0));
                                    makeRequest(currentPersonTransID, currentPersonName);
                                    break;
                                }
                                case 1: { //add panel
                                    connect.closeCon();
                                    connect = new SQLConnect();
                                    currentPersonID = pID.getText();

                                    connect.closeCon();
                                    connect = new SQLConnect();
                                    if (!connect.hasTrans(currentPersonID)) {
                                        connect.closeCon();
                                        connect = new SQLConnect();
                                        connect.storeTrans(currentPersonID);

                                        connect.closeCon();
                                        connect = new SQLConnect();
                                        currentPersonTransID = connect.getpTransID(currentPersonID);
                                        logHandler.saveLog("Created New Transaction with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName); //Viewed Transaction ID; Person ID, Name	

                                    } else {
                                        currentPersonTransID = connect.getpTransID(currentPersonID);
                                    }

                                    makeRequest(currentPersonTransID, currentPersonName);
                                    break;
                                }
                                case 2: { // view citizens panel
                                    connect.closeCon();
                                    connect = new SQLConnect();
                                    currentPersonID = String.valueOf(viewCitizensTable.getValueAt(viewCitizensTable.getSelectedRow(), 0));
                                    connect.closeCon();
                                    connect = new SQLConnect();
                                    if (!connect.hasTrans(currentPersonID)) {
                                        connect.closeCon();
                                        connect = new SQLConnect();
                                        connect.storeTrans(currentPersonID);

                                        connect.closeCon();
                                        connect = new SQLConnect();
                                        currentPersonTransID = connect.getpTransID(currentPersonID);
                                        logHandler.saveLog("Created New Transaction with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName); //Viewed Transaction ID; Person ID, Name	

                                    } else {
                                        currentPersonTransID = connect.getpTransID(currentPersonID);
                                    }
                                    makeRequest(currentPersonTransID, String.valueOf(viewCitizensTable.getValueAt(viewCitizensTable.getSelectedRow(), 2)) + " " + String.valueOf(viewCitizensTable.getValueAt(viewCitizensTable.getSelectedRow(), 1)));
                                    break;
                                }
                            }
                            break; //end loop when curr is selected
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }//GEN-LAST:event_requestFormActionPerformed

    private void calendarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarActionPerformed
        hideAllPanels();
        if (cal != null) {
            System.out.println("CALENDAR RESET");
            cal.reset();
        }
        calendarPanel.setVisible(true);
    }//GEN-LAST:event_calendarActionPerformed

    private void optionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsActionPerformed
        if (!logged_userType.equals("Level 3")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            hideAllPanels();
            settingsPanel.setVisible(true);
        }
    }//GEN-LAST:event_optionsActionPerformed

    private void profileSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileSaveActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            String msg = "Added";
            boolean print = true;
            connect.closeCon();
            connect = new SQLConnect();

            switch (addFunctionType) {
                case "addc": //WHEN USER ADDS A NEW CITIZEN
                    if ((lname.getText().length() > 0 && fname.getText().length() > 0 && address.getText().length() > 0)
                            && Pattern.matches("^[a-zA-Z]+$", fname.getText())
                            && Pattern.matches("^[a-zA-Z]+$", lname.getText())
                            && Pattern.matches("^[0-9]+$", telNum.getText())) { //required fields

                        //if (!personExist()) {
                        connect.storeNewPerson("citizen", createAddQuery("person", "none"));

                        //confirm print
                        System.out.println("Person Added..");

                        connect.closeCon();
                        connect = new SQLConnect();
                        currentPersonID = connect.getLatestPersonId();
                        currentPersonName = fname.getText() + " " + lname.getText();
                        logHandler.saveLog("Added New Citizen Profile with -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName);

                        if (requesting) {
                            connect.closeCon();
                            connect = new SQLConnect();
                            //currentPersonTransType = ""; //provided by setRequestHistory
                            if (!connect.hasTrans(currentPersonID)) { //for citizens
                                connect.storeTrans(currentPersonID);

                                connect.closeCon();
                                connect = new SQLConnect();
                                currentPersonTransID = connect.getpTransID(currentPersonID);
                                logHandler.saveLog("Created New Transaction with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName); //Viewed Transaction ID; Person ID, Name	
                            } else {
                                currentPersonTransID = connect.getpTransID(currentPersonID);
                            }

                            makeRequest(currentPersonTransID, fname.getText() + " " + lname.getText());
                        } else {
                            personInfoSaver("citizen");
                            hideAllPanels();
                            setViewCitizensData(); //refresh
                            viewCitizensPanel.setVisible(true); //call view citizens panel
                        }

                        emptyFields();
                        siblingCount = 0;
                        childrenCount = 0;
                        //}
                    } else {
                        String err = "";
                        if ((!Pattern.matches("^[a-zA-Z]+$", fname.getText()) || !Pattern.matches("^[a-zA-Z]+$", lname.getText())) && (fname.getText().length() > 0 && lname.getText().length() > 0)) {
                            err += "Names must contain letters only.";
                        } else {
                            err = "Required fields cannot be empty.";
                        }

                        if (!Pattern.matches("^[0-9]+$", telNum.getText()) && telNum.getText().length() > 0) {
                            err += "\nTelephone Number must contain numbers only";
                        }

                        JOptionPane.showMessageDialog(this, err);
                        print = false;
                    }
                    break;
                case "editc":
                    //WHEN USER VIEW's A CITIZEN PROFILE
                    enableFields();
                    printButtProfile.setVisible(false);
                    profileSave.setText("Save");
                    statusXspouse();
                    addFunctionType = "updatec";
                    print = false;
                    break;
                case "updatec": //WHEN USER UPDATE CITIZEN
                    if (lname.getText().length() > 0 && fname.getText().length() > 0 && address.getText().length() > 0) { //required fields

                        connect.closeCon();
                        connect = new SQLConnect();
                        connect.updatePerson(currentPersonID, "citizen", "person", this.createUpdateQuery("person", ""), "none");

                        msg = "Updated";
                        personInfoSaver("citizen");
                        logHandler.saveLog("Edited Citizen Profile with -Person ID = " + currentPersonID + " -Person Name = " + (currentPersonName = fname.getText() + " " + lname.getText()));
                        emptyFields();
                        siblingCount = 0;
                        childrenCount = 0;
                        addFunctionType = "editc";
                        hideAllPanels();
                        setCitizenData(currentPersonID); //refresh
                        profileSave.setText("Edit");
                        disableFields();
                        profilePanel.setVisible(true); //call view citizens panel
                        printButtProfile.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Required fields cannot be empty.");
                        print = false;
                    }
                    break;
                case "addg":

                    if (lname.getText().length() > 0 && fname.getText().length() > 0 && address.getText().length() > 0) { //required fields

                        connect.storeNewPerson("guest", createAddQuery("person", "none"));

                        connect.closeCon();
                        connect = new SQLConnect();
                        currentPersonID = connect.getLatestPersonId();
                        currentPersonName = fname.getText() + " " + lname.getText();
                        logHandler.saveLog("Added New Guest Profile with -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName);

                        if (requesting) {
                            System.out.println("Requesting for Guest..");
                            connect.closeCon();
                            connect = new SQLConnect();
                            //currentPersonTransType = ""; //provided by setRequestHistory
                            if (!connect.hasTrans(currentPersonID)) { //for citizens
                                System.out.println("New Transaction Created!");
                                connect.storeTrans(currentPersonID);

                                connect.closeCon();
                                connect = new SQLConnect();
                                currentPersonTransID = connect.getpTransID(currentPersonID);
                                logHandler.saveLog("Created New Transaction with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName); //Viewed Transaction ID; Person ID, Name	

                            } else {
                                currentPersonTransID = connect.getpTransID(currentPersonID);
                            }

                            makeRequest(currentPersonTransID, fname.getText() + " " + lname.getText());
                        } else {
                            personInfoSaver("guest");
                            System.out.println("Post-Save Methods for Guest..");
                            hideAllPanels();
                            setTransactionsData(); //refresh
                            transactionsPanel.setVisible(true); //call view citizens panel
                        }

                        emptyFields();

                    } else {
                        JOptionPane.showMessageDialog(this, "Required fields cannot be empty.");
                    }

                    //System.out.println("ADDG SAVE Functions Coming Soon");
                    break;
                case "editg":
                    enableFields();
                    printButtProfile.setVisible(false);
                    profileSave.setText("Save");
                    statusXspouse();
                    addFunctionType = "updateg";
                    print = false;
                    break;
                case "updateg":
                    msg = "Updated";
                    if (lname.getText().length() > 0 && fname.getText().length() > 0 && address.getText().length() > 0) { //required fields

                        connect.closeCon();
                        connect = new SQLConnect();
                        connect.updatePerson(currentPersonID, "guest", "person", this.createUpdateQuery("person", ""), "none");

                        msg = "Updated";
                        personInfoSaver("guest");
                        logHandler.saveLog("Edited Guest Profile with -Person ID = " + currentPersonID + " -Person Name = " + (currentPersonName = fname.getText() + " " + lname.getText()));
                        emptyFields();
                        addFunctionType = "editg";
                        hideAllPanels();
                        setCitizenData(currentPersonID); //refresh
                        profileSave.setText("Edit");
                        disableFields();
                        profilePanel.setVisible(true);

                        printButtProfile.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Required fields cannot be empty.");
                    }
                    //System.out.println("UPDATEG SAVE Functions Coming Soon");
                    break;
            }

            if (print) {
                //Log entry
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
                JOptionPane.showMessageDialog(this, "Successfully " + msg + " Record!");
            }
        }
    }//GEN-LAST:event_profileSaveActionPerformed

    private void profileCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileCancelActionPerformed

        printButtProfile.setVisible(true);
        jLabel42.setVisible(true);
        switch (addFunctionType) {
            case "addc":
                emptyFields();
                newSiblings = new ArrayList<>();
                newChildren = new ArrayList<>(); //cancel new relatives saved
                hideAllPanels();
                viewCitizensPanel.setVisible(true);

                if (requesting) {
                    requestingPanel = 3;
                    returnToPanel();
                }

                break;
            case "editc":
                //WHEN FROM VIEWING OF CITIZEN PROFILE
                hideAllPanels();
                switch (profileViewType) {

                    case "rhView": //REDIRECT TO REQUEST HISTORY
                        hideAllPanels();
                        requestHistoryPanel.setVisible(true);
                        break;

                    case "vcView": //REDIRECT TO VIEW CITIEZNS
                        hideAllPanels();
                        viewCitizensPanel.setVisible(true);
                        break;

                    case "oView": //REDIRECT TO OFFICIALS PANEL
                        hideAllPanels();
                        officialsPanel.setVisible(true);
                        break;

                    case "uView": //REDIRECT TO USERS PANEL
                        hideAllPanels();
                        usersPanel.setVisible(true);
                        break;

                    case "bdayView":
                        birthday.callClass();
                        hideAllPanels();
                        homePanel.setVisible(true);
                        break;
                    default:
                        break;
                }
                break;
            case "updatec":
                disableFields();
                profileViewType = "vcView";
                siblingCount = 0;
                childrenCount = 0;
                viewProf("citizen");
                break;
            case "addg":
            case "editg":
                addTab.add(jPanel2);
                addTab.add(educ);
                addTab.setTitleAt(1, "Family Background");
                addTab.setTitleAt(2, "Educational Background");
                requestingPanel = 3; //transaction Panel
                returnToPanel();
                break;
            case "updateg":
                addFunctionType = "editg";
                disableFields();
                setCitizenData(currentPersonID);
                profileSave.setText("Edit");
                profilePanel.setVisible(true);
                break;

        }
    }//GEN-LAST:event_profileCancelActionPerformed

    private void vc_viewProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vc_viewProfileButtonActionPerformed
        if (viewCitizensTable.getSelectedRow() >= 0) {
            currentPersonID = String.valueOf(viewCitizensTable.getValueAt(viewCitizensTable.getSelectedRow(), 0));
            profileViewType = "vcView";
            siblingCount = 0;
            childrenCount = 0;
            viewProf("citizen");
            logHandler.saveLog("Viewed Person Profile via View Citizens Panel with -Person ID: " + currentPersonID + "-Person Name: " + currentPersonName); //param 1 is Action
        }

    }//GEN-LAST:event_vc_viewProfileButtonActionPerformed

    private void vc_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vc_backActionPerformed
        vc_back.setEnabled(false);
        searchField.setText("");
        setModelAndClearModelItems(viewCitizensTable);
        setViewCitizensData();
    }//GEN-LAST:event_vc_backActionPerformed

    private void vc_searchCitizenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vc_searchCitizenActionPerformed
        vc_search();
    }//GEN-LAST:event_vc_searchCitizenActionPerformed

    private void vc_filterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vc_filterActionPerformed
        JPanel rPanel = new JPanel();
        JRadioButton n = new JRadioButton("No Filter");
        JRadioButton g = new JRadioButton("Gender");
        JRadioButton b = new JRadioButton("Birthday");
        JRadioButton cs = new JRadioButton("Civil Status");
        JRadioButton key = new JRadioButton("Keyword Search");
        ButtonGroup x = new ButtonGroup();
        x.add(n);
        x.add(g);
        x.add(b);
        x.add(cs);
        x.add(key);
        n.setSelected(true);
        rPanel.add(n);
        rPanel.add(g);
        rPanel.add(b);
        rPanel.add(cs);
        rPanel.add(key);

        int result = JOptionPane.showConfirmDialog(null, rPanel,
                "Choose Filter", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        //-----------------------SELECTED CITIZEN---------------------
        if (result == JOptionPane.OK_OPTION) {
            if (n.isSelected()) {
                setViewCitizensData();
            } else if (g.isSelected()) {

                JPanel rGPanel = new JPanel();
                JComboBox jcb = new JComboBox(new String[]{"Male", "Female"});
                rGPanel.add(jcb);

                int gresult = JOptionPane.showConfirmDialog(null, rGPanel,
                        "Select Gender to Display", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (gresult == JOptionPane.OK_OPTION) {
                    vc_back.setEnabled(true);

                    connect.closeCon();
                    connect = new SQLConnect();
                    setModelAndClearModelItems(viewCitizensTable);
                    ResultSet rs = null;
                    switch (jcb.getSelectedItem().toString()) {
                        case "Male": {
                            rs = connect.getPersonViaGender("citizen", "Male");
                            break;
                        }
                        case "Female":
                            rs = connect.getPersonViaGender("citizen", "Female");
                            break;
                    }

                    try {
                        for (int q = 0; rs.next(); q++) {
                            model.insertRow(q, new String[]{String.valueOf(rs.getInt("personID")), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
                        }
                    } catch (SQLException ex) {
                        System.out.println("Error: " + ex);
                    }
                }

            } else if (b.isSelected()) {
                JPanel rGPanel = new JPanel();

                String[] months = {"--", "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
                JComboBox jcm = new JComboBox(months);
                jcm.setSelectedIndex(0);

                String[] days = new String[32];
                days[0] = "--";
                for (int m = 1; m <= 31; m++) {
                    days[m] = String.valueOf(m);
                }
                JComboBox jcd = new JComboBox(days);
                jcd.setSelectedIndex(0);
                JLabel jl1 = new JLabel(" ");
                JLabel jl2 = new JLabel(" ");
                JTextField jtx = new JTextField();
                jtx.setText("  2016"); //has spaces; trim

                rGPanel.add(jcm);
                rGPanel.add(jl1);
                rGPanel.add(jcd);
                rGPanel.add(jl2);
                rGPanel.add(jtx);

                int bresult = JOptionPane.showConfirmDialog(null, rGPanel,
                        "Select Birthdate of Persons to Display", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (bresult == JOptionPane.OK_OPTION) {
                    vc_back.setEnabled(true);

                    connect.closeCon();
                    connect = new SQLConnect();
                    setModelAndClearModelItems(viewCitizensTable);
                    ResultSet rs = null;
                    String mo = String.valueOf(jcm.getSelectedIndex());
                    String da = String.valueOf(jcd.getSelectedIndex());
                    String ye = jtx.getText();
                    boolean yearLegit = true;

                    if (jcm.getSelectedIndex() < 10) {
                        mo = "0" + mo;
                    }

                    if (jcd.getSelectedIndex() < 10) {
                        da = "0" + da;
                    }

                    if (!ye.equals("")) {
                        int l = 0;
                        ye = ye.trim();
                        while (l < ye.length()) {
                            if (!Character.isDigit(ye.charAt(l))) {
                                yearLegit = false;
                                break;
                            }
                            l++;
                        }

                        if (yearLegit) {
                            ye = ye.substring(ye.length() - 2, ye.length());
                        }
                    }

                    if (jcm.getSelectedIndex() != 0 && jcd.getSelectedIndex() != 0 && yearLegit) { //V
                        //MDY
                        rs = connect.getPersonBday("citizen", mo + "-" + da + "-" + ye, "MDY");
                    } else if (jcm.getSelectedIndex() != 0 && jcd.getSelectedIndex() != 0 && !yearLegit) { //V 
                        //MD
                        rs = connect.getPersonBday("citizen", mo + "-" + da + "-", "MD");
                    } else if (jcm.getSelectedIndex() == 0 && jcd.getSelectedIndex() != 0 && yearLegit) { //V
                        //DY
                        rs = connect.getPersonBday("citizen", "-" + da + "-" + ye, "DY");
                    } else if (jcm.getSelectedIndex() != 0 && jcd.getSelectedIndex() == 0 && yearLegit) { //V
                        //MY
                        String temp = mo + "-/-" + ye;
                        rs = connect.getPersonBday("citizen", temp, "MY");
                    } else if (jcm.getSelectedIndex() != 0 && jcd.getSelectedIndex() == 0 && !yearLegit) {
                        //M
                        rs = connect.getPersonBday("citizen", mo + "-", "M");
                    } else if (jcm.getSelectedIndex() == 0 && jcd.getSelectedIndex() != 0 && !yearLegit) {
                        //D
                        rs = connect.getPersonBday("citizen", "-" + da + "-", "D");
                    } else if (jcm.getSelectedIndex() == 0 && jcd.getSelectedIndex() == 0 && yearLegit) {
                        //Y
                        rs = connect.getPersonBday("citizen", "-" + ye, "Y");
                    }

                    try {
                        for (int q = 0; rs.next(); q++) {
                            model.insertRow(q, new String[]{String.valueOf(rs.getInt("personID")), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
                        }
                    } catch (SQLException ex) {
                        System.out.println("Error: " + ex);
                    }
                }

            } else if (cs.isSelected()) {
                String stats[] = {"Single", "Married", "Separated", "Widowed", "Annuled"};
                JPanel rCsPanel = new JPanel();
                JComboBox jcb = new JComboBox(stats);
                rCsPanel.add(jcb);

                int csresult = JOptionPane.showConfirmDialog(null, rCsPanel,
                        "Select Civil Status to Display", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (csresult == JOptionPane.OK_OPTION) {
                    vc_back.setEnabled(true);

                    connect.closeCon();
                    connect = new SQLConnect();
                    setModelAndClearModelItems(viewCitizensTable);
                    ResultSet rs = connect.getAllCitizenPersons();
                    int rowCounter = 0;
                    try {
                        for (int q = 0; rs.next(); q++) {
                            connect = new SQLConnect();
                            if (connect.personHas(String.valueOf(rs.getInt("personID")), "personaddinfo")) {
                                connect = new SQLConnect();

                                ResultSet sd = connect.getPersonInfo("personAddInfo", String.valueOf(rs.getInt("personID")), "none");
                                if (sd.next() && (sd.getString("status").equals(jcb.getSelectedItem()))) {
                                    model.insertRow(rowCounter++, new String[]{String.valueOf(rs.getInt("personID")), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
                                }
                            }
                        }
                    } catch (SQLException ex) {
                        System.out.println("Error: " + ex);
                    }
                }
            } else if (key.isSelected()) {
                //get input string from joptionmessage
                String seacrhSTR = JOptionPane.showInputDialog(this, "Enter Keyword:", "Find Citizen Via Keyword", JOptionPane.INFORMATION_MESSAGE);
                if (seacrhSTR.length() > 0) {
                    connect = new SQLConnect(); //toDelete
                    ArrayList<ArrayList<String>> dataAll = connect.searchPersonViaKeyword(seacrhSTR);
                    setModelAndClearModelItems(viewCitizensTable);
                    int rowCounter = 0;
                    while (rowCounter < dataAll.size()) {
                        ArrayList<String> data = dataAll.get(rowCounter);
                        model.insertRow(rowCounter++, new String[]{data.get(0), data.get(1), data.get(2), data.get(3)});
                    }
                    vc_back.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Number of Citizens Found: " + dataAll.size(), "Filter Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a key word!", "Input Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_vc_filterActionPerformed

    private void tableOfficialsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableOfficialsMouseReleased
        if (tableOfficials.getSelectedRow() >= 0) {
            if (String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 1)).equals("null")) {
                off_editOfficial.setText("Add");
            } else {
                off_editOfficial.setText("Edit");
            }
        }
    }//GEN-LAST:event_tableOfficialsMouseReleased

    private void off_createNewAdminButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_off_createNewAdminButtonActionPerformed
        newAdminCreate.callClass(tableOfficials, logHandler);
    }//GEN-LAST:event_off_createNewAdminButtonActionPerformed

    private void off_viewProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_off_viewProfileButtonActionPerformed
        if (tableOfficials.getSelectedRow() >= 0 && !String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 1)).equals("null")) {
            currentPersonID = officialPersonIDs.get(tableOfficials.getSelectedRow());
            profileViewType = "oView";
            viewProf("citizen");
            String offid = officialIDs.get(tableOfficials.getSelectedRow());
            currentPersonName = String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 1));
            logHandler.saveLog("Viewed Official Person Profile via Officials Panel with -Administration ID: " + activeAdminId
                    + " -Official ID: " + offid
                    + " -Official Position: " + String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 0))
                    + " -Official Person ID: " + currentPersonID
                    + " -Official Person Name: " + currentPersonName); //param 1 is Action
        }
    }//GEN-LAST:event_off_viewProfileButtonActionPerformed

    private void off_editOfficialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_off_editOfficialActionPerformed
        int officialIndex;
        if (tableOfficials.getSelectedRow() >= 0) {
            if (off_editOfficial.getText().equals("Edit")) {
                officialIndex = tableOfficials.getSelectedRow();
                String officialName = String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 1));
                officialSelection.callClass(String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 0)), officialIDs.get(officialIndex), officialName, String.valueOf(activeAdminId), tableOfficials);
            } else if (off_editOfficial.getText().equals("Add")) {
                officialSelection.callClass(String.valueOf(tableOfficials.getValueAt(tableOfficials.getSelectedRow(), 0)), String.valueOf(activeAdminId), tableOfficials);
            }
        }
    }//GEN-LAST:event_off_editOfficialActionPerformed

    private void off_backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_off_backButtonActionPerformed
        hideAllPanels();
        settingsPanel.setVisible(true);
    }//GEN-LAST:event_off_backButtonActionPerformed

    private void off_prevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_off_prevActionPerformed
        prevnextcounter--;
        nextFlag = false;
        off_editOfficial.setEnabled(false);
        if (prevnextcounter == activeAdminId) {
            loadCurrentAdmin(tableOfficials);
        } else {
            setModelAndClearModelItems(tableOfficials);
            loadAdmin(prevnextcounter);
        }
    }//GEN-LAST:event_off_prevActionPerformed

    private void off_nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_off_nextActionPerformed
        prevnextcounter++;
        prevFlag = false;
        off_editOfficial.setEnabled(false); //cannot edit past admins; only current
        if (prevnextcounter == activeAdminId) {
            loadCurrentAdmin(tableOfficials);
        } else {
            setModelAndClearModelItems(tableOfficials);
            loadAdmin(prevnextcounter);
        }
    }//GEN-LAST:event_off_nextActionPerformed

    private void tableUsersMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableUsersMouseReleased
        if (tableUsers.getSelectedRow() >= 0) {
            if (tableUsers.getValueAt(tableUsers.getSelectedRow(), 4).equals("active")) {
                use_deact.setText("Deactivate User");
            } else if (tableUsers.getValueAt(tableUsers.getSelectedRow(), 4).equals("inactive")) {
                use_deact.setText("Activate User");
            }
        }
    }//GEN-LAST:event_tableUsersMouseReleased

    private void use_deactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_deactActionPerformed
        if (tableUsers.getSelectedRow() >= 0) { //IF ITEM IS SELECTED FROM TABLE
            String newStat = "";
            currentPersonID = String.valueOf(tableUsers.getValueAt(tableUsers.getSelectedRow(), 0));
            if (tableUsers.getValueAt(tableUsers.getSelectedRow(), 4).equals("active")) {
                newStat = "inactive";
            } else if (tableUsers.getValueAt(tableUsers.getSelectedRow(), 4).equals("inactive")) {
                newStat = "active";
            }

            connect.closeCon();
            connect = new SQLConnect();
            connect.changeUserStatus(currentPersonID, newStat);

            try {
                currentPersonID = usersPersonIDs.get(tableUsers.getSelectedRow());
                currentPersonName = String.valueOf(tableUsers.getValueAt(tableUsers.getSelectedRow(), 1));
                String userId = String.valueOf(tableUsers.getValueAt(tableUsers.getSelectedRow(), 0));

                SQLConnect tc = new SQLConnect();
                ResultSet rs = tc.getOffIDByUserID(userId);

                logHandler.saveLog("Deactivated User with"
                        + " -User ID: " + userId
                        + " -Official ID: " + rs.getInt("officialID")
                        + " -Official Person ID: " + currentPersonID
                        + " -Official Person Name: " + currentPersonName); //param 1 is Action}
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            loadUsers();
        }

    }//GEN-LAST:event_use_deactActionPerformed

    private void use_viewProfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_viewProfActionPerformed
        if (tableUsers.getSelectedRow() >= 0) {
            try {
                currentPersonID = usersPersonIDs.get(tableUsers.getSelectedRow());
                String userId = String.valueOf(tableUsers.getValueAt(tableUsers.getSelectedRow(), 0));
                profileViewType = "uView";
                viewProf("citizen");

                SQLConnect tc = new SQLConnect();
                ResultSet rs = tc.getOffIDByUserID(userId);

                logHandler.saveLog("Viewed User Person Profile via Users Panel with"
                        + " -User ID: " + userId
                        + " -Official ID: " + rs.getInt("officialID")
                        + " -Official Person ID: " + currentPersonID
                        + " -Official Person Name: " + currentPersonName); //param 1 is Action}
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_use_viewProfActionPerformed

    private void use_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_editActionPerformed
        if (tableUsers.getSelectedRow() >= 0) {
            currentPersonID = String.valueOf(tableUsers.getValueAt(tableUsers.getSelectedRow(), 0));
            currentPersonName = String.valueOf(tableUsers.getValueAt(tableUsers.getSelectedRow(), 1));
            System.out.println("userID: " + this.currentPersonID);
            createUser.callClassEdit(currentPersonID, "update");
            //String adminID, String position, String personID, String personName
            createUser.setLogDetails(currentPersonID, currentPersonName);
        }
    }//GEN-LAST:event_use_editActionPerformed

    private void use_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_backActionPerformed
        hideAllPanels();
        settingsPanel.setVisible(true);
    }//GEN-LAST:event_use_backActionPerformed

    private void use_newUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_use_newUserActionPerformed
        connect.closeCon();
        connect = new SQLConnect();
        userSelection.callClass(String.valueOf(connect.getActiveAdminId()));
    }//GEN-LAST:event_use_newUserActionPerformed

    private void childAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childAddActionPerformed
        relativeType = "Child";
        ant.setTitle("Child");
        ant.setVisible(true);
        this.setEnabled(false);
    }//GEN-LAST:event_childAddActionPerformed

    private void siblingAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_siblingAddActionPerformed
        //CALLS A DIFF CLASS THAT DISPLAYS A FORM THAT ADDS SIBLINGS
        relativeType = "Sibling";
        ant.setTitle("Sibling");
        ant.setVisible(true);
        this.setEnabled(false);
    }//GEN-LAST:event_siblingAddActionPerformed

    private void viewDayEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDayEventActionPerformed
        hideAllPanels();
        eventPanel.setVisible(true);
        viewEventsOnThisDay();
    }//GEN-LAST:event_viewDayEventActionPerformed

    private void backToCalendarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backToCalendarActionPerformed
        hideAllPanels();
        calendarPanel.setVisible(true);
    }//GEN-LAST:event_backToCalendarActionPerformed

    private void addNewEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewEventActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            nev.setDate(cal.getMonthSelected(), calendarDaySelected, cal.getYearSelected());
            nev.setVisible(true);
        }
    }//GEN-LAST:event_addNewEventActionPerformed

    private void t_viewRequestHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_viewRequestHistoryActionPerformed
        if (transactionTable.getSelectedRow() >= 0) {
            currentPersonTransID = String.valueOf(transactionTable.getValueAt(transactionTable.getSelectedRow(), 0));
            setRequestHistoryData(currentPersonTransID, String.valueOf(transactionTable.getValueAt(transactionTable.getSelectedRow(), 1)) + ", " + String.valueOf(transactionTable.getValueAt(transactionTable.getSelectedRow(), 2)));
            logHandler.saveLog("Viewed Transaction with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName); //Viewed Transaction ID; Person ID, Name	
            hideAllPanels();
            requestHistoryPanel.setVisible(true);
        }
    }//GEN-LAST:event_t_viewRequestHistoryActionPerformed

    private void t_searchTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_searchTransActionPerformed
        trans_search();
    }//GEN-LAST:event_t_searchTransActionPerformed

    private void t_filterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_filterActionPerformed
        JPanel rPanel = new JPanel();
        JRadioButton a = new JRadioButton("All");
        JRadioButton c = new JRadioButton("Citizen");
        JRadioButton g = new JRadioButton("Guest");

        JRadioButton d = new JRadioButton("Date");
        JRadioButton f = new JRadioButton("Form");
        JRadioButton k = new JRadioButton("Keyword");

        ButtonGroup x = new ButtonGroup();
        x.add(a);
        x.add(c);
        x.add(g);
        x.add(d);
        x.add(f);
        x.add(k);

        a.setSelected(true);

        rPanel.add(a);
        rPanel.add(c);
        rPanel.add(g);
        rPanel.add(d);
        rPanel.add(f);
        rPanel.add(k);

        int result = JOptionPane.showConfirmDialog(null, rPanel,
                "Filter Display", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        //-----------------------SELECTED CITIZEN---------------------
        if (result == JOptionPane.OK_OPTION) {
            if (a.isSelected()) {
                setTransactionsData();
            } else if (c.isSelected()) {
                setCitizenTransactionData();
            } else if (g.isSelected()) {
                setGuestTransactionsData();
            } else if (d.isSelected()) {
                JPanel rCsPanel = new JPanel();
                JLabel jl = new JLabel("Select Date: ");
                org.jdesktop.swingx.JXDatePicker dater = new org.jdesktop.swingx.JXDatePicker();
                rCsPanel.add(jl);
                rCsPanel.add(dater);

                int csresult = JOptionPane.showConfirmDialog(null, rCsPanel,
                        "Select Transactions Via Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (csresult == JOptionPane.OK_OPTION) {
                    connect = new SQLConnect(); //toDelete
                    SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yy");
                    String formattedDate = formatter.format(dater.getDate());
                    ArrayList<ArrayList<String>> dataAll = connect.searchTransactionViaWhere(formattedDate, "date");
                    setModelAndClearModelItems(transactionTable);

                    int rowCounter = 0;
                    while (rowCounter < dataAll.size()) {
                        ArrayList<String> data = dataAll.get(rowCounter);
                        model.insertRow(rowCounter++, new String[]{data.get(0), data.get(1), data.get(2), data.get(3)});
                    }

                    t_back.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Number of Transactions Found: " + dataAll.size(), "Filter Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a key word!", "Input Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } else if (k.isSelected()) {
                String seacrhSTR = JOptionPane.showInputDialog(this, "Enter Keyword:", "Find Transaction Via Keyword", JOptionPane.INFORMATION_MESSAGE);
                if (seacrhSTR.length() > 0) {
                    connect = new SQLConnect(); //toDelete
                    ArrayList<ArrayList<String>> dataAll = connect.searchTransactionViaWhere(seacrhSTR, "keyword");
                    setModelAndClearModelItems(transactionTable);
                    int rowCounter = 0;
                    while (rowCounter < dataAll.size()) {
                        ArrayList<String> data = dataAll.get(rowCounter);
                        model.insertRow(rowCounter++, new String[]{data.get(0), data.get(1), data.get(2), data.get(3)});
                    }
                    t_back.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Number of Transactions Found: " + dataAll.size(), "Filter Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a key word!", "Input Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } else if (f.isSelected()) {
                JPanel rCsPanel = new JPanel();
                JLabel jl = new JLabel("Select Form Name: ");
                connect = new SQLConnect(); //toDelete
                ArrayList<ArrayList<String>> formAll = connect.getAllFormsArrayList();
                ArrayList<String> formIDs = new ArrayList<>();
                String[] formNames = new String[formAll.size()];

                for (int q = 0; q < formAll.size(); q++) {
                    formIDs.add(formAll.get(q).get(0));
                    formNames[q] = formAll.get(q).get(1);
                }

                JComboBox jcb = new JComboBox(formNames);
                rCsPanel.add(jl);
                rCsPanel.add(jcb);

                int csresult = JOptionPane.showConfirmDialog(null, rCsPanel,
                        "Select Transactions Via Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (csresult == JOptionPane.OK_OPTION) {
                    connect = new SQLConnect(); //toDelete
                    ArrayList<ArrayList<String>> dataAll = connect.searchTransactionViaWhere(formIDs.get(jcb.getSelectedIndex()), "form");
                    setModelAndClearModelItems(transactionTable);

                    int rowCounter = 0;
                    while (rowCounter < dataAll.size()) {
                        ArrayList<String> data = dataAll.get(rowCounter);
                        model.insertRow(rowCounter++, new String[]{data.get(0), data.get(1), data.get(2), data.get(3)});
                    }

                    t_back.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Number of Transactions Found: " + dataAll.size(), "Filter Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a key word!", "Input Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

    }//GEN-LAST:event_t_filterActionPerformed

    private void t_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_backActionPerformed
        t_back.setEnabled(false);
        t_searchField.setText("");
        setModelAndClearModelItems(transactionTable);
        setTransactionsData();
    }//GEN-LAST:event_t_backActionPerformed

    private void rh_viewProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rh_viewProfileActionPerformed
        profileViewType = "rhView";
        siblingCount = 0;
        childrenCount = 0;
        viewProf(rh_type.getText().toLowerCase());
        logHandler.saveLog("Viewed Person Profile via Request History Panel with -Person ID: " + currentPersonID + "-Person Name: " + currentPersonName); //param 1 is Action
    }//GEN-LAST:event_rh_viewProfileActionPerformed

    private void rh_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rh_backActionPerformed
        if (!profileViewType.equals("claimView")) {
            hideAllPanels();
            setTransactionsData(); //in-case Request -> Existing/New -> Form Select --Back-> Request History -> Transaction Table
            t_back.setEnabled(false);
            transactionsPanel.setVisible(true);
        } else {
            claimables.callClass();
            hideAllPanels();
            homePanel.setVisible(true);
        }
    }//GEN-LAST:event_rh_backActionPerformed

    private void rh_requestFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rh_requestFormActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            hideAllPanels();
            formSelectPanel.setVisible(true);
            setFormSelectionListData(formsSelectTable);
        }
    }//GEN-LAST:event_rh_requestFormActionPerformed

    private void rh_claimFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rh_claimFormActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else if (requestHistoryTable.getSelectedRow() >= 0) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("HH:mm:ss");
            String timeClaimed = sdf.format(c.getTime());

            sdf = new SimpleDateFormat("MM-dd-yy");
            String dateClaimed = sdf.format(new Date());

            connect.closeCon();
            connect = new SQLConnect();
            connect.claimForm(currentPersonTransID, String.valueOf(requestHistoryTable.getValueAt(requestHistoryTable.getSelectedRow(), 0)), timeClaimed, dateClaimed);
            logHandler.saveLog("Claimed Requested Form with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " - Person Name: " + currentPersonName + " -Request Form ID: " + String.valueOf(requestHistoryTable.getValueAt(requestHistoryTable.getSelectedRow(), 0)) + " -Requested Form Name: " + String.valueOf(requestHistoryTable.getValueAt(requestHistoryTable.getSelectedRow(), 1)));

            JOptionPane.showMessageDialog(null, "Successfully Claimed Form!");
            refreshRequestHistory(requestHistoryTable);
        }
    }//GEN-LAST:event_rh_claimFormActionPerformed

    private void f_SelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f_SelectActionPerformed
        if (formsSelectTable.getSelectedRow() >= 0) {
            requestedFormID = String.valueOf(formsSelectTable.getValueAt(formsSelectTable.getSelectedRow(), 0));
            requestedFormName = String.valueOf(formsSelectTable.getValueAt(formsSelectTable.getSelectedRow(), 1));

            //read form forms.tb and place in temp folder
            readBlob.setFileName(requestedFormName);
            readBlob.read();

            //hideAllPanels();
            browser.setVisible(true);
            browser.navigate(currentPersonTransID, requestedFormName, requestedFormID);

            hideAllPanels();
            requestHistoryPanel.setVisible(true);
            //display JPanel with PDF form
            logHandler.saveLog("Selected Form to Request with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName + " -Form ID: " + requestedFormID + " -Form Name: " + requestedFormName); //Viewed Transaction ID; Person ID, Name	

        }
    }//GEN-LAST:event_f_SelectActionPerformed

    private void f_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f_backActionPerformed
        hideAllPanels();
        returnToPanel();
    }//GEN-LAST:event_f_backActionPerformed

    private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        logOutHideThis();
        logHandler.saveLog("Logged Out with -User ID: " + logged_userID + " -User Name: " + logged_username);
        logIn.callClass();
        System.out.println("\n*********************************************");
        System.out.println("LOG OUT SUCCESSFUL!");
        System.out.println("*********************************************\n");
    }//GEN-LAST:event_logoutActionPerformed

    private void rh_viewFromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rh_viewFromActionPerformed
        if (requestHistoryTable.getSelectedRow() >= 0) {
            requestedFormID = String.valueOf(requestHistoryTable.getValueAt(requestHistoryTable.getSelectedRow(), 0));
            requestedFormName = String.valueOf(requestHistoryTable.getValueAt(requestHistoryTable.getSelectedRow(), 1));

            connect = new SQLConnect();
            int formID = connect.getFormEntryFormID(requestedFormID);
            System.out.println("" + formID);

            //read form forms.tb and place in temp folder
            readBlob.setFileName(requestedFormName);
            readBlob.readForFormDisplay("" + formID);

            browser.setDataToPdf(requestedFormName, requestedFormID);

            //hideAllPanels();
            browser.setVisible(true);
            browser.navigate(requestedFormName, "printable"); //form name, folder name
            //display JPanel with PDF form

            //RequestFormBrowser rfb = new RequestFormBrowser(this, screenSize, "SAVE", requestedFormName);
            logHandler.saveLog("Viewed Requested Form with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " - Person Name: " + currentPersonName + " -Request Form ID: " + requestedFormID + " -Requested Form Name: " + requestedFormName);
        }
    }//GEN-LAST:event_rh_viewFromActionPerformed

    private void requestHistoryTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_requestHistoryTableMouseReleased
        if (requestHistoryTable.getSelectedRow() >= 0) {
            if (requestHistoryTable.getValueAt(requestHistoryTable.getSelectedRow(), 5).equals("N/A; N/A")) {
                rh_claimForm.setEnabled(true);
            } else {
                rh_claimForm.setEnabled(false);
            }
        }
    }//GEN-LAST:event_requestHistoryTableMouseReleased

    private void lh_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lh_backActionPerformed
        hideAllPanels();
        settingsPanel.setVisible(true);
    }//GEN-LAST:event_lh_backActionPerformed

    private void fl_viewFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fl_viewFormActionPerformed
        if (formListTable.getSelectedRow() >= 0) {
            requestedFormID = String.valueOf(formListTable.getValueAt(formListTable.getSelectedRow(), 0));
            requestedFormName = String.valueOf(formListTable.getValueAt(formListTable.getSelectedRow(), 1));

            //read form forms.tb and place in temp folder
            readBlob.setFileName(requestedFormName);
            readBlob.readForFormDisplay(requestedFormID);

            //hideAllPanels();
            browser.setVisible(true);
            browser.navigate(requestedFormName, "template"); //form name, folder name
            //display JPanel with PDF form
            logHandler.saveLog("Viewed Form with -Form ID: " + requestedFormID + " -Form Name: " + requestedFormName);

        }
    }//GEN-LAST:event_fl_viewFormActionPerformed

    private void fl_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fl_backActionPerformed
        hideAllPanels();
        settingsPanel.setVisible(true);
    }//GEN-LAST:event_fl_backActionPerformed

    private void fl_editFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fl_editFormActionPerformed
        if (formListTable.getSelectedRow() >= 0) {
            requestedFormID = String.valueOf(formListTable.getValueAt(formListTable.getSelectedRow(), 0));
            requestedFormName = String.valueOf(formListTable.getValueAt(formListTable.getSelectedRow(), 1));
            browser.dumbNavigate();
            formEditor.callClass(requestedFormID, requestedFormName, "***", String.valueOf(formListTable.getValueAt(formListTable.getSelectedRow(), 3)), logged_userID); //*** means hidden
            //display JPanel with PDF form
        }
    }//GEN-LAST:event_fl_editFormActionPerformed

    private void fl_addFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fl_addFormActionPerformed
        browser.dumbNavigate();
        formEditor.callClass(logged_userID);
    }//GEN-LAST:event_fl_addFormActionPerformed

    private void home_viewClaimablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_home_viewClaimablesActionPerformed
        claimables.callClass();
    }//GEN-LAST:event_home_viewClaimablesActionPerformed

    private void home_viewBdayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_home_viewBdayActionPerformed
        birthday.callClass();
    }//GEN-LAST:event_home_viewBdayActionPerformed

    private void home_userManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_home_userManualActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_home_userManualActionPerformed

    private void home_aboutBRMSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_home_aboutBRMSActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_home_aboutBRMSActionPerformed

    private void set_viewFormsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_viewFormsActionPerformed
        hideAllPanels();
        setFormSelectionListData(formListTable);
        formListPanel.setVisible(true);
    }//GEN-LAST:event_set_viewFormsActionPerformed

    private void set_viewOfficialsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_viewOfficialsActionPerformed
        hideAllPanels();
        nextFlag = true;
        loadCurrentAdmin(tableOfficials);
        officialsPanel.setVisible(true);
    }//GEN-LAST:event_set_viewOfficialsActionPerformed

    private void set_viewUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_viewUsersActionPerformed
        hideAllPanels();
        loadUsers();
        usersPanel.setVisible(true);
    }//GEN-LAST:event_set_viewUsersActionPerformed

    private void set_viewLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_viewLogActionPerformed
        hideAllPanels();
        logHandler.saveLog("Viewed Log History");
        setLogHistoryData();
        logHistoryPanel.setVisible(true);
    }//GEN-LAST:event_set_viewLogActionPerformed

    private void profile_rhTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_profile_rhTableMouseReleased
        if (profile_rhTable.getSelectedRow() >= 0) {
            if (profile_rhTable.getValueAt(profile_rhTable.getSelectedRow(), 5).equals("N/A; N/A")) {
                profile_claimForm.setEnabled(true);
            } else {
                profile_claimForm.setEnabled(false);
            }
        }
    }//GEN-LAST:event_profile_rhTableMouseReleased

    private void profile_requestFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_requestFormActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else {
            hideAllPanels();
            requestingPanel = 4;
            formSelectPanel.setVisible(true);
            setFormSelectionListData(formsSelectTable);
        }
    }//GEN-LAST:event_profile_requestFormActionPerformed

    private void profile_viewFromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_viewFromActionPerformed
        if (profile_rhTable.getSelectedRow() >= 0) {
            requestedFormID = String.valueOf(profile_rhTable.getValueAt(profile_rhTable.getSelectedRow(), 0));
            requestedFormName = String.valueOf(profile_rhTable.getValueAt(profile_rhTable.getSelectedRow(), 1));

            connect = new SQLConnect();
            int formID = connect.getFormEntryFormID(requestedFormID);
            System.out.println("" + formID);

            //read form forms.tb and place in temp folder
            readBlob.setFileName(requestedFormName);
            readBlob.readForFormDisplay("" + formID);

            browser.setDataToPdf(requestedFormName, requestedFormID);

            //hideAllPanels();
            browser.setVisible(true);
            browser.navigate(requestedFormName, "printable"); //form name, folder name
            //display JPanel with PDF form

            //RequestFormBrowser rfb = new RequestFormBrowser(this, screenSize, "SAVE", requestedFormName);
            logHandler.saveLog("Viewed Requested Form with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " - Person Name: " + currentPersonName + " -Request Form ID: " + requestedFormID + " -Requested Form Name: " + requestedFormName);
        }
    }//GEN-LAST:event_profile_viewFromActionPerformed

    private void profile_claimFormActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_claimFormActionPerformed
        if (logged_userType.equals("Level 1")) {
            JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
        } else if (profile_rhTable.getSelectedRow() >= 0) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("HH:mm:ss");
            String timeClaimed = sdf.format(c.getTime());

            sdf = new SimpleDateFormat("MM-dd-yy");
            String dateClaimed = sdf.format(new Date());

            connect.closeCon();
            connect = new SQLConnect();
            connect.claimForm(currentPersonTransID, String.valueOf(profile_rhTable.getValueAt(profile_rhTable.getSelectedRow(), 0)), timeClaimed, dateClaimed);
            logHandler.saveLog("Claimed Requested Form with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " - Person Name: " + currentPersonName + " -Request Form ID: " + String.valueOf(profile_rhTable.getValueAt(profile_rhTable.getSelectedRow(), 0)) + " -Requested Form Name: " + String.valueOf(profile_rhTable.getValueAt(profile_rhTable.getSelectedRow(), 1)));

            JOptionPane.showMessageDialog(null, "Successfully Claimed Form!");
            refreshRequestHistory(profile_rhTable);
        }
    }//GEN-LAST:event_profile_claimFormActionPerformed

    private void profile_addNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_addNoteActionPerformed
        vnote.callClass(1, this.currentPersonTransID, "", connect);
    }//GEN-LAST:event_profile_addNoteActionPerformed

    private void profile_viewNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_viewNoteActionPerformed
        vnote.callClass(2, this.currentPersonTransID, profile_notesTable.getValueAt(profile_notesTable.getSelectedRow(), 0).toString(), connect);
    }//GEN-LAST:event_profile_viewNoteActionPerformed

    private void set_importActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_importActionPerformed
        imprt.callClass(connect);
    }//GEN-LAST:event_set_importActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        pdf.callClass(0, "List of Barangay Residents", viewCitizensTable);
        pdf.openBrowser(browser);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void t_viewRequestHistory1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_t_viewRequestHistory1ActionPerformed
        pdf.callClass(1, "List of Barangay Transactions", transactionTable);
        pdf.openBrowser(browser);
    }//GEN-LAST:event_t_viewRequestHistory1ActionPerformed

    private void profile_claimForm1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_claimForm1ActionPerformed
        pdf.callClass(2, "Request History Profile", profile_rhTable);
        pdf.openBrowser(browser);
    }//GEN-LAST:event_profile_claimForm1ActionPerformed

    private void profile_claimForm2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profile_claimForm2ActionPerformed
        pdf.callClass(3, "Request History Profile", requestHistoryTable);
        pdf.openBrowser(browser);
    }//GEN-LAST:event_profile_claimForm2ActionPerformed

    private void printButtProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtProfileActionPerformed
        pdf.createProfilePDF(this.currentPersonID, createProfilePDF());
        pdf.openBrowser(browser);
    }//GEN-LAST:event_printButtProfileActionPerformed

    private void fl_formSettsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fl_formSettsActionPerformed
        formOp.callClass(connect);
    }//GEN-LAST:event_fl_formSettsActionPerformed

    private void dobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dobActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dobActionPerformed

    private void lnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lnameActionPerformed

    private void vc_searchCitizenKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_vc_searchCitizenKeyReleased

    }//GEN-LAST:event_vc_searchCitizenKeyReleased

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            vc_search();
        } else if (evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (searchField.getText().length() <= 0) {
                setViewCitizensData();
            } else {
                vc_search();
            }
        } else {
            vc_search();
        }
    }//GEN-LAST:event_searchFieldKeyReleased

    private void t_searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_t_searchFieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            trans_search();
        } else if (evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (t_searchField.getText().length() <= 0) {
                setTransactionsData();
            } else {
                trans_search();
            }
        } else {
            trans_search();
        }
    }//GEN-LAST:event_t_searchFieldKeyReleased

    private void off_prevMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_off_prevMouseClicked
        if (!off_prev.isEnabled() && prevFlag) {
            JOptionPane.showMessageDialog(null, "There are no more previous administrations.");
        } else {
            prevFlag = true;
        }
    }//GEN-LAST:event_off_prevMouseClicked

    private void off_nextMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_off_nextMouseClicked
        if (!off_next.isEnabled() && nextFlag) {
            JOptionPane.showMessageDialog(null, "Current administration in display.");
        } else {
            nextFlag = true;
        }
    }//GEN-LAST:event_off_nextMouseClicked
    
    boolean prevFlag = false; //controls the prompt for prev and next buttons in officials
    boolean nextFlag = false;
    
//user-defined
    public void loadUsers() {
        try {
            setModelAndClearModelItems(tableUsers);
            SQLConnect t = new SQLConnect();
            ResultSet rx = t.getUsers();
            usersPersonIDs = new ArrayList<>();

            for (int x = 0; rx.next(); x++) {
                SQLConnect tx = new SQLConnect();
                ResultSet z = tx.getPersonIdByUserId(String.valueOf(rx.getInt("userID")));
                z.next();
                String pId = String.valueOf(z.getInt("personID"));
                tx = new SQLConnect();
                z = tx.getPerson(pId);
                usersPersonIDs.add(pId);
                z.next();
                model.insertRow(x, new String[]{String.valueOf(rx.getInt("userID")), z.getString("fname") + " " + z.getString("lname"), rx.getString("username"), rx.getString("adminType"), rx.getString("status")});

            }
            //loadCurrentAdminOfficials(activeAdminId);
        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    //official settings
    public String dateMaker(String input) {
        String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

        String data[] = input.split("-");
        int mont = Integer.parseInt(data[0]);

        return months[mont - 1] + " " + data[1] + ", 20" + data[2];
    }

    public void loadCurrentAdmin(JTable j) { //JTable is paramter used by external forms
        setModelAndClearModelItems(j);
        connect.closeCon();
        connect = new SQLConnect();
        activeAdminId = connect.getActiveAdminId();
        prevnextcounter = activeAdminId;  //asumes adminID is same as row number in ResultSet for adminRS

        connect.closeCon();
        connect = new SQLConnect();
        brgyadmins = connect.getAllAdminsCount();

        off_editOfficial.setEnabled(true);

        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAdmin(String.valueOf(activeAdminId));
        try {
            if (rs.next()) {
                home_brgyTerm.setText(dateMaker(rs.getString("dateStart")) + " to " + dateMaker(rs.getString("dateEnd")));

            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        loadAdmin(activeAdminId);
    }

    private void loadAdmin(int adminID) {
        try {
            System.out.println("\n*****************LoadAdmin****************\n");
            System.out.println("Active AdminID: " + activeAdminId);
            System.out.println("Curr AdminID: " + adminID);

            off_prev.setEnabled(true);
            off_next.setEnabled(true);

            adminIDLabel.setText("Administration ID:    " + activeAdminId);
//token
            if (prevnextcounter - 1 < 1) { //no more prev officials
                off_prev.setEnabled(false);
            } else {
                System.out.println("Curr Prev: " + (prevnextcounter - 1));
            }

            if (prevnextcounter + 1 > brgyadmins) {
                off_next.setEnabled(false);
            } else {
                System.out.println("Curr Next: " + (prevnextcounter + 1));
            }

            connect.closeCon();
            connect = new SQLConnect();
            ResultSet rs = connect.getAdmin(String.valueOf(adminID));
            if (rs.next()) {
                off_adminYear.setText(dateMaker(rs.getString("dateStart")) + " to " + dateMaker(rs.getString("dateEnd")));
            } else {
            }
            for (int x = 0; x < 13; x++) {
                model.insertRow(x, new String[]{positionValues[x]});
            }
            loadAdminOfficials(adminID);

        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\n******************************************\n");
    }

    private void loadAdminOfficials(int adminId) {
        try {
            connect.closeCon();
            connect = new SQLConnect();
            officialIDs = new ArrayList<>();
            officialPersonIDs = new ArrayList<>();
            for (int x = 0; x < 13; x++) {
                ResultSet rs;

                if (adminId != activeAdminId) {
                    rs = connect.getOfficialsInAdminViaPosition(String.valueOf(adminId), positionValues[x], "inactive");

                } else {
                    rs = connect.getOfficialsInAdminViaPosition(String.valueOf(adminId), positionValues[x], "active");
                }

                if (rs.next()) {
                    int y = rs.getInt("officialID");
                    int z = rs.getInt("personID");
                    officialIDs.add(String.valueOf(y));
                    officialPersonIDs.add(String.valueOf(z));

                    int temp = rs.getInt("personID");
                    connect.closeCon();
                    connect = new SQLConnect();
                    ResultSet sr = connect.getPerson(String.valueOf(temp));
                    sr.next();
                    model.setValueAt(sr.getString("fname") + " " + sr.getString("lname"), x, 1);
                    //model.insertRow(x, new String[]{rs.getString("position"), sr.getString("fname") + " "+ sr.getString("lname")});
                } else {
                    officialIDs.add(String.valueOf(""));
                    officialPersonIDs.add("");
                    continue;

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deactivateAdminOfficials() {
        String temp;
        connect.closeCon();
        connect = new SQLConnect();
        for (int x = 0; x < officialIDs.size(); x++) {
            if (!(temp = officialIDs.get(x)).equalsIgnoreCase("")) {
                connect.deactivateAdminOfficials(String.valueOf(activeAdminId), officialIDs.get(x));
            }
        }
    }

    private void disableFields() {

        lname.setEnabled(false);
        fname.setEnabled(false);
        mname.setEnabled(false);
        nameEx.setEnabled(false);
        sex.setEnabled(false);
        status.setEnabled(false);
        dob.setEnabled(false);
        pob.setEnabled(false);
        age.setEnabled(false);
        religion.setEnabled(false);
        address.setEnabled(false);
        telNum.setEnabled(false);
        zipCode.setEnabled(false);
        precint.setEnabled(false);
        occupation.setEnabled(false);
        email.setEnabled(false);
        flname.setEnabled(false);
        ffname.setEnabled(false);
        fmname.setEnabled(false);
        mlname.setEnabled(false);
        mfname.setEnabled(false);
        mmname.setEnabled(false);
        siblingAdd.setEnabled(false);
        slname.setEnabled(false);
        sfname.setEnabled(false);
        smname.setEnabled(false);
        childAdd.setEnabled(false);
        eSchool.setEnabled(false);
        eYear.setEnabled(false);
        hsSchool.setEnabled(false);
        hsYear.setEnabled(false);
        vSchool.setEnabled(false);
        vYear.setEnabled(false);
        cSchool.setEnabled(false);
        cYear.setEnabled(false);
        gSchool.setEnabled(false);
        gYear.setEnabled(false);

    }

    private String createAddQuery(String queryType, String addParam) { //addParam ==> spouse, father, mother, sibling, child;

        String query = "";
        String formattedDate;
        Date data = dob.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");

        if (data != null) {
            formattedDate = sdf.format(data);
        } else {
            formattedDate = sdf.format(new Date());
        }

        switch (queryType) {
            case "person":
                //query = "INSERT INTO person (personType, lname, fname, mname, nameEx, dob, gender, address) VALUES (\"" + personType + "\", " + values + ");";
                query = "\"" + lname.getText() + "\", \"" + fname.getText() + "\", \"" + mname.getText() + "\", \"" + nameEx.getSelectedItem() + "\", \"" + formattedDate + "\", \"" + sex.getSelectedItem() + "\", \"" + address.getText() + "\"";
                break;
            case "personAddInfo":
                //query = "INSERT INTO personaddinfo (age, pob, status, contact, zipCode, precint, occupation, email, religion, personID) VALUES (" + values + ", \"" + personID + "\";";
                query = "\"" + age.getText() + "\", \"" + pob.getText() + "\", \"" + status.getSelectedItem() + "\", \"" + telNum.getText() + "\", \"" + zipCode.getText() + "\", \"" + precint.getText() + "\", \"" + occupation.getText() + "\", \"" + email.getText() + "\", \"" + religion.getText() + "\"";
                break;
            case "personFamily":
                //query = "INSERT INTO personfamily (lname, fname, mname, dob, relativeType, personID) VALUES (" + values + ", \"" + personID + "\";";
                if (addParam.equalsIgnoreCase("spouse")) {
                    query = "\"" + slname.getText() + "\", \"" + sfname.getText() + "\", \"" + smname.getText() + "\", \"\", \"SPOUSE\"";
                } else if (addParam.equalsIgnoreCase("father")) {
                    query = "\"" + flname.getText() + "\", \"" + ffname.getText() + "\", \"" + fmname.getText() + "\", \"\", \"FATHER\"";
                } else if (addParam.equalsIgnoreCase("mother")) {
                    query = "\"" + mlname.getText() + "\", \"" + mfname.getText() + "\", \"" + mmname.getText() + "\", \"\", \"MOTHER\"";
                } else if (addParam.equalsIgnoreCase("sibling")) {
                    query = "\"" + sibAndChildEntries[0] + "\", \"" + sibAndChildEntries[1] + "\", \"" + sibAndChildEntries[2] + "\", \"" + sibAndChildEntries[3] + "\", \"SIBLING\"";
                } else if (addParam.equalsIgnoreCase("child")) {
                    query = "\"" + sibAndChildEntries[0] + "\", \"" + sibAndChildEntries[1] + "\", \"" + sibAndChildEntries[2] + "\", \"" + sibAndChildEntries[3] + "\", \"CHILD\"";
                }
                break;
            case "person_education":
                if (addParam.equalsIgnoreCase("elementary")) {
                    query = "\"ELEMENTARY\", \"" + eSchool.getText() + "\", \"" + eYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("highSchool")) {
                    query = "\"HIGH SCHOOL\", \"" + hsSchool.getText() + "\", \"" + hsYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("college")) {
                    query = "\"COLLEGE\", \"" + cSchool.getText() + "\", \"" + cYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("vocational")) {
                    query = "\"VOCATIONAL\", \"" + vSchool.getText() + "\", \"" + vYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("graduate")) {
                    query = "\"GRADUATE\", \"" + vSchool.getText() + "\", \"" + vYear.getText() + "\"";
                }
                break;
        }

        return query;
    }

    private String createUpdateQuery(String queryType, String addParam) { //addParam ==> spouse, father, mother, sibling, child;

        String query = "";
        String formattedDate;
        Date data = dob.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");

        if (data != null) {
            formattedDate = sdf.format(data);
        } else {
            formattedDate = sdf.format(new Date());
        }

        switch (queryType) {
            case "person":
                //query = "INSERT INTO person (personType, lname, fname, mname, nameEx, dob, gender, address) VALUES (\"" + personType + "\", " + values + ");";
                query = "lname = \"" + lname.getText() + "\", fname = \"" + fname.getText() + "\", mname = \"" + mname.getText() + "\", nameEx = \"" + nameEx.getSelectedItem() + "\", dob = \"" + formattedDate + "\", gender = \"" + sex.getSelectedItem() + "\", address = \"" + address.getText() + "\"";
                break;
            case "personAddInfo":
                //query = "INSERT INTO personaddinfo (age, pob, status, contact, zipCode, precint, occupation, email, religion, personID) VALUES (" + values + ", \"" + personID + "\";";
                query = "age = \"" + age.getText() + "\", pob = \"" + pob.getText() + "\", status = \"" + status.getSelectedItem() + "\", contact = \"" + telNum.getText() + "\", zipCode = \"" + zipCode.getText() + "\", precint = \"" + precint.getText() + "\", occupation = \"" + occupation.getText() + "\", email = \"" + email.getText() + "\", religion = \"" + religion.getText() + "\"";
                break;
            case "personFamily":
                //query = "INSERT INTO personfamily (lname, fname, mname, dob, relativeType, personID) VALUES (" + values + ", \"" + personID + "\";";
                if (addParam.equalsIgnoreCase("spouse")) {
                    query = "lname = \"" + slname.getText() + "\", fname = \"" + sfname.getText() + "\", mname = \"" + smname.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("father")) {
                    query = "lname = \"" + flname.getText() + "\", fname = \"" + ffname.getText() + "\", mname = \"" + fmname.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("mother")) {
                    query = "lname = \"" + mlname.getText() + "\", fname = \"" + mfname.getText() + "\", mname = \"" + mmname.getText() + "\"";
                }
                break;
            case "person_education":
                if (addParam.equalsIgnoreCase("elementary")) {
                    query = "school_name = \"" + eSchool.getText() + "\", year_graduated = \"" + eYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("highSchool")) {
                    query = "school_name = \"" + hsSchool.getText() + "\",  year_graduated  = \"" + hsYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("college")) {
                    query = "school_name = \"" + cSchool.getText() + "\",  year_graduated  = \"" + cYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("vocational")) {
                    query = "school_name = \"" + vSchool.getText() + "\",  year_graduated  = \"" + vYear.getText() + "\"";
                } else if (addParam.equalsIgnoreCase("graduate")) {
                    query = "school_name = \"" + vSchool.getText() + "\",  year_graduated  = \"" + vYear.getText() + "\"";
                }
                break;
        }
        System.out.println("QUERY CREATED FOR UPDATE: " + query);
        return query;
    }

    public ArrayList<String> createProfilePDF() { //for pdf profile
        ArrayList<String> dataAll = new ArrayList<>();

        String formattedDate;
        Date data = dob.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");

        if (data != null) {
            formattedDate = sdf.format(data);
        } else {
            formattedDate = sdf.format(new Date());
        }

        //query = "INSERT INTO person (personType, lname, fname, mname, nameEx, dob, gender, address) VALUES (\"" + personType + "\", " + values + ");";
        dataAll.add(lname.getText());
        dataAll.add(fname.getText());
        dataAll.add(mname.getText());
        dataAll.add(nameEx.getSelectedItem().toString());
        dataAll.add(formattedDate);
        dataAll.add(sex.getSelectedItem().toString());
        dataAll.add(address.getText());

        //query = "INSERT INTO personaddinfo (age, pob, status, contact, zipCode, precint, occupation, email, religion, personID) VALUES (" + values + ", \"" + personID + "\";";
        dataAll.add(age.getText());
        dataAll.add(pob.getText());
        dataAll.add(status.getSelectedItem().toString());
        dataAll.add(telNum.getText());
        dataAll.add(zipCode.getText());
        dataAll.add(precint.getText());
        dataAll.add(occupation.getText());
        dataAll.add(email.getText());
        dataAll.add(religion.getText());
        return dataAll;
    }

    private void childAndSibCaretaker() {
        int x;

        for (x = 0; x < newSiblings.size(); x++) {
            sibAndChildEntries = newSiblings.get(x).split("-");
            connect.storeNewPersonAddInfo("personFamily", this.createAddQuery("personFamily", "sibling"), currentPersonID);
        }

        if (x == newSiblings.size()) {
            System.out.println(x + " Sibilings Added");
        }

        for (x = 0; x < newChildren.size(); x++) {
            sibAndChildEntries = newChildren.get(x).split("-");
            connect.storeNewPersonAddInfo("personFamily", this.createAddQuery("personFamily", "child"), currentPersonID);
        }

        if (x == newChildren.size()) {
            System.out.println(x + " Children Added");
        }
    }

    private void personInfoSaver(String pType) { //------------CHECK CHANGE----------------------
        boolean loop = true;
        System.out.println("PINFS");
        for (int x = 1; loop; x++) {
            connect.closeCon();
            connect = new SQLConnect();
            switch (x) {
                case 1:
                    if (connect.personHas(currentPersonID, "personaddinfo")) {
                        //udpate
                        connect.closeCon();
                        connect = new SQLConnect();
                        connect.updatePerson(currentPersonID, pType, "personaddinfo", createUpdateQuery("personAddInfo", "none"), "none");

                        //confirm print
                        System.out.println("Person Add Info Updated..");
                    } else if (!age.getText().equals("")
                            || !pob.getText().equals("")
                            || status.getSelectedIndex() >= 0
                            || !telNum.getText().equals("")
                            || !zipCode.getText().equals("")
                            || !precint.getText().equals("")
                            || !occupation.getText().equals("")
                            || !email.getText().equals("")) {
                        connect.closeCon();
                        connect = new SQLConnect();
                        connect.storeNewPersonAddInfo("personAddInfo", createAddQuery("personAddInfo", "none"), currentPersonID);

                        //confirm print
                        System.out.println("Person Add Info Added");
                    } //saved

                    if (pType.equals("guest")) { //this is the only case it needs to update for guest
                        loop = false;
                    }
                    break;
                case 2:
                    if (pType.equals("citizen")) {
                        if (connect.personHas(currentPersonID, "personfamily")) { //updating sib and child is at CASE 4
                            //udpate
                            connect.closeCon();
                            connect = new SQLConnect();

                            if (!connect.personHas(currentPersonID, "personfamily", "MOTHER", "relativeType")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "personfamily", createUpdateQuery("personFamily", "mother"), "MOTHER");
                            }

                            connect.closeCon();
                            connect = new SQLConnect();
                            if (!connect.personHas(currentPersonID, "personfamily", "FATHER", "relativeType")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "personfamily", createUpdateQuery("personFamily", "father"), "FATHER");
                            }

                            connect.closeCon();
                            connect = new SQLConnect();
                            if (!connect.personHas(currentPersonID, "personfamily", "SPOUSE", "relativeType")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "personfamily", createUpdateQuery("personFamily", "spouse"), "SPOUSE");
                            }

                            //confirm print
                            System.out.println("Person Family Updated");
                        } else {
                            boolean famPrint = false;
                            if (!ffname.getText().equals("") || !flname.getText().equals("") || !fmname.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("personFamily", createAddQuery("personFamily", "father"), currentPersonID);
                                famPrint = true;
                            }

                            if (!mfname.getText().equals("") || !mlname.getText().equals("") || !mmname.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("personFamily", createAddQuery("personFamily", "mother"), currentPersonID);
                                famPrint = true;
                            }

                            if (!sfname.getText().equals("") || !slname.getText().equals("") || !smname.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("personFamily", createAddQuery("personFamily", "spouse"), currentPersonID);
                                famPrint = true;
                            }

                            //confirm print
                            if (famPrint) {
                                System.out.println("Person Family Added");
                            }//saved
                            //check if father, mother, spouse
                            //check if fields of addinfo have values
                            //if yes, save
                            //if no, skip
                        }
                    }
                    break;
                case 3:
                    if (pType.equals("citizen")) {
                        if (connect.personHas(currentPersonID, "person_education")) { //update

                            connect.closeCon();
                            connect = new SQLConnect();

                            if (!connect.personHas(currentPersonID, "person_education", "ELEMENTARY", "level")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "person_education", createUpdateQuery("person_education", "elementary"), "ELEMENTARY");
                            }

                            connect.closeCon();
                            connect = new SQLConnect();
                            if (!connect.personHas(currentPersonID, "person_education", "HIGH SCHOOL", "level")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "person_education", createUpdateQuery("person_education", "highSchool"), "HIGH SCHOOL");
                            }

                            connect.closeCon();
                            connect = new SQLConnect();
                            if (!connect.personHas(currentPersonID, "person_education", "COLLEGE", "level")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "person_education", createUpdateQuery("person_education", "college"), "COLLEGE");
                            }

                            connect.closeCon();
                            connect = new SQLConnect();
                            if (!connect.personHas(currentPersonID, "person_education", "VOCATIONAL", "level")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "person_education", createUpdateQuery("person_education", "vocational"), "VOCATIONAL");
                            }

                            connect.closeCon();
                            connect = new SQLConnect();
                            if (!connect.personHas(currentPersonID, "person_education", "GRADUATE", "level")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.updatePerson(currentPersonID, "citizen", "person_education", createUpdateQuery("person_education", "graduate"), "GRADUATE");
                            }
                            //confirm print
                            System.out.println("Person Education Updated");
                        } else { //add
                            boolean edPrint = false;

                            if (!eYear.getText().equals("") || !eSchool.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("person_education", createAddQuery("person_education", "elementary"), currentPersonID);
                                edPrint = true;
                            }

                            if (!hsYear.getText().equals("") || !hsSchool.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("person_education", createAddQuery("person_education", "highSchool"), currentPersonID);
                                edPrint = true;
                            }

                            if (!vYear.getText().equals("") || !vSchool.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("person_education", createAddQuery("person_education", "vocational"), currentPersonID);
                                edPrint = true;
                            }

                            if (!cYear.getText().equals("") || !cSchool.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("person_education", createAddQuery("person_education", "college"), currentPersonID);
                                edPrint = true;
                            }

                            if (!gYear.getText().equals("") || !gSchool.getText().equals("")) {
                                connect.closeCon();
                                connect = new SQLConnect();
                                connect.storeNewPersonAddInfo("person_education", createAddQuery("person_education", "graduate"), currentPersonID);
                                edPrint = true;
                            }

                            //confirm print
                            if (edPrint) {
                                System.out.println("Person Education Added");
                            }
                        }
                    }
                    break;
                case 4:
                    if (pType.equals("citizen")) {
                        childAndSibCaretaker();
                    }
                    break;
                default:
                    loop = false;
                    break;
            }

        }
    }

    private void emptyFields() {
        lname.setText("");
        fname.setText("");
        mname.setText("");
        nameEx.setSelectedIndex(0);
        sex.setSelectedIndex(0);
        status.setSelectedIndex(0);
        dob.setDate(new Date());
        pob.setText("");
        age.setText("");
        religion.setText("");
        address.setText("");
        telNum.setText("");
        zipCode.setText("");
        precint.setText("");
        occupation.setText("");
        email.setText("");
        flname.setText("");
        ffname.setText("");
        fmname.setText("");
        mlname.setText("");
        mfname.setText("");
        mmname.setText("");

        DefaultTableModel model1;
        model1 = (DefaultTableModel) sibling.getModel();

        while (sibling.getRowCount() > 0) {
            model1.removeRow(0);
        }

        slname.setText("");
        sfname.setText("");
        smname.setText("");

        model1 = (DefaultTableModel) child.getModel();

        while (child.getRowCount() > 0) {
            model1.removeRow(0);
        }

        eSchool.setText("");
        eYear.setText("");
        hsSchool.setText("");
        hsYear.setText("");
        vSchool.setText("");
        vYear.setText("");
        cSchool.setText("");
        cYear.setText("");
        gSchool.setText("");
        gYear.setText("");

    }

    private void enableFields() {

        lname.setEnabled(true);
        fname.setEnabled(true);
        mname.setEnabled(true);
        nameEx.setEnabled(true);
        sex.setEnabled(true);
        status.setEnabled(true);
        dob.setEnabled(true);
        pob.setEnabled(true);
        age.setEnabled(true);
        religion.setEnabled(true);
        address.setEnabled(true);
        telNum.setEnabled(true);
        zipCode.setEnabled(true);
        precint.setEnabled(true);
        occupation.setEnabled(true);
        email.setEnabled(true);
        flname.setEnabled(true);
        ffname.setEnabled(true);
        fmname.setEnabled(true);
        mlname.setEnabled(true);
        mfname.setEnabled(true);
        mmname.setEnabled(true);
        siblingAdd.setEnabled(true);
        slname.setEnabled(true);
        sfname.setEnabled(true);
        smname.setEnabled(true);
        childAdd.setEnabled(true);
        eSchool.setEnabled(true);
        eYear.setEnabled(true);
        hsSchool.setEnabled(true);
        hsYear.setEnabled(true);
        vSchool.setEnabled(true);
        vYear.setEnabled(true);
        cSchool.setEnabled(true);
        cYear.setEnabled(true);
        gSchool.setEnabled(true);
        gYear.setEnabled(true);

    }

    private void statusXspouse() {
        //WHEN STATUS IS CHANGED
        if (!status.getSelectedItem().toString().equals("Married")) { //echos sa spouse
            slname.setEnabled(false);
            sfname.setEnabled(false);
            smname.setEnabled(false);
        } else {
            slname.setEnabled(true);
            sfname.setEnabled(true);
            smname.setEnabled(true);
        }

        new_note.setVisible(true);
        new_s1.setVisible(true);
        new_s2.setVisible(true);
        new_s3.setVisible(true);
    }

    public void backFromClass(String returnStatement) {
        this.setEnabled(true);
        this.setVisible(true);

        switch (returnStatement) {
            case "Saved Requested Form":
                //From Browser
                connect.closeCon();
                connect = new SQLConnect();
                ResultSet temp = connect.getLatestRequestID();
                try {
                    logHandler.saveLog("Requested Form with -Transaction ID: " + currentPersonTransID + " -Person ID: " + currentPersonID + " -Person Name: " + currentPersonName + " -Form ID: " + requestedFormID + " -Form Name: " + requestedFormName + " - Request ID: " + temp.getInt("formentryID"));

                } catch (SQLException ex) {
                    Logger.getLogger(Main.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "Saved New Event":
                //From New Event
                connect.closeCon();
                connect = new SQLConnect();
                try {
                    ResultSet xd = connect.getLatestEventID();
                    connect = new SQLConnect();
                    ResultSet temp2 = connect.getEvent(String.valueOf(xd.getInt("eventID")));
                    logHandler.saveLog("Added New Calendar Event with -Event ID: " + xd.getInt("eventID") + " -Event Name: " + temp2.getString("title"));

                } catch (SQLException ex) {
                    Logger.getLogger(Main.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                break;
            //From Add New Relative
            //From SearchPerson
            default:
                break;
        }

    }

    private void hideAllPanels() {
        viewCitizensPanel.setVisible(false);
        profilePanel.setVisible(false);
        settingsPanel.setVisible(false);
        officialsPanel.setVisible(false);
        usersPanel.setVisible(false);
        calendarPanel.setVisible(false);
        eventPanel.setVisible(false);
        transactionsPanel.setVisible(false);
        requestHistoryPanel.setVisible(false);
        formSelectPanel.setVisible(false);
        logHistoryPanel.setVisible(false);
        formListPanel.setVisible(false);
        homePanel.setVisible(false);
    }

    public void setModelAndClearModelItems(JTable tb) {
        //ERASES ALL DATA FROM A TABLE 
        model = (DefaultTableModel) tb.getModel();
        while (tb.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    public void setDataFromAddNewRelative() {
        //DISPLAY NEW STUFF TO TABLE AT ADD PANEL (SIBLING/CHILDREN)
        ArrayList<String> temp = ant.getData();
        DefaultTableModel model1;
        if (relativeType.equals("Sibling")) {
            model1 = (DefaultTableModel) sibling.getModel();
            model1.insertRow(siblingCount++, new String[]{temp.get(0), temp.get(1), temp.get(2), temp.get(3)});
            newSiblings.add(temp.get(0) + "-" + temp.get(1) + "-" + temp.get(2) + "-" + temp.get(3));
        } else if (relativeType.equals("Child")) {
            model1 = (DefaultTableModel) child.getModel();
            model1.insertRow(childrenCount++, new String[]{temp.get(0), temp.get(1), temp.get(2), temp.get(3)});
            newChildren.add(temp.get(0) + "-" + temp.get(1) + "-" + temp.get(2) + "-" + temp.get(3));
        }
        pack();
    }

    private void setViewCitizensData() {
        //DISPLAY ALL CITIZEN RECORDS AT VIEW CITIZENS PANEL'S TABLE
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAllCitizenPersons();
        setModelAndClearModelItems(viewCitizensTable);

        try {
            for (int x = 0; rs.next(); x++) {
                model.insertRow(x, new String[]{String.valueOf(rs.getInt("personID")), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex);
        }
    }

    private void setTransactionsData() {
        //DISPLAY ALL TRANSACTION RECORDS AT TRANSACTION PANEL'S TABLE
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAllTransactions();
        setModelAndClearModelItems(transactionTable);

        try {
            for (int x = 0; rs.next(); x++) {
                //transID, last name, firstname, date last req, date last claim
                connect = new SQLConnect();
                ResultSet rs2 = connect.getPerson(String.valueOf(rs.getInt("personID")));

                if (rs2.next()) {
                    model.insertRow(x, new String[]{String.valueOf(rs.getInt("transID")), rs2.getString("lname"), rs2.getString("fname"), rs.getString("date_last_trans")});
                    connect.closeCon();
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex);
        }
    }

    private void setCitizenTransactionData() {
        //DISPLAY ALL TRANSACTION RECORDS AT TRANSACTION PANEL'S TABLE
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAllCitizenPersons();
        setModelAndClearModelItems(transactionTable);

        try {
            int num = 0;
            for (int x = 0; rs.next(); x++) {
                connect = new SQLConnect();
                if (connect.hasTrans(String.valueOf(rs.getInt("personID")))) {
                    connect = new SQLConnect();
                    ResultSet rs2 = connect.getTransactionViaPersonID(String.valueOf(rs.getInt("personID")));
                    if (rs2.next()) {
                        model.insertRow(num++, new String[]{String.valueOf(rs2.getInt("transID")), rs.getString("lname"), rs.getString("fname"), rs2.getString("date_last_trans")});
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("setcittrans Error: " + ex);
        }
    }

    private void setGuestTransactionsData() {
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAllGuestPersons();
        setModelAndClearModelItems(transactionTable);

        try {
            int num = 0;
            for (int x = 0; rs.next(); x++) {
                connect = new SQLConnect();
                ResultSet rs2 = connect.getTransactionViaPersonID(String.valueOf(rs.getInt("personID")));
                if (rs2.next()) {
                    model.insertRow(num++, new String[]{String.valueOf(rs2.getInt("transID")), rs.getString("lname"), rs.getString("fname"), rs2.getString("date_last_trans")});
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex);
        }
    }

    private void setRequestHistoryData(String tID, String name) {
        try {
            rh_TID.setText("Trans ID: " + tID);
            rh_name.setText("Name: " + name);
            connect.closeCon();
            connect = new SQLConnect();

            System.out.println("\n**********************************");
            System.out.println("Setting Request History Data for (Prev) Person ID: " + currentPersonID);
            System.out.println("Setting Request History Data for Person Trans ID: " + currentPersonTransID + " or (param) Trans ID: " + tID);

            currentPersonID = String.valueOf(connect.getPersonIDViaTransID(tID)); //refresh person ID

            System.out.println("Setting Request History Data for (New) Person ID: " + currentPersonID);

            connect.closeCon();
            connect = new SQLConnect();

            ResultSet x = connect.getPerson(currentPersonID);
            if (x.next()) {
                rh_type.setText("Type: " + (currentPersonTransType = x.getString("personType").toUpperCase()));
                System.out.println("Setting Request History of a " + currentPersonTransType);
            }

            currentPersonName = x.getString("fname") + " " + x.getString("lname");
            System.out.println("**********************************\n");

            refreshRequestHistory(requestHistoryTable);

        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void refreshRequestHistoryByBrowser() {
        refreshRequestHistory(requestHistoryTable);
    }

    public void refreshRequestHistory(JTable table) {//called by BROWSER after requesting a form
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getPersonFormsByTransID(currentPersonTransID);

        System.out.println("\n*********************************************");
        System.out.println("Refreshing Request History");
        System.out.println("*********************************************\n");

        int y = 0;
        setModelAndClearModelItems(table);

        try {
            while (rs.next()) {
                SQLConnect connect1 = new SQLConnect();
                ResultSet rs2 = connect1.getForm(String.valueOf(rs.getInt("formID")));
                if (rs2.next()) {
                    model.insertRow(y++, new String[]{String.valueOf(rs.getInt("formEntryID")), rs2.getString("formName"), rs.getString("date_requested") + "; " + rs.getString("time_requested"), rs.getString("date_to_claim"), rs.getString("date_claimed") + "; " + rs.getString("time_claimed")});
                    connect1.closeCon();
                }
            }
            requesting = false; //assumed when this method is called by Browser after "DONE" button is clicked

        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setCitizenData(String personID) {
        //CALLED TO DISPLAY DATA WHEN USER VIEWS A PROFILE
        try {
            ResultSet rs;

            connect.closeCon();
            connect = new SQLConnect();//T
            rs = connect.getPerson(personID);
            if (rs.next()) {
                pID.setText(personID);
                lname.setText(rs.getString("lname"));
                fname.setText(rs.getString("fname"));
                mname.setText(rs.getString("mname"));
                nameEx.setSelectedItem(rs.getString("nameEx"));
                sex.setSelectedItem(rs.getString("gender"));
                address.setText(rs.getString("address"));

                DateFormat formatter = new SimpleDateFormat("MM-dd-yy");
                Date date = formatter.parse(rs.getString("dob"));
                dob.setDate(date);
            }

            currentPersonName = fname.getText() + " " + lname.getText();

            connect.closeCon();
            connect = new SQLConnect();

            if (connect.personHas(currentPersonID, "personaddinfo")) {
                connect.closeCon();
                connect = new SQLConnect();
                rs = connect.getPersonInfo("personAddInfo", currentPersonID, "none");
                if (rs.next()) {
                    pob.setText(rs.getString("pob"));
                    status.setSelectedItem(rs.getString("status"));
                    age.setText(rs.getString("age"));
                    religion.setText(rs.getString("religion"));
                    //telNum.setText(rs.getString("telNum"));
                    zipCode.setText(rs.getString("zipCode"));
                    precint.setText(rs.getString("precint"));
                    occupation.setText(rs.getString("occupation"));
                    email.setText(rs.getString("email"));
                }
            }

            connect.closeCon();
            connect = new SQLConnect();

            if (connect.personHas(currentPersonID, "personfamily")) {
                connect.closeCon();
                connect = new SQLConnect();
                if (connect.personHas(currentPersonID, "personfamily", "FATHER", "relativeType")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("personFamily", currentPersonID, "FATHER");
                    if (rs.next()) {
                        flname.setText(rs.getString("lname"));
                        ffname.setText(rs.getString("fname"));
                        fmname.setText(rs.getString("mname"));
                    }
                }
                if (connect.personHas(currentPersonID, "personfamily", "MOTHER", "relativeType")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("personFamily", currentPersonID, "MOTHER");
                    if (rs.next()) {
                        mlname.setText(rs.getString("lname"));
                        mfname.setText(rs.getString("fname"));
                        mmname.setText(rs.getString("mname"));
                    }
                }
                if (connect.personHas(currentPersonID, "personfamily", "SPOUSE", "relativeType")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("personFamily", currentPersonID, "SPOUSE");
                    if (rs.next()) {
                        slname.setText(rs.getString("lname"));
                        sfname.setText(rs.getString("fname"));
                        smname.setText(rs.getString("mname"));
                    }
                }
                if (connect.personHas(currentPersonID, "personfamily", "SIBLING", "relativeType")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("personFamily", currentPersonID, "SIBLING");
                    setModelAndClearModelItems(sibling);

                    while (rs.next()) {
                        model.insertRow(siblingCount++, new String[]{rs.getString("lname"), rs.getString("fname"), rs.getString("mname"), rs.getString("dob")});
                        System.out.println("sib table entry #" + siblingCount);
                    }
                }
                if (connect.personHas(currentPersonID, "personfamily", "CHILD", "relativeType")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("personFamily", currentPersonID, "CHILD");
                    setModelAndClearModelItems(child);

                    while (rs.next()) {
                        model.insertRow(childrenCount++, new String[]{rs.getString("lname"), rs.getString("fname"), rs.getString("mname"), rs.getString("dob")});
                        System.out.println("child table entry #" + childrenCount);
                    }

                }
            }

            connect.closeCon();
            connect = new SQLConnect();

            if (connect.personHas(currentPersonID, "person_education")) {
                connect.closeCon();
                connect = new SQLConnect();
                if (connect.personHas(currentPersonID, "person_education", "ELEMENTARY", "level")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("person_education", currentPersonID, "ELEMENTARY");
                    if (rs.next()) {
                        eSchool.setText(rs.getString("school_name"));
                        eYear.setText(rs.getString("year_graduated"));
                    }
                }
                if (connect.personHas(currentPersonID, "person_education", "HIGH SCHOOL", "level")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("person_education", currentPersonID, "HIGH SCHOOL");
                    if (rs.next()) {
                        hsSchool.setText(rs.getString("school_name"));
                        hsYear.setText(rs.getString("year_graduated"));
                    }
                }
                if (connect.personHas(currentPersonID, "person_education", "VOCATIONAL", "level")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("person_education", currentPersonID, "VOCATIONAL");
                    if (rs.next()) {
                        vSchool.setText(rs.getString("school_name"));
                        vYear.setText(rs.getString("year_graduated"));
                    }
                }
                if (connect.personHas(currentPersonID, "person_education", "COLLEGE", "level")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("person_education", currentPersonID, "COLLEGE");
                    if (rs.next()) {
                        cSchool.setText(rs.getString("school_name"));
                        cYear.setText(rs.getString("year_graduated"));
                    }
                }
                if (connect.personHas(currentPersonID, "person_education", "GRADUATE", "level")) {
                    connect.closeCon();
                    connect = new SQLConnect();
                    rs = connect.getPersonInfo("person_education", currentPersonID, "GRADUATE");
                    if (rs.next()) {
                        gSchool.setText(rs.getString("school_name"));
                        gYear.setText(rs.getString("year_graduated"));

                    }
                }
            }

        } catch (ParseException | SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setCalendarComponentsArray() {
        JButton days[] = {day0, day1, day2, day3, day4, day5, day6, day7, day8, day9, day10, day11, day12, day13, day14, day15, day16, day17, day18, day19, day20, day21, day22, day23, day24, day25, day26, day27, day28, day29, day30, day31, day32, day33, day34, day35, day36, day37, day38, day39, day40, day41};

        //Center Text in Cell
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        //eventsTable.setDefaultRenderer(String.class, centerRenderer);

        cal = new BRMS_Calendar(this, calendarPanel, days, monthChoice, yearChoice);

    }

    public void setFormSelectionListData(JTable table) {
        connect.closeCon();
        connect = new SQLConnect();
        ResultSet rs = connect.getAllForms();
        setModelAndClearModelItems(table);

        try {
            for (int x = 0; rs.next();) {
                if (table == formsSelectTable) {
                    if (rs.getString("status").equalsIgnoreCase("Available")) {
                        model.insertRow(x, new String[]{String.valueOf(rs.getInt("formID")), rs.getString("formName"), rs.getString("status"), rs.getString("dateAdded"), rs.getString("dateModified"), rs.getString("userID")});
                        x++;
                    }
                } else if (table == formListTable) {
                    model.insertRow(x, new String[]{String.valueOf(rs.getInt("formID")), rs.getString("formName"), rs.getString("status"), rs.getString("dateAdded"), rs.getString("dateModified"), rs.getString("userID")});
                    x++;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error: " + ex);
        }

    }

    public void formEditorSetFormMainList() {
        setFormSelectionListData(formListTable);
    }

    public void getCalendarDaySelected(String i) {
        //called by calendar class
        calendarDaySelected = i;
    }

    public void viewEventsOnThisDay() {
        connect.closeCon();
        connect = new SQLConnect();
        dateSelected.setText(cal.getMonthSelected() + " " + calendarDaySelected);
        //logHandler.saveLog("Viewed Events on Date: " + dateSelected.getText());

        ResultSet rs = connect.getAllEvents(cal.getMonthSelected(), calendarDaySelected);
        setModelAndClearModelItems(eventsTable);
        try {
            for (int x = 0; rs.next(); x++) {
                model.insertRow(x, new String[]{String.valueOf(rs.getString("year")), rs.getString("title"), rs.getString("venue"), rs.getString("time"), rs.getString("remarks"), String.valueOf(rs.getInt("userID"))});

            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void viewProf(String pType) {
        //CALLED BY LISTENERS THAT REQUIRE VIEWING PRFILE
        emptyFields();
        hideAllPanels();
        disableFields();
        setCitizenData(currentPersonID); //-----------------ERROR FOR GUEST
        setProfileRequestHistory();
        refreshNotes();
        profileSave.setText("Edit");
        profilePanel.setVisible(true);

        if (addTab.getTabCount() > 2 && addTab.getTabCount() < 4) {
            addTab.add(rhtab);
            addTab.setTitleAt(3, "Request History");
            addTab.add(notetab);
            addTab.setTitleAt(4, "Visit Notes");

        }

        if (pType.equals("guest")) {
            addFunctionType = "editg";
            viewGuest();
        } else {
            addFunctionType = "editc";
        }

        new_note.setVisible(false);
        new_s1.setVisible(false);
        new_s2.setVisible(false);
        new_s3.setVisible(false);
    }

    public void setProfileRequestHistory() {
        connect = new SQLConnect();
        currentPersonTransID = connect.getpTransID(currentPersonID);
        refreshRequestHistory(profile_rhTable);
    }

    public void returnToPanel() { //After Requesting or When Requesting is cancelled
        hideAllPanels();
        requesting = false;
        switch (requestingPanel) {
            case 1:
            case 2:
                viewCitizensPanel.setVisible(true);
                break; //vc
            case 3:
                transactionsPanel.setVisible(true);
                break; //t
            case 4:
                profilePanel.setVisible(true);
                break;
            default:
                System.out.println("WUT");
                requestHistoryPanel.setVisible(true);
                break;
        }
    }

    public void makeRequest(String pTransID, String nameConcat) { //called by Search Pop up after person is selected
        //CALLED BY ANYTHING THAT REDIRECTS TO REQUESTING A FORM (NEW TRANSACTION/EXISTING TRANSACTION)
        currentPersonTransID = pTransID;
        System.out.println("Making Request for Trans ID: " + pTransID + " or Current Trans ID: " + currentPersonTransID);
        setRequestHistoryData(currentPersonTransID, nameConcat);
        hideAllPanels();
        requestingPanel = 0; //default so requestHistoryPanel
        formSelectPanel.setVisible(true);
        setFormSelectionListData(formsSelectTable);
    }

    public void viewGuest() {//REMOVE two TABs from Add Person Panel for Guest
        profilePanel.setVisible(true);
        if (addTab.getTabCount() > 2) {
            addTab.removeTabAt(1);
            addTab.removeTabAt(1);
        }
    }

    public void setLogHistoryData() {
        logHandler.setLogHistory();
    }

    public void setHomePanelData() {
        loadCurrentAdmin(homeBrgyTable);
    }

    public void birthdayAccess(String id) {
        this.currentPersonID = id;
        this.profileViewType = "bdayView";
        siblingCount = 0;
        childrenCount = 0;
        viewProf("citizen");
    }

    public void claimablesAccess(String id, String name) {
        currentPersonTransID = id;
        currentPersonName = name;
        setRequestHistoryData(id, name);
        hideAllPanels();
        profileViewType = "claimView";
        requestHistoryPanel.setVisible(true);
    }

    public boolean personExist() {
        try {
            connect.closeCon();
            connect = new SQLConnect();
            ResultSet rx = connect.getUsers();
            while (rx.next()) {
                if (lname.getText().equals(rx.getString("lname")) && fname.getText().equals(rx.getString("fname"))) {
                    return true;

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void returnConnect(SQLConnect connect) {
        this.connect = connect;
    }

    public void refreshNotes() {
        connect = new SQLConnect(); //toDelete
        ArrayList<ArrayList<String>> dataAll = connect.getAllNotes();
        setModelAndClearModelItems(profile_notesTable);
        int rowCounter = 0;
        while (rowCounter < dataAll.size()) {
            ArrayList<String> data = dataAll.get(rowCounter);
            model.insertRow(rowCounter++, new String[]{data.get(0), data.get(1), data.get(5), data.get(6)});
        }
    }

    public void vc_search() {
        try {
            setModelAndClearModelItems(viewCitizensTable);
            connect.closeCon();
            connect = new SQLConnect();
            ResultSet rs = connect.getPerson("citizen", searchField.getText());
            int x;
            for (x = 0; rs.next(); x++) {
                model.insertRow(x, new String[]{rs.getString("personID"), rs.getString("lname"), rs.getString("fname"), rs.getString("address")});
                System.out.println("child table entry #" + siblingCount);
            }

            vc_back.setEnabled(true);

            if (x == 0) {
                JPanel p1 = new JPanel();
                p1.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));
                p1.add(new JLabel("No Results Found. Would you like to add \"" + searchField.getText() + "\" to the system?"));
                int choice = JOptionPane.showConfirmDialog(null, p1,
                        "Message", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice == JOptionPane.OK_OPTION) {
                    hideAllPanels();
                    if (logged_userType.equals("Level 1")) {
                        JOptionPane.showMessageDialog(null, "You do not have enough Administrative Priveleges to use the function!");
                    } else {
                        newSiblings = new ArrayList<>();
                        newChildren = new ArrayList<>();

                        siblingCount = 0;
                        childrenCount = 0;
                        addFunctionType = "addc";

                        emptyFields();
                        enableFields();
                        hideAllPanels();
                        statusXspouse();

                        if (addTab.getTabCount() > 3) {
                            addTab.removeTabAt(3); //request history
                        }

                        profileSave.setText("Save");
                        dob.setDate(new Date());
                        profilePanel.setVisible(true);
                        jLabel42.setVisible(false);
                        fname.setText(searchField.getText());
                        System.out.println(""); //new Line in log
                    }
                }
                setViewCitizensData();
                vc_back.setEnabled(false);

            }

        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void trans_search() {
        try {
            setModelAndClearModelItems(transactionTable);
            connect.closeCon();
            connect = new SQLConnect();
            ResultSet rs = connect.getPerson("all", t_searchField.getText());
            int x = 0;
            while (rs.next()) {
                SQLConnect connect1 = new SQLConnect();
                if (connect1.hasTrans(String.valueOf(rs.getInt("personID")))) {
                    SQLConnect connect2 = new SQLConnect();
                    ResultSet rs2 = connect2.getPerson(String.valueOf(rs.getInt("personID")));
                    SQLConnect connect3 = new SQLConnect();
                    ResultSet rs3 = connect3.getTransactionViaPersonID(String.valueOf(rs.getInt("personID")));
                    if (rs3.next()) {
                        if (rs2.next()) {
                            model.insertRow(x++, new String[]{rs3.getString("transID"), rs2.getString("lname"), rs2.getString("fname"), rs3.getString("date_last_trans")});
                            connect1.closeCon();
                            connect2.closeCon();
                            connect3.closeCon();
                        }
                    }
                }
            }
            t_back.setEnabled(true);

        } catch (SQLException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Day;
    private javax.swing.JPanel DayName;
    private javax.swing.JPanel Weeks;
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JButton addCitizen;
    private javax.swing.JButton addNewEvent;
    private javax.swing.JTabbedPane addTab;
    private javax.swing.JTextField address;
    private javax.swing.JLabel adminIDLabel;
    private javax.swing.JTextField age;
    private javax.swing.JButton backToCalendar;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JTextField cSchool;
    private javax.swing.JTextField cYear;
    private javax.swing.JButton calendar;
    private javax.swing.JPanel calendarPanel;
    private javax.swing.JTable child;
    private javax.swing.JButton childAdd;
    private javax.swing.JPanel claimableFormsPanel;
    private javax.swing.JPanel claimableFormsPanel1;
    private javax.swing.JPanel claimableFormsPanel12;
    private javax.swing.JPanel claimableFormsPanel3;
    private javax.swing.JPanel claimableFormsPanel4;
    private javax.swing.JLabel dateSelected;
    private javax.swing.JButton day0;
    private javax.swing.JButton day1;
    private javax.swing.JButton day10;
    private javax.swing.JButton day11;
    private javax.swing.JButton day12;
    private javax.swing.JButton day13;
    private javax.swing.JButton day14;
    private javax.swing.JButton day15;
    private javax.swing.JButton day16;
    private javax.swing.JButton day17;
    private javax.swing.JButton day18;
    private javax.swing.JButton day19;
    private javax.swing.JButton day2;
    private javax.swing.JButton day20;
    private javax.swing.JButton day21;
    private javax.swing.JButton day22;
    private javax.swing.JButton day23;
    private javax.swing.JButton day24;
    private javax.swing.JButton day25;
    private javax.swing.JButton day26;
    private javax.swing.JButton day27;
    private javax.swing.JButton day28;
    private javax.swing.JButton day29;
    private javax.swing.JButton day3;
    private javax.swing.JButton day30;
    private javax.swing.JButton day31;
    private javax.swing.JButton day32;
    private javax.swing.JButton day33;
    private javax.swing.JButton day34;
    private javax.swing.JButton day35;
    private javax.swing.JButton day36;
    private javax.swing.JButton day37;
    private javax.swing.JButton day38;
    private javax.swing.JButton day39;
    private javax.swing.JButton day4;
    private javax.swing.JButton day40;
    private javax.swing.JButton day41;
    private javax.swing.JButton day5;
    private javax.swing.JButton day6;
    private javax.swing.JButton day7;
    private javax.swing.JButton day8;
    private javax.swing.JButton day9;
    private org.jdesktop.swingx.JXDatePicker dob;
    private javax.swing.JTextField eSchool;
    private javax.swing.JTextField eYear;
    private javax.swing.JPanel educ;
    private javax.swing.JTextField email;
    private javax.swing.JPanel eventPanel;
    private javax.swing.JTable eventsTable;
    private javax.swing.JButton f_Select;
    private javax.swing.JButton f_back;
    private javax.swing.JScrollPane famback;
    private javax.swing.JTextField ffname;
    private javax.swing.JButton fl_addForm;
    private javax.swing.JButton fl_back;
    private javax.swing.JButton fl_editForm;
    private javax.swing.JButton fl_formSetts;
    private javax.swing.JButton fl_viewForm;
    private javax.swing.JTextField flname;
    private javax.swing.JTextField fmname;
    private javax.swing.JTextField fname;
    private javax.swing.JPanel formListPanel;
    private javax.swing.JTable formListTable;
    private javax.swing.JScrollPane formSelectPane;
    private javax.swing.JPanel formSelectPanel;
    private javax.swing.JTable formsSelectTable;
    private javax.swing.JButton fri;
    private javax.swing.JTextField gSchool;
    private javax.swing.JTextField gYear;
    private javax.swing.JButton home;
    private javax.swing.JTable homeBrgyTable;
    private javax.swing.JPanel homePanel;
    private javax.swing.JButton home_aboutBRMS;
    private javax.swing.JLabel home_brgyTerm;
    private javax.swing.JButton home_userManual;
    private javax.swing.JButton home_viewBday;
    private javax.swing.JButton home_viewClaimables;
    private javax.swing.JTextField hsSchool;
    private javax.swing.JTextField hsYear;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton lh_back;
    private javax.swing.JTextField lname;
    private javax.swing.JPanel logHistoryPanel;
    private javax.swing.JTable logHistoryTable;
    private javax.swing.JButton logout;
    private javax.swing.JTextField mfname;
    private javax.swing.JTextField mlname;
    private javax.swing.JTextField mmname;
    private javax.swing.JTextField mname;
    private javax.swing.JButton mon;
    private javax.swing.JComboBox monthChoice;
    private javax.swing.JComboBox nameEx;
    private javax.swing.JLabel new_note;
    private javax.swing.JLabel new_s1;
    private javax.swing.JLabel new_s2;
    private javax.swing.JLabel new_s3;
    private javax.swing.JPanel notetab;
    private javax.swing.JTextField occupation;
    private javax.swing.JLabel off_adminYear;
    private javax.swing.JButton off_backButton;
    private javax.swing.JButton off_createNewAdminButton;
    private javax.swing.JButton off_editOfficial;
    private javax.swing.JButton off_next;
    private javax.swing.JButton off_prev;
    private javax.swing.JButton off_viewProfileButton;
    private javax.swing.JScrollPane officialsPane;
    private javax.swing.JPanel officialsPanel;
    private javax.swing.JButton options;
    private javax.swing.JLabel pID;
    private javax.swing.JPanel personalInfo;
    private javax.swing.JTextField pob;
    private javax.swing.JTextField precint;
    private javax.swing.JButton printButtProfile;
    private javax.swing.JButton profileCancel;
    private javax.swing.JPanel profilePanel;
    private javax.swing.JButton profileSave;
    private javax.swing.JButton profile_addNote;
    private javax.swing.JButton profile_claimForm;
    private javax.swing.JButton profile_claimForm1;
    private javax.swing.JButton profile_claimForm2;
    private javax.swing.JTable profile_notesTable;
    private javax.swing.JButton profile_requestForm;
    private javax.swing.JTable profile_rhTable;
    private javax.swing.JButton profile_viewFrom;
    private javax.swing.JButton profile_viewNote;
    private javax.swing.JLabel prompt;
    private javax.swing.JTextField religion;
    private javax.swing.JButton requestForm;
    private javax.swing.JPanel requestHistoryPanel;
    private javax.swing.JTable requestHistoryTable;
    private javax.swing.JLabel rh_TID;
    private javax.swing.JButton rh_back;
    private javax.swing.JButton rh_claimForm;
    private javax.swing.JLabel rh_name;
    private javax.swing.JButton rh_requestForm;
    private javax.swing.JLabel rh_type;
    private javax.swing.JButton rh_viewFrom;
    private javax.swing.JButton rh_viewProfile;
    private javax.swing.JPanel rhtab;
    private javax.swing.JButton sat;
    private javax.swing.JTextField searchField;
    private javax.swing.JPanel set1;
    private javax.swing.JPanel set3;
    private javax.swing.JPanel set4;
    private javax.swing.JPanel set5;
    private javax.swing.JPanel set6;
    private javax.swing.JButton set_import;
    private javax.swing.JButton set_viewForms;
    private javax.swing.JButton set_viewLog;
    private javax.swing.JButton set_viewOfficials;
    private javax.swing.JButton set_viewUsers;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JComboBox sex;
    private javax.swing.JTextField sfname;
    private javax.swing.JTable sibling;
    private javax.swing.JButton siblingAdd;
    private javax.swing.JTextField slname;
    private javax.swing.JTextField smname;
    private javax.swing.JComboBox status;
    private javax.swing.JButton sun;
    private javax.swing.JButton t_back;
    private javax.swing.JButton t_filter;
    private javax.swing.JTextField t_searchField;
    private javax.swing.JButton t_searchTrans;
    private javax.swing.JScrollPane t_transactionsScrollPane1;
    private javax.swing.JScrollPane t_transactionsScrollPane2;
    private javax.swing.JScrollPane t_transactionsScrollPane3;
    private javax.swing.JScrollPane t_transactionsScrollPane4;
    private javax.swing.JButton t_viewRequestHistory;
    private javax.swing.JButton t_viewRequestHistory1;
    private javax.swing.JTable tableOfficials;
    private javax.swing.JTable tableUsers;
    private javax.swing.JTextField telNum;
    private javax.swing.JButton thu;
    private javax.swing.JTable transactionTable;
    private javax.swing.JButton transactions;
    private javax.swing.JPanel transactionsPanel;
    private javax.swing.JButton tue;
    private javax.swing.JLabel use_adminYear1;
    private javax.swing.JButton use_back;
    private javax.swing.JButton use_deact;
    private javax.swing.JButton use_edit;
    private javax.swing.JButton use_newUser;
    private javax.swing.JButton use_viewProf;
    private javax.swing.JScrollPane usersPane;
    private javax.swing.JPanel usersPanel;
    private javax.swing.JTextField vSchool;
    private javax.swing.JTextField vYear;
    private javax.swing.JScrollPane v_viewCitizensScrollPane;
    private javax.swing.JScrollPane v_viewCitizensScrollPane1;
    private javax.swing.JScrollPane v_viewCitizensScrollPane2;
    private javax.swing.JButton vc_back;
    private javax.swing.JButton vc_filter;
    private javax.swing.JButton vc_searchCitizen;
    private javax.swing.JButton vc_viewProfileButton;
    private javax.swing.JButton viewCitizens;
    private javax.swing.JPanel viewCitizensPanel;
    private javax.swing.JTable viewCitizensTable;
    private javax.swing.JButton viewDayEvent;
    private javax.swing.JButton wed;
    private javax.swing.JPanel week1;
    private javax.swing.JPanel week2;
    private javax.swing.JPanel week3;
    private javax.swing.JPanel week5;
    private javax.swing.JPanel week6;
    private javax.swing.JPanel week7;
    private javax.swing.JComboBox yearChoice;
    private javax.swing.JTextField zipCode;
    // End of variables declaration//GEN-END:variables

}
