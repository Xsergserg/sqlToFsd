import java.util.*;

public class Select {
	ArrayList<String> wordArr;
	// queryArr - array with Pair objects (String operator, int index)
	ArrayList<AbstractMap.SimpleEntry<String, Integer>> queryArr = null;
	Map<String, String> selectMap = null;
	//distinct flag: 'Y' or 'N'
	char distinctFlag = 'N';
	//if from is null - means select from subquery.
	String from = null;
	String fromAlias = null;
	//if From is null - means select from db (without subquery)
	Select From = null;
	//joinArr - Array of join objects
	ArrayList<Select> joinArr = new ArrayList<>();
	//joinTypeFlag: 'L' - Left, 'R' - right, 'I' - inner, 'O' - Outer, 0 - main select
	char joinTypeFlag;
	String joinAlias = null;
	//joinDB filled only if join like "join joinDbName alias"
	String joinDb = null;
	String joinOn = null;
	String where = null;
	String groupBy = null;
	String having = null;
	//Characters: 'A' - ASC, 'D' - DSC
	ArrayList<AbstractMap.SimpleEntry<String, Character>> orderByType = null;
	final static String[] OPERATORS = {"SELECT", "FROM", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "WHERE", "GROUP", "ORDER", "HAVING"};
	final static String[] JOINS = {"LEFT", "RIGHT", "INNER", "OUTER"};

	Select(ArrayList<String> wordArrSource, char join, String onString, String joinAl, String joinDbName) {
		this.wordArr = wordArrSource;
		this.joinTypeFlag = join;
		this.joinOn = onString;
		this.joinAlias = joinAl;
		this.joinDb = joinDbName;
		parse();
	}

	void parse() {
		selectMapper();
		queryParser();
	}

	private void selectMapper() {
		queryArr = new ArrayList<AbstractMap.SimpleEntry<String, Integer>>();
		for (int i = 0; i < wordArr.size(); i++) {
			int bracket = 0;
			if (wordArr.get(i).equals("(")) {
				bracket++;
				i++;
				while (bracket != 0) {
					if (wordArr.get(i).equals(")")) {
						bracket--;
					}
					if (wordArr.get(i).equals("(")) {
						bracket++;
					}
					i++;
				}
			}
			int finalI = i;
			if (Arrays.stream(OPERATORS).anyMatch(el -> el.equals(wordArr.get(finalI)))) {
				queryArr.add(new AbstractMap.SimpleEntry<>(wordArr.get(i), i));
			}
			if (Arrays.stream(JOINS).anyMatch(el -> el.equals(wordArr.get(finalI)))) {
				i++;
			}
		}
	}

	private void queryParser() {
		int size = queryArr.size();
		for (int i = 0; i < size; i++) {
			if (i == size - 1) {
				attFuller(wordArr.subList(queryArr.get(i).getValue(), wordArr.size()));
				break;
			}
			attFuller(wordArr.subList(queryArr.get(i).getValue(), queryArr.get(i + 1).getValue()));
		}
	}

	private void attFuller(List<String> attribute) {
		if (attribute.get(0).equals("SELECT")) {
			selectParse(attribute);
		}
		if (attribute.get(0).equals("FROM")) {
			fromParse(attribute);
		}
		if (attribute.get(0).equals("WHERE")) {
			whereParse(attribute);
		}
		if (attribute.get(0).equals("GROUP")) {
			groupParse(attribute);
		}
		if (attribute.get(0).equals("ORDER")) {
			orderParse(attribute);
		}
		if (attribute.get(0).equals("HAVING")) {
			havingParse(attribute);
		}
		if (attribute.get(0).equals("JOIN") | Arrays.stream(JOINS).anyMatch(el -> el.equals(attribute.get(0)))) {
			joinParse(attribute);
		}
	}

	private void selectParse(List<String> attribute) {
		int i = 1;
		selectMap = new HashMap<String, String>();
		int size = attribute.size();

		if (attribute.get(i).equals("DISTINCT")) {
			distinctFlag = 'Y';
			i++;
		}
		for (; i < size; i++) {
			if (i + 1 < size) {
				if (attribute.get(i + 1).equals("AS")) {
					selectMap.put(attribute.get(i), attribute.get(i + 2));
					i += 2;
				}
			} else {
				selectMap.put(attribute.get(i), null);
				i++;
			}
			if (i + 1 < size) {
				if (attribute.get(i + 1).equals(",")) {
					i++;
				}
			}
		}
		return;
	}

	private void fromParse(List<String> attribute) {
		int size = attribute.size();

		if (!attribute.get(1).equals("(")) {
			from = attribute.get(1);
			if (size > 2) {
				fromAlias = attribute.get(2);
			}
			return;
		}
		if (!attribute.get(size - 1).equals(")")) {
			fromAlias = attribute.get(size - 1);
			From = new Select(new ArrayList<>(attribute.subList(2, size - 2)), '\0', null, null, null);
		} else {
			From = new Select(new ArrayList<>(attribute.subList(2, size - 1)), '\0', null, null, null);
		}
	}

	private void whereParse(List<String> attribute) {
		where = String.join(" ", attribute.subList(1, attribute.size()));
	}

	private void groupParse(List<String> attribute) {
		attribute.remove(0);
		attribute.remove(1);
		groupBy = String.join(" ", attribute);
	}

	private void orderParse(List<String> attribute) {
		orderByType = new ArrayList<AbstractMap.SimpleEntry<String, Character>>();
		for (int i = 2; i < attribute.size(); i++) {
			if (attribute.get(i).equals("ASC") | attribute.get(i).equals("DESC") | attribute.get(i).equals(",")) {
				i++;
			} else {
				if (i + 1 < attribute.size()) {
					if (attribute.get(i + 1).equals("DESC")) {
						orderByType.add(new AbstractMap.SimpleEntry<>(attribute.get(i), 'D'));
						continue;
					}
				}
				orderByType.add(new AbstractMap.SimpleEntry<>(attribute.get(i), 'A'));
			}
		}
	}

	private void havingParse(List<String> attribute) {
		attribute.remove(0);
		having = String.join(" ", attribute);
	}

	private void joinParse(List<String> attribute) {
		int size = attribute.size();
		int onPos = attribute.size() - 1;
		String onStr;

		while (!attribute.get(onPos).equals("ON")) {
			onPos--;
		}

		onStr = String.join(" ", attribute.subList(onPos + 1, size));
		String joinAl = null;
		int joinAlInt = 0;
		String joinDbName = null;

		if (attribute.get(0).equals("JOIN")) {
			if (attribute.get(1).equals("(")) {
				if (!attribute.get(onPos - 1).equals(")")) {
					joinAl = attribute.get(onPos - 1);
					joinAlInt = 1;
				}
				joinArr.add(new Select(new ArrayList<>(attribute.subList(2, onPos - 1 - joinAlInt)), 'I', onStr, joinAl, null));
			} else {
				if (!attribute.get(2).equals("ON")) {
					joinAl = attribute.get(2);
					joinAlInt = 1;
				}
				joinDbName = attribute.get(1);
				joinArr.add(new Select(new ArrayList<>(attribute.subList(1, onPos - joinAlInt)), 'I', onStr, joinAl, joinDbName));
			}
			return;
		}
		char joinT = 0;
		if (attribute.get(0).equals("RIGHT")) {
			joinT = 'R';
		}
		if (attribute.get(0).equals("LEFT")) {
			joinT = 'L';
		}
		if (attribute.get(0).equals("OUTER")) {
			joinT = 'O';
		}
		if (attribute.get(0).equals("INNER")) {
			joinT = 'I';
		}
		if (attribute.get(2).equals("(")) {
			if (!attribute.get(onPos - 1).equals(")")) {
				joinAl = attribute.get(onPos - 1);
				joinAlInt = 1;
			}
			joinArr.add(new Select(new ArrayList<>(attribute.subList(3, onPos - 1 - joinAlInt)), joinT, onStr, joinAl, null));
		} else {
			if (!attribute.get(3).equals("ON")) {
				joinAl = attribute.get(3);
				joinAlInt = 1;
			}
			joinDbName = attribute.get(2);
			joinArr.add(new Select(new ArrayList<>(attribute.subList(2, onPos - joinAlInt)), joinT, onStr, joinAl, joinDbName));
		}
	}
}

