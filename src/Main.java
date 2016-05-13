import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * @author Steven Teplica
 *
 * instantiates and runs the JFrame window and creates the portfolio adapter
 */
public class Main {

    public static PortfolioAdapter portfolioAdapter = new PortfolioAdapter();
    public static JFrame frame;
    public static JPanelLogin pnlLogin;
    public static JPanel pnlPortfolio;

    public static int txtWidth = 250;
    public static int txtHeight = 30;

    public static int btnWidth = 120;
    public static int btnHeight = 30;

    public static int windowWidth = 800;
    public static int windowHeight = 600;

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        // Load the PortfolioAdapter module
        loadFPTS();

        //To delete a user, use command line args:   -delete username
        if (args.length > 0) {
            if (args[0].equals("-delete")) {
                String usernameToDelete = args[1];
                int indexOfPortfolioToDelete = -1;

                for (int i = 0; i < portfolioAdapter.portfolios.size(); i++) {
                    Portfolio p = portfolioAdapter.portfolios.get(i);
                    if (p.getUsername().equals(usernameToDelete)) {
                        indexOfPortfolioToDelete = i;
                        break;
                    }
                }

                if (indexOfPortfolioToDelete == -1) {
                    System.out.println("User to delete does not exist.");
                } else {
                    portfolioAdapter.portfolios.remove(indexOfPortfolioToDelete);
                    saveFPTS();
                    System.out.println("User " + usernameToDelete + " deleted.");
                }
            }
        }

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // Set up the main frame
        frame = new JFrame("Financial Portfolio Tracking System");
        Dimension window = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(windowWidth, windowHeight);
        frame.setLocation((int) (window.getWidth() / 2 - frame.getWidth() / 2), (int) (window.getHeight() / 2 - frame.getHeight() / 2));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (PortfolioAdapter.currentUser != null) {
                    Object[] options = {"Save", "Don't Save"};
                    int choice = JOptionPane.showOptionDialog(Main.frame, "Would you like to save your portfolio?", "Logout",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                    // If choice == 0: Save         choice == 1: Don't Save
                    if (choice == 0) {
                        Main.saveFPTS();
                        System.exit(1);
                    } else if (choice == 1) {
                        System.exit(1);
                    }
                } else {
                    System.exit(1);
                }
            }
        });

        // Set up both panels and launch frame
        pnlPortfolio = new JPanel();
        pnlLogin = new JPanelLogin();
        frame.add(pnlLogin);
        frame.setVisible(true);
        JPanelLogin.txtUsername.requestFocus();
    }

    /**
     * Sets the focus to a target panel
     *
     * @param targetPanel the target JPanel
     */
    public static void focus(JPanel targetPanel) {
        frame.setContentPane(targetPanel);
        targetPanel.requestFocus();

        // Forces the frame to update
        frame.setSize(windowWidth - 1, windowHeight - 1);
        frame.setSize(windowWidth, windowHeight);
    }

    /**
     * Places a component in the middle of the screen
     *
     * @param c
     */
    public static void centerComponent(Component c) {
        c.setLocation(windowWidth / 2 - c.getWidth() / 2, c.getY());
    }


    /**
     * Checks to see if a file already exists and loads it if it does.
     */
    private static void loadFPTS() {

        File file = new File("FPTS_Output.ser");

        // Check for existing file
        if (file.exists()) {

            // Load the file
            try {
                FileInputStream fileInputStream = new FileInputStream("FPTS_Output.ser");
                ObjectInputStream in = new ObjectInputStream(fileInputStream);

                // Read the serialized file to the PortfolioAdapter object
                portfolioAdapter = (PortfolioAdapter) in.readObject();
                in.close();
                fileInputStream.close();

                // Catch exceptions
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            return;
        }
    }

    /**
     * Saves the system
     */
    public static void saveFPTS() {
        try {
            // Create the file stream
            FileOutputStream fileOutputStream = new FileOutputStream("FPTS_Output.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);

            // Write the serialized portfolioAdapter object
            out.writeObject(portfolioAdapter);
            out.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines if a given string is numeric
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Checks to see if a username exists
     *
     * @param username
     * @return if the username was found
     */
    public static boolean userExists(String username) {
        for (Portfolio p : portfolioAdapter.portfolios) {
            if (username.equals(p.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public static void makeFocused(Component c){
        c.addHierarchyListener(new HierarchyListener()
        {
            public void hierarchyChanged(HierarchyEvent e)
            {
                final Component c = e.getComponent();
                if (c.isShowing() && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
                {
                    Window toplevel = SwingUtilities.getWindowAncestor(c);
                    toplevel.addWindowFocusListener(new WindowAdapter()
                    {
                        public void windowGainedFocus(WindowEvent e)
                        {
                            c.requestFocus();
                        }
                    });
                }
            }
        });
    }

    public static double round(double value) {
        return (double) Math.round(value * 100d) / 100d;
    }
}