import java.io.BufferedReader;
import java.io.FileReader;

import java.util.HashMap;



public class MyParser2 {
	private HashMap<String, HashMap<String, Double>> graph = new HashMap<>(2000000);
	
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
					graph.get(authors[i]).computeIfPresent(authors[j], (k, v) -> v + (1d/authors.length));
					graph.get(authors[i]).putIfAbsent(authors[j], 1d/authors.length);
				}
		}
	}
	
	
	public HashMap<String, HashMap<String, Double>> getGraph() {
		return graph;
	}

}
