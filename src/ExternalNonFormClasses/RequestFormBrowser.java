/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import brms_v2.Main;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author mrRNBean
 */
public final class RequestFormBrowser extends JDialog {

    private final Dimension screenSize;
    private String form_name = "KPForm1_2";
    private final Main main;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;

    public RequestFormBrowser(Main main, Dimension dim, String withSave, String form_name) {
        super(main);
        this.main = main;
        this.screenSize = dim;
        this.form_name = form_name;

        setLayoutProperties();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension((int) (SCREEN_WIDTH), 45));
        JLabel title = new JLabel("Create Form");
        title.setHorizontalAlignment(JLabel.CENTER);
        topPanel.add(title, BorderLayout.CENTER);

        JPanel compsToExperiment = new JPanel();
        compsToExperiment.setPreferredSize(new Dimension((int) SCREEN_WIDTH, (int) 45));
        FlowLayout experimentLayout = new FlowLayout();
        compsToExperiment.setLayout(experimentLayout);
        compsToExperiment.setBackground(Color.darkGray);

        experimentLayout.setAlignment(FlowLayout.CENTER);

        JButton b1 = new JButton("Save");
        JButton b2 = new JButton("Cancel");

        final JWebBrowser j = new JWebBrowser();
        j.setBarsVisible(false);
        j.setStatusBarVisible(false);
        j.setPreferredSize(new Dimension(300, 500));
        j.navigate("C:\\Users\\mrRNBean\\Documents\\NetBeansProjects\\BRMS_V2\\build\\templates\\" + form_name + ".pdf");
        //j.navigate("C:\\Users\\mrRNBean\\Downloads\\" + form_name + ".pdf");

        b1.addActionListener((ActionEvent e) -> {
            disableFormOnly(false); //false for now since wala pa ang database naayo
            j.navigate("C:\\Users\\mrRNBean\\Documents\\NetBeansProjects\\BRMS_V2\\build\\printables\\" + form_name + ".pdf");
        }
        );

        b2.addActionListener((ActionEvent e) -> {
            dispose();
        }
        );

        b1.setPreferredSize(new Dimension((int) (SCREEN_WIDTH / 3), 35));
        b2.setPreferredSize(new Dimension((int) (SCREEN_WIDTH / 3), 35));
        if (withSave.equals("SAVE")) {
            compsToExperiment.add(b1);
        }
        compsToExperiment.add(b2);

        getContentPane().add(BorderLayout.SOUTH, compsToExperiment);
        getContentPane().add(BorderLayout.NORTH, topPanel);
        getContentPane().add(BorderLayout.CENTER, j);

    }

    public void setLayoutProperties() {
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                main.dispose();
            }
        });

        //screen X and Y
        SCREEN_WIDTH = screenSize.width;
        SCREEN_HEIGHT = screenSize.height;
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public void disableFormOnly(boolean choice) { //save data
        try {
            PdfReader pdfTemplate = new PdfReader("C:\\Users\\mrRNBean\\Documents\\NetBeansProjects\\BRMS_V2\\build\\templates\\" + form_name + ".pdf");
            FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\mrRNBean\\Documents\\NetBeansProjects\\BRMS_V2\\build\\printables\\" + form_name + ".pdf");
            PdfStamper stamper = new PdfStamper(pdfTemplate, fileOutputStream);
            stamper.setFormFlattening(true);

            if (choice) { //post-request
                //manipiulate form fields--------------------------------------------------------------------
                stamper.getAcroFields().setField("12", "CHORVES");
                //String x = stamper.getAcroFields().getField("province");
                //-------------------------------------------------------------------------------------------

                //get field form_vars from form tb
                //slice data with delimiter and store to array
                //fetch array
                //use as basis for loop
                //get field using variable name in each array entry
                //every field entry should be added to a string concatenation
                //save string concatenation and form in "printables/" to FormRequest.tb
            }

            stamper.close();
            pdfTemplate.close();
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(RequestFormBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
