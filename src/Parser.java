import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Parser {
	String fileName;
	String strFromFile;
	ArrayList<String> wordArr;

	Parser(String str) {
		fileName = str;
	}

	int parse() {
		ArrayList<String> stringArr;
		ArrayList<Integer> quoteArr;

		if (fileOpen(fileName) != 0) {
			return 1;
		}
		quoteArr = quoteCounter();
		stringArr = stringModifier(quoteArr);
		this.wordArr = wordsFromFile(stringArr);
		return 0;
	}

	int fileOpen(String fileName) {
		try {
			strFromFile = new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			return 1;
		}
		return 0;
	}
	ArrayList<Integer> quoteCounter() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		int quotes = 0;

		for (int i = 0; i < strFromFile.length(); i++) {
			if (strFromFile.charAt(i) == '\'') {
				if (quotes % 2 == 0) {
					result.add(i);
				} else {
					result.add(i + 1);
				}
				quotes++;
			}
		}
		result.add(0, 0);
		result.add(strFromFile.length());
		return result;
	}
	ArrayList<String> stringModifier(ArrayList<Integer> quotes) {
		ArrayList<String> stringArr = new ArrayList<String>();
		String strTmp;

		for (int i = 0; i < quotes.size() - 1; i++) {
			if (i % 2 == 0) {
				strTmp = strFromFile.substring(quotes.get(i), quotes.get(i + 1))
						.toUpperCase()
						.trim()
						.replaceAll(",", " , ")
						.replaceAll(";", " ; ")
						.replaceAll("\\)", " ) ")
						.replaceAll("\\(", " ( ");
			} else {
				strTmp = strFromFile.substring(quotes.get(i), quotes.get(i + 1));
			}
			strTmp = strTmp.replaceAll("\\p{Space}+", " ");
			stringArr.add(strTmp);
		}
		return (stringArr);
	}
	ArrayList<String> wordsFromFile(ArrayList<String> stringArr) {
		ArrayList<String> resultArr = new ArrayList<String>();

		for (int i = 0; i < stringArr.size(); i++) {
			if (i % 2 == 0) {
				//resultArr.addAll(new ArrayList<String>(Arrays.asList(stringArr.get(i).split(" "))));
				resultArr.addAll(Arrays.asList(stringArr.get(i).split(" "))
						.stream()
						.filter(el -> !el.isEmpty())
						.collect(Collectors.toList()));
			} else {
				resultArr.add(stringArr.get(i));
			}
		}
		if (resultArr.get(resultArr.size() - 1).equals(";")) {
			resultArr.remove(resultArr.size() - 1);
		}
		return resultArr;
	}
	ArrayList<String> getWordArr() {
		return wordArr;
	}
}

