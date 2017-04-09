/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brms_v2;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import javax.swing.SwingUtilities;

/**
 *
 * @author mrRNBean
 */
public class BRMS_V2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        NativeInterface.open(); // not sure what else may be needed for this

        SwingUtilities.invokeLater(() -> {
            LogIn logIn = new LogIn();
            logIn.callClass();
        });

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>


    }

}
