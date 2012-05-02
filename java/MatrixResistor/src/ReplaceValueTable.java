import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * @author amejia
 *
 */
public class ReplaceValueTable {
	
	public static String REPLACEMENT_PATTERN = "\\{\\{([^\\}]+)\\}\\}";

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String sqlFilePath = "";
		String dataFilePath = "";
		String outFilePath = "";
		
		if(args.length == 2){
			System.out.println(args[0]);
			System.out.println(args[1]);
		} else {
			dataFilePath = "../../replace_value_table/ps_values.csv";
			sqlFilePath = "../../replace_value_table/matrix_resistors_placeholders.sql";
		}
		
		Set<String> sqls = replacePlaceHolders(sqlFilePath,dataFilePath,outFilePath);
		
		for(String sql : sqls){
			System.out.println(sql);
		}
	}
	
	public static Matcher getMatcher(String str){
		Pattern p = Pattern.compile(REPLACEMENT_PATTERN);
		return p.matcher(str);
	}
	
	public static HashMap<String, ArrayList<String>> convertCSV2Hash(String dataFilePath) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(dataFilePath));
		HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
		String[] header = null;
		int times = 2; 
		while(scanner.hasNextLine()){
			String line = scanner.nextLine().trim();
			String[] csvLine = sanitize(line).split(",");
			for(int i=0; i<csvLine.length; i++)
				csvLine[i] = csvLine[i].trim(); 
			// header
			if (hash.isEmpty()){
				header = csvLine;
				for(String h: header){
					hash.put(h, new ArrayList<String>());
					//System.out.println(h);
				}
			}
			// data
			else {
				for(int i=0; i<csvLine.length; i++){
					//System.out.println(String.format("i=%d; key=<%s>; value=<%s>;", i,header[i],sanitize(csvLine[i])));
					hash.get(header[i]).add(sanitize(csvLine[i]));
				}
			}
			
			times--;
			if(times < 0) break; // TODO debug
		}
		
		return hash;
	}
	
	public static String sanitize(String str){
		return str.trim().trim().replaceAll("[^\\\\ -\\}]", "");
	}

	private static Set<String> replacePlaceHolders(String sqlFilePath,
			String dataFilePath, String outFilePath) throws FileNotFoundException {
		
		Scanner sqlScan = new Scanner(new File(sqlFilePath));
		HashMap<String, ArrayList<String>> dataHash = convertCSV2Hash(dataFilePath);
		Set<String> sqls = new HashSet<String>();
		
		while(sqlScan.hasNextLine()){
			String sql = sqlScan.nextLine().trim();
			
			Matcher m = getMatcher(sql);
			ArrayList<String> columnsToReplace = new ArrayList<String>();
			
			while(m.find()){
				columnsToReplace.add(m.group(1));
				//System.out.println(column);
			}
			if(columnsToReplace.size() < 1) {
				sqls.add(sql);
				continue;
			}
			//System.out.println();
			for(int i=0; i<  dataHash.get("MANUFACTURER_ITEM_ID").size() ; i++){
				for(String key: ArrayList2String(columnsToReplace)){
					if (dataHash.containsKey(key)){
						sql = sql.replaceAll("\\{\\{"+key+"\\}\\}", "'"+dataHash.get(key).get(i)+"'");
						//System.out.println("replaced: '"+key+"' => '"+dataHash.get(key).get(i)+"'");
					} else {
						System.err.println("key = <"+key+"> doesn't exists.");
					}
				}
				sqls.add(sql);
			}
		}
		return sqls;
	}
	
	public static String[] ArrayList2String(ArrayList<String> al){
		String[] a = new String[al.size()];
		for(int i=0; i<al.size(); i++){
			a[i] = al.get(i);
			//System.out.println(a[i]);
		}
		return a;
	}

}
