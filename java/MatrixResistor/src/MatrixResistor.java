import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
// http://www.javacodegeeks.com/2012/02/regular-expressions-in-java-soft.html
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */

/**
 * @author amejia
 *
 */
public class MatrixResistor {
	
	private static String INSERT_INTO_PATTERN = "";//"/insert\W+into\W+(\w+)\W*\(([^)]*)\)\W*values\W*\(([^;]*)\)/i";
	private static String SELECT_PATTERN = "select";
	private static String INSERT_PATTERN = "insert";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dataFile = "";
		String queryFile = "";
		
		HashMap hashValues = convertCSV2Hash(dataFile); 
		
		Scanner scan = new Scanner(queryFile);
		while(scan.hasNextLine()){
			String line = sanitize(scan.nextLine());
			
			Pattern p = Pattern.compile(INSERT_INTO_PATTERN);
			Matcher m = p.matcher(line);
			
			if(m.matches()){
				// insert statements
				
			}
			else if (line.toLowerCase().indexOf("select") >= 0 &&
					line.toLowerCase().indexOf("insert") >= 0){
				// insert select statements
			}
			else if (line.toLowerCase().indexOf("update") >= 0){
				// update statements
			}
			else {
				// non-processed
			}
		}

	}

	private static String sanitize(String nextLine) {
		// TODO Auto-generated method stub
		return null;
	}

	private static HashMap convertCSV2Hash(String dataFile) {
		// TODO Auto-generated method stub
		return null;
	}

}
