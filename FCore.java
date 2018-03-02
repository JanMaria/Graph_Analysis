import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * The purpose of this class is to trim a weighted graph to it's f-core with given threshold. The threshold is the minimal 
 * weight of each vertex in resulting graph. It's important to mention that the initial weight of one vertex is likely to
 * decrease after each trimming. 
 * @author Jan Prokop
 *
 */
public class FCore{
	private HashMap<String, HashMap<String, Double>> graph;
	private static double threshold = 2;
	
	public FCore(HashMap<String, HashMap<String, Double>> graph) {
		this.graph = graph;
	}
	
	public void setThreshold(int threshold) {
		FCore.threshold = threshold;
	}
	
	public HashMap<String, HashMap<String, Double>> findFCore() {
		HashSet<String> exterior = new HashSet<>();
		for (String author : graph.keySet()) 
			findExterior(exterior, author);
		for (String author : exterior)
			graph.remove(author);
		for (Map.Entry<String, HashMap<String, Double>> coauthorship : graph.entrySet()) {
			Iterator<String> coauthors = coauthorship.getValue().keySet().iterator();
			while (coauthors.hasNext()) 
				if (exterior.contains(coauthors.next()))
					coauthors.remove();
		}
		
		return graph;
	}
	/*Helper function. It was easier to first calculate what doesn't belong to f-core with a given threshold
	 * and then to remove it from the graph. 
	 */
	private HashSet<String> findExterior(HashSet<String> exterior, String currAuthor) {
		if (!exterior.contains(currAuthor)) {
			double sum;
			HashMap<String, Double> currMap = graph.get(currAuthor); 
			sum = currMap.entrySet().stream().filter(e -> !exterior.contains(e.getKey()))
					.mapToDouble(e -> e.getValue()).sum();
			if (sum < threshold) {
				exterior.add(currAuthor);
				for (String author : currMap.keySet())
					findExterior(exterior, author);
			}
		}
		
		return exterior;
	}
	
	public HashMap<String, HashMap<String, Double>> getGraph() {
		return graph;
	}

}
