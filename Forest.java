import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class Forest<E> {
	private HashMap<E, HashMap<E, Double>> graph;
	private HashMap<E, HashSet<E>> cutEdges;
	private ArrayList<Branch<E>> branchTips;
	// Isn't a tree, in essence, a big branch? ;) 
	private ArrayList<Branch<E>> trees;
	
	//how deeply to split the graph
	private int maxNumNodes = 5; 
	
	
	public Forest(HashMap<E, HashMap<E, Double>> graph) {
		if (graph == null || graph.isEmpty()) throw (new IllegalArgumentException("Cannot pass graph cannot be empty"));
		this.graph = graph;
		this.cutEdges = new HashMap<>();
		//this.branchTips = new ArrayList<>();
		//this.trees = new ArrayList<>();
		
	}
	
	public void growForest() throws Exception {
		if (!trees.isEmpty()) throw (new Exception("Forest already grows")); 
		
		trees = branchOut(graph.keySet());
		branchTips = trees;
	}
	
	//Edge Clustering Coefficient
	private double ECC (E start, E end) {
		double sum = 1.0; //so as to avoid 0 in the numerator
		sum += graph.get(start).keySet().stream().filter(other -> !other.equals(end) && !isCut(start, other)).count();
		//maximum number of possible triangles created with this edge 
		int maxTriangles = (newDegree(start) > newDegree(end)) ? newDegree(start) - 1 : newDegree(end) - 1;
		
		return sum / maxTriangles;
	}
	
	private int newDegree(E node) {
		int degree = 0;
		degree += graph.get(node).keySet().stream().filter(other -> !isCut(node, other)).count();
		
		return degree;
	}
	
	/*//temporary testing method
	public ArrayList<Branch<E>> getFirstCCs() {
		return findCCs(graph.keySet());
	}*/
	
	//find Connected Components
	private ArrayList<Branch<E>> branchOut(Set<E> motherBranch) {
		ArrayList<Branch<E>> CCs = new ArrayList<>();
		Set<E> visited = new HashSet<>();
		for(E node : motherBranch) 
			if (!visited.contains(node)) {
				Set<E> newCC = DFS(node);
				visited.addAll(newCC);
				CCs.add(new Branch(this, newCC));
			}
		return CCs;
	}
	
	private Set<E> DFS (E node) {
		Set<E> newCC = new HashSet<>();
		Stack<E> stack = new Stack<>();
		stack.push(node);
		E curr;
		
		while(!stack.isEmpty()) {
			curr = stack.pop();
			newCC.add(curr);
			if (graph.get(curr) != null && !graph.get(curr).isEmpty()) 
				for(E child : graph.get(curr).keySet())
					if (!isCut(curr, child) && !newCC.contains(child))
						stack.push(child);
		}
		
		return newCC;
	}
	//this method dosn't run on huge data sets due to a stack overflow
	private Set<E> DFS (Set<E> newCC, E curr) {
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
		private Set<E> nodes;
		private ArrayList<Branch<E>> children;
		
		
		public Branch(Forest<E> forest, Set<E> nodes) {
			this.nodes = nodes;
			children = new ArrayList<>(); //a moze null
			this.forest = forest;
		}
		
		//temporary method
		public Set<E> getNodes() {
			return nodes;
		}
	}
	
	public static void main (String ... args) {
		HashMap<String, HashMap<String, String>> hm = new HashMap<>();
		//System.out.println(hm.keySet() instanceof Set<?>);
		/*hm.put("bla", null);
		System.out.println	(hm.get("bla").isEmpty());*/
	}

}
