import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Steven Teplica
 * @contributors Sean Levorse
 * <p>
 * Panel containing the portfolio simulator
 */
public class JPanelSimulate extends JPanel {

    public static String currDate = "";
    public static String startDate = "";
    private static JPanel pnlEquities = new JPanel(new BorderLayout());
    private static JButton btnStartNext = new JButton("Start Simulation");
    private static JComboBox cmbInterval = new JComboBox();
    private static JTextField txtSteps = new JTextField();
    private static JPanelGraph graph = new JPanelGraph(null);
    private static JProgressBar stepProgress = new JProgressBar(0, 100);
    private static JTextPlaceholder txtPerAnnum = new JTextPlaceholder("5 is 5% per annum");
    private static JLabel tooltip = new JLabel(
            "<html><i><h4>% / Annum Guide</h4>" +
                    "+: Bull Market" +
                    "<br>- : Bear Market" +
                    "<br>0: No-Growth Market</i></html>");
    private static int startStep = 0;
    private static int currStep = 0;
    private static int steps = 0;
    private static boolean isRunning = false;
    private static ArrayList<GraphScore> recentPortfolioValues = new ArrayList<GraphScore>();
    private static Calendar c = Calendar.getInstance();

    public JPanelSimulate() {
        super(new BorderLayout());
        JPanel pnlLeft = new JPanel(new BorderLayout());
        JPanel pnlRight = new JPanel(null);
        pnlRight.setPreferredSize(new Dimension(150, 550));

        // ---------------- SIMULATION CONTROL PANEL -------------------
        btnStartNext.addActionListener(e -> startSimulation());
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> resetSimulation());

        cmbInterval = new JComboBox(TimeInterval.values());
        txtSteps.setBackground(Color.white);

        pnlRight.add(btnStartNext);
        pnlRight.add(btnReset);
        pnlRight.add(new JLabel("Interval"));
        pnlRight.add(cmbInterval);
        pnlRight.add(new JLabel("Steps"));
        pnlRight.add(txtSteps);
        pnlRight.add(new JLabel("Per Annum (%)"));
        pnlRight.add(txtPerAnnum);
        pnlRight.add(stepProgress);
        pnlRight.add(tooltip);


        // Set the bounds and background color of each component
        for (int i = 0; i < pnlRight.getComponents().length; i++) {
            if (i <= 1) {
                pnlRight.getComponent(i).setBounds(5, i * 36 + 24, 140, 30);
            } else {
                pnlRight.getComponent(i).setBounds(5, i * 30 + 29, 140, 30);
            }
        }
        stepProgress.setBounds(5, 8 * 30 + 37, 140, 30);
        tooltip.setBounds(8, 200, 200, 300);

        graph.setBounds(0, -30, 630, 340);
        JPanel pnlTopLeft = new JPanel(null);
        pnlTopLeft.setPreferredSize(new Dimension(630, 340));
        pnlTopLeft.add(graph);

        pnlLeft.add(pnlTopLeft, BorderLayout.PAGE_START);
        pnlLeft.add(pnlEquities, BorderLayout.PAGE_END);

        add(pnlLeft, BorderLayout.CENTER);
        add(pnlRight, BorderLayout.LINE_END);
    }

    /**
     * Starts a fresh simulation and locks all input fields
     */
    private void startSimulation() {
        if (Main.isNumeric(txtSteps.getText()) && Main.isNumeric(txtPerAnnum.getText())
                && ((int) Double.parseDouble(txtSteps.getText())) >= 1) {
            if (PortfolioAdapter.currentUser.getHoldings().size() == 0) {
                JOptionPane.showMessageDialog(Main.frame, "You have no equity shares to simulate", "Empty Simulation", JOptionPane.OK_OPTION);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            startDate = sdf.format(Calendar.getInstance().getTime());

            btnStartNext.setText("Next Step");
            btnStartNext.removeActionListener(btnStartNext.getActionListeners()[0]);
            btnStartNext.addActionListener(e -> nextStep());
            cmbInterval.setEnabled(false);
            txtSteps.setEnabled(false);
            txtPerAnnum.setEnabled(false);

            startStep = currStep;
            steps += Integer.parseInt(txtSteps.getText());
            double perAnnum = Float.parseFloat(txtPerAnnum.getText()) / 100;
            TimeInterval interval = (TimeInterval) cmbInterval.getSelectedItem();

            if (isRunning) {
                SimulationProxy.getInstance().changeSimulation(Integer.parseInt(txtSteps.getText()), interval, perAnnum);
            } else {
                SimulationProxy.getInstance().createSimulation(Integer.parseInt(txtSteps.getText()), interval, perAnnum);
                isRunning = true;
            }
            SimulationProxy.getInstance().run(true);
            nextStep();
        } else {
            JOptionPane.showMessageDialog(Main.frame, "<html>Enter a positive integer number of<br>steps and a percentage per annum</html>", "Bad Input", JOptionPane.OK_OPTION);
        }
    }

    /**
     * Walks through the simulation to the next step
     */
    private void nextStep() {
        currStep++;
        Portfolio[] portfolios = SimulationProxy.getInstance().getResult();
        update(portfolios[currStep]);

        if (currStep >= steps) {

            txtSteps.setEnabled(true);
            txtPerAnnum.setEnabled(true);
            btnStartNext.setText("Continue");
            btnStartNext.removeActionListener(btnStartNext.getActionListeners()[0]);
            btnStartNext.addActionListener(e -> startSimulation());
            cmbInterval.setEnabled(true);
        }
    }

    /**
     * Resets the simulation
     */
    public void resetSimulation() {
        c.setTime(new Date()); // Now use today date
        stepProgress.setValue(0);
        currStep = 0;
        steps = 0;
        isRunning = false;
        currDate = "";
        btnStartNext.setText("Start Simulation");
        btnStartNext.removeActionListener(btnStartNext.getActionListeners()[0]);
        btnStartNext.addActionListener(e -> startSimulation());
        cmbInterval.setEnabled(true);
        txtSteps.setEnabled(true);
        txtSteps.setText("");
        txtSteps.requestFocus();
        txtPerAnnum.setEnabled(true);
        txtPerAnnum.setText("");
        update(PortfolioAdapter.currentUser);
        SimulationProxy.getInstance().reset();

        recentPortfolioValues.clear();
        recentPortfolioValues.add(new GraphScore(PortfolioAdapter.currentUser.getHoldingValue(), 1));
        graph.setScores(recentPortfolioValues);

    }

    /**
     * Called when this Panel's tab is selected
     * Used to retrieve recent information and update Components
     */
    public void update(Portfolio portfolio) {
        ArrayList<Holding> holdingsAL = portfolio.getHoldings();
        String[][] holdings = new String[holdingsAL.size()][5];
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        for (int i = 0; i < holdingsAL.size(); i++) {
            holdings[i][0] = holdingsAL.get(i).identifier;
            holdings[i][1] = holdingsAL.get(i).name;
            holdings[i][2] = Integer.toString((int)holdingsAL.get(i).unitsOwned);
            holdings[i][3] = String.format("%.2f", holdingsAL.get(i).getUnitPrice());
            holdings[i][4] = String.format("%.2f", holdingsAL.get(i).getUnitPrice() * Main.round(holdingsAL.get(i).unitsOwned));
        }

        String[][] cashAccounts = new String[1][2];
        cashAccounts[0][0] = "Total Portfolio Value:";
        cashAccounts[0][1] = String.format("%.2f", portfolio.getHoldingValue());
        String[] bottomColEquitiesNames = {"Equity Name", "Balance"};
        JTable tblEquitiesSummary = new JTable(cashAccounts, bottomColEquitiesNames);
        tblEquitiesSummary.setEnabled(false);

        // Create a new Table with these column names and this fresh data
        String[] columnNamesEquity = {"Ticker", "Equity Name", "Shares", "$ / unit", "Value"};
        JTable tblEquities;
        tblEquities = new JTable(holdings, columnNamesEquity);
        tblEquities.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblEquities.getColumnModel().getColumn(1).setPreferredWidth(450);
        tblEquities.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblEquities.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblEquities.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblEquities.setEnabled(false);
        JScrollPane scrollEquities = new JScrollPane(tblEquities);
        scrollEquities.setBounds(0, 0, 600, 150);

        int sel = cmbInterval.getSelectedIndex();
        if (sel == 0) {
            c.add(Calendar.DATE, 1);
            recentPortfolioValues.add(new GraphScore(portfolio.getHoldingValue(), 1));
        } else if (sel == 1) {
            c.add(Calendar.MONTH, 1);
            recentPortfolioValues.add(new GraphScore(portfolio.getHoldingValue(), c.getActualMaximum(Calendar.DAY_OF_MONTH)));
        } else if (sel == 2) {
            c.add(Calendar.YEAR, 1);
            recentPortfolioValues.add(new GraphScore(portfolio.getHoldingValue(), 365));
        }

        currDate = sdf.format(c.getTime());

        graph.setScores(recentPortfolioValues);

        pnlEquities.removeAll();
        pnlEquities.setPreferredSize(new Dimension(450, 150));
        pnlEquities.add(scrollEquities, BorderLayout.CENTER);
        pnlEquities.add(tblEquitiesSummary, BorderLayout.PAGE_END);

        double progress = (double) (currStep - startStep) / (double) (steps - startStep) * 100;
        stepProgress.setValue((int) progress);
        revalidate();
        repaint();
    }
}
