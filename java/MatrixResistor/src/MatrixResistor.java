import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
// http://www.javacodegeeks.com/2012/02/regular-expressions-in-java-soft.html
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author amejia
 *
 */
public class MatrixResistor {
	
	public static String INSERT_INTO_PATTERN = "insert\\W+into\\W+(\\w+)\\W*\\(([^)]*)\\)\\W*values\\W*\\(([^)]*)\\)"; //"/insert\W+into\W+(\w+)\W*\(([^)]*)\)\W*values\W*\(([^;]*)\)/i";
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
		
		Scanner scan = new Scanner(new File(queryFile));
		while(scan.hasNextLine()){
			String line = sanitize(scan.nextLine());
			
			Pattern p = Pattern.compile(MatrixResistor.INSERT_INTO_PATTERN, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(line);
			
			if(m.matches()){
				// insert statements
				System.out.println(line);
				System.out.println(45);
			}
			else if (line.toLowerCase().indexOf("select") >= 0 &&
					line.toLowerCase().indexOf("insert") >= 0){
				// insert select statements
				System.out.println(50);
			}
			else if (line.toLowerCase().indexOf("update") >= 0){
				// update statements
				System.out.println(54);
			}
			else {
				// non-processed
				System.out.println(58);
			}
		}

	}

	public static String sanitize(String string) {
		string = string.trim();
		return string;
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
