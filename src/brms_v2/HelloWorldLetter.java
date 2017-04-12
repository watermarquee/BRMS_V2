/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brms_v2;

import java.util.regex.Pattern;
import javax.swing.JOptionPane;

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

        if (!(Pattern.matches("^[0-9]+$", "12"))) {
            JOptionPane.showMessageDialog(null, "Please enter a valid character", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
