import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Steven Teplica
 *         <p>
 *         Panel containing the user's watchlist
 */
public class JPanelWatchlist extends JPanel {

    private static JPanel pnlAddRemove = new JPanel(null);
    NonEditableModel tblmdlWatchlist = new NonEditableModel();
    NonEditableModel tblmdlSearchedEquities = new NonEditableModel();
    JTextPlaceholder txtSimpleSearch = new JTextPlaceholder("Find an equity...");
    JTextPlaceholder txtTicker = new JTextPlaceholder("Ticker");
    JTextPlaceholder txtName = new JTextPlaceholder("Equity name");
    JTextPlaceholder txtIndexSector = new JTextPlaceholder("Equity's index/sector");

    Color lowTriggerBG = new Color(243, 100, 100);
    Color lowTriggerFG = new Color(40, 40, 40);

    Color highTriggerBG = new Color(109, 243, 82);
    Color highTriggerFG = new Color(70, 70, 70);

    public JPanelWatchlist() {
        super(new BorderLayout());

        JPanel pnlWatchlist = new JPanel(null);
        pnlWatchlist.setPreferredSize(new Dimension(400, 550));
        pnlWatchlist.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "My Watchlist",
                TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, new Color(0, 0, 0)));
        add(pnlWatchlist, BorderLayout.CENTER);

        JScrollPane watchlistScrollPane = new JScrollPane();
        watchlistScrollPane.setBounds(4, 60, 348, 409);
        pnlWatchlist.add(watchlistScrollPane);

        JTable tblWatchlist = new JTable(tblmdlWatchlist);
        tblmdlWatchlist.addColumn("Ticker");
        tblmdlWatchlist.addColumn("$ / unit");
        tblmdlWatchlist.addColumn("Low Trigger");
        tblmdlWatchlist.addColumn("High Trigger");

        JButton btnResetTriggered = new JButton("Reset Past Triggers");
        btnResetTriggered.setBounds(4, 469, 348, 30);
        btnResetTriggered.addActionListener(e -> {
            for (WatchlistEquity w : PortfolioAdapter.currentUser.getWatchlist().watchlistEquities) {
                w.resetTripHistory();
                update(PortfolioAdapter.currentUser.getWatchlist().watchlistEquities);
            }
        });

        pnlWatchlist.add(btnResetTriggered);


        watchlistScrollPane.setViewportView(tblWatchlist);
        tblWatchlist.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                double price = Double.parseDouble(table.getValueAt(row, 1).toString());
                String lowTrigger = table.getValueAt(row, 2).toString().replaceAll("\\<.*?>", "").replace("-- ", "");
                String highTrigger = table.getValueAt(row, 3).toString().replaceAll("\\<.*?>", "").replace("++ ", "");
                if (!lowTrigger.equals("") && price < Double.parseDouble(lowTrigger)) {
                    c.setBackground(lowTriggerBG);
                    c.setForeground(lowTriggerFG);
                } else if (!highTrigger.equals("") && price > Double.parseDouble(highTrigger)) {
                    c.setBackground(highTriggerBG);
                    c.setForeground(highTriggerFG);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(javax.swing.UIManager.getColor("Table.dropCellForeground"));
                }
                return c;
            }
        });

        JTextPlaceholder txtFilterWatchlist = new JTextPlaceholder("Filter your watchlist...");
        txtFilterWatchlist.setBounds(4, 30, 348, 30);
        pnlWatchlist.add(txtFilterWatchlist);
        txtFilterWatchlist.getDocument().addDocumentListener(new DocumentListener() {
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
                if (txtFilterWatchlist.getText().length() > 0 && !txtFilterWatchlist.getText().equals("Filter your watchlist...")) {
                    update(PortfolioAdapter.searchWatchlistEquities(PortfolioAdapter.currentUser.getWatchlist().watchlistEquities, txtFilterWatchlist.getText()));
                } else {
                    update(PortfolioAdapter.currentUser.getWatchlist().watchlistEquities);
                }
            }
        });

        // ----------------------------- BUY/SELL PANEL -----------------------------

        pnlAddRemove.setPreferredSize(new Dimension(400, 550));
        pnlAddRemove.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Manage Watchlist",
                TitledBorder.CENTER, TitledBorder.BELOW_TOP, null, new Color(0, 0, 0)));
        add(pnlAddRemove, BorderLayout.LINE_END);

        JScrollPane filterScrollPane = new JScrollPane();
        filterScrollPane.setBounds(4, 60, 392, 220);
        pnlAddRemove.add(filterScrollPane);

        JTable tblSearchedEquities = new JTable(tblmdlSearchedEquities);
        tblmdlSearchedEquities.addColumn("Equity Name");
        tblmdlSearchedEquities.addColumn("$ / unit");
        tblmdlSearchedEquities.addColumn("Ticker");
        tblSearchedEquities.getColumnModel().getColumn(0).setPreferredWidth(280);
        tblSearchedEquities.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblSearchedEquities.getColumnModel().getColumn(2).setPreferredWidth(60);

        filterScrollPane.setViewportView(tblSearchedEquities);

        txtSimpleSearch.setBounds(4, 30, 392, 30);
        pnlAddRemove.add(txtSimpleSearch);
        ArrayList<Holding> allEquities = new ArrayList<>(PortfolioAdapter.equities);
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
                if (txtSimpleSearch.getText().equals("Find an equity...") || txtSimpleSearch.getText().length() == 0) {
                    updateSearchResults(allEquities);
                } else if (txtSimpleSearch.getText().length() > 0) {
                    updateSearchResults(PortfolioAdapter.searchEquities(allEquities, txtSimpleSearch.getText()));
                }
            }
        });

        updateSearchResults(allEquities);

        txtName.setBounds(5, 290, 389, 30);
        txtIndexSector.setBounds(5, 325, 192, 30);
        txtTicker.setBounds(202, 325, 192, 30);

        addChange(txtName);
        addChange(txtIndexSector);
        addChange(txtTicker);

        pnlAddRemove.add(txtTicker);
        pnlAddRemove.add(txtName);
        pnlAddRemove.add(txtIndexSector);

        JSeparator sepUpper = new JSeparator();
        sepUpper.setForeground(Color.GRAY);
        sepUpper.setBounds(8, 365, 386, 26);
        pnlAddRemove.add(sepUpper);

        JLabel lblEquity = new JLabel("Equity Ticker:");
        lblEquity.setBounds(10, 380, 190, 30);
        pnlAddRemove.add(lblEquity);

        JTextPlaceholder txtBuySellTicker = new JTextPlaceholder("i.e. GOOG");
        txtBuySellTicker.setBounds(100, 380, 290, 30);
        pnlAddRemove.add(txtBuySellTicker);

        JLabel lblLowTrigger = new JLabel("Low Trigger:");
        lblLowTrigger.setBounds(18, 420, 280, 16);
        pnlAddRemove.add(lblLowTrigger);

        JTextField txtLowTrigger = new JTextField();
        txtLowTrigger.setBounds(100, 415, 90, 30);
        txtLowTrigger.setBackground(Color.white);
        pnlAddRemove.add(txtLowTrigger);
        txtLowTrigger.setColumns(10);

        JLabel lblHighTrigger = new JLabel("High Trigger:");
        lblHighTrigger.setBounds(212, 420, 280, 16);
        pnlAddRemove.add(lblHighTrigger);

        JTextField txtHighTrigger = new JTextField();
        txtHighTrigger.setBounds(300, 415, 90, 30);
        txtHighTrigger.setBackground(Color.white);
        pnlAddRemove.add(txtHighTrigger);
        txtHighTrigger.setColumns(10);

        JSeparator sepLower = new JSeparator();
        sepLower.setForeground(Color.GRAY);
        sepLower.setBounds(8, 453, 386, 26);
        pnlAddRemove.add(sepLower);

        tblSearchedEquities.getSelectionModel().addListSelectionListener(searchEquitiesUpdate -> {
            if (tblSearchedEquities.getSelectedRow() >= 0) {
                txtBuySellTicker.setText(tblSearchedEquities.getValueAt(tblSearchedEquities.getSelectedRow(), 2).toString());
                txtLowTrigger.setText("");
                txtHighTrigger.setText("");
                txtLowTrigger.requestFocus();
                txtBuySellTicker.setFont(new Font(getFont().getFamily(), Font.PLAIN, getFont().getSize()));
                tblSearchedEquities.clearSelection();
            }
        });

        tblWatchlist.getSelectionModel().addListSelectionListener(watchlistUpdate -> {
            if (tblWatchlist.getSelectedRow() >= 0) {
                txtBuySellTicker.setText(tblWatchlist.getValueAt(tblWatchlist.getSelectedRow(), 0).toString());
                txtLowTrigger.setText(tblWatchlist.getValueAt(tblWatchlist.getSelectedRow(), 2).toString().replaceAll("\\<.*?>", "").replace("-- ", ""));
                txtHighTrigger.setText(tblWatchlist.getValueAt(tblWatchlist.getSelectedRow(), 3).toString().replaceAll("\\<.*?>", "").replace("++ ", ""));
                txtBuySellTicker.setFont(new Font(getFont().getFamily(), Font.PLAIN, getFont().getSize()));
                txtLowTrigger.requestFocus();
                tblWatchlist.clearSelection();
            }
        });

        JButton btnAddUpdate = new JButton("Add/Update");
        btnAddUpdate.setBounds(5, 464, 192, 30);
        btnAddUpdate.addActionListener(e -> {
            String equityTicker = txtBuySellTicker.getText().toUpperCase();

            // Check if the equity already exists in the system
            if (equityExists(equityTicker)) {
                double lowTrigger;
                double highTrigger;

                if (Main.isNumeric(txtLowTrigger.getText())) {
                    lowTrigger = Double.parseDouble(txtLowTrigger.getText());
                    if (lowTrigger <= 0) {
                        lowTrigger = 0;
                    }
                } else {
                    lowTrigger = 0;
                }

                if (Main.isNumeric(txtHighTrigger.getText())) {
                    highTrigger = Double.parseDouble(txtHighTrigger.getText());
                    if (highTrigger <= 0) {
                        highTrigger = 0;
                    }
                } else {
                    highTrigger = Double.MAX_VALUE;
                }

                if (highTrigger <= lowTrigger) {
                    JOptionPane.showMessageDialog(Main.frame, "High trigger cannot be less than low trigger", "Bad Input", JOptionPane.OK_OPTION);
                } else {
                    PortfolioAdapter.currentUser.getWatchlist().addWatchlistEquity(equityTicker, highTrigger, lowTrigger);
                }
            } else {
                JOptionPane.showMessageDialog(Main.frame, "Equity ticker does not exist", "Cannot Find Equity", JOptionPane.OK_OPTION);
            }
            update(PortfolioAdapter.currentUser.getWatchlist().watchlistEquities);
            revalidate();
            repaint();
        });
        pnlAddRemove.add(btnAddUpdate);

        JButton btnRemove = new JButton("Remove");
        btnRemove.setBounds(202, 464, 192, 30);
        btnRemove.addActionListener(e -> {
            String equityTicker = txtBuySellTicker.getText().toUpperCase();

            // Iterate through each watchlist equity until a match is found
            PortfolioAdapter.currentUser.getWatchlist().removeWatchlistEquity(equityTicker);
            update(PortfolioAdapter.currentUser.getWatchlist().watchlistEquities);
        });
        pnlAddRemove.add(btnRemove);
    }

    /**
     * Updates the list of owned equities on the UI.
     *
     * @param equities The list of owned equities (used for filtering)
     */
    public void update(ArrayList<WatchlistEquity> equities) {
        tblmdlWatchlist.setRowCount(0);
        for (WatchlistEquity filteredEquity : equities) {
            equities.stream().filter(ownedEquity -> ownedEquity.getTicker().equals(filteredEquity.getTicker())).forEach(ownedEquity -> {
                Object[] data = new Object[4];
                String ticker = ownedEquity.getTicker();

                data[0] = ticker;
                data[1] = String.format("%.2f", PortfolioAdapter.findEquityByTicker(ownedEquity.getTicker()).getUnitPrice());
                data[2] = ownedEquity.getLowTrigger();
                data[3] = ownedEquity.getHighTrigger();
                if ((double)data[2] == 0){
                    data[2] = "";
                } else if (filteredEquity.hasLowTripped()) {
                    ticker = "<html><b>--</b> " + ticker + "</html>";
                    data[2] = "<html><b>--</b> " + ownedEquity.getLowTrigger() + "</html>";
                }
                if ((double)data[3] == Double.MAX_VALUE){
                    data[3] = "";
                } else if (filteredEquity.hasHighTripped()) {
                    ticker = "<html><b>++</b> " + ticker + "</html>";
                    data[3] = "<html><b>++</b> " + ownedEquity.getHighTrigger() + "</html>";
                }

                tblmdlWatchlist.addRow(data);
            });
        }
        revalidate();
        repaint();
    }

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
