import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class MyParser {
	private HashMap<String, HashMap<String, Float>> graph = new HashMap<>();
	
	public void firstParse(String filename) {
		try (BufferedReader bf = new BufferedReader(new FileReader(filename))) {
			String s = "";
			
			while ((s = bf.readLine()) != null) {
				String[] authors = getAuthors(s);
				if (authors != null) addCoop(authors);
				
			}
		} catch (Exception e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	private String[] getAuthors (String line) {
		int beg = line.indexOf(", [");
		int end = line.indexOf("], ");
		if (line.indexOf(", ", beg + 3) == -1 
				|| line.indexOf(", ", beg + 3) > end) {return null;}
		line = line.substring(beg+3, end);
		line = line.replaceAll("\"", "");
		String[] authors = line.split(", ");
		
		return authors;	
	}
	
	private void addCoop(String[] authors) {
		for (int i = 0; i < authors.length; i++) {
			graph.putIfAbsent(authors[i], new HashMap<>());
			for (int j = 0; j < authors.length; j++)
				if (i != j) {
					graph.get(authors[i]).computeIfPresent(authors[j], (k, v) -> v + (1f/authors.length));
					graph.get(authors[i]).putIfAbsent(authors[j], 1f/authors.length);
				}
		}
	}
	/*
	 * The following two methods don't really makes much sense because they takes much more time than parsing method
	 */
	@SuppressWarnings("unchecked")
	public void readGraph(String filename) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));) {
			graph = (HashMap<String, HashMap<String, Float>>) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeGraph(String filename) {
		if ((new File(filename)).exists()) {
			System.err.println("You're trying to overwrite an existing file");
			return;
			}
		try (ObjectOutputStream oos= new ObjectOutputStream(new FileOutputStream(filename));){	
			oos.writeObject(graph);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/*
	 * This method is really quick, but parsing resultant file is much longer than parsing the initial database
	 * even though file sizes are similar. I don't know why it is so. 
	 */
	public void writeCSV (String filename) {
		if (new File(filename).exists()) {
			System.err.println("You are trying to overwrite an existing file");
			return;
		}
		File file = new File (filename);
		//file.deleteOnExit();
		try (CSVPrinter printer = new CSVPrinter(new BufferedWriter(new FileWriter(file)), 
				CSVFormat.INFORMIX_UNLOAD_CSV.withFirstRecordAsHeader())) {
			printer.printRecord("node", "neighbors", "weight");
			for (String no : graph.keySet()) { 
				int i = 0;
				for (String ne : graph.get(no).keySet()) {
					if (i++ == 0) printer.printRecord(no, ne, graph.get(no).get(ne));
					else {
						/*printer.print(new String());
						printer.print(ne);
						printer.print(graph.get(no).get(ne));
						printer.println();*/
						printer.printRecord("", ne, graph.get(no).get(ne));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readCSV(String filename) {
		if (!graph.isEmpty()) return;
		try (CSVParser parser = 
				CSVFormat.INFORMIX_UNLOAD_CSV.withFirstRecordAsHeader().parse(new BufferedReader(new FileReader(filename)))) {
		/*try (CSVParser parser = new CSVParser(new BufferedReader(new FileReader(filename)), 
				CSVFormat.INFORMIX_UNLOAD_CSV.withFirstRecordAsHeader())) {*/
		/*try (CSVParser parser = CSVParser.parse(new File(filename), 
				Charset.forName("UTF-8"), CSVFormat.EXCEL.withFirstRecordAsHeader())) {*/
			String node = "";
			for (CSVRecord rec : parser) {
				String no = rec.get(0);
				if (!no.isEmpty()) node = no;
				String ne = rec.get(1);
				Float we = Float.parseFloat(rec.get(2));
				graph.putIfAbsent(node, new HashMap<>());
				graph.get(node).put(ne,we);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, HashMap<String, Float>> getGraph() {
		return graph;
	}
	
	
	
	public static void main (String... args) {
		MyParser mp = new MyParser();
		
		String newFile = "test.csv";
		String filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/tiny.json";
		//String filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/1989-93.json";
		//String filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/dblp.json";
		
		long startP = System.nanoTime();
		mp.firstParse(filename);
		System.out.println("PARSE TIME: " + (System.nanoTime() - startP) / Math.pow(10, 9));
		
		HashMap<String, HashMap<String, Float>> graph = mp.getGraph();
		
		
			
			
		
		/*long startW = System.nanoTime();
		mp.writeCSV(newFile);
		System.out.println("WRITE TIME: " + (System.nanoTime() - startW) / Math.pow(10, 9));*/
		
		//graph = new HashMap<>();
		
		/*long start = System.nanoTime();
		mp.readCSV(newFile);
		System.out.println("READ TIME: " + (System.nanoTime()-start)/Math.pow(10,9));
		
		for (String s : mp.graph.keySet()) System.out.println(s + " {" + mp.graph.get(s) + "}");
		*/
		
		
		
		
	
		
		
		
	}

}
