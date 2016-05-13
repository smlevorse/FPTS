import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * @author Steven Teplica
 *         <p>
 *         Panel showing every "transaction" object that the user has created
 *         Contains Undo and Redo functionality
 */

public class JPanelHistory extends JPanel {

    // The main panel that stores all history events
    private static JPanel pnlHistory = new JPanel(new BorderLayout());
    private static JButton btnUndo = new JButton("<  Undo");
    private static JButton btnRedo = new JButton("Redo  >");

    public JPanelHistory() {
        super(new BorderLayout());

        btnUndo.addActionListener(e -> {
            PortfolioAdapter.currentUser.undoTransaction();
            update();
        });
        btnRedo.addActionListener(e -> {
            PortfolioAdapter.currentUser.redoTransaction();
            update();
        });

        add(pnlHistory, BorderLayout.CENTER);
    }

    /**
     * Called when this Panel's tab is selected
     * Used to retrieve recent information and update Components
     */
    public void update() {
        // Get all transactions for currenUser and store them in a 2D array
        ArrayList<String> historyAL = PortfolioAdapter.currentUser.getTransactionHistory();
        Collections.reverse(historyAL);
        String[][] history = new String[historyAL.size()][2];
        for (int i = 0; i < historyAL.size(); i++) {
            // Break up each history string into its event and date occurred
            String s = historyAL.get(i);
            history[i][0] = s.substring(0, s.length() - 18);

            //If portfolio is imported, the timestamp is missing info so add it:
            String tempS = s.substring(s.length() - 18).replace("~", "");
            if (tempS.contains("Z")) {
                tempS = tempS.replace("Z", ":00Z");
            } else {
                tempS = tempS.concat(":00Z");
            }
            Calendar c = javax.xml.bind.DatatypeConverter.parseDateTime(tempS);
            history[i][1] = c.getTime().toString().replace(":00 ", " ");
        }
        String[] columnNamesHistory = {"Event", "Date Occurred"};
        JTable tblHistory = new JTable(history, columnNamesHistory);
        tblHistory.setEnabled(false);
        JScrollPane scrollHistory = new JScrollPane(tblHistory);
        pnlHistory.removeAll();
        pnlHistory.add(scrollHistory, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new GridLayout(1, 2));
        pnlBottom.add(btnUndo);
        pnlBottom.add(btnRedo);
        pnlHistory.add(pnlBottom, BorderLayout.PAGE_END);
    }
}
