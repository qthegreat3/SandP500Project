package org.harquintech.sp500;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.Environment;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		// get list of tickers
		try {
			Reader in = new FileReader(
					"C:\\Users\\Amandla Blue-Ashley\\Documents\\SP500-Project\\sp500\\src\\main\\resources\\companylist.csv");

			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Symbol", "Name", "LastSale", "MarketCap",
					"ADR TSO", "IPOyear", "Sector", "Industry", "Summary Quote").parse(in);

			Map<String, Double> marketCapMap = new HashMap<>();

			for (CSVRecord record : records) {
				String symbol = record.get("Symbol");
				System.out.println("Symbol: " + symbol);

				double marketCap = getMarketCap(symbol);

				marketCapMap.put(symbol, marketCap);
			}

			in = new FileReader(
					"C:\\Users\\Amandla Blue-Ashley\\Documents\\SP500-Project\\sp500\\src\\main\\resources\\companylist (1).csv");

			records = CSVFormat.DEFAULT.withHeader("Symbol", "Name", "LastSale", "MarketCap", "ADR TSO", "IPOyear",
					"Sector", "Industry", "Summary Quote").parse(in);

			for (CSVRecord record : records) {
				String symbol = record.get("Symbol");
				System.out.println("Symbol: " + symbol);

				double marketCap = getMarketCap(symbol);

				marketCapMap.put(symbol, marketCap);
			}
		
			in = new FileReader(
					"C:\\Users\\Amandla Blue-Ashley\\Documents\\SP500-Project\\sp500\\src\\main\\resources\\ASXListedCompanies.csv");

			Iterable<CSVRecord> amexRecords = CSVFormat.DEFAULT.withHeader("Company name", "ASX code", "GICS industry group").parse(in);

			for (CSVRecord record : amexRecords) {
				String symbol = record.get("ASX code");
				System.out.println("Symbol: " + symbol);

				
				double marketCap = getMarketCap(symbol);

				marketCapMap.put(symbol, marketCap);
			}

			List<MarketCapMarker> sAndP500Symbols = getSandP500List(marketCapMap);

			printSAndP500List(sAndP500Symbols);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// go through list of tickers
		// for each ticker, get webpage from yahoo "https://finance.yahoo.com/quote/" +
		// <symbol>
		// grab tag for market cap "data-test" = MARKET_CAP-value attribute
		// get span inside of it
		// get inner text
	}

	private static void printSAndP500List(List<MarketCapMarker> sAndP500Symbols) throws IOException {
		FileWriter writer = new FileWriter(
				"C:\\Users\\Amandla Blue-Ashley\\Documents\\SP500-Project\\sp500\\src\\main\\resources\\sAndp500.txt");

		int lastPositionIndex = sAndP500Symbols.size() - 1;

		for (int index = 0; index < sAndP500Symbols.size(); index++) {
			if (499 < index) {
				break;
			}

			System.out.println((index + 1) + ": " + sAndP500Symbols.get(lastPositionIndex - index).getSymbol() + " market cap: " + sAndP500Symbols.get(lastPositionIndex - index).getMarketCap());

			String line = (index + 1) + ": " + sAndP500Symbols.get(lastPositionIndex - index).getSymbol() + " market cap: " + sAndP500Symbols.get(lastPositionIndex - index).getMarketCap() + "\r\n";

			writer.write(line);
		}

		writer.close();
	}

	private static final String BASE_URL = "https://finance.yahoo.com/quote/";
	private static final String MARKET_CAP_ATRIBBUTE = "[data-test=MARKET_CAP-value]";

	private static List<MarketCapMarker> getSandP500List(Map<String, Double> marketCapMap) {
		Set<String> symbols = marketCapMap.keySet();
		List<MarketCapMarker> sAndP500Symbols = new ArrayList<MarketCapMarker>();

		for (String symbol : symbols) {
			sAndP500Symbols.add(new App.MarketCapMarker(symbol, marketCapMap.get(symbol)));
		}

		Collections.sort(sAndP500Symbols, new App.MarketCapMarkerComparer());

		return sAndP500Symbols;
	}

	private static double getMarketCap(String symbol) {
		Document doc;
		double marketCap = 0;

		try {
			doc = Jsoup.connect(BASE_URL + symbol).get();
			Elements elementList = doc.select(MARKET_CAP_ATRIBBUTE);

			for (int elementIndex = 0; elementIndex < elementList.size(); elementIndex++) {

				Element element = elementList.get(elementIndex);

				int numberOfChildNodes = elementList.get(elementIndex).childNodeSize();

				if (numberOfChildNodes > 0) {
					String childText = element.child(0).ownText();

					System.out.println("The Text Found: " + childText);

					// Get number string
					String numberPart = childText.substring(0, childText.length() - 1);
					String endingType = childText.substring(childText.length() - 1);
					System.out.println("NumberPart: " + numberPart);
					System.out.println("EndingType: " + endingType);

					if (endingType.equalsIgnoreCase("B")) {
						marketCap = Double.parseDouble(numberPart);

					} else if (endingType.equalsIgnoreCase("M")) {
						marketCap = Double.parseDouble(numberPart) * .001;
					}

					System.out.println("Markt Cap: " + marketCap);
				}

				for (int childIndex = 0; childIndex < numberOfChildNodes; childIndex++) {
					System.out.println("Text printed: " + elementList.get(elementIndex).child(childIndex).ownText());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return marketCap;
	}

	public static class MarketCapMarker {
		private String symbol;
		private double marketCap;

		public MarketCapMarker(String symbol, double marketCap) {
			this.symbol = symbol;
			this.marketCap = marketCap;
		}

		public String getSymbol() {
			return symbol;
		}

		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}

		public double getMarketCap() {
			return marketCap;
		}

		public void setMarketCap(double marketCap) {
			this.marketCap = marketCap;
		}
	}

	public static class MarketCapMarkerComparer implements Comparator<MarketCapMarker> {
		public int compare(MarketCapMarker a, MarketCapMarker b) {
			if (a.getMarketCap() > b.getMarketCap()) {
				return 1;
			} else if (a.getMarketCap() < b.getMarketCap()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
