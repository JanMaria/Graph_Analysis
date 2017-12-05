import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Forest<E> {
	private HashMap<E, HashMap<E, Double>> graph;
	private HashMap<E, HashSet<E>> cutEdges;
	private ArrayList<Branch<E>> branchTips;
	// Isn't a tree, in essence, a big branch? ;) 
	private ArrayList<Branch<E>> trees;
	
	
	public Forest(HashMap<E, HashMap<E, Double>> graph) {
		this.graph = graph;
		this.cutEdges = new HashMap<>();
		this.branchTips = new ArrayList<>();
		this.trees = new ArrayList<>();
		
	}
	
	public ArrayList<Branch<E>> getFirstCCs(/*HashSet<E> branch*/) {
		return findCCs((HashSet<E>)graph.keySet());
	}
	
	private ArrayList<Branch<E>> findCCs(HashSet<E> motherBranch) {
		ArrayList<Branch<E>> CCs = new ArrayList<>();
		HashSet<E> visited = new HashSet<>();
		for(E node : motherBranch) 
			if (!visited.contains(node)) {
				HashSet<E> newCC = DFS(new HashSet<E>(), node);
				visited.addAll(newCC);
				CCs.add(new Branch(this, newCC));
			}
		return CCs;
	}
	
	private HashSet<E> DFS (HashSet<E> newCC, E curr) {
		newCC.add(curr);
		if (graph.get(curr) != null && !graph.get(curr).isEmpty())
			for(E node : graph.get(curr).keySet())
				if (!isCut(curr, node) && !newCC.contains(node))
					DFS(newCC, node);
		return newCC;
	}
	
	private boolean isCut(E curr, E neighbor) {
		if (cutEdges.keySet().contains(curr))
			if (cutEdges.get(curr).contains(neighbor))
				return true;
		return false;
	}
	
	//temporary public
	public class Branch<E> {
		private Forest<E> forest;
		private HashSet<E> nodes;
		private ArrayList<Branch<E>> children;
		
		
		public Branch(Forest<E> forest, HashSet<E> nodes) {
			this.nodes = nodes;
			children = new ArrayList<>(); //a moze null
			this.forest = forest;
		}
		
		//temporary method
		public HashSet<E> getNodes() {
			return nodes;
		}
	}
	
	public static void main (String ... args) {
		HashMap<String, HashMap<String, String>> hm = new HashMap<>();
		System.out.println(hm.keySet() instanceof Set<?>);
		/*hm.put("bla", null);
		System.out.println	(hm.get("bla").isEmpty());*/
	}

}
