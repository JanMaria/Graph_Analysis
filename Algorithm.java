import java.util.HashMap;

public abstract class Algorithm {
	private HashMap<String, HashMap<String, Float>> graph;
	
	public abstract void processGraph();
	
	public HashMap<String, HashMap<String, Float>> getGraph () {
		return graph;
	}
	

}
