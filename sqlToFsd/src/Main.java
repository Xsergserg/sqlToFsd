import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Error: Program need 1 argument - file name for parsing");
			return;
		}
		Parser Expression = new Parser(args[0]);
		if (Expression.parse() != 0) {
			return;
		}
		ArrayList<String> wordArr = Expression.getWordArr();
		Select mainQuery = new Select(wordArr, '\0', null, null, null);
		return;
	}
}
