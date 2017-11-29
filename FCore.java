import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FCore{
	private HashMap<String, HashMap<String, Double>> graph;
	private static final double THRESHOLD = 2;
	
	public FCore(HashMap<String, HashMap<String, Double>> graph) {
		this.graph = graph;
	}
	
	public HashMap<String, HashMap<String, Double>> findFCore() {
		//HashMap<String, HashMap<String, Double>> hm = graph;
		HashSet<String> exterior = new HashSet<>();
		for (String author : graph.keySet()) 
			findExterior(exterior, author);
		
		System.out.println("graph size: " + graph.size());
		
		for (String author : exterior)
			graph.remove(author);
		
		for (Map.Entry<String, HashMap<String, Double>> coauthorship : graph.entrySet()) {
			Iterator<String> coauthors = coauthorship.getValue().keySet().iterator();
			while (coauthors.hasNext()) 
				if (exterior.contains(coauthors.next()))
					coauthors.remove();
		}
		
		/*for (String author : hm.keySet()) {
			if (exterior.contains(author)) hm.remove(author);
			else {
				Iterator<Map.Entry<String,Double>> coauthors = hm.get(author).entrySet().iterator();
				while (coauthors.hasNext())
					if (exterior.contains(coauthors.next().getKey()))
						coauthors.remove();
			}
		}*/
				
				
				/*for (String coauthor : hm.get(author).keySet())
				if (exterior.contains(coauthor)) hm.get(author).remove(coauthor);*/
		
		System.out.println("Exterior size: " + exterior.size() + 
				"\nHM Size: " + graph.size() + 
				"\nGraph size: " + graph.size());
		
		return graph;
	}
	
	public void findFcore(float f) {
		
	}
	
	private HashSet<String> findExterior(HashSet<String> exterior, String currAuthor) {
		if (!exterior.contains(currAuthor)) {
			double sum;
			HashMap<String, Double> currMap = graph.get(currAuthor); 
			sum = currMap.entrySet().stream().filter(e -> !exterior.contains(e.getKey()))
					.mapToDouble(e -> e.getValue()).sum();
			if (sum < THRESHOLD) {
				exterior.add(currAuthor);
				for (String author : currMap.keySet())
					findExterior(exterior, author);
			}
		}
		
		return exterior;
	}

}
