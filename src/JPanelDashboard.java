import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * @author Steven Teplica
 *         <p>
 *         The first panel you see when you log in, contains a brief overview
 *         of the portfolio's information
 */

public class JPanelDashboard extends JPanel {

    // Creates static references to all fields that must be updated at some point
    private static JTextArea historyTextField = new JTextArea(5, 30);
    private static JPanel pnlCashAccounts = new JPanel(new BorderLayout());
    private static JPanel pnlEquities = new JPanel(new BorderLayout());

    public JPanelDashboard() {
        super(new BorderLayout());

        // Transaction History (Bottom)
        JPanel pnlHistory = new JPanel(new GridLayout(1, 1));
        historyTextField.setEditable(false);
        JScrollPane historyPane = new JScrollPane(historyTextField, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pnlHistory.add(historyPane);
        pnlHistory.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Transaction History", TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM, null, new Color(0, 0, 0)));

        // Cash Accounts (Left)
        pnlCashAccounts.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "My Cash Accounts", TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, new Color(0, 0, 0)));

        // Holdings Owned (Right)
        pnlEquities.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Owned Equities", TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, new Color(0, 0, 0)));

        add(pnlHistory, BorderLayout.PAGE_END);
        add(pnlCashAccounts, BorderLayout.CENTER);
        add(pnlEquities, BorderLayout.LINE_END);
    }

    /**
     * Clears the history label
     */
    public static void clearHistoryLabel() {
        historyTextField.setText("");
    }

    /**
     * Called when this Panel's tab is selected
     * Used to retrieve recent information and update Components
     */
    public void update() {

        // Update Portfolio Cash Accounts
        ArrayList<CashAccount> cashAccountsAL = PortfolioAdapter.currentUser.getCashAccounts();
        String[][] cashAccounts = new String[cashAccountsAL.size()][2];

        for (int i = 0; i < cashAccountsAL.size(); i++) {
            cashAccounts[i][0] = cashAccountsAL.get(i).getAccountName();
            cashAccounts[i][1] = String.format("%.2f", cashAccountsAL.get(i).getCashAmount());
        }
        // Create a new Table with these column names and this fresh data
        String[] columnNamesCashAccounts = {"Account Name", "Balance"};
        JTable tblCashAccounts = new JTable(cashAccounts, columnNamesCashAccounts);
        tblCashAccounts.setEnabled(false);
        JScrollPane scrollCashAccounts = new JScrollPane(tblCashAccounts);
        scrollCashAccounts.setPreferredSize(new Dimension(240, 400));

        cashAccounts = new String[1][2];
        cashAccounts[0][0] = "Total Account Value:";
        cashAccounts[0][1] = String.format("%.2f", PortfolioAdapter.currentUser.getCashAccountValue());
        String[] bottomColNames = {"Account Name", "Balance"};
        JTable tblCashAccountsSummary = new JTable(cashAccounts, bottomColNames);
        tblCashAccountsSummary.setEnabled(false);

        pnlCashAccounts.removeAll();
        pnlCashAccounts.add(scrollCashAccounts, BorderLayout.CENTER);
        pnlCashAccounts.add(tblCashAccountsSummary, BorderLayout.PAGE_END);

        // Update Portfolio Holdings
        ArrayList<Holding> holdingsAL = PortfolioAdapter.currentUser.getHoldings();
        String[][] holdings = new String[holdingsAL.size()][5];

        for (int i = 0; i < holdingsAL.size(); i++) {
            holdings[i][0] = holdingsAL.get(i).identifier;
            holdings[i][1] = holdingsAL.get(i).name;
            holdings[i][2] = Integer.toString((int)holdingsAL.get(i).unitsOwned);
            holdings[i][3] = String.format("%.2f", holdingsAL.get(i).getUnitPrice());
            holdings[i][4] = String.format("%.2f", holdingsAL.get(i).unitsOwned * holdingsAL.get(i).getUnitPrice());
        }

        // Create a new Table with these column names and this fresh data
        String[] columnNamesEquity = {"Ticker", "Equity", "Shares", "$ / unit", "Value"};
        JTable tblEquities;
        tblEquities = new JTable(holdings, columnNamesEquity);
        tblEquities.getColumnModel().getColumn(0).setPreferredWidth(55);
        tblEquities.getColumnModel().getColumn(1).setPreferredWidth(225);
        tblEquities.getColumnModel().getColumn(2).setPreferredWidth(55);
        tblEquities.getColumnModel().getColumn(3).setPreferredWidth(60);
        tblEquities.getColumnModel().getColumn(4).setPreferredWidth(60);
        tblEquities.setEnabled(false);
        JScrollPane scrollEquities = new JScrollPane(tblEquities);
        scrollEquities.setPreferredSize(new Dimension(440, 400));

        cashAccounts = new String[1][2];
        cashAccounts[0][0] = "Total Equities Value:";
        cashAccounts[0][1] = String.format("%.2f", PortfolioAdapter.currentUser.getHoldingValue());
        String[] bottomColEquitiesNames = {"Equity Name", "Balance"};
        JTable tblEquitiesSummary = new JTable(cashAccounts, bottomColNames);
        tblEquitiesSummary.setEnabled(false);

        pnlEquities.removeAll();
        pnlEquities.add(scrollEquities, BorderLayout.CENTER);
        pnlEquities.add(tblEquitiesSummary, BorderLayout.PAGE_END);

        // Update Recent History
        ArrayList<String> historyAL = PortfolioAdapter.currentUser.getTransactionHistory();

        if (historyAL.size() != 0) {
            Collections.reverse(historyAL);

            int historyLimit = 0;
            String strHistory = "";
            // Iterate through all Transaction items in reverse chronological order (recent first)
            // Break up each Transaction into its event String and its date occurred
            // Only displays the 5 most recent Transactions
            for (String s : historyAL) {
                //If portfolio is imported, the timestamp is missing info so add it:
                String tempS = s.substring(s.length() - 18).replace("~", "");
                if (tempS.contains("Z")) {
                    tempS = tempS.replace("Z", ":00Z");
                } else {
                    tempS = tempS.concat(":00Z");
                }
                Calendar c = javax.xml.bind.DatatypeConverter.parseDateTime(tempS);
                String d = c.getTime().toString().replace(":00 ", " ");
                s = s.substring(0, s.length() - 18);
                strHistory += s + " at date:  " + d + "\n";
                if (historyLimit >= 4) {
                    break;
                }
                historyLimit++;
            }
            strHistory = strHistory.replaceAll("\\<.*?>", "");
            historyTextField.setText(strHistory.substring(0, strHistory.length() - 1));

            // Removes any possible scrollbars to make the TextArea feel like a nice display
            DefaultCaret caret = (DefaultCaret) historyTextField.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }
}
