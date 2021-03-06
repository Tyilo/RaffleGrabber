package com.gmail.gazlloyd.rafflegrabber.gui;

import java.util.ArrayList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import javax.swing.*;

import com.gmail.gazlloyd.rafflegrabber.*;

public class RaffleFrame extends JFrame {

    private JFileChooser fc;
    private ImageHandler ih;


    /**
     * Create the frame.
     */
    public RaffleFrame(ImageHandler ih, File path) {
        Main.logger.info("Creating raffle frame");
        this.ih = ih;

        setTitle("Raffle Image Grabber");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 275, 251);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setOpaque(false);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JTextPane txtpnWelcomeToThe = new JTextPane();
        txtpnWelcomeToThe.setEditable(false);
        txtpnWelcomeToThe.setOpaque(false);
        txtpnWelcomeToThe.setText("Welcome to the raffle image grabber!\r\n\r\nPress 'Load' to load in an already captured image.\r\nPress 'Capture' to obtain an image from the screen.");
        //txtpnWelcomeToThe.setBounds(10, 11, 239, 88);
        contentPane.add(txtpnWelcomeToThe);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(buttons);

        JButton loadButton = new JButton("Load");
        Action loadAction = new LoadAction();
        loadButton.setAction(loadAction);
        //loadButton.setBounds(65, 111, 113, 23);
        loadButton.setAlignmentX(CENTER_ALIGNMENT);
        buttons.add(loadButton);

        buttons.add(Box.createRigidArea(new Dimension(10,0)));

        JButton captureButton = new JButton("Capture");
        Action captureAction = new CaptureAction();
        captureButton.setAction(captureAction);
        //captureButton.setBounds(51, 145, 141, 23);
        captureButton.setAlignmentX(CENTER_ALIGNMENT);
        buttons.add(captureButton);

        buttons.add(Box.createRigidArea(new Dimension(10,0)));

        JButton exitButton = new JButton("Exit");
        Action exitAction = new ExitAction();
        exitButton.setAction(exitAction);
        //exitButton.setBounds(75, 179, 89, 23);
        exitButton.setAlignmentX(CENTER_ALIGNMENT);
        buttons.add(exitButton);

        pack();

        fc = new JFileChooser(path);
        fc.setMultiSelectionEnabled(false);
        ImageFilter filter = new ImageFilter();
        fc.addChoosableFileFilter(filter);
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setAccessory(new ImagePreview(fc));

        Main.logger.info("Finished creating raffle frame!");

    }
    private class LoadAction extends AbstractAction {
        public LoadAction() {
            putValue(NAME, "Load");
            putValue(SHORT_DESCRIPTION, "Load an already-saved image");
        }
        public void actionPerformed(ActionEvent e) {
            Main.logger.info("Load button pressed");

            int returnVal = fc.showOpenDialog(RaffleFrame.this);
            Main.logger.info("Return value obtained: " + returnVal);

            if(JFileChooser.APPROVE_OPTION == returnVal) {
                File file = fc.getSelectedFile();
                Main.logger.info("File chosen: " + file.getPath());
                BufferedImage img = ih.loadImage(file);
                try {
                    RaffleImage rafimg = ResourceReader.getRaffleImg(img);
                    Entrant entrant = new Entrant(rafimg);
                    Main.logger.info("Entrant found, values: " + entrant);
                    RaffleImageFrame rif = new RaffleImageFrame(entrant);
                    rif.go();
                }
                catch (RaffleImageException rie) {
                    Main.logger.severe("There was an error loading the entrant: " + rie.msg);
                    new RaffleErrorPopup(rie.msg);
                }

            }
        }
    }
    private class ExitAction extends AbstractAction {
        public ExitAction() {
            putValue(NAME, "Exit");
            putValue(SHORT_DESCRIPTION, "Exit the raffle grabber");
        }
        public void actionPerformed(ActionEvent e) {
            Main.logger.info("Exit pressed, exiting...");
            System.exit(0);
        }
    }
    private class CaptureAction extends AbstractAction {
        public CaptureAction() {
            putValue(NAME, "Capture");
            putValue(SHORT_DESCRIPTION, "Capture an image from the screen");
        }
        public void actionPerformed(ActionEvent e) {
            Main.logger.info("Capture pressed");
            BufferedImage img = null;
            RaffleImage rafimg;

            try {
                Main.logger.info("Attempting screen capture...");


                img = this.getImage(false);
                Main.logger.info("Screen captured! Dimensions: " + img.getWidth() + "x" + img.getHeight());

                if (Main.isMac)
                    rafimg = ResourceReader.getRaffleImgSupressed(img);
                else
                    rafimg = ResourceReader.getRaffleImg(img);

                if (rafimg == null) {
                    //Mac can use screencapture to try instead
                    if (Main.isMac) {
                        Main.logger.info("Couldn't find resource window with Robot, using screencapture");
                        img = this.getImage(true);
                        Main.logger.info("Screen captured! Dimensions: " + img.getWidth() + "x" + img.getHeight());

                        rafimg = ResourceReader.getRaffleImg(img);
                    }

                    //other OSs cannot
                    else {
                        throw new RaffleImageException("Could not find resource window");
                    }
                }

                Entrant entrant = new Entrant(rafimg);
                RaffleImageFrame rif = new RaffleImageFrame(entrant);
                rif.go();

            }

            catch (RaffleImageException rie) {
                Main.logger.severe("There was an error loading the entrant: " + rie.msg);

                if(img != null) {
                    try {
                        File f = File.createTempFile("rafflegrabber-", ".png");
                        ImageIO.write(img, "PNG", f);
                        Main.logger.info("Saved the grabbed image to: " + f.getPath());
                    }
                    catch(IOException e1) {
                        Main.logger.severe("Failed to save grabbed image.");
                    }
                }

                new RaffleErrorPopup(rie.msg);
            }

            catch (AWTException ex) {
                Main.logger.severe("There was an error capturing the screen");
                ex.printStackTrace();
            }

        }

        public BufferedImage getImage(Boolean useScreencapture) throws AWTException, RaffleImageException {
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = g.getScreenDevices();
            int deviceNum = devices.length;
            ArrayList<BufferedImage> images = new ArrayList<BufferedImage>(deviceNum);

            //if not mac, use java.awt.Robot
            if(!useScreencapture) {
                Main.logger.info("Attempting screen capture with java.awt.Robot");
                for (GraphicsDevice device : devices) {
                    Robot r = new Robot(device);
                    images.add(r.createScreenCapture(new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize())));
                }
            }
            //if mac, use default screencapture
            else {
                if (!Main.isMac) {
                    Main.logger.severe("OS is not Mac OSX, cannot use screencapture utility");
                    return null;
                }
                Main.logger.info("Attempting screen capture with Mac OSX screencapture utility");
                try {
                    ArrayList<File> files = new ArrayList<File>(deviceNum);
                    ArrayList<String> args = new ArrayList<String>(deviceNum + 2);
                    args.add("/usr/sbin/screencapture");
                    args.add("-x");

                    for (GraphicsDevice device : devices) {
                        File f = File.createTempFile("rafflegrabber-", ".png");
                        files.add(f);
                        String path = f.getPath();
                        args.add(path);
                    }
                    Process p = new ProcessBuilder(args).start();
                    p.waitFor();

                    for (int i = 0; i < deviceNum; i++) {
                        try {
                            File f = files.get(i);
                            BufferedImage img1 = ImageIO.read(f);
                            images.add(img1);
                            f.delete();
                        }
                        catch (Exception e3) {
                            Main.logger.warning("There was a problem reading images");
                            e3.printStackTrace();
                        }
                    }
                }
                catch (Exception e2) {
                    throw new RaffleImageException("Failed to capture image using screencapture!");
                }
            }

            int totalWidth = 0;
            int maxHeight = 0;

            for(int i = 0; i < deviceNum; i++) {
                totalWidth += images.get(i).getWidth();
                maxHeight = Math.max(maxHeight, images.get(i).getHeight());
            }

            BufferedImage img = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_RGB);

            int currentWidth = 0;
            for(int i = 0; i < deviceNum; i++) {
                try {
                    img.createGraphics().drawImage(images.get(i), currentWidth, 0, null);
                    currentWidth += images.get(i).getWidth();
                }

                catch (Exception e3) {
                    Main.logger.warning("Error creating new image");
                    e3.printStackTrace();
                }
            }

            return img;
        }
    }
}
