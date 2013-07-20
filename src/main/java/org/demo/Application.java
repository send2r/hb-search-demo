package org.demo;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.demo.ui.DemoUI;

public class Application {

    private static final Log LOG = LogFactory.getLog(Application.class);

    public static void main(String[] args) throws ParseException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    final DemoUI ui = new DemoUI();
                    LOG.info("Launching..");
                    ui.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}