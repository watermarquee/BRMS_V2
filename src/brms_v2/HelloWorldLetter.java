/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brms_v2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Hello World example using the paper size Letter.
 */
public class HelloWorldLetter {

    /**
     * Path to the resulting PDF file.
     */
    public static final String RESULT
            = "hello_letter.pdf";

    /**
     * Creates a PDF file: hello_letter.pdf.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
//        JTextField textField1 = new JTextField();
//        JTextField textField2 = new JTextField();
//        //textField2.setEnabled(false);
//
//        Object[] inputFields = {"Enter Text 01", textField1,
//            "Enter Text 02", textField2};
//
//        int option = JOptionPane.showConfirmDialog(null,
//                inputFields, "Multiple Inputs", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
//
//        if (option == JOptionPane.OK_OPTION) {
//            System.out.println("YEAH");
//            //    String text = textField1.getText() + "\n" + (checkBox.isSelected() ? "Checked" : "Unchecked") + "\n" + textField2.getText() + "\n";
//        }

//        if (!(Pattern.matches("^[0-9]+$", "12"))) {
//            JOptionPane.showMessageDialog(null, "Please enter a valid character", "Error", JOptionPane.ERROR_MESSAGE);
//        }
        String[] columnNames = {"Form ID"};

        Object[][] data = {
            {"Kathy"},
            {"John"},
            {"Sue"},
            {"Jane"},
            {"Joe"}
        };

        JTable formsPanelTable = new JTable(data, columnNames);
        formsPanelTable.setPreferredScrollableViewportSize(new Dimension(50, 70));
        formsPanelTable.setFillsViewportHeight(true);
        formsPanelTable.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        formsPanelTable.setDragEnabled(false);
        JTableHeader head = formsPanelTable.getTableHeader();
        head.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JScrollPane formsPanelSP = new JScrollPane(formsPanelTable);
        formsPanelSP.setPreferredSize(formsPanelSP.getPreferredSize());
        //formsPanelSP.setBorder(BorderFactory.createMatteBorder(0, 25, 0, 25, Color.CYAN));
        
        JLabel jl = new JLabel("Duplicates Events Found in the following dates. Continue?");
        JPanel pan = new JPanel();
        GridLayout gl = new GridLayout(0, 1);
        pan.setLayout(gl);
        pan.add(jl);
        pan.add(formsPanelSP);
        pan.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
        pan.setPreferredSize(pan.getPreferredSize());
        int result = JOptionPane.showConfirmDialog(null, pan, "Warning",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (JOptionPane.OK_OPTION == result) {
            System.out.println("PAK!");
        }
    }
}
