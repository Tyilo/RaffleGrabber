package com.gmail.gazlloyd.rafflegrabber.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.eclipse.wb.swing.FocusTraversalOnArray;

public class RaffleErrorPopup extends JDialog {

    /**
     * Create the dialog.
     */
    public RaffleErrorPopup(String msg) {
        Main.logger.info("Creating raffle error popup");
        setIconImage(Toolkit.getDefaultToolkit().getImage(RaffleErrorPopup.class.getResource("/templates/error.png")));
        setTitle("Error");
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setOpaque(false);
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        {
            JPanel panel = new IconPanel();
            contentPanel.add(panel);
        }

        {
            JTextPane textPane = new JTextPane();
            textPane.setText(msg);
            textPane.setOpaque(false);
            textPane.setEditable(false);
            contentPanel.add(textPane);
        }

        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            JButton okButton;
            {
                okButton = new JButton("OK");
                Action okAction = new OKAction();
                okButton.setAction(okAction);
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }

            buttonPane.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{okButton}));
        }

        this.pack();
        go();
    }

    private class IconPanel extends JPanel {
        BufferedImage img;
        public IconPanel() {
            try {
                img = ImageIO.read(RaffleErrorPopup.class.getClassLoader().getResourceAsStream("templates/error.png"));
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void paint(Graphics g) {
            g.drawImage(img, 0, 0, null);
        }

        public Dimension getPreferredSize() {
            return new Dimension(30,30);
        }
    }

    private class OKAction extends AbstractAction {
        public OKAction() {
            putValue(NAME, "OK");
            putValue(SHORT_DESCRIPTION, "Close the dialog");
        }
        public void actionPerformed(ActionEvent e) {
            Main.logger.info("OK button pressed");
            RaffleErrorPopup.this.dispose();
        }
    }

    public void go() {
        setVisible(true);
    }
}
