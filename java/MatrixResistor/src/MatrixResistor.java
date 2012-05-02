import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
// http://www.javacodegeeks.com/2012/02/regular-expressions-in-java-soft.html
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author amejia
 *
 */
public class MatrixResistor {
	
	public static String INSERT_INTO_PATTERN = ".*INSERT\\W+into\\W+(\\w+)\\W*\\(([^)]*)\\)\\W*VALUES\\W*\\(([^\\)]*)\\).*"; //"/insert\W+into\W+(\w+)\W*\(([^)]*)\)\W*values\W*\(([^;]*)\)/i";
	//private static String SELECT_PATTERN = "select";
	//private static String INSERT_PATTERN = "insert";
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Matrix Resistor");
		
		String dataFile = "../../matrix/ps_values.csv";
		String queryFile = "../../matrix/matrix_resistors.sql";
		
		HashMap hashValues = convertCSV2Hash(dataFile); 
		Set<String> queriesSet = new HashSet<String>();
		
		Scanner scan = new Scanner(new File(queryFile));
		while(scan.hasNextLine()){
			String line = sanitize(scan.nextLine());
			
			Pattern p = Pattern.compile(MatrixResistor.INSERT_INTO_PATTERN, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(line);
			
			if(m.matches()){
				// insert statements
				String sql_processed = processInsert(hashValues,line, m);
			}
			else if (line.toLowerCase().indexOf("select") >= 0 &&
					line.toLowerCase().indexOf("insert") >= 0){
				// insert select statements
				System.out.println(50);
				String sql_processed = processInsertSelect(hashValues,line);
				
			}
			else if (line.toLowerCase().indexOf("update") >= 0){
				// update statements
				System.out.println(54);
				String sql_processed = processUpdate(hashValues,line);
			}
			else {
				// non-processed
				System.out.println(58);
			}
		}

	}

	public static String processInsertSelect(HashMap hashValues, String line) {
		
		return null;
	}

	private static String processUpdate(HashMap hashValues, String line) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String processInsert(HashMap hashValues, String line, Matcher m) {
		// convert the SQL to a hash
		HashMap<String,String> sqlHash = convertSqlInsertToHash(line, m); 
		// replace the SQLhash with the hashValues if they exits.
		for(int i=0; i<hashValues.size(); i++){
			
		}
		return null;
	}

	public static HashMap<String, String> convertSqlInsertToHash(String line, Matcher m) {
		String tablename;
		String[] columns;
		String[] values;
		
		while(m.find()){
			tablename = m.group(1);
			columns = m.group(2).split(",");
			values = splitCsvWithFunctions(m.group(3));
		}
		
		
		return null;
	}

	public static String[] splitCsvWithFunctions(String string) {
		String[] res = null; //= string.split(",");
		char[] c = string.toCharArray();
		StringBuilder buffer = new StringBuilder("");
		ArrayList<String> array = new ArrayList<String>();
		int parenthesis=0;
		
		for(int i=0; i<string.length(); i++){
			switch(c[i]){
			case '(':
				while(i < string.length()){
					buffer.append(c[i]);
					if (c[i] == '(') parenthesis++;
					if (c[i] == ')') parenthesis--;
					if (parenthesis <= 0)
						break;
					else
						i++;	
				}
				break;
			case ',':
				array.add(buffer.toString());
				buffer = new StringBuilder("");
				break;
			default:
				buffer.append(c[i]);
			}
		}
		array.add(buffer.toString());
		//return array.toArray(res);
		return ArrayList2String(array);
	}

	public static String sanitize(String string) {
		string = string.trim();
		return string;
	}
	
	public static String[] ArrayList2String(ArrayList<String> al){
		String[] a = new String[al.size()];
		for(int i=0; i<al.size(); i++){
			a[i] = al.get(i);
			System.out.println(a[i]);
		}
		return a;
	}

	public static HashMap convertCSV2Hash(String dataFilePath) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(dataFilePath));
		HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
		String[] header = null;
		while(scanner.hasNextLine()){
			String line = sanitize(scanner.nextLine());
			String[] csv = line.split(",");
			// header
			if (hash.isEmpty()){
				header = csv;
				for(String h: header)
					hash.put(h, new ArrayList<String>());
			}
			// data
			else {
				for(int i=0; i<csv.length; i++){
					hash.get(header[i]).add(csv[i]);
				}
			}
		}
		return hash;
	}

}
