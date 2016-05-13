import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Steven Teplica
 *         <p>
 *         The first panel you see when you launch the program
 *         Lets the user log in or create/import an account
 */

public class JPanelLogin extends JPanel {

    // Creates username and password TextFields
    public static JTextPlaceholder txtUsername = new JTextPlaceholder("Enter your username");
    public static JPasswordPlaceholder txtPassword = new JPasswordPlaceholder();
    public static JLabel lblErrorMessage = new JLabel("* Invalid username or password", JLabel.CENTER);

    public JPanelLogin() {
        super(null);

        // Use special HTML string creation to put the system title across two lines from one Label
        JLabel lblTitle = new JLabel("<html><div style='text-align: center;'>Financial Portfolio<br>Tracking System</html>", JLabel.CENTER);
        lblTitle.setBounds(0, 20, 500, 100);
        lblTitle.setFont(new Font(null, Font.PLAIN, 30));
        Main.centerComponent(lblTitle);

        // Label to tell user of incorrect pnlLogin credentials (invisible at start)
        lblErrorMessage.setBounds(0, 90, 500, 100);
        lblErrorMessage.setFont(new Font(null, Font.PLAIN, 14));
        lblErrorMessage.setForeground(Color.RED);
        lblErrorMessage.setVisible(false);
        Main.centerComponent(lblErrorMessage);

        // Create and center both TextFields
        txtUsername.setBounds(0, 180, Main.txtWidth, Main.txtHeight);
        Main.centerComponent(txtUsername);
        txtUsername.addActionListener(e -> login());
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(txtUsername.getX(), txtUsername.getY() - 25, 150, 30);

        txtPassword.setBounds(0, 235, Main.txtWidth, Main.txtHeight);
        Main.centerComponent(txtPassword);
        txtPassword.addActionListener(e -> login());
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(txtPassword.getX(), txtPassword.getY() - 25, 150, 30);

        // Create and place both Buttons
        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(275, 275, Main.btnWidth, Main.btnHeight);
        btnLogin.addActionListener(e -> login());

        JButton btnCreate = new JButton("Create");
        btnCreate.setBounds(405, 275, Main.btnWidth, Main.btnHeight);
        btnCreate.addActionListener(e -> createDialogue());

        // Add all components to the Panel
        add(lblTitle);
        add(lblErrorMessage);
        add(lblUsername);
        add(lblPassword);
        add(btnLogin);
        add(btnCreate);
        add(txtUsername);
        add(txtPassword);
    }

    /**
     * When called, gets the current text from each TextField and attempts to call PortfolioAdapter.pnlLogin
     * which returns a boolean showing success or failure. If success, add JPanelPortfolio to the main Frame
     */
    private void login() {
        Boolean loginSuccess = Main.portfolioAdapter.login(txtUsername.getText(),
                new String(txtPassword.getPassword()));

        if (loginSuccess) {
            // Login has succeeded. Wipe correct fields and create a JPanelPortfolio object
            Main.frame.remove(Main.pnlPortfolio);
            Main.pnlPortfolio.removeAll();
            Main.pnlPortfolio.add(new JPanelPortfolio());
            Main.frame.add(Main.pnlPortfolio);
            Main.focus(Main.pnlPortfolio);
            ((JPanelPortfolio) Main.pnlPortfolio.getComponent(0)).updateAll(false);
            lblErrorMessage.setVisible(false);
            txtPassword.setText(null);

            // Reset the simulation manager
            SimulationProxy.getInstance().reset();

        } else {
            // Login has failed. Display error Label, wipe correct TextFields and focus correct TextField
            txtPassword.setText(null);
            if (txtUsername.getText().equals("Enter your username") || txtUsername.getText().equals("")) {
                txtUsername.requestFocus();
            } else {
                txtPassword.requestFocus();
            }
            lblErrorMessage.setVisible(true);
        }
    }

    /**
     * Creates a dialogue when the 'Create' btnDeleteAc is clicked
     */
    private void createDialogue() {
        Object[] options = {"New", "Import.."};
        int choice = JOptionPane.showOptionDialog(Main.frame,
                "How would you like to create an account?", "Create an PortfolioAdapter Portfolio",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                options, options[0]);

        if (choice == 0) {
            createPortfolio();
        } else if (choice == 1) {
            importPortfolio();
        }
    }

    /**
     * Creates a new FileChooser that lets a user select their exported Portfolio file to be imported
     */
    private void importPortfolio() {
        // Create FileChooser and set its size
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(600, 400));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(this);

        // If the user chose a file
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get a reference to the file
            File selectedFile = fileChooser.getSelectedFile();
            if (createPortfolio()) {
                PortfolioAdapter.currentUser.importPortfolio(selectedFile);
            }
        }
    }

    /**
     * Retrieves the Strings from each TextField, makes a new Portfolio from them
     * and then adds a fresh instance of the JPanelPortfolio (could not be created until the moment
     * that currentFUser is NOT null)
     */
    private boolean createPortfolio() {
        JPanel pnlCreateAccount = new JPanel(null);
        pnlCreateAccount.setPreferredSize(new Dimension(250, 130));

        JTextPlaceholder txtNewUsername = new JTextPlaceholder("Enter your username");
        JPasswordPlaceholder txtNewPassword = new JPasswordPlaceholder();

        txtNewUsername.setBounds(0, 30, Main.txtWidth, Main.txtHeight);
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(txtNewUsername.getX(), txtNewUsername.getY() - 25, 150, 30);

        txtNewPassword.setBounds(0, 85, Main.txtWidth, Main.txtHeight);
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(txtNewPassword.getX(), txtNewPassword.getY() - 25, 150, 30);

        Main.makeFocused(txtNewUsername);

        pnlCreateAccount.add(txtNewUsername);
        pnlCreateAccount.add(txtNewPassword);
        pnlCreateAccount.add(lblPassword);
        pnlCreateAccount.add(lblUsername);

        int choice = JOptionPane.showConfirmDialog(null, pnlCreateAccount, "Create Portfolio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice == 2 || choice == -1) {
            return false;
        } else if (Main.userExists(txtNewUsername.getText()) || txtNewUsername.getText().replace(" ", "").equals("")
                || txtNewUsername.getText().equals("Enter a unique username")) {
            lblErrorMessage.setVisible(true);
            return false;
        }

        lblErrorMessage.setVisible(false);
        Portfolio newPortfolio = new Portfolio(txtNewUsername.getText(), new String(txtNewPassword.getPassword()));
        JPanelLogin.txtUsername.setText(newPortfolio.getUsername());
        Main.portfolioAdapter.portfolios.add(newPortfolio);
        PortfolioAdapter.currentUser = newPortfolio;
        PortfolioAdapter.startUpdateCycle();
        Main.frame.remove(Main.pnlPortfolio);
        Main.pnlPortfolio.removeAll();
        Main.pnlPortfolio.add(new JPanelPortfolio());
        Main.frame.add(Main.pnlPortfolio);
        Main.focus(Main.pnlPortfolio);
        return true;
    }
}