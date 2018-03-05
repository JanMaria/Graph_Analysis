import java.io.BufferedReader;
import java.io.FileReader;

import java.util.HashMap;

//class parsing a dataset available at: http://projects.csail.mit.edu/dnd/DBLP/
public class MyParser {
	private HashMap<String, HashMap<String, Double>> graph = new HashMap<>(2000000);
	
	/**
	 * Parsing method. It's called "first" just for "historical reasons". 
	 * @param filename The name of the file to parse
	 */
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
	
	/**
	 * This private method parse one line of the .json file to get a list of co-authors of one paper
	 * @param line One line of the parsed file
	 * @return Array of names of co-authors of one paper
	 */
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
	
	//this adds weighted edge to the graph or increase the weight of existing edge
	private void addCoop(String[] authors) {
		for (int i = 0; i < authors.length; i++) {
			graph.putIfAbsent(authors[i], new HashMap<>());
			for (int j = 0; j < authors.length; j++)
				if (i != j) {
					graph.get(authors[i]).computeIfPresent(authors[j], (k, v) -> v + (1d/authors.length));
					graph.get(authors[i]).putIfAbsent(authors[j], 1d/authors.length);
				}
		}
	}
	
	
	public HashMap<String, HashMap<String, Double>> getGraph() {
		return graph;
	}

}
