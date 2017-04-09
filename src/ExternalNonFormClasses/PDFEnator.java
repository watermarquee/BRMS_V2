/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExternalNonFormClasses;

import ExternalForms.Browser;
import ExternalForms.Network;
import brms_v2.LogIn;
import brms_v2.Main;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author mrRNBean
 */
public class PDFEnator {

    Main main;
    JTable table;
    String data_title, personID;
    int caller;
    int row, column, totalDataSize; //for table data
    float x, y; //for coordinate placement
    float tableX, tableY;
    ArrayList<String> tableHeaderData;

    public PDFEnator(Main main) {
        this.main = main;
    }

    public void callClass(int caller, String data_title, JTable table) {
        this.data_title = data_title;
        this.table = table;
        this.caller = caller; //0 for VIEW CITIZENS
        this.row = table.getRowCount();
        this.column = table.getColumnCount();
        this.totalDataSize = row * column;

        getTableHeaderData();
        createPDF();
    }

    public String getDirectory() {
        CodeSource codeSource = LogIn.class.getProtectionDomain().getCodeSource();
        File jarFile = null;
        try {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jarFile.getParentFile().getPath();
    }

    public void getTableHeaderData() {
        tableHeaderData = new ArrayList<>();
        JTableHeader th = table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        for (int z = 0, a = tcm.getColumnCount(); z < a; z++) {
            TableColumn tc = tcm.getColumn(z);
            tableHeaderData.add(tc.getHeaderValue().toString());
        }
        //fix id
        tableHeaderData.set(0, tableHeaderData.get(0).split(" ")[1]);
    }

    public PdfPTable writeHeaders(Font font) {
        PdfPTable pdftable = new PdfPTable(setTableDimensions());
        pdftable.setTotalWidth(480);
        for (int a = 0; a < this.tableHeaderData.size(); a++) {
            System.out.println("Header #" + a);
            PdfPCell cell1 = new PdfPCell(new Paragraph(this.tableHeaderData.get(a), font));
            cell1.setPaddingBottom(5);
            cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdftable.addCell(cell1);
        }
        return pdftable;
    }

    public float[] setTableDimensions() {
        switch (caller) {
            case 0:
                return new float[]{15, 75, 75, 100}; //view citizens
            case 1:
                return new float[]{15, 100, 100, 75}; //transaction
            case 2:
                return new float[]{15, 100, 75, 50, 75};//request history on profile
            case 3:
                return new float[]{15, 100, 75, 50, 75};//request history on trans
            case 4:
                return new float[]{25, 100};//profile
            default:
                return new float[]{0};
        }
    }

    String app_path = "";

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
        File file3 = new File(jarDir + "\\VALUES.txt");
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

    public void createPDF() {
        try {
            Rectangle one = new Rectangle(PageSize.LETTER);
            Document doc = new Document(one);
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(getDirectory() + "\\" + data_title + ".pdf"));
            //doc.setMargins(1, 1, 1, 1);
            doc.open();
            PdfContentByte canvas = writer.getDirectContent();
            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 11);
            x = one.getWidth();
            y = one.getHeight();
            tableX = (x / 2) - 235;
            tableY = y - 180;

            getHeader();
            Paragraph para = new Paragraph("Province of " + formHeaderValues[0], font);
            para.setAlignment(Element.ALIGN_CENTER);
            doc.add(para);
            Paragraph para2 = new Paragraph("City/Municipality of " + formHeaderValues[1], font);
            para2.setAlignment(Element.ALIGN_CENTER);
            doc.add(para2);
            Paragraph para3 = new Paragraph("Barangay " + formHeaderValues[2], font);
            para3.setAlignment(Element.ALIGN_CENTER);
            doc.add(para3);
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            Paragraph para4 = new Paragraph(data_title, font);
            para4.setAlignment(Element.ALIGN_CENTER);
            doc.add(para4);

            PdfPTable pdftable;

            pdftable = writeHeaders(font);

            //pdftable.writeSelectedRows(0, -1, (x / 2) - 235, y - 180, canvas);
            int counter = 1, rowCounter = 0, columnCounter = 0;
            int yIncrementor = 16;
            boolean test = true;
            while (counter <= this.totalDataSize) {
                //System.out.println("data#: " + counter + "@ (" + rowCounter + "," + (columnCounter) + ") = " + this.table.getValueAt(rowCounter, columnCounter).toString());
                PdfPCell cell1 = new PdfPCell(new Paragraph(this.table.getValueAt(rowCounter, columnCounter++).toString(), font));
                cell1.setPaddingBottom(5);
                cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdftable.addCell(cell1);

                if (counter > 0 && (counter % column) == 0) { //if there are 4 columns
                    //pdftable.writeSelectedRows(0, -1, (x / 2) - 235, (y - 180) - yIncrementor, canvas); //print row of 4 columns
                    yIncrementor += 16; //move to next row coordinate, so y coordinate plus plus
                    rowCounter++; //move to next row
                    columnCounter = 0; //back to start column
                    boolean newPage = false;

                    if ((tableY - ((yIncrementor - 16) + 80)) <= 72) { //if it exceeds 1 inch in footer, new page
//                        System.out.println("(" + tableY + "-((" + yIncrementor + "-16)+" + 16 + "))");
//                        System.out.println("Nanobra? YES; " + (tableY - ((yIncrementor - 16) + 16)));
                        newPage = true;
                    } else if ((tableY - (yIncrementor + 80)) <= 72) {
//                        System.out.println("(" + tableY + "-(" + yIncrementor + "+" + 16 + "))");
//                        System.out.println("Nanobra? YES; " + (tableY - (yIncrementor + 16)));
                        newPage = true;
                    }

                    if (newPage) {
                        pdftable.completeRow();
                        pdftable.writeSelectedRows(0, -1, tableX, tableY, canvas); //print the data for the current page
                        doc.newPage(); //create new page
                        pdftable = writeHeaders(font); //write headers to new page
                        yIncrementor = 16; //restore default
                    }
                }

//                if (counter == this.totalDataSize) { //para lng mudouble ang data
//                    if (test) {
//                        test = false;
//                        counter = 0;
//                        rowCounter = 0;
//                        columnCounter = 0;
//                    }
//                } else if (counter == this.totalDataSize) {
//                    if (!test) {
//                        break;
//                    }
//                }
                counter++;
            }
            pdftable.completeRow();
            pdftable.writeSelectedRows(0, -1, tableX, tableY, canvas);
            doc.close();

        } catch (DocumentException | FileNotFoundException ex) {
            Logger.getLogger(PDFEnator.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void openBrowser(Browser browser) {
        browser.navigate(data_title);
        browser.setVisible(true);
    }

    public void createProfilePDF(String id, ArrayList<String> data) {
        this.data_title = "Citizen Profile";
        this.table = null;
        this.caller = 4;
        this.row = 17;
        this.column = 2;
        this.personID = id;
        this.totalDataSize = row * column;
        tableHeaderData = new ArrayList<>();
        tableHeaderData.add("Label");
        tableHeaderData.add("Information");

        ArrayList<String> dataAll = data;

        try {
            getHeader();
            Document doc = new Document();
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(getDirectory() + "\\" + data_title + ".pdf"));
            Rectangle one = new Rectangle(PageSize.LETTER);
            doc.setPageSize(one);
            //doc.setMargins(1, 1, 1, 1);
            doc.open();
            PdfContentByte canvas = writer.getDirectContent();
            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 11);
            x = one.getWidth();
            y = one.getHeight();
            tableX = (x / 2) - 235;
            tableY = y - 180;

            Paragraph para = new Paragraph("Province of " + formHeaderValues[0], font);
            para.setAlignment(Element.ALIGN_CENTER);
            doc.add(para);
            Paragraph para2 = new Paragraph("City/Municipality of " + formHeaderValues[1], font);
            para2.setAlignment(Element.ALIGN_CENTER);
            doc.add(para2);
            Paragraph para3 = new Paragraph("Barangay " + formHeaderValues[2], font);
            para3.setAlignment(Element.ALIGN_CENTER);
            doc.add(para3);
            Paragraph para4 = new Paragraph(data_title, font);
            para4.setAlignment(Element.ALIGN_CENTER);
            doc.add(para4);

            PdfPTable pdftable;
            pdftable = writeHeaders(font);
            int counter = 1, rowCounter = 0, columnCounter = 0;
            int yIncrementor = 16;
            String label = null, value = null;
            System.out.println(dataAll.size());
            while (counter <= this.row) {
                switch (counter - 1) {
                    case 0:
                        label = "Citizen ID";
                        value = id;
                        break;
                    case 1:
                        label = "Last Name";
                        value = dataAll.get(counter - 2);
                        break;
                    case 2:
                        label = "First Name";
                        value = dataAll.get(counter - 2);
                        break;
                    case 3:
                        label = "Middle Name";
                        value = dataAll.get(counter - 2);
                        break;
                    case 4:
                        label = "Name Suffix";
                        value = dataAll.get(counter - 2);
                        break;
                    case 5:
                        label = "Date of Birth";
                        value = dataAll.get(counter - 2);
                        break;
                    case 6:
                        label = "Gender";
                        value = dataAll.get(counter - 2);
                        break;
                    case 7:
                        label = "Address";
                        value = dataAll.get(counter - 2);
                        break;
                    case 8:
                        label = "Age";
                        value = dataAll.get(counter - 2);
                        break;
                    case 9:
                        label = "Place of Birth";
                        value = dataAll.get(counter - 2);
                        break;
                    case 10:
                        label = "Civil Status";
                        value = dataAll.get(counter - 2);
                        break;
                    case 11:
                        label = "Contact";
                        value = dataAll.get(counter - 2);
                        break;
                    case 12:
                        label = "Zip Code";
                        value = dataAll.get(counter - 2);
                        break;
                    case 13:
                        label = "Precinct Number";
                        value = dataAll.get(counter - 2);
                        break;
                    case 14:
                        label = "Occupation";
                        value = dataAll.get(counter - 2);
                        break;
                    case 15:
                        label = "Email Address";
                        value = dataAll.get(counter - 2);
                        break;
                    case 16:
                        label = "Religion";
                        value = dataAll.get(counter - 2);
                        break;
                }
                if (value.length() <= 0) {
                    value = "N/A";
                }

                System.out.println(label + ": " + value);
                PdfPCell cell1 = new PdfPCell(new Paragraph(label, font));
                cell1.setPaddingBottom(5);
                cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdftable.addCell(cell1);

                PdfPCell cell2 = new PdfPCell(new Paragraph(value, font));
                cell2.setPaddingBottom(5);
                cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
                pdftable.addCell(cell2);

                if (counter > 0 && (counter % column) == 0) { //if there are 4 columns
                    yIncrementor += 16; //move to next row coordinate, so y coordinate plus plus
                    rowCounter++; //move to next row
                    columnCounter = 0; //back to start column
                    boolean newPage = false;

                    if ((tableY - ((yIncrementor - 16) + 80)) <= 72 || (tableY - (yIncrementor + 80)) <= 72) { //if it exceeds 1 inch in footer, new page
                        newPage = true;
                    }

                    if (newPage) {
                        pdftable.completeRow();
                        pdftable.writeSelectedRows(0, -1, tableX, tableY, canvas); //print the data for the current page
                        doc.newPage(); //create new page
                        pdftable = writeHeaders(font); //write headers to new page
                        yIncrementor = 16; //restore default
                    }
                }
                counter++;
            }
            pdftable.completeRow();
            pdftable.writeSelectedRows(0, -1, tableX, tableY, canvas);
            doc.close();

        } catch (DocumentException | FileNotFoundException ex) {
            Logger.getLogger(PDFEnator.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

}
