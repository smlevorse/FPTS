import javax.swing.*;
import java.awt.*;

/**
 * @author Steven Teplica
 *         <p>
 *         Panel where you can set the equity update timer, export your portfolio,
 *         and log out
 */
public class JPanelMyAccount extends JPanel {

    private int minUpdateMS = 60000;

    private JLabel lblUsername = new JLabel();
    private JLabel lblTimer = new JLabel();

    public JPanelMyAccount() {
        super(new BorderLayout());

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel pnlBottom = new JPanel();
        JPanel pnlCenter = new JPanel();

        JButton btnSetTimer = new JButton("Set Timer");
        btnSetTimer.addActionListener(e -> setUpdateDelay());

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());
        btnLogout.setPreferredSize(new Dimension(150, 30));

        JButton btnExport = new JButton("Export Portfolio");
        btnExport.addActionListener(e -> exportPortfolio());
        btnExport.setPreferredSize(new Dimension(150, 30));

        pnlTop.add(lblUsername);

        pnlCenter.add(lblTimer);
        pnlCenter.add(btnSetTimer);

        pnlBottom.add(btnLogout);
        pnlBottom.add(btnExport);

        add(pnlTop, BorderLayout.PAGE_START);
        add(pnlCenter, BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.PAGE_END);
    }

    /**
     * Creates dialogue to let currentUser log out of their Portfolio
     */
    private void logout() {
        Object[] options = {"Save", "Don't Save"};
        int choice = JOptionPane.showOptionDialog(Main.frame, "Would you like to save your portfolio?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        // If choice == 0: Save         choice == 1: Don't Save
        if (choice == 0) {
            Main.saveFPTS();
            PortfolioAdapter.logout();
            Main.focus(Main.pnlLogin);
        } else if (choice == 1) {
            PortfolioAdapter.logout();
            Main.focus(Main.pnlLogin);
        }

        JPanelDashboard.clearHistoryLabel();
        JPanelLogin.txtPassword.requestFocus();
    }

    private void exportPortfolio() {
        JPanel pnlNewAccount = new JPanel(new BorderLayout());
        JTextField dialogueTextField1 = new JTextField();
        dialogueTextField1.setBackground(Color.WHITE);
        pnlNewAccount.add(dialogueTextField1, BorderLayout.PAGE_END);
        Main.makeFocused(dialogueTextField1);
        JLabel lblSetTimer = new JLabel("Export as (file name):");
        pnlNewAccount.add(lblSetTimer, BorderLayout.PAGE_START);

        JOptionPane.showConfirmDialog(null, pnlNewAccount,
                "Export Portfolio", JOptionPane.OK_CANCEL_OPTION);

        // Check for valid input then export portfolio
        if (!dialogueTextField1.getText().equals("")) {
            PortfolioAdapter.currentUser.exportPortfolio(dialogueTextField1.getText());
            update();
        }
    }

    private void setUpdateDelay() {
        JPanel pnlSetTimer = new JPanel(null);
        pnlSetTimer.setPreferredSize(new Dimension(250, 60));

        JLabel lblSetTimer = new JLabel("<html>Set update delay <i>(60 sec minimum)</i></html>");
        lblSetTimer.setBounds(0, 0, 250, 30);
        pnlSetTimer.add(lblSetTimer);

        JTextPlaceholder dlgHour = new JTextPlaceholder("Hour");
        JTextPlaceholder dlgMin = new JTextPlaceholder("Min");
        JTextPlaceholder dlgSec = new JTextPlaceholder("Sec");
        dlgHour.setBounds(0, 30, 60, 30);
        dlgMin.setBounds(70, 30, 60, 30);
        dlgSec.setBounds(140, 30, 60, 30);
        pnlSetTimer.add(dlgHour);
        pnlSetTimer.add(dlgMin);
        pnlSetTimer.add(dlgSec);
        Main.makeFocused(dlgSec);

        int choice = JOptionPane.showConfirmDialog(null, pnlSetTimer,
                "New Transaction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        int delay = 0;
        if (choice == 0 && dlgHour.getText().equals("") && dlgMin.getText().equals("") && dlgSec.getText().equals("")) {
            return;
        }
        if (Main.isNumeric(dlgHour.getText())) {
            delay += Integer.parseInt(dlgHour.getText()) * 60 * 60 * 1000;
        } else {
            delay++;
        }
        if (Main.isNumeric(dlgMin.getText())) {
            delay += Integer.parseInt(dlgMin.getText()) * 60 * 1000;
        } else {
            delay++;
        }
        if (Main.isNumeric(dlgSec.getText())) {
            delay += Integer.parseInt(dlgSec.getText()) * 1000;
        } else {
            delay++;
        }

        // Check for valid input then export portfolio
        if (delay >= minUpdateMS) {
            PortfolioAdapter.currentUser.setUpdateDelayMS(delay);
            update();
        } else if (choice == 0) {
            if (delay == 3) {
                JOptionPane.showMessageDialog(Main.frame, "Input cannot be empty", "Bad Input", JOptionPane.OK_OPTION);
            } else if (delay < minUpdateMS) {
                JOptionPane.showMessageDialog(Main.frame, "Time must be 1 minute or greater", "Bad Input", JOptionPane.OK_OPTION);
            }
            setUpdateDelay();
        }
    }

    /**
     * Called when this Panel's tab is selected
     * Used to retrieve recent information and update Components
     */
    public void update() {

        // Retrieve the currentUser's set update timer, then break it down into
        // hours, minutes, and seconds.
        // Finally, display them nicely as such
        int delay = PortfolioAdapter.currentUser.getUpdateDelayMS();
        int hours = delay / 3600000;
        int minutes = delay % 3600000 / 60000;
        int seconds = delay % 3600000 % 60000 / 1000;

        String label = "<html>Update stocks every: <b>";

        if (hours > 1) {
            label += hours + " hours";
        } else if (hours > 0) {
            label += hours + " hour";
        }
        if (hours > 0 && (minutes > 0 || seconds > 0)) {
            label += ",";
        }
        if (minutes > 1) {
            label += " " + minutes + " minutes";
        } else if (minutes > 0) {
            label += " " + minutes + " minute";
        }
        if (minutes > 0 && seconds > 0) {
            label += ",";
        }
        if (seconds > 1) {
            label += " " + seconds + " seconds";
        } else if (seconds > 0) {
            label += " " + seconds + " second";
        }

        lblTimer.setText(label);
        lblUsername.setText("<html>Signed in as: <b>" + PortfolioAdapter.currentUser.getUsername() + "</html></b>");
    }
}