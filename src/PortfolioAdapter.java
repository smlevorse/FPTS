import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author Paul Hulbert
 *
 * Handles functionality behind portfolio subsystem
 */
public class PortfolioAdapter implements Serializable {

    /**
     * String representing the file address of the equity csv file
     */
    private final static String ASSETS = "./res/equities.csv";
    public static ArrayList<Equity> equities = new ArrayList<>();
    public static Portfolio currentUser = null;
    public static ArrayList<String> indices = new ArrayList<>();
    public static Timer updateTimer;

    private static long DEFAULT_UPDATE_INTERVAL = 60000;
    public ArrayList<Portfolio> portfolios = new ArrayList<>();


    public PortfolioAdapter() {
        PortfolioAdapter.loadEquitiesFile();
    }

    /**
     * loads the list of equities on startup assigning 1 as the amount of shares
     * owned by default
     */
    public static void loadEquitiesFile() {
        // Declare variables
        Equity currentEquity;
        String line;
        String[] fields;

        try {
            // Load the file
            BufferedReader reader = new BufferedReader(new FileReader(ASSETS));

            // Loop through each line until the end
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                fields = parseForFields(line);
                // strip trailing spaces from corrupted csv file
                currentEquity = new Equity(Double.parseDouble(fields[2]), fields[0].trim(), 1, fields[1]);

                //After storing initial fields, remaining fields are either indexes or sectors
                int x = 3;
                while (x < fields.length) {
                    currentEquity.indexSectorNames.add(fields[x]);
                    x++;
                }
                equities.add(currentEquity);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Record indexes and sectors
        for (Equity equity : equities) {
            for (String s : equity.indexSectorNames) {
                if (!indices.contains(s)) {
                    indices.add(s);
                }
            }
        }
    }


    /**
     * builds lines into a series of fields representing needed data from csv
     *
     * @param line a string with info for one equity
     * @return fields an array of an equities info seperated appropriately
     */
    private static String[] parseForFields(String line) {
        // Declare variables
        String[] fields = line.split("\",\"");
        StringBuilder fieldOne = new StringBuilder(fields[0]);
        StringBuilder fieldTwo = new StringBuilder(fields[fields.length - 1]);

        // Remove "
        fieldOne.deleteCharAt(0);
        fieldTwo.deleteCharAt(fieldTwo.length() - 1);

        // Restore fields string
        fields[0] = fieldOne.toString();
        fields[fields.length - 1] = fieldTwo.toString();
        return fields;
    }

    /**
     * TODO: Fix CSV to replace or remove incorrect characters
     * create a timer to update holdings values off of values received from
     * Yahoo financial web service.
     */
    public static void startUpdateCycle() {

        // Create timer
        updateTimer = new java.util.Timer();
        YahooAPI yahoo = new YahooAPI();

        // Task to be executed by timer
        TimerTask updateHoldings = new TimerTask() {
            @Override
            public void run() {
                yahoo.updateSystem();
                if (currentUser != null) {
                    currentUser.getWatchlist().update();
                }
                ((JPanelPortfolio) Main.pnlPortfolio.getComponent(0)).updateAll(true);
            }
        };
        updateTimer.schedule(updateHoldings, DEFAULT_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL);
    }


    /**
     * Searches the equities file for any equities that contain the strin in their identifier, name, index, or sector
     *
     * @param holdings The holdings you want to search through
     * @param query    The string you want to search for
     * @return an ArrayList of Holdings in holdings that contain the query
     */
    public static ArrayList<Holding> searchEquities(ArrayList<Holding> holdings, String query) {

        // Declare variables
        Iterator<Holding> holdingsIt = holdings.iterator();
        ArrayList<Holding> result = new ArrayList<Holding>();

        // Iterate through each equity
        query = query.toUpperCase();
        while (holdingsIt.hasNext()) {
            Holding holding = holdingsIt.next();
            // Account for differences between an Equity and an IndexSectorShare object
            holding.first();
            int count = 0;
            while (!holding.isDone()) {
                String field = holding.getCurrent();
                if (field.toUpperCase().contains(query.toUpperCase())) {
                    result.add(holding);
                    break;
                }
                holding.next();
            }
            holding.first();

        }
        return result;
    }

    /**
     * Searches the equities file for any equities that contain the strin in their identifier, name, index, or sector
     *
     * @param holdings The holdings you want to search through
     * @param query    The string you want to search for
     * @return an ArrayList of Holdings in holdings that contain the query
     */
    public static ArrayList<WatchlistEquity> searchWatchlistEquities(ArrayList<WatchlistEquity> holdings, String query) {

        // Declare variables
        Iterator<WatchlistEquity> holdingsIt = holdings.iterator();
        ArrayList<WatchlistEquity> result = new ArrayList<WatchlistEquity>();

        // Iterate through each equity
        query = query.toUpperCase();
        while (holdingsIt.hasNext()) {
            WatchlistEquity holding = holdingsIt.next();
            // Account for differences between an Equity and an IndexSectorShare object
            if (holding instanceof WatchlistEquity) {
                WatchlistEquity searchEquity = (WatchlistEquity) holding;
                Equity equity = findEquityByTicker(searchEquity.getTicker());
                // Check equity for search term
                if (
                        searchEquity.getTicker().toUpperCase().contains(query) ||
                                equity.name.toUpperCase().contains(query) ||
                                indexSectorContains(equity.indexSectorNames, query)
                        ) {

                    result.add(searchEquity);
                }
            }
        }
        return result;
    }

    public static ArrayList<Holding> refinedSearch(String firstSearch, String ticker, String name, String indexSector) {
        ticker = ticker.toUpperCase();
        if (ticker.equals("TICKER")) {
            ticker = "";
        }
        name = name.toUpperCase();
        if (name.equals("EQUITY NAME")) {
            name = "";
        }
        indexSector = indexSector.toUpperCase();
        if (indexSector.equals("EQUITY'S INDEX/SECTOR")) {
            indexSector = "";
        }

        ArrayList<Holding> allEquities = new ArrayList<>(equities);
        ArrayList<Holding> firstResults = searchEquities(allEquities, firstSearch);
        ArrayList<Holding> refinedSearch = new ArrayList<>();
        for (Holding h : firstResults) {
            refinedSearch.add(h);
        }

        for (Holding h : firstResults) {
            if ((!ticker.equals("") && !h.identifier.toUpperCase().contains(ticker))
                    || (!name.equals("") && !h.name.toUpperCase().contains(name))) {
                refinedSearch.remove(h);
            }
            for (String s : h.indexSectorNames) {
                if (!s.contains(indexSector)) {
                    refinedSearch.remove(h);
                }
            }
        }

        return refinedSearch;
    }

    /**
     * Checks for a search query in an equity's index sector share object
     *
     * @param indexSectors the equity's indexSectorShare array list
     * @param query        the query string in all caps
     * @return if the query string is in the indexSectorStare array list
     */
    private static boolean indexSectorContains(ArrayList<String> indexSectors, String query) {
        for (String is : indexSectors) {
            // See if is contains the query, it is assumed that query is in
            // all caps for efficiency since I am the only one using this method
            if (is.toUpperCase().contains(query)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Searches the list of equities to find the one with the correct ticker
     *
     * @param ticker ticker to search for
     * @return equity with given ticker (returns null if not found)
     */
    public static Equity findEquityByTicker(String ticker) {
        for (Equity equity : equities) {
            if (equity.identifier.equals(ticker)) {
                return equity;
            }
        }
        // Check through all holdings in case of custom equity
        for (Holding holding : currentUser.getHoldings()) {
            if (holding instanceof Equity) {
                Equity equity = (Equity) holding;
                if (equity.identifier.equals(ticker)) {
                    return equity;
                }
            }
        }

        return null;
    }

    /**
     * Log the user out of the system
     */
    public static void logout() {
        updateTimer.cancel();
        updateTimer.purge();
        currentUser = null;
    }

    /**
     * Log the user into the system via authentication of their username and password
     *
     * @param username the username
     * @param password the user's password
     * @return successful login
     */
    public boolean login(String username, String password) {

        // Check each portfolio
        for (Portfolio portfolio : portfolios) {
            // If the user if found, verify password
            if (portfolio.getUsername().equals(username)) {
                if (portfolio.verifyPassword(password)) {
                    currentUser = portfolio;
                    startUpdateCycle();
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }
}
