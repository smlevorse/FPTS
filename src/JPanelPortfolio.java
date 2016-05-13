import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Steven Teplica
 *         <p>
 *         Panel containing all the content and panels you see when you log in
 */

public class JPanelPortfolio extends JPanel {

    public static JPanelCashAccounts cashAccounts;
    // Create a static reference to dashboard to be accessed by the update() method
    public JPanelDashboard dashboard;
    public JPanelEquities equities;
    public JPanelWatchlist watchlist;
    public JPanelSimulate simulate;
    public JPanelHistory history;
    public JPanelMyAccount myAccount;
    public JTabbedPane tabbedPane = new JTabbedPane();

    public JPanelPortfolio() {
        // Create all the unique panels to be added as tabs in the TabbedPane
        dashboard = new JPanelDashboard();
        equities = new JPanelEquities();
        watchlist = new JPanelWatchlist();
        cashAccounts = new JPanelCashAccounts();
        simulate = new JPanelSimulate();
        history = new JPanelHistory();
        myAccount = new JPanelMyAccount();

        // Add each Panel as a tab
        tabbedPane.addTab("Dashboard", dashboard);
        tabbedPane.addTab("Equities", equities);
        tabbedPane.addTab("Watchlist", watchlist);
        tabbedPane.addTab("Cash Accounts", cashAccounts);
        tabbedPane.addTab("Simulate", simulate);
        tabbedPane.addTab("History", history);
        tabbedPane.addTab("My Account", myAccount);

        // This event occurs when a different tab is selected
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                updateAll(false);
            }
        });

        // Make the TabbedPane take up the entire Frame
        tabbedPane.setPreferredSize(new Dimension(776, 540));
        updateAll(false);
        add(tabbedPane);
    }

    /**
     * Updates the default selected Panel, dashboard.
     * Used to refresh it because of initial load not displaying Panel properly
     */
    public void updateAll(boolean yqlUpdate) {
        ArrayList<Holding> allEquities = new ArrayList<>(PortfolioAdapter.equities);
        dashboard.update();
        equities.updateSearchResults(allEquities);
        equities.update(PortfolioAdapter.currentUser.getHoldings());
        watchlist.updateSearchResults(allEquities);
        watchlist.update(PortfolioAdapter.currentUser.getWatchlist().watchlistEquities);
        cashAccounts.update();
        if (tabbedPane.getSelectedIndex() == 4 && !yqlUpdate) {
            simulate.update(PortfolioAdapter.currentUser);
            simulate.resetSimulation();
        }
        history.update();
        myAccount.update();

        revalidate();
        repaint();
    }
}
