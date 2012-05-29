import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.csvreader.CsvReader;

/**
 * @author amejia
 *
 */
public class ReplaceValueTable {
	
	public static String REPLACEMENT_PATTERN = "\\{\\{([^\\}]+)\\}\\}";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Value Replacer v.1.0.1");
		System.out.println("Adrian Mejia 5/29/2012");
		//String path = "../../replace_value_table/"; 
		String path = System.getProperty("user.dir")+ File.separator; 
		String sqlFilePath = "";
		String dataFilePath = "";
		String outFilePath = path+"output_replaced_values.sql";
		String logFilePath = path+"output_changes.log";
		
		if(args.length == 2){
			System.out.println(args[0]);
			System.out.println(args[1]);
		} else {
			dataFilePath = path+"input_placeholders_values.csv";
			sqlFilePath = path+ "input_placeholders.sql";
		}
		
		OutputStreamWriter out = null;
		BufferedWriter log = null;
		
		try{
			out = new OutputStreamWriter(new FileOutputStream(outFilePath), "UTF-8");
			log = new BufferedWriter(new FileWriter(logFilePath, false));
			log.write("===== "+(new Date()).toString()+"=====\n");
			
			Set<String> sqls = replacePlaceHolders(sqlFilePath,dataFilePath,outFilePath, log);
			for(String sql : sqls){
				//System.out.println(sql);
				out.write(sql);
				out.write("\n");
			}
			log.write("\n== Total Items generated: "+ sqls.size()+" ==\n");
			System.out.println("\n== Total Items generated: "+ sqls.size()+" ==\n");
		} 
		catch (Exception ex){
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			if(log != null){
				log.write("Error:\n");
				log.write(ex.getMessage());
				log.write("\n");
			}
		} 
		finally {
			out.close();
			log.close();
		}
		//System.out.println("done");
	}
	
	public static Matcher getMatcher(String str){
		Pattern p = Pattern.compile(REPLACEMENT_PATTERN);
		return p.matcher(str);
	}
	
	public static HashMap<String, ArrayList<String>> convertCSV2Hash(String dataFilePath) throws IOException {
		Scanner scanner = new Scanner(new File(dataFilePath));
		HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
		String[] header = null;
		int times = 0; 
		while(scanner.hasNextLine()){
			times++;
			String line = sanitize(scanner.nextLine());
			String[] csvLine = csvSplit(line);
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
			
			//times--;
			//if(times < 0) break; // TODO debug
		}
		
		//System.out.println("count="+times);
		
		return hash;
	}
	
	private static String[] csvSplit(String s) throws IOException {
		CsvReader reader = CsvReader.parse(s);
		String[] csv = null;
		if(reader.readRecord()){
			csv = new String[reader.getColumnCount()];
			for(int i=0; i < csv.length; i++)
				csv[i] = reader.get(i);
		}
		return csv;
	}

	public static String sanitize(String str){
		return str.trim().trim().replaceAll("[^\\\\ -\\}]", "");
	}
	

	private static Set<String> replacePlaceHolders(String sqlFilePath,
			String dataFilePath, String outFilePath, BufferedWriter log) throws IOException {
		
		Scanner sqlScan = new Scanner(new File(sqlFilePath));
		HashMap<String, ArrayList<String>> dataHash = convertCSV2Hash(dataFilePath);
		Set<String> sqls = new HashSet<String>();
		Set<String> notFoundColumns = new HashSet<String>();
		
		int hashSize = dataHash.entrySet().iterator().next().getValue().size();
		
		//System.out.println("dataHash size = " + hashSize);
		
		while(sqlScan.hasNextLine()){
			String sql = sqlScan.nextLine().trim();
			
			Matcher m = getMatcher(sql);
			ArrayList<String> columnsToReplace = new ArrayList<String>();

			// identify columns to replace
			while(m.find()){
				columnsToReplace.add(m.group(1));
			}
			
			// if not columns to replace move to the next.
			if(columnsToReplace.size() < 1) {
				sqls.add(sql);
				continue;
			}
			
			//------------------------ Do Replacement
			/*
			 * For each row in input_placeholders, the placeholders {{.+}} are
			 * substituted by each of the values of the rows in the column of 
			 * the input_placeholder_values, which column name match the placeholder
			 * name.   
			 */
			
			log.write("\tProcessing placeholder: " + sql);
			log.write("\r\n");
			log.write("\t\tColumns to replace: " + columnsToReplace.toString());
			log.write("\r\n");
			
			int rowsGenerated = 0;
			for (int i = 0; i < hashSize; i++) {
				String sql_replaced = sql;
				for (String key : ArrayList2String(columnsToReplace)) {
					if (dataHash.containsKey(key)) {
						if (i < dataHash.get(key).size()) {
							sql_replaced = sql_replaced.replaceAll("\\{\\{"
									+ key + "\\}\\}", dataHash.get(key).get(i));
						} else {
							sql_replaced = sql_replaced.replaceAll("\\{\\{" + key + "\\}\\}", "");
							log.write("WARNING: column <" + key	+ "> is empty in the row " + i + "\n");
							System.err.println("WARNING: column <" + key + "> is empty in the row " + i);
						}
					} else {
						System.err.println("key = <" + key + "> doesn't exists.");
						// log.write("column <"+key+"> doesn't exists in "+dataFilePath+"\n");
						notFoundColumns.add(key);
					}
				}
				sqls.add(sql_replaced);
				rowsGenerated++;
			}
			log.write("\t\tRows generated: " + rowsGenerated);
			log.write("\r\n");
			//-------------------- End Replacement
			
			
		} // while(sqlScan.hasNextLine()) - reads each line of input_placeholders 
		
		if(notFoundColumns.size()>0){
			log.write("The following columns were not found in "+dataFilePath+" file, please add them:\n");
			for(String s: notFoundColumns)
				log.write("\t"+s+"\n");
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
