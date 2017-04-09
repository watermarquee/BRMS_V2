/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalForms;

import ExternalNonFormClasses.WriteBlob;
import brms_v2.Main;
import ExternalNonFormClasses.SQLConnect;
import brms_v2.LogIn;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.ColorUIResource;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author mrRNBean
 */
public class Browser extends JDialog {

    Main main;
    String path;
    JButton b, c;
    String dateClaim;
    File directory;
    File dirTemplates;
    File dirPrintables;
    WriteBlob writeBlob;
    final JWebBrowser j;
    String formID, formName, transID; //reference for saving
    String app_path = "";
    String documentsPath = "";
    SQLConnect connect;

    public Browser(Main main) {
        super(main);

        connect = new SQLConnect();

        //Frame methods
        //setVisible(true);
        //setLocationRelativeTo(main);
        setResizable(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);

        //get instance of Main
        this.main = main;

        //instantiate 
        writeBlob = new WriteBlob();

        checkReportsFolder();

//        directory = new File("");
//        path = directory.getAbsolutePath(); //path of project folder
        //dirTemplates = new File("C:\\Users\\mrRNBean\\Documents\\NetBeansProjects\\BRMS_V2\\templates");
        dirTemplates = new File(app_path + "\\templates");

        //dirPrintables = new File("C:\\Users\\mrRNBean\\Documents\\NetBeansProjects\\BRMS_V2\\printables");
        dirPrintables = new File(app_path + "\\printables");

        this.documentsPath = System.getProperty("user.home") + "\\Documents";

        //creating Browser Panel and components
        JPanel panelBrowser = new JPanel();
        panelBrowser.setLayout(new BorderLayout());

        j = new JWebBrowser(JWebBrowser.destroyOnFinalization());
        j.setBarsVisible(false);
        j.setStatusBarVisible(false);

        panelBrowser.add(j, BorderLayout.CENTER);

        b = new JButton("Done");

        ActionListener done = (ActionEvent ae) -> {
            File test = new File(documentsPath, formName + ".pdf");
            System.out.println("DOCS FILE: " + test.getAbsolutePath());
            boolean check = test.exists();

            //this means, need to save pdf.
            if (check) {
                JPanel p1 = new JPanel();
                JXDatePicker k = new JXDatePicker();
                k.setDate(new Date());
                p1.add(k);
                int choice = JOptionPane.showConfirmDialog(null, p1,
                        "Select Claim Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice == JOptionPane.OK_OPTION) {
                    Date date = k.getDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
                    dateClaim = sdf.format(date);

                    String newDateReq = sdf.format(new Date());

                    Calendar cal = Calendar.getInstance();
                    sdf = new SimpleDateFormat("HH:mm:ss");
                    String timeReq = sdf.format(cal.getTime());

//                    writeBlob.setFileName(formName);
//                    writeBlob.createQueryValues(transID, formID, newDateReq, dateClaim, timeReq);
//                    writeBlob.write();
//                    
                    //diri natong kuhaon ang unod sa pdf tas iconcat
                    String inputs = getDataFromPDF();
                    connect.storeFormRequestData(createQueryValues(inputs, transID, formID, newDateReq, dateClaim, timeReq));

//--------------//delete form at /templates and /printables
//                    j.navigate(app_path + "\\temp.txt"); //cannot delete file recently navigated cause it's still on use on Acrobat Reader so had to redirect.
                    SQLConnect connect = new SQLConnect();
                    connect.updateTransaction(transID, newDateReq);
                    connect.closeCon();

                    System.out.println("\n*********************************************");
                    System.out.println("Transaction Updated.");
                    System.out.println("*********************************************\n");

                    main.refreshRequestHistoryByBrowser();
                    main.backFromClass("Saved Requested Form");
                    //disableBrowser();
                }
            } else {
                JOptionPane.showMessageDialog(main, "Please Save the Document to My Documents first");
            }
        };

        b.addActionListener(done);
        b.setBackground(new ColorUIResource(189, 195, 198));
        b.setForeground(new ColorUIResource(0, 102, 153));

        c = new JButton("Cancel");

        ActionListener cancel = (ActionEvent ae) -> {
            //----------//delete form at /templates and /printables
            purgeDirectories();
            disableBrowser();
        };

        c.addActionListener(cancel);
        c.setBackground(new ColorUIResource(189, 195, 198));
        c.setForeground(new ColorUIResource(0, 102, 153));

        JPanel compsToExperiment = new JPanel();
        compsToExperiment.setPreferredSize(new Dimension((int) Toolkit.getDefaultToolkit().getScreenSize().width, (int) 45));
        FlowLayout experimentLayout = new FlowLayout();
        compsToExperiment.setLayout(experimentLayout);
        compsToExperiment.setBackground(Color.darkGray);

        experimentLayout.setAlignment(FlowLayout.CENTER);

        b.setPreferredSize(new Dimension((int) (Toolkit.getDefaultToolkit().getScreenSize().width / 3), 35));
        c.setPreferredSize(new Dimension((int) (Toolkit.getDefaultToolkit().getScreenSize().width / 3), 35));

        //if (withSave.equals("SAVE")) {
        compsToExperiment.add(b);
        compsToExperiment.add(c);

//        JPanel panelBrowser2 = new JPanel();
//        panelBrowser2.add(b);
//        panelBrowser2.add(c);
        add(panelBrowser);
        panelBrowser.add(compsToExperiment, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() { //when closed via X button
            @Override
            public void windowClosing(WindowEvent e) {
                //j.navigate(app_path + "\\temp.txt"); //cannot delete file recently navigated cause it's still on use on Acrobat Reader so had to redirect.
                purgeDirectories();
            }
        });
    }

    public String createQueryValues(String inputs, String transId, String formId, String dateReq, String dateClaim, String timeReq) {
        String values = "\"" + inputs + "\",\"" + transId + "\",\"" + formId + "\",\"" + dateReq + "\",\"" + dateClaim + "\",\"" + timeReq + "\"";
        System.out.println("\n*************************************\nVALUES to save to formentry.tbl: " + values + "\n*************************************\n");
        return values;
    }

    public void purgeDirectories() { //can be accessed by main to delete files after close
        purgeDirectoryButKeepSubDirectories(dirTemplates);
        purgeDirectoryButKeepSubDirectories(dirPrintables);
    }

    private void disableBrowser() {
        this.setVisible(false);
    }

    private void purgeDirectoryButKeepSubDirectories(File dir) { //deletes files in folder but not subdir's.
        System.out.println("\n*********************************************");
        System.out.println("Deleting Files at: " + dir.getAbsolutePath());
        System.out.println("*********************************************\n");

        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }

    private void purgeDirectory(File dir) { //deletes subdirectories using recursion
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                purgeDirectory(file);
            }
            file.delete();
        }
    }

    public void deactivateBrowser() {
        if (NativeInterface.isOpen()) {
            NativeInterface.close(); //j.navigate to temp.pdf called after to permanently disable connection to Adobe Reader
            j.navigate(app_path + "\\temp.txt"); //call to be able to be Adobe Reader's recent open file so /printables and /templates can be deleted
            purgeDirectories();
        }
    }

    public void navigate(String transID, String fileName, String formID) { //for form requesting purposes
        this.transID = transID;
        this.formID = formID;
        b.setVisible(true);
        c.setText("Cancel");
        System.out.println("\n**********************************************");
        System.out.println("Navigating to File: " + dirTemplates.getAbsolutePath() + "\\" + (formName = fileName) + ".pdf");
        System.out.println("**********************************************\n");
        j.navigate(dirTemplates.getAbsolutePath() + "\\" + (formName = fileName) + ".pdf");
    }

    public void navigate(String fileName, String folderName) { //for view form purposes
        b.setVisible(false);
        c.setText("Cancel");
        System.out.println("\n**********************************************");
        System.out.println("Navigating to File: " + dirPrintables.getAbsolutePath() + "\\" + (formName = fileName) + ".pdf");
        System.out.println("**********************************************\n");
        if (folderName.equals("printable")) {
            j.navigate(dirPrintables.getAbsolutePath() + "\\" + (formName = fileName) + ".pdf");
        } else if (folderName.equals("template")) {
            j.navigate(dirTemplates.getAbsolutePath() + "\\" + (formName = fileName) + ".pdf");
        } else {
            j.navigate(app_path + "\\" + (formName = fileName) + ".pdf");
        }
    }

    public void navigate(String fileName) { //for view form purposes
        b.setVisible(false);
        c.setText("Cancel");
        System.out.println("\n**********************************************");
        System.out.println("Navigating to File: " + app_path + "\\" + (formName = fileName) + ".pdf");
        System.out.println("**********************************************\n");
        j.navigate(app_path + "\\" + (formName = fileName) + ".pdf");
    }

    public void dumbNavigate() {
        j.navigate(app_path + "\\temp.txt"); //cannot delete file recently navigated cause it's still on use on Acrobat Reader so had to redirect.
    }

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

        //create folder in app path
        File file3 = new File(jarDir + "\\temp.txt");
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

        File file4 = new File(jarDir + "\\VALUES.txt");
        if (!file4.exists()) {
            try {
                if (file4.createNewFile()) {
                    System.out.println("temp is created!");
                } else {
                    System.out.println("Failed to create temp!");
                }
            } catch (IOException ex) {
                Logger.getLogger(Browser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    String formHeaderValues[] = new String[3];

    public void getHeader() {
        FileReader fread = null;
        String line;
        try {
            checkReportsFolder();
            fread = new FileReader(new File(app_path + "\\VALUES.txt"));
            BufferedReader bread = new BufferedReader(fread);
            int x = 0;
            while ((line = bread.readLine()) != null) {
                formHeaderValues[x] = line;
                x++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fread.close();
            } catch (IOException ex) {
                Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String getDataFromPDF() {
        String all_inputs = ""; //concatenated string of inputs in order
        try {

            final int MAX_VARIABLES = connect.getFormVarCount(formID); //get from db
            System.out.println("MAX VARIABLES: " + MAX_VARIABLES);
            //----------------------------------------------------------

            final String DELIMITER = "~"; //pwede mani i-change
            dumbNavigate();
            PdfReader pdfTemplate = new PdfReader(documentsPath + "\\" + formName + ".pdf");
            FileOutputStream fileOutputStream = new FileOutputStream(dirPrintables.getAbsolutePath() + "\\" + formName + ".pdf");
            PdfStamper stamper = new PdfStamper(pdfTemplate, fileOutputStream);
            //System.out.println("TRY: " + stamper.getAcroFields().getField("1"));
            stamper.setFormFlattening(true);
            System.out.println("Display Inputs Per Variable and Concatenate after\n");

            //change textFields--------------------------------------------------------------------
            for (int var = 1; var <= MAX_VARIABLES; var++) {
                String input_from_pdf_field = stamper.getAcroFields().getField("" + var); //get field input using variable
                System.out.println("Input #" + var + " = " + input_from_pdf_field); //display
                all_inputs += DELIMITER + input_from_pdf_field; //concatenate
            }
            stamper.close();
            pdfTemplate.close();
            //--------------------------------------------------------------------------------------
            System.out.println("\nConcatenated String to Store to DB\n");

            System.out.println("\n" + all_inputs + "\n");

            try {

                File pdfFile = new File(documentsPath + "\\" + formName + ".pdf");
                if (pdfFile.exists()) {
                    if (pdfFile.delete()) {
                        //Desktop.getDesktop().open(pdfFile);
                        System.out.println("Form in Documents Deleted");
                    }
                } else {
                    System.out.println("File to delete in My Documents does not exists!");
                }

            } catch (Exception ex) {
            }

            b.setVisible(false);
            c.setText("Done");
            j.navigate(dirPrintables.getAbsolutePath() + "\\" + formName + ".pdf");
            System.out.println(dirPrintables.getAbsolutePath() + "\\" + formName + ".pdf");

        } catch (IOException | DocumentException ex) {
            Logger.getLogger(Browser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return all_inputs;
    }

    public void setDataToPdf(String fName, String formEntryID) {
        try {

            String all_inputs = connect.getFormEntryFormData(formEntryID); //assuming na gusto ka magbutang ug tulo ka value sa first 3 fields

            final String DELIMITER = "~";

            System.out.println("\nSplit Concatenated String and Display\n");
            String split_inputs[] = all_inputs.split(DELIMITER);

            PdfReader pdfTemplate = new PdfReader(dirTemplates.getAbsolutePath() + "\\" + (formName = fName) + ".pdf");
            FileOutputStream fileOutputStream = new FileOutputStream(dirPrintables.getAbsolutePath() + "\\" + formName + ".pdf");
            PdfStamper stamper = new PdfStamper(pdfTemplate, fileOutputStream);
            stamper.setFormFlattening(true);

            //change textFields--------------------------------------------------------------------
            for (int var = 1; var < split_inputs.length; var++) {
                stamper.getAcroFields().setField("" + var, split_inputs[var]); //get field input using variable
                System.out.println("Input #" + var + " = " + split_inputs[var]); //display
            }
            //--------------------------------------------------------------------------------------

            stamper.close();
            pdfTemplate.close();

            b.setVisible(false);
            c.setText("Done");
            j.navigate(dirPrintables.getAbsolutePath() + "\\" + formName + ".pdf");
            System.out.println(dirPrintables.getAbsolutePath() + "\\" + formName + ".pdf");
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(Browser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
