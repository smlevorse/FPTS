import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author David Egan
 *
 * Pull information from YahooAPI and update equities with it
 */
public class YahooAPI {

    private final String BASE_URL = "http://query.yahooapis.com/v1/public/yql?q=select" +
            "%20LastTradePriceOnly,Symbol%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22";
    private final String DATABASE_URL = "%22)&env=store://datatables.org/alltableswithkeys";

    //update all the equities in the system
    public void updateSystem() {
        updateEquities(PortfolioAdapter.equities);

        //update users portfolio
        ArrayList portfolioHoldings;
        if (PortfolioAdapter.currentUser != null)
            updateEquities(PortfolioAdapter.currentUser.getHoldings());


    }


    //Get All Equities from equity and All Equities from each Portfolios 'holdings' and each Portfolios 'watchlist'
    //THEN...
    //// TODO: throw exception that can be handled by calling method
    //// TODO: 3/29/2016 WARNING! SOME SYMBOLS ARE INCORRECT AS A RESULT OF THE CSV FILE
    public void updateEquities(ArrayList<? extends Holding> holdingList) {

        // Components of the request URL, all tickers are put in a string
        // so that one request can be made to the web service

        String tickerList = "";

        // currently this algorithm is too inefficient to handle all equities
        int tickersUsedSize = holdingList.size();
        int tickerListSize = 0;
        ArrayList<Equity> equityList = new ArrayList<>();


        // how many stocks to include in each query
        int querySize = 100;
        // need to build arraylist holding only equities to update only equities
        for (int i = 0; i < tickersUsedSize; i = i + querySize) {
            for (int j = 0; j < querySize && j + i < tickersUsedSize; j++) {
                // Market shares will update as equities are updated
                Holding currentHolding = holdingList.get(j + i);
                if (currentHolding instanceof Equity) {
                    tickerList = tickerList.concat(holdingList.get(j + i).identifier + ",");
                    equityList.add((Equity) currentHolding);
                }
                tickerListSize += 1;
            }

            try {
                executeEquityUpdates((tickerList).toString(), equityList);

            } catch (Exception e) {
                e.printStackTrace();
            }
            ;

            tickerList = "";

        }
    }


    private void executeEquityUpdates(String tickerList, ArrayList<Equity> equityList) {


        try {

            URL YahooURL = new URL(BASE_URL + tickerList + DATABASE_URL);
            HttpURLConnection con = (HttpURLConnection) YahooURL.openConnection();
            // Set the HTTP Request type method to GET (Default: GET)
            con.setRequestMethod("GET");
            // The server may be too slow to handle data in just 10 seconds?
            // if possible should focus on optimizing this
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            // Get DOM Builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Disable validation for speed
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse document
            Document document = builder.parse(con.getInputStream());

            // Get last trade price
            NodeList nlist = document.getElementsByTagName("quote");

            for (int i = 0; i < nlist.getLength(); i++) {

                Node nodeQ = nlist.item(i);
                Element elemQ = (Element) nodeQ;

                NodeList symbolList = elemQ.getElementsByTagName("Symbol");
                NodeList priceList = elemQ.getElementsByTagName("LastTradePriceOnly");

                Node nodeS = symbolList.item(0);
                Node nodePrice = priceList.item(0);

                Element symbolElement = (Element) nodeS;
                Element priceElement = (Element) nodePrice;

                //ignore if the ticker is not a real ticker (no LastTradePrice)
                String lastTradePriceStr = priceElement.getTextContent();
                if (lastTradePriceStr.length() != 0) {

                    // no manual rounding done
                    Double lastTradePrice = Double.valueOf(priceElement.getTextContent());
                    String symbol = symbolElement.getTextContent();

                    for (Equity e : equityList) {
                        if (e.identifier.equals(symbol)) {
                            e.updateUnitPrice(lastTradePrice);
                        }
                    }

                }
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
