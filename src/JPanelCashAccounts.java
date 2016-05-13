import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Steven Teplica
 *         <p>
 *         Panel where the user can create/delete cash accounts
 *         or perform any action on them (deposit, withdraw, delete)
 */

public class JPanelCashAccounts extends JPanel {

    public static JTable tblCashAccounts;
    public static String clicked = "";
    public static String account = "";
    // Creates primary JPanel and ComboBoxes & TextField for dialogues
    private static JPanel pnlCashAccounts = new JPanel(new BorderLayout());
    private static JComboBox dialogueComboBox1;
    private static JComboBox dialogueComboBox2;
    private static JTextPlaceholder dialogueTextField1;
    private static JTextPlaceholder dialogueTextField2;

    public JPanelCashAccounts() {
        super(new BorderLayout());

        add(pnlCashAccounts, BorderLayout.CENTER);

        // Create all CashAccount buttons
        JPanel pnlButtons = new JPanel(new GridLayout(1, 5));

        JButton btnNewAccount = new JButton("Create Account");
        btnNewAccount.addActionListener(e -> newAccountDialogue());

        JButton btnTransfer = new JButton("Transfer Funds");
        btnTransfer.addActionListener(e -> transferDialogue());

        // Add all buttons
        pnlButtons.add(btnNewAccount);
        pnlButtons.add(btnTransfer);

        add(pnlButtons, BorderLayout.PAGE_END);
    }

    /**
     * Creates a dialogue for cash account creation
     */
    private void newAccountDialogue() {
        // define settings for dialogue
        JPanel pnlNewAccount = new JPanel(null);
        pnlNewAccount.setPreferredSize(new Dimension(250, 125));
        dialogueTextField1 = new JTextPlaceholder("Enter an account name");
        dialogueTextField2 = new JTextPlaceholder("Enter a starting amount");
        JLabel lblAccountName = new JLabel("Account Name:");
        JLabel lblStartingBalance = new JLabel("Starting Balance:");
        dialogueTextField1.setBounds(0, 30, 250, 30);
        dialogueTextField2.setBounds(0, 95, 250, 30);
        lblAccountName.setBounds(0, 0, 250, 30);
        lblStartingBalance.setBounds(0, 65, 250, 30);
        Main.makeFocused(dialogueTextField1);

        pnlNewAccount.add(dialogueTextField1);
        pnlNewAccount.add(dialogueTextField2);
        pnlNewAccount.add(lblAccountName);
        pnlNewAccount.add(lblStartingBalance);

        int choice1 = JOptionPane.showConfirmDialog(null, pnlNewAccount,
                "New Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Creates dialogue
        if (!dialogueTextField1.getText().equals("") && !dialogueTextField2.getText().equals("")
                && Main.isNumeric(dialogueTextField2.getText()) && Double.parseDouble(dialogueTextField2.getText()) >= 0
                && !dialogueTextField1.getText().equals("Enter an account name")) {

            String name = dialogueTextField1.getText();
            double amount = Float.parseFloat(dialogueTextField2.getText());
            // Check for import conflict
            for (CashAccount c : PortfolioAdapter.currentUser.getCashAccounts()) {
                if (c.getAccountName().equals(name)) {
                    String[] buttons = {"Cancel", "Replace Account", "Add Funds"};
                    int choice2 = JOptionPane.showOptionDialog(null, "Account name exists. What would you like to do?", "Account Import Conflict",
                            JOptionPane.WARNING_MESSAGE, 0, null, buttons, buttons[2]);

                    if (choice2 == 2) {
                        c.setCashAmount(c.getCashAmount() + amount);
                    } else if (choice2 == 1) {
                        c.setCashAmount(amount);
                    }

                }
            }
            // Gets current date & time
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
            String date = df.format(new Date()).substring(0, 16) + "Z";
            PortfolioAdapter.currentUser.addTransaction(new CreateCashAccountTransaction(name, amount, date));
            update();
        } else if (choice1 == 0) {
            JOptionPane.showMessageDialog(Main.frame, "<html>Enter a unique account name<br>and a positive starting balance<br>greater or equal to 0", "Bad Input", JOptionPane.OK_OPTION);
            newAccountDialogue();
        }
    }

    /**
     * Creates a fund transfer dialogue
     */
    private void transferDialogue() {
        JPanel pnlTransfer = new JPanel(null);
        pnlTransfer.setPreferredSize(new Dimension(250, 190));

        // Retrieve currentUser's CashAccounts and stores them in both ComboBoxes
        ArrayList<CashAccount> choicesAL = PortfolioAdapter.currentUser.getCashAccounts();
        String[] choices = new String[choicesAL.size()];
        for (int i = 0; i < choicesAL.size(); i++) {
            choices[i] = choicesAL.get(i).getAccountName() + " ($" + Main.round(choicesAL.get(i).getCashAmount()) + ")";
        }

        // Create fresh ComboBoxes and Textfield
        dialogueComboBox1 = new JComboBox(choices);
        dialogueComboBox1.setBounds(0, 30, 250, 30);
        dialogueComboBox2 = new JComboBox(choices);
        dialogueComboBox2.setBounds(0, 95, 250, 30);
        dialogueTextField1 = new JTextPlaceholder("Enter transfer amount");
        dialogueTextField1.setBounds(0, 160, 250, 30);
        Main.makeFocused(dialogueTextField1);
        JLabel lblFrom = new JLabel("From:");
        JLabel lblTo = new JLabel("To:");
        JLabel lblAmount = new JLabel("Amount:");
        lblFrom.setBounds(0, 0, 250, 30);
        lblTo.setBounds(0, 65, 250, 30);
        lblAmount.setBounds(0, 130, 250, 30);

        pnlTransfer.add(dialogueComboBox1);
        pnlTransfer.add(dialogueComboBox2);
        pnlTransfer.add(dialogueTextField1);
        pnlTransfer.add(lblFrom);
        pnlTransfer.add(lblTo);
        pnlTransfer.add(lblAmount);

        // If there are at least 2 accounts, set the second box to autoselect the second account
        if (choicesAL.size() > 1) {
            dialogueComboBox2.setSelectedIndex(1);
        }

        // Creates the dialogue
        int choice = JOptionPane.showConfirmDialog(null, pnlTransfer,
                "Account Transfer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Check for valid input
        if (choice == 0 && dialogueComboBox1.getSelectedIndex() >= 0 && dialogueComboBox2.getSelectedIndex() >= 0
                && Main.isNumeric(dialogueTextField1.getText())
                && dialogueComboBox1.getSelectedIndex() != dialogueComboBox2.getSelectedIndex()
                && Double.parseDouble(dialogueTextField1.getText()) >= 0
                && Double.parseDouble(dialogueTextField1.getText()) % 1 == 0) {

            // Retrieve the account name of the selected item in each ComboBox through substringing
            String s1 = dialogueComboBox1.getSelectedItem().toString();
            String s2 = dialogueComboBox2.getSelectedItem().toString();
            s1 = s1.substring(0, s1.indexOf("$") - 2);
            s2 = s2.substring(0, s2.indexOf("$") - 2);
            Double amount = Double.parseDouble(dialogueTextField1.getText());
            PortfolioAdapter.currentUser.addTransaction(new TransferTransaction(s1, s2, amount));
            for (CashAccount cashAccount : PortfolioAdapter.currentUser.getCashAccounts()) {
                if (cashAccount.getAccountName().equals(s1)) {
                    if (cashAccount.getCashAmount() < 0) {
                        PortfolioAdapter.currentUser.getTransactions().get(PortfolioAdapter.currentUser.getTransactions().size() - 1)
                                .undoTransaction(PortfolioAdapter.currentUser);
                        PortfolioAdapter.currentUser.getTransactions().remove(PortfolioAdapter.currentUser.getTransactions().size() - 1);
                        JOptionPane.showMessageDialog(Main.frame, "<html>Not enough funds in account:<br><b>" + cashAccount.accountName + "</b></html>", "Bad Input", JOptionPane.OK_OPTION);
                        transferDialogue();
                        return;
                    }
                }
            }
            update();
        } else if (choice == 0) {
            JOptionPane.showMessageDialog(Main.frame, "<html>Choose two unique accounts and<br>enter a positive integer transfer amount</html>", "Bad Input", JOptionPane.OK_OPTION);
            transferDialogue();
        }
    }

    /**
     * Takes in a string "delete", "withdraw", or "deposit" and performs the respective action
     *
     * @param s
     */
    public void accountAction(String s) {
        if (s.equals("delete")) {
            int choice = JOptionPane.showConfirmDialog(null, "<html>You are deleting:<br><b>" + clicked + "</b></br></html>",
                    "Delete Account", JOptionPane.OK_CANCEL_OPTION);

            // Check for valid input
            if (choice == 0) {
                PortfolioAdapter.currentUser.addTransaction(new DeleteCashAccountTransaction(clicked));
                JPanelEquities.selectedIndex = 0;
            }
        } else {
            // Create a panel that will be put into the dialogue
            JPanel pnlTransaction = new JPanel(null);
            pnlTransaction.setPreferredSize(new Dimension(250, 60));

            // Retrieve currentUser's CashAccounts and stores them in a ComboBox
            ArrayList<CashAccount> choicesAL = PortfolioAdapter.currentUser.getCashAccounts();
            String[] choices = new String[choicesAL.size() + 1];
            for (int i = 0; i < choicesAL.size(); i++) {
                choices[i + 1] = choicesAL.get(i).getAccountName() + " ($" + Main.round(choicesAL.get(i).getCashAmount()) + ")";
            }
            choices[0] = "Select account..";

            dialogueTextField1 = new JTextPlaceholder("Enter funds amount");
            JLabel lblAmount = new JLabel("Amount:");

            dialogueTextField1.setBounds(0, 30, 250, 30);
            lblAmount.setBounds(0, 0, 250, 30);

            pnlTransaction.add(dialogueTextField1, BorderLayout.PAGE_END);
            Main.makeFocused(dialogueTextField1);

            pnlTransaction.add(lblAmount);

            // Creates dialogue with the panel as its content
            int result = JOptionPane.showConfirmDialog(null, pnlTransaction,
                    "New Transaction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Check for valid input
            if (result == 0 && !dialogueTextField1.getText().equals("") && Main.isNumeric(dialogueTextField1.getText())
                    && Double.parseDouble(dialogueTextField1.getText()) > 0
                    && Double.parseDouble(dialogueTextField1.getText()) % 1 == 0) {

                // If isDeposit is true, then deposit the desired funds. Else, withdraw them
                if (s.equals("deposit")) {
                    PortfolioAdapter.currentUser.addTransaction(new DepositTransaction(clicked, Double.parseDouble(dialogueTextField1.getText())));
                } else {
                    PortfolioAdapter.currentUser.addTransaction(new WithdrawalTransaction(clicked, Double.parseDouble(dialogueTextField1.getText())));
                    for (CashAccount cashAccount : PortfolioAdapter.currentUser.getCashAccounts()) {
                        if (cashAccount.getAccountName().equals(clicked)) {
                            if (cashAccount.getCashAmount() < 0) {
                                PortfolioAdapter.currentUser.getTransactions().get(PortfolioAdapter.currentUser.getTransactions().size() - 1)
                                        .undoTransaction(PortfolioAdapter.currentUser);
                                PortfolioAdapter.currentUser.getTransactions().remove(PortfolioAdapter.currentUser.getTransactions().size() - 1);
                                JOptionPane.showMessageDialog(Main.frame, "<html>Not enough funds in account:<br><b>" + cashAccount.accountName + "</b></html>", "Bad Input", JOptionPane.OK_OPTION);
                                accountAction(s);
                                return;
                            }
                        }
                    }
                }
            } else if (result == 0) {
                JOptionPane.showMessageDialog(Main.frame, "Enter a positive integer money value", "Bad Input", JOptionPane.OK_OPTION);
                accountAction(s);
                return;
            }
        }
        update();
    }

    /**
     * Called when this Panel's tab is selected
     * Used to retrieve recent information and update Components
     */
    public void update() {
        ArrayList<CashAccount> cashAccountsAL = PortfolioAdapter.currentUser.getCashAccounts();
        String[][] footerDataCashAccounts = new String[cashAccountsAL.size()][3];
        for (int i = 0; i < cashAccountsAL.size(); i++) {
            footerDataCashAccounts[i][0] = cashAccountsAL.get(i).getAccountName();
            footerDataCashAccounts[i][1] = String.format("%.2f", cashAccountsAL.get(i).getCashAmount());
        }
        String[] columnNamesCashAccounts = {"Account Name", "Balance", "Actions"};
        tblCashAccounts = new JTable(footerDataCashAccounts, columnNamesCashAccounts) {
            public boolean isCellEditable(int row, int column) {
                if (column == 2) return true;
                return false;
            }
        };
        tblCashAccounts.setEnabled(true);

        JScrollPane scrollCashAccounts = new JScrollPane(tblCashAccounts);
        tblCashAccounts.getColumnModel().getColumn(0).setPreferredWidth(200);
        tblCashAccounts.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblCashAccounts.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblCashAccounts.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        tblCashAccounts.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        tblCashAccounts.getSelectionModel().addListSelectionListener(tableClicked -> {
            if (tblCashAccounts.getSelectedRow() >= 0) {
                clicked = (tblCashAccounts.getValueAt(tblCashAccounts.getSelectedRow(), 0).toString());
            }
            tblCashAccounts.clearSelection();
        });

        footerDataCashAccounts = new String[1][2];
        footerDataCashAccounts[0][0] = "Total Cash Account Value:";
        footerDataCashAccounts[0][1] = String.format("%.2f", PortfolioAdapter.currentUser.getCashAccountValue());
        String[] bottomColNames = {"Account Name", "Balance"};
        JTable tblCashAccountsSummary = new JTable(footerDataCashAccounts, bottomColNames);
        tblCashAccountsSummary.getColumnModel().getColumn(0).setPreferredWidth(170);
        tblCashAccountsSummary.getColumnModel().getColumn(1).setPreferredWidth(430);
        tblCashAccountsSummary.setEnabled(false);

        pnlCashAccounts.removeAll();
        pnlCashAccounts.add(scrollCashAccounts, BorderLayout.CENTER);
        pnlCashAccounts.add(tblCashAccountsSummary, BorderLayout.PAGE_END);
        repaint();
        revalidate();
    }
}

/**
 * A custom TableCellRenderer used to generate the Actions column
 */
class ButtonRenderer extends JPanel implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        removeAll();
        setLayout(new GridLayout(1, 3));
        add(new JButton("Deposit"));
        add(new JButton("Withdraw"));
        add(new JButton("Delete"));
        return this;
    }
}

/**
 * A custom DefaultCellEditor used to generate the Actions column
 */
class ButtonEditor extends DefaultCellEditor {
    protected JPanel pnlButtons;

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        pnlButtons = new JPanel(new GridLayout(1, 3));

        JButton btnDeposit = new JButton("Deposit");
        btnDeposit.addActionListener(deposit -> JPanelPortfolio.cashAccounts.accountAction("deposit"));

        JButton btnWithdraw = new JButton("Withdraw");
        btnWithdraw.addActionListener(withdraw -> JPanelPortfolio.cashAccounts.accountAction("withdraw"));

        JButton btnDeleteAc = new JButton("Delete");
        btnDeleteAc.addActionListener(delete -> JPanelPortfolio.cashAccounts.accountAction("delete"));

        pnlButtons.add(btnDeposit);
        pnlButtons.add(btnWithdraw);
        pnlButtons.add(btnDeleteAc);
    }

    /**
     * Return pnlButtons
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        return pnlButtons;
    }

    public Object getCellEditorValue() {
        return new String();
    }

    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}