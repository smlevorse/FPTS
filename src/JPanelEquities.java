import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Steven Teplica
 *         <p>
 *         Panel that lets the user view and buy/sell their equities
 */

public class JPanelEquities extends JPanel {
    // create objects for JPanel to hold
    public static JPanel pnlBuySell = new JPanel(null);
    public static JPanel pnlOwnedEquities = new JPanel(new BorderLayout());
    public static JTable tblEquitiesSummary;
    public static JComboBox cmbSelectedAccount = new JComboBox();
    public static int selectedIndex = 0;

    private static JTextPlaceholder txtSimpleSearch = new JTextPlaceholder("Find an equity...");
    private static JTextPlaceholder txtTicker = new JTextPlaceholder("Ticker");
    private static JTextPlaceholder txtName = new JTextPlaceholder("Equity name");
    private static JTextPlaceholder txtIndexSector = new JTextPlaceholder("Equity's index/sector");

    private NonEditableModel tblmdlOwnedEquities = new NonEditableModel();
    private NonEditableModel tblmdlSearchedEquities = new NonEditableModel();

    public JPanelEquities() {
        super(new BorderLayout());

        JTable tblOwnedEquities = new JTable(tblmdlOwnedEquities);
        tblmdlOwnedEquities.addColumn("Ticker");
        tblmdlOwnedEquities.addColumn("$ / unit");
        tblmdlOwnedEquities.addColumn("Shares");
        tblmdlOwnedEquities.addColumn("Total Value");

        JScrollPane equitiesScrollPane = new JScrollPane();
        equitiesScrollPane.setViewportView(tblOwnedEquities);

        JTextPlaceholder txtFilterOwned = new JTextPlaceholder("Filter your holdings...");
        txtFilterOwned.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                if (txtFilterOwned.getText().length() > 0 && !txtFilterOwned.getText().equals("Filter your holdings...")) {
                    update(
                            PortfolioAdapter.searchEquities(
                                    PortfolioAdapter.currentUser.getHoldings(), txtFilterOwned.getText()));
                } else if (!txtFilterOwned.getText().equals("Filter your holdings...")) {
                    update(PortfolioAdapter.currentUser.getHoldings());
                }
            }
        });

        pnlOwnedEquities.setBounds(4, 30, 348, 468);
        pnlOwnedEquities.add(equitiesScrollPane);
        pnlOwnedEquities.add(txtFilterOwned, BorderLayout.PAGE_START);

        JPanel pnlOwnedEquitiesContainer = new JPanel(null);
        pnlOwnedEquitiesContainer.setPreferredSize(new Dimension(400, 550));
        pnlOwnedEquitiesContainer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "My Equities",
                TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, new Color(0, 0, 0)));
        pnlOwnedEquitiesContainer.add(pnlOwnedEquities);

        // ----------------------------- BUY/SELL PANEL --------------------------------

        JTable tblSearchedEquities = new JTable(tblmdlSearchedEquities);
        tblmdlSearchedEquities.addColumn("Equity Name");
        tblmdlSearchedEquities.addColumn("$ / unit");
        tblmdlSearchedEquities.addColumn("Ticker");
        tblSearchedEquities.getColumnModel().getColumn(0).setPreferredWidth(280);
        tblSearchedEquities.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblSearchedEquities.getColumnModel().getColumn(2).setPreferredWidth(60);

        JScrollPane filterScrollPane = new JScrollPane();
        filterScrollPane.setBounds(4, 60, 392, 180);
        filterScrollPane.setViewportView(tblSearchedEquities);

        txtSimpleSearch.setBounds(4, 30, 392, 30);
        txtSimpleSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                if (!txtSimpleSearch.getText().equals("Find an equity...") && !txtSimpleSearch.getText().equals("")) {
                    updateSearchResults(PortfolioAdapter.refinedSearch(txtSimpleSearch.getText(), txtTicker.getText(), txtName.getText(), txtIndexSector.getText()));
                } else {
                    updateSearchResults(PortfolioAdapter.refinedSearch("", txtTicker.getText(), txtName.getText(), txtIndexSector.getText()));
                }
            }
        });

        add(pnlOwnedEquitiesContainer);
        add(pnlBuySell, BorderLayout.LINE_END);

        txtName.setBounds(5, 250, 389, 30);
        addChange(txtName);

        txtIndexSector.setBounds(5, 285, 192, 30);
        addChange(txtIndexSector);

        txtTicker.setBounds(202, 285, 192, 30);
        addChange(txtTicker);

        JSeparator sepUpper = new JSeparator();
        sepUpper.setForeground(Color.GRAY);
        sepUpper.setBounds(8, 327, 386, 26);

        JLabel lblAccount = new JLabel("Account:");
        lblAccount.setBounds(40, 345, 190, 30);

        cmbSelectedAccount.setBounds(100, 345, 290, 30);
        cmbSelectedAccount.addActionListener(e -> {
            selectedIndex = cmbSelectedAccount.getSelectedIndex();
        });

        JLabel lblEquity = new JLabel("Equity Ticker:");
        lblEquity.setBounds(10, 380, 190, 30);

        JTextPlaceholder txtBuySellTicker = new JTextPlaceholder("i.e. GOOG");
        txtBuySellTicker.setBounds(100, 380, 290, 30);

        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setBounds(40, 420, 280, 16);

        JTextField txtQuantity = new JTextField();
        txtQuantity.setBounds(100, 415, 290, 30);
        txtQuantity.setBackground(Color.white);
        txtQuantity.setColumns(10);

        JSeparator sepLower = new JSeparator();
        sepLower.setForeground(Color.GRAY);
        sepLower.setBounds(8, 453, 386, 26);

        tblSearchedEquities.getSelectionModel().addListSelectionListener(searchEquitiesChange -> {
            if (tblSearchedEquities.getSelectedRow() >= 0) {
                txtBuySellTicker.setText(tblSearchedEquities.getValueAt(tblSearchedEquities.getSelectedRow(), 2).toString());
                txtQuantity.requestFocus();
                txtBuySellTicker.setFont(new Font(getFont().getFamily(), Font.PLAIN, getFont().getSize()));
                tblSearchedEquities.clearSelection();
            }
        });

        tblOwnedEquities.getSelectionModel().addListSelectionListener(ownedEquitiesChange -> {
            if (tblOwnedEquities.getSelectedRow() >= 0) {
                txtBuySellTicker.setText(tblOwnedEquities.getValueAt(tblOwnedEquities.getSelectedRow(), 0).toString());
                txtQuantity.requestFocus();
                txtBuySellTicker.setFont(new Font(getFont().getFamily(), Font.PLAIN, getFont().getSize()));
                tblOwnedEquities.clearSelection();
            }
        });

        JButton btnBuy = new JButton("Buy");
        btnBuy.setBounds(5, 464, 192, 30);
        btnBuy.addActionListener(e -> {
            if (!Main.isNumeric(txtQuantity.getText()) || Double.parseDouble(txtQuantity.getText()) <= 0
                    || txtQuantity.getText().equals("i.e. GOOG") || txtQuantity.getText().equals("")
                    || Double.parseDouble(txtQuantity.getText()) % 1 != 0) {
                JOptionPane.showMessageDialog(Main.frame, "Enter a valid ticker and an integer quantity greater than 0", "Bad Input", JOptionPane.OK_OPTION);
                return;
            }

            String equityTicker = txtBuySellTicker.getText().toUpperCase();
            int quantity = (int)Double.parseDouble(txtQuantity.getText());
            String s = cmbSelectedAccount.getSelectedItem().toString();

            if (selectedIndex > 0) {
                s = s.substring(0, s.indexOf(" ($"));
            }

            // Check if the equity already exists in the system
            if (equityExists(equityTicker) || PortfolioAdapter.indices.contains(equityTicker)) {
                if (selectedIndex == 0) {
                    PortfolioAdapter.currentUser.addTransaction(new BuyEquityTransaction(equityTicker, quantity));
                } else {
                    PortfolioAdapter.currentUser.addTransaction(new BuyEquityTransaction(equityTicker, quantity, s));
                    for (CashAccount cashAccount : PortfolioAdapter.currentUser.getCashAccounts()) {
                        if (cashAccount.getAccountName().equals(s) && cashAccount.getCashAmount() < 0) {
                            JOptionPane.showMessageDialog(Main.frame, "<html>Insufficient funds in account:<br><b>" + cashAccount.getAccountName() + "</b></html>", "Insufficient Funds", JOptionPane.OK_OPTION);
                            PortfolioAdapter.currentUser.getTransactions().get(PortfolioAdapter.currentUser.getTransactions().size() - 1)
                                    .undoTransaction(PortfolioAdapter.currentUser);
                            PortfolioAdapter.currentUser.getTransactions().remove(PortfolioAdapter.currentUser.getTransactions().size() - 1);
                        }
                    }
                }

            } else { // Equity does not exist in the system, user is buying "custom" equity
                // Check if the user already owns the equity
                if (customEquityOwned(equityTicker)) {
                    // Purchase equity
                    if (selectedIndex == 0) {
                        PortfolioAdapter.currentUser.addTransaction(new BuyEquityTransaction(equityTicker, quantity));
                    } else {
                        PortfolioAdapter.currentUser.addTransaction(new BuyEquityTransaction(equityTicker, quantity, s));
                        for (CashAccount cashAccount : PortfolioAdapter.currentUser.getCashAccounts()) {
                            if (cashAccount.getAccountName().equals(s) && cashAccount.getCashAmount() < 0) {
                                PortfolioAdapter.currentUser.getTransactions().get(PortfolioAdapter.currentUser.getTransactions().size() - 1)
                                        .undoTransaction(PortfolioAdapter.currentUser);
                                PortfolioAdapter.currentUser.getTransactions().remove(PortfolioAdapter.currentUser.getTransactions().size() - 1);
                                JOptionPane.showMessageDialog(Main.frame, "<html>Insufficient funds in account:<br><b>" + cashAccount.getAccountName() + "</b></html>", "Insufficient Funds", JOptionPane.OK_OPTION);
                            }
                        }
                    }

                } else { // The user does not own any of the equity

                    // Create a panel for the user to input information about the new equity
                    JPanel pnlImportEquity = new JPanel(null);
                    pnlImportEquity.setPreferredSize(new Dimension(210, 130));

                    JTextPlaceholder txtEquityPrice = new JTextPlaceholder(" New equity's price per share");
                    JTextPlaceholder txtEquityName = new JTextPlaceholder("New equity's name");
                    JLabel lblEquityPrice = new JLabel("Equity Price:");
                    JLabel lblEquityName = new JLabel("Equity Name:");

                    txtEquityPrice.setBounds(10, 30, 220, 30);
                    txtEquityName.setBounds(10, 95, 220, 30);
                    lblEquityPrice.setBounds(10, 0, 220, 30);
                    lblEquityName.setBounds(10, 65, 220, 30);

                    pnlImportEquity.add(txtEquityPrice);
                    pnlImportEquity.add(txtEquityName);
                    pnlImportEquity.add(lblEquityPrice);
                    pnlImportEquity.add(lblEquityName);

                    int choice = JOptionPane.showConfirmDialog(null, pnlImportEquity, "New Equity",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                    if (choice == JOptionPane.OK_OPTION && Main.isNumeric(txtEquityPrice.getText())
                            && Double.parseDouble(txtEquityPrice.getText()) > 0 && !txtEquityName.getText().equals("")
                            && !txtEquityName.getText().equals("New equity's price per share")) {
                        // Purchase equity
                        Double pricePerShare = Double.parseDouble(txtEquityPrice.getText());

                        if (selectedIndex == 0) {
                            PortfolioAdapter.currentUser.addTransaction(new BuyEquityTransaction(equityTicker, quantity,
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX").withZone(ZoneOffset.UTC).format(Instant.now()),
                                    pricePerShare, txtEquityName.getText()));

                        } else {
                            PortfolioAdapter.currentUser.addTransaction(new BuyEquityTransaction(equityTicker, quantity,
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX").withZone(ZoneOffset.UTC).format(Instant.now()),
                                    pricePerShare, txtEquityName.getText(), s));

                            for (CashAccount cashAccount : PortfolioAdapter.currentUser.getCashAccounts()) {
                                if (cashAccount.getAccountName().equals(s) && cashAccount.getCashAmount() < 0) {
                                    PortfolioAdapter.currentUser.getTransactions().get(PortfolioAdapter.currentUser.getTransactions().size() - 1)
                                            .undoTransaction(PortfolioAdapter.currentUser);
                                    PortfolioAdapter.currentUser.getTransactions().remove(PortfolioAdapter.currentUser.getTransactions().size() - 1);
                                    JOptionPane.showMessageDialog(Main.frame, "<html>Insufficient funds in account:<br><b>" + cashAccount.getAccountName() + "</b></html>", "Insufficient Funds", JOptionPane.OK_OPTION);
                                }
                            }
                        }
                    }
                }
            }
            update(PortfolioAdapter.currentUser.getHoldings());
        });

        JButton btnSell = new JButton("Sell");
        btnSell.setBounds(202, 464, 192, 30);
        btnSell.addActionListener(e -> {
            if (!Main.isNumeric(txtQuantity.getText()) || Double.parseDouble(txtQuantity.getText()) <= 0
                    || txtQuantity.getText().equals("i.e. GOOG") || txtQuantity.getText().equals("")) {
                JOptionPane.showMessageDialog(Main.frame, "Enter a valid ticker an an amount greater than 0", "Bad Input", JOptionPane.OK_OPTION);
                return;
            }

            String equityTicker = txtBuySellTicker.getText().toUpperCase();
            Double amount = Double.parseDouble(txtQuantity.getText());
            String s = cmbSelectedAccount.getSelectedItem().toString();

            if (selectedIndex > 0) {
                s = s.substring(0, s.indexOf(" ($"));
            }

            boolean userOwnsEquityToSell = false;
            Holding holdingThatUserIsSelling = null;

            for (Holding holding : PortfolioAdapter.currentUser.getHoldings()) {
                if (holding.identifier.equals(equityTicker)) {
                    userOwnsEquityToSell = true;
                    holdingThatUserIsSelling = holding;
                    break;
                }
            }

            if (userOwnsEquityToSell) {
                // Check if the equity exists in the system
                if (equityExists(equityTicker) || holdingThatUserIsSelling instanceof IndexSectorShare) {
                    PortfolioAdapter.currentUser.addTransaction(new SellEquityTransaction(equityTicker, amount, s));
                } else {
                    // The equity is a "custom" equity, so check if the user owns any of that equity
                    if (customEquityOwned(equityTicker)) {
                        // Sell equity
                        PortfolioAdapter.currentUser.addTransaction(new SellEquityTransaction(equityTicker, amount, s));
                    }
                }
                for (Holding holding : PortfolioAdapter.currentUser.getHoldings()) {
                    if (holding.identifier.equals(equityTicker) && holding.unitsOwned < 0) {
                        PortfolioAdapter.currentUser.getTransactions().get(PortfolioAdapter.currentUser.getTransactions().size() - 1)
                                .undoTransaction(PortfolioAdapter.currentUser);
                        PortfolioAdapter.currentUser.getTransactions().remove(PortfolioAdapter.currentUser.getTransactions().size() - 1);
                        JOptionPane.showMessageDialog(Main.frame, "<html>You don't own that many shares of:<br><b>" + holding.identifier + "</b></html>", "Insufficient Shares", JOptionPane.OK_OPTION);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(Main.frame, "<html>You don't own any shares of:<br><b>" + equityTicker + "</b></html>", "Custom Equity Not Found", JOptionPane.OK_OPTION);
            }
            update(PortfolioAdapter.currentUser.getHoldings());
        });

        pnlBuySell.setPreferredSize(new Dimension(400, 550));
        pnlBuySell.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Manage Equities",
                TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, new Color(0, 0, 0)));
        pnlBuySell.add(filterScrollPane);
        pnlBuySell.add(txtSimpleSearch);
        pnlBuySell.add(txtTicker);
        pnlBuySell.add(txtName);
        pnlBuySell.add(txtIndexSector);
        pnlBuySell.add(sepUpper);
        pnlBuySell.add(lblAccount);
        pnlBuySell.add(cmbSelectedAccount);
        pnlBuySell.add(lblEquity);
        pnlBuySell.add(txtBuySellTicker);
        pnlBuySell.add(lblQuantity);
        pnlBuySell.add(txtQuantity);
        pnlBuySell.add(sepLower);
        pnlBuySell.add(btnBuy);
        pnlBuySell.add(btnSell);
    }

    /**
     * Updates the list of owned equities on the UI.
     *
     * @param equities The list of owned equities (used for filtering)
     */
    public void update(ArrayList<Holding> equities) {

        tblmdlOwnedEquities.setRowCount(0);
        Object[] data = new Object[4];
        for (Holding equity : equities) {
            data[0] = equity.identifier;
            data[1] = String.format("%.2f", equity.getUnitPrice());
            data[2] = (int)equity.unitsOwned;
            data[3] = String.format("%.2f", equity.unitsOwned * equity.getUnitPrice());
            tblmdlOwnedEquities.addRow(data);
        }

        // Retrieve currentUser's CashAccounts and stores them in both ComboBoxes
        ArrayList<CashAccount> choicesAL = PortfolioAdapter.currentUser.getCashAccounts();
        String[] choices = new String[choicesAL.size() + 1];
        for (int i = 0; i < choicesAL.size(); i++) {
            choices[i + 1] = choicesAL.get(i).getAccountName() + " ($" + String.format("%.2f", choicesAL.get(i).getCashAmount()) + ")";
        }
        choices[0] = "External Transaction";

        // Update ComboBox item list
        DefaultComboBoxModel model = new DefaultComboBoxModel(choices);
        cmbSelectedAccount.setModel(model);
        cmbSelectedAccount.setSelectedIndex(selectedIndex);

        for (Component c : pnlOwnedEquities.getComponents()) {
            if (c == tblEquitiesSummary) {
                pnlOwnedEquities.remove(tblEquitiesSummary);
            }
        }

        String[][] footerDataEquities = new String[1][2];
        footerDataEquities[0][0] = "Total Equities Value:";
        footerDataEquities[0][1] = String.format("%.2f", PortfolioAdapter.currentUser.getHoldingValue());
        String[] footerColsEquities = {"Equity Name", "Balance"};
        tblEquitiesSummary = new JTable(footerDataEquities, footerColsEquities);
        tblEquitiesSummary.setEnabled(false);
        tblEquitiesSummary.revalidate();

        pnlOwnedEquities.add(tblEquitiesSummary, BorderLayout.PAGE_END);
        ArrayList<Holding> allEquities = new ArrayList<>(PortfolioAdapter.equities);
        updateSearchResults(allEquities);
        revalidate();
        repaint();
    }

    /**
     * Update search results based on equities
     */
    public void updateSearchResults(ArrayList<Holding> equities) {
        tblmdlSearchedEquities.setRowCount(0);

        for (Holding filteredEquity : equities) {

            equities.stream().filter(ownedEquity -> ownedEquity.identifier.equals(filteredEquity.identifier)).forEach(ownedEquity -> {
                Object[] data = new Object[3];

                data[0] = ownedEquity.name;
                data[1] = String.format("%.2f", ownedEquity.getUnitPrice());
                data[2] = ownedEquity.identifier;
                tblmdlSearchedEquities.addRow(data);
            });
        }
    }

    /**
     * Determines if an equity exists in the system's equity database.
     *
     * @param ticker The equity ticker.
     * @return If the equity exists in the equity database.
     */
    public boolean equityExists(String ticker) {
        for (Equity equity : PortfolioAdapter.equities) {
            if (equity.identifier.equals(ticker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a custom equity is owned by the user.
     *
     * @param ticker The equity ticker.
     * @return If the equity is owned by the user.
     */
    public boolean customEquityOwned(String ticker) {
        for (Holding holding : PortfolioAdapter.currentUser.getHoldings()) {
            if (holding instanceof Equity) {
                Equity e = (Equity) holding;
                if (e.identifier.equals(ticker)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add update to the given JTextPlaceholder
     */
    private void addChange(JTextPlaceholder t) {
        t.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                if (!txtSimpleSearch.getText().equals("Find an equity...") && !txtSimpleSearch.getText().equals("")) {
                    updateSearchResults(PortfolioAdapter.refinedSearch(txtSimpleSearch.getText(), txtTicker.getText(), txtName.getText(), txtIndexSector.getText()));
                } else {
                    updateSearchResults(PortfolioAdapter.refinedSearch("", txtTicker.getText(), txtName.getText(), txtIndexSector.getText()));
                }
            }
        });
    }

    /**
     * Create a NonEditableModel to be used by JPanelEquities
     */
    public class NonEditableModel extends DefaultTableModel {

        NonEditableModel() {
            super();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
