import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;


/**
 * @author Paul Hulbert
 * @contributors Sean Levorse
 * <p>
 * A portfolio that represents a user and all their owned equities and equities on their watchlist.
 */
public class Portfolio implements Serializable {

    private String username;
    private int password;
    private ArrayList<Holding> holdings = new ArrayList<>();
    private ArrayList<CashAccount> cashAccounts = new ArrayList<>();

    private ArrayList<Transaction> transactions = new ArrayList<>();
    //List of transactions that can be redone
    private ArrayList<Transaction> undoneTransactions = new ArrayList<>();

    private Watchlist watchlist = new Watchlist();
    private int updateDelayMS = 60000;

    public Portfolio(String username, String password) {
        this.username = username;
        this.password = password.hashCode();
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public ArrayList<Holding> getHoldings() {
        return holdings;
    }

    public ArrayList<CashAccount> getCashAccounts() {
        return cashAccounts;
    }

    public int getUpdateDelayMS() {
        return updateDelayMS;
    }

    public void setUpdateDelayMS(int delaySec) {
        updateDelayMS = delaySec;
    }


    /**
     * Portfolio to apply transaction to
     *
     * @param transaction transaction object to apply
     * @return successful transaction
     */
    public boolean addTransaction(Transaction transaction) {
        transaction.runTransaction(this);
        transactions.add(transaction);

        undoneTransactions.clear();

        //TODO: Make this check whether transaction ran successfully
        return true;
    }


    public boolean undoTransaction() {
        if (transactions.isEmpty() || !transactions.get(transactions.size() - 1).undoable) {
            return false;
        }

        Transaction temp = transactions.remove(transactions.size() - 1);

        temp.undoTransaction(this);

        undoneTransactions.add(temp);


        return true;
    }


    public boolean redoTransaction() {
        if (undoneTransactions.isEmpty()) {
            return false;
        }

        Transaction temp = undoneTransactions.remove(undoneTransactions.size() - 1);

        temp.runTransaction(this);

        transactions.add(temp);


        return true;
    }


    /**
     * @param ID       ticker/index
     * @param quantity how many to buy
     * @return total cost  (-1.0 if invalid ticker)
     */
    public Double buy(String ID, Double quantity) {

        Equity equityToBuy = PortfolioAdapter.findEquityByTicker(ID);

        for (Holding holding : holdings) {

            if (holding.identifier.equals(ID)) {
                holding.unitsOwned += quantity;
                return quantity * holding.pricePerUnit;
            }
        }

        if (equityToBuy == null) {
            IndexSectorShare indexSectorShare = new IndexSectorShare(ID, quantity);
            holdings.add(indexSectorShare);

            //TODO: Make this check to make sure index exists
            return quantity * indexSectorShare.getUnitPrice();
        }

        Equity newEquity = new Equity(equityToBuy.getUnitPrice(), ID, quantity, equityToBuy.name);

        holdings.add(newEquity);


        return quantity * newEquity.pricePerUnit;
    }

    /**
     * @param ID           ticker/index
     * @param quantity     how many were bought
     * @param date         when was it purchased
     * @param pricePerUnit how much was paid per unit
     * @param nameOfEquity full name of equity to buy
     */
    public void buy(String ID, double quantity, String date, double pricePerUnit, String nameOfEquity) {

        for (Holding holding : holdings) {

            if (holding.identifier.equals(ID)) {
                holding.unitsOwned += quantity;
                return;
            }
        }

        Equity newEquity = new Equity(pricePerUnit, ID, quantity, nameOfEquity);

        holdings.add(newEquity);
    }


    /**
     * @param ID       ticker/index
     * @param quantity how many to sell
     * @return total sale price (-1.0 if invalid)
     */
    public double sell(String ID, double quantity) {
        Equity equityToSell = PortfolioAdapter.findEquityByTicker(ID);


        Holding holdingToSell = null;

        for (Holding holding : holdings) {

            if (holding.identifier.equals(ID)) {


                holding.unitsOwned -= quantity;


                holdingToSell = holding;
            }
        }

        if (holdingToSell == null) {
            return -1.0;
        }

        if (holdingToSell.unitsOwned == 0) {
            holdings.remove(holdingToSell);
        }

        return quantity * holdingToSell.pricePerUnit;


    }

    /**
     * @param name        name of cash account
     * @param amount      amount in cash account
     * @param dateCreated date cash account was created
     * @return true if successfully created
     */
    public boolean createCashAccount(String name, double amount, String dateCreated) {

        for (CashAccount cashAccount : cashAccounts) {
            if (cashAccount.getAccountName().equals(name)) {
                return false;
            }
        }

        cashAccounts.add(new CashAccount(name, dateCreated, amount));
        return true;

    }

    /**
     * @param name account to delete
     * @return true if successfully deleted
     */
    public boolean deleteCashAccount(String name) {
        for (int i = 0; i < cashAccounts.size(); i++) {
            if (cashAccounts.get(i).getAccountName().equals(name)) {
                cashAccounts.remove(i);
                return true;
            }
        }

        return false;
    }


    /**
     * @param name   name of cash account
     * @param amount amount to deposit
     * @return true is successfully deposited
     */
    public boolean deposit(String name, double amount) {
        for (CashAccount cashAccount : cashAccounts) {
            if (cashAccount.getAccountName().equals(name)) {
                cashAccount.setCashAmount(cashAccount.getCashAmount() + amount);
                return true;
            }
        }

        return false;
    }


    /**
     * @param name   name of the cash account
     * @param amount amount to withdraw
     * @return 1 if successful, 0 if account not found, -1 if account doesn't have enough funds
     */
    public int withdrawal(String name, double amount) {
        for (CashAccount cashAccount : cashAccounts) {
            if (cashAccount.getAccountName().equals(name)) {

                cashAccount.setCashAmount(cashAccount.getCashAmount() - amount);
                return 1;
            }
        }

        return 0;
    }

    /**
     * @param source      name of source cash account
     * @param destination name of target cash account
     * @param amount      amount to transfer
     * @return 1 if successful, -1 if insufficient funds, 2 if unable to find source, 3 if unable to find target
     */
    public int transferFunds(String source, String destination, double amount) {

        CashAccount sourceAccount = null;
        CashAccount targetAccount = null;

        for (CashAccount cashAccount : cashAccounts) {
            if (cashAccount.getAccountName().equals(source)) {
                sourceAccount = cashAccount;
            }
            if (cashAccount.getAccountName().equals(destination)) {
                targetAccount = cashAccount;
            }
        }

        if (sourceAccount == null) {
            return 2;
        }
        if (targetAccount == null) {
            return 3;
        }

        sourceAccount.setCashAmount(sourceAccount.getCashAmount() - amount);
        targetAccount.setCashAmount(targetAccount.getCashAmount() + amount);


        return 0;
    }


    /**
     * Takes a line and breaks it into tokens while handling commas in the ticker.
     *
     * @param line string to parse
     * @return the list of tokens
     */
    private String[] importPortfolioHelper(String line) {

        boolean inQuote = false;


        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '"') {
                inQuote = !inQuote;
            } else if (line.charAt(i) == ',' && !inQuote) {
                line = line.substring(0, i) + "_" + line.substring(i + 1);
            }
        }

        line = line.replaceAll("\"", "");


        return line.split("_");
    }

    /**
     * Takes a file and converts it into portfolio data
     * <p>
     * Export standard:
     * https://docs.google.com/document/d/1R1wO4254QWiOaoGbpM7IyeY4qxamMkkAvZOkhbHPURk/edit
     *
     * @param file file to import into portfolio
     */
    public void importPortfolio(File file) {
        try {
            Scanner scanner = new Scanner(file);


            //Import holdings
            while (true) {
                String line = scanner.nextLine();

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                df.setTimeZone(tz);
                String date = df.format(new Date());

                if (!line.contains("\"")) {
                    break;
                }
                //line = line.replaceAll("\"","");
                //String[] tokens = line.split(",");
                String[] tokens = importPortfolioHelper(line);

                String ticker = tokens[0];
                String name = tokens[1];
                double pricePerShare = Double.valueOf(tokens[2]);
                String indexSector1 = tokens[3];
                String indexSector2;
                double numberOfSharesOwned;

                //Two index sector shares
                if (tokens.length == 6) {
                    indexSector2 = tokens[4];
                    numberOfSharesOwned = Double.valueOf(tokens[5]);
                } else {
                    numberOfSharesOwned = Double.valueOf(tokens[4]);
                }

                buy(ticker, numberOfSharesOwned, date, pricePerShare, name);
            }


            //Import index/sector shares
            while (true) {
                String line = scanner.nextLine();

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                df.setTimeZone(tz);
                String date = df.format(new Date());

                if (!line.contains("\"")) {
                    break;
                }
                //line = line.replaceAll("\"","");
                //String[] tokens = line.split(",");
                String[] tokens = importPortfolioHelper(line);

                String name = tokens[0];
                double amount = Double.valueOf(tokens[1]);


                buy(name, amount);
            }


            //Transaction history
            while (true) {
                String line = scanner.nextLine();


                if (!line.contains("\"")) {
                    break;
                }
                //line = line.replaceAll("\"","");
                //String[] tokens = line.split(",");
                String[] tokens = importPortfolioHelper(line);

                String details = tokens[0];
                String date = tokens[1];

                addTransaction(new StringHistoryTransaction(details, date));


            }


            //Cash accounts
            while (true) {
                String line = scanner.nextLine();


                if (!line.contains("\"")) {
                    break;
                }
                //line = line.replaceAll("\"","");
                //String[] tokens = line.split(",");
                String[] tokens = importPortfolioHelper(line);

                String name = tokens[0];
                String date = tokens[1];
                double amount = Double.valueOf(tokens[2]);

                createCashAccount(name, amount, date);


            }

            //Watchlist
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();


                if (!line.contains("\"")) {
                    break;
                }
                //line = line.replaceAll("\"","");
                //String[] tokens = line.split(",");
                String[] tokens = importPortfolioHelper(line);

                String ticker = tokens[0];
                double minTrigger = Double.valueOf(tokens[1]);
                double maxTrigger = Double.valueOf(tokens[2]);

                watchlist.addWatchlistEquity(ticker, minTrigger, maxTrigger);


            }


            //This commented block of code is for when we used to import the transaction history and
            //use that to make all the transactions.
            /*
            // Read the transaction history
            while (true) {
                String line = scanner.nextLine();

                // Break at the end of transaction history
                if (!line.contains("\"")) {
                    break;
                }

                // Get the parts of the transaction history
                String firstHalf = line.split(",")[0];
                firstHalf = firstHalf.substring(1, firstHalf.length() - 1);
                String[] firstHalfSplit = firstHalf.split(" ");

                String secondHalf = line.split(",")[1];
                secondHalf = secondHalf.substring(1, secondHalf.length() - 2);

                // Determine what to do based on the first word of the transaction history
                if (firstHalfSplit[0].equals("Created")) {
                    String name = firstHalfSplit[5];
                    Double amount = Double.parseDouble(firstHalfSplit[7].substring(1, firstHalfSplit[7].length()));
                    String dateCreated = secondHalf;
                    addTransaction(new CreateCashAccountTransaction(name, amount, dateCreated));
                } else if (firstHalfSplit[0].equals("Deleted")) {
                    String name = firstHalfSplit[4];
                    addTransaction((new DeleteCashAccountTransaction(name)));
                } else if (firstHalfSplit[0].equals("Bought")) {
                    String ID = firstHalfSplit[4];
                    double quantity = Double.parseDouble(firstHalfSplit[1]);
                    addTransaction(new BuyEquityTransaction(ID, quantity));
                } else if (firstHalfSplit[0].equals("Deposited")) {
                    String name = firstHalfSplit[3];
                    double amount = Double.parseDouble(firstHalfSplit[1].substring(1, firstHalfSplit[1].length()));
                    addTransaction(new DepositTransaction(name, amount));
                } else if (firstHalfSplit[0].equals("Sold")) {
                    String ID = firstHalfSplit[4];
                    double quantity = Double.parseDouble(firstHalfSplit[1]);
                    addTransaction(new SellEquityTransaction(ID, quantity));
                } else if (firstHalfSplit[0].equals("Transferred")) {
                    String source = firstHalfSplit[3];
                    String destination = firstHalfSplit[5];
                    double amount = Double.parseDouble(firstHalfSplit[1].substring(1, firstHalfSplit[1].length()));
                    addTransaction(new TransferTransaction(source, destination, amount));
                } else if (firstHalfSplit[0].equals("Withdrew")) {
                    String name = firstHalfSplit[3];
                    double amount = Double.parseDouble(firstHalfSplit[1].substring(1, firstHalfSplit[1].length()));
                    addTransaction(new WithdrawalTransaction(name, amount));
                }

            }
            */
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Export this portfolio to a file
     * <p>
     * Export standard:
     * https://docs.google.com/document/d/1R1wO4254QWiOaoGbpM7IyeY4qxamMkkAvZOkhbHPURk/edit
     */
    public void exportPortfolio(String fileName) {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ArrayList<Equity> equities = new ArrayList<>();
        ArrayList<IndexSectorShare> indexSectorShares = new ArrayList<>();

        for (Holding holding : holdings) {
            if (holding instanceof IndexSectorShare) {
                IndexSectorShare indexSectorShare = (IndexSectorShare) holding;
                indexSectorShares.add(indexSectorShare);
            } else {
                Equity equity = (Equity) holding;
                equities.add(equity);
            }
        }

        for (Equity equity : equities) {
            String outString = "";
            outString += "\"" + equity.identifier + "\",";
            outString += "\"" + equity.name + "\",";
            outString += "\"" + equity.pricePerUnit + "\",";
            outString += "\"" + equity.name + "\",";

            for (String indexSectorName : equity.indexSectorNames) {

                outString += "\"" + indexSectorName + "\",";
            }


            outString += "\"" + equity.unitsOwned + "\"";

            writer.println(outString);
        }

        writer.println();

        for (IndexSectorShare indexSectorShare : indexSectorShares) {
            String outString = "";
            outString += "\"" + indexSectorShare.identifier + "\",";
            outString += "\"" + indexSectorShare.unitsOwned + "\"";

            writer.println(outString);
        }

        writer.println();


        for (Transaction transaction : transactions) {
            String outString = "";
            outString += "\"" + transaction.toString().split("~")[0] + "\",";
            outString += "\"" + transaction.getTime() + "\"";

            writer.println(outString);
        }

        writer.println();
        for (CashAccount cashAccount : cashAccounts) {
            String outString = "";
            outString += "\"" + cashAccount.getAccountName() + "\",";
            outString += "\"" + cashAccount.getDateCreated() + "\",";
            outString += "\"" + cashAccount.getCashAmount() + "\"";

            writer.println(outString);
        }

        writer.println();
        for (WatchlistEquity watchlistEquity : watchlist.watchlistEquities) {
            String outString = "";
            outString += "\"" + watchlistEquity.getTicker() + "\",";
            outString += "\"" + watchlistEquity.getLowTrigger() + "\",";
            outString += "\"" + watchlistEquity.getHighTrigger() + "\"";

            writer.println(outString);
        }

        writer.close();
    }


    /**
     * Verify that the user entered the correct password
     *
     * @param password the user's password
     * @return is correct
     */
    public boolean verifyPassword(String password) {
        return password.hashCode() == this.password;
    }

    /**
     * Clones the portfolio in memory and returns a reference to that Portfolio object
     */
    public Portfolio clone() {
        // Make a new portfolio object
        Portfolio newPort = new Portfolio(this.username, "");

        // Copy over holdings
        ArrayList<Holding> newHoldings = newPort.getHoldings();
        for (Holding h : this.holdings) {
            newHoldings.add(h.clone());
        }

        // Copy over cash accounts
        ArrayList<CashAccount> newCashAccounts = newPort.getCashAccounts();
        for (CashAccount ca : this.cashAccounts) {
            newCashAccounts.add(ca);
        }

        return newPort;
    }

    /**
     * Gets all transactions
     *
     * @return the list of transactions in order that they were given
     */
    public ArrayList<String> getTransactionHistory() {

        ArrayList<String> transactionHistory = new ArrayList<>();

        for (Transaction transaction : transactions) {
            transactionHistory.add(transaction.toString());
        }


        return transactionHistory;
    }

    /**
     * Sums up the value of all of the portfolio's holdings
     *
     * @return
     */
    public double getHoldingValue() {
        double sum = 0;
        for (Holding holding : holdings) {
            sum += holding.getUnitPrice() * holding.unitsOwned;
        }
        return sum;
    }

    /**
     * Sums up the value of all of the portfolio's cash accounts
     *
     * @return
     */
    public double getCashAccountValue() {
        double sum = 0;
        for (CashAccount cashAccount : cashAccounts) {
            sum += cashAccount.getCashAmount();
        }
        return sum;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }
}
