package org.harquintech.sp500;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class UUIDGenerator {

	public static void main(String[] args) throws IOException {
		String uuid = UUID.randomUUID().toString();
		System.out.println("UUID: " + uuid);
		InputStream fileStream = ClassLoader.getSystemResourceAsStream("sAndp500.txt");
		List<String> lines = IOUtils.readLines(fileStream, "UTF-8");
		
		File spTickerListFile = new File("src/main/resources/sp500-Ticker-List.txt");
		
		List<String> listOfTickers = new ArrayList<>();
		
		for (String line : lines)
		{
			int positionOfFirstBlank = line.indexOf(" ");
			if (positionOfFirstBlank > -1)
			{
				String beginningOfQuote = line.substring(positionOfFirstBlank + 1);
				System.out.println("beginningQuote: " + beginningOfQuote);
				int positionOfNextBlank = beginningOfQuote.indexOf(" ");
				String entireQuote = beginningOfQuote.substring(0, positionOfNextBlank);
				System.out.println("entireQUoete: " + entireQuote);
				listOfTickers.add(entireQuote);
			}
		}
		
		FileUtils.writeLines(spTickerListFile, listOfTickers);
	}
	
}
