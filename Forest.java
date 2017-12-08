import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

public class Forest<E> {
	private HashMap<E, HashMap<E, Double>> graph;
	/*
	 * in both of following structures there will be only one entry for one edge
	 * (either node1-node2 or node2-node1 but not both)  
	 */
	private HashMap<E, HashSet<E>> cutEdges;
	private HashMap<E, HashMap<E, Double>> coefficients;
	
	private ArrayList<Branch<E>> branchTips;
	// Isn't a tree, in essence, a big branch? ;) 
	private ArrayList<Branch<E>> trees;
	
	//how deeply to split the graph
	private int maxNumNodes = 5; 
	
	
	public Forest(HashMap<E, HashMap<E, Double>> graph) {
		if (graph == null || graph.isEmpty()) throw (new IllegalArgumentException("Cannot pass empty graph"));
		this.graph = graph;
		this.cutEdges = new HashMap<>();
		this.coefficients = new HashMap<>();
		//this.branchTips = new ArrayList<>();
		this.trees = new ArrayList<>();
		
	}
	
	public void growForest() throws Exception {
		if (!trees.isEmpty()/* != null*/) throw (new Exception("Forest already grows")); 
		
		for (Set<E> tree : branchOut(graph.keySet())) trees.add(new Branch<E>(tree));
		branchTips = trees;
	}
	
	private void recalcCoeffs(E node) {
		for (E neighbor : graph.get(node).keySet())
			if (!isCut(node, neighbor)) {
				if (coefficients.containsKey(node))
					if (coefficients.get(node).containsKey(neighbor)) {
						coefficients.get(node).put(neighbor, ECC(node, neighbor));
						return;
					}
				coefficients.get(neighbor).put(node, ECC(node, neighbor));
			}
	}
	
	//this exception might be pointless
	private double findMinCoeff(Branch<E> branch) throws Exception {
		if (branch.getNodes().size() < 2) throw (new Exception("branch has only one node"));
		double min = Double.POSITIVE_INFINITY;
		double partialMin = Double.POSITIVE_INFINITY;
		for(E node : branch.getNodes())
			if (coefficients.containsKey(node)) {
				try {
				partialMin = coefficients.get(node).entrySet().stream()
						.filter(e -> branch.getNodes().contains(e.getKey()) && !isCut(node, e.getKey()))
						.mapToDouble(e -> e.getValue()).min().getAsDouble();
				} catch (NoSuchElementException err) {
					partialMin = Double.POSITIVE_INFINITY;
					System.out.println("No such element catched");
				}
				min = (partialMin < min) ? partialMin : min;
			}
		return min;
	}
	
	private void cutEdge(E start, E end) {
		if (cutEdges.containsKey(start)) cutEdges.get(start).add(end);
		else cutEdges.get(end).add(start);
		
	}
	
	private void initAllCoeff(Branch<E> branch) {
		for (E node : branch.getNodes())
			for (E neighbor : graph.get(node).keySet())
				if (!hasCoeff(node, neighbor) && !isCut(node, neighbor)) {
					if (!coefficients.containsKey(node)) coefficients.put(node, new HashMap<>());  
					coefficients.get(node).put(neighbor, ECC(node, neighbor));
				}
	}
	
	private boolean hasCoeff(E start, E end) {
		if (coefficients.keySet().contains(start))
			if (coefficients.get(start).keySet().contains(end)) return true;
		if (coefficients.keySet().contains(end))
			if (coefficients.get(end).keySet().contains(start)) return true;
		return false;
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
	
	//find Connected Components ::: maybe other name e.g. findBranches or findCCs
	private ArrayList<Set<E>> branchOut(Set<E> motherSet) {
		ArrayList<Set<E>> CCs = new ArrayList<>();
		Set<E> visited = new HashSet<>();
		for(E node : motherSet) 
			if (!visited.contains(node)) {
				Set<E> newCC = DFS(node);
				visited.addAll(newCC);
				CCs.add(newCC/*new Branch(this, newCC, new Branch<E>(motherBranch))*/);
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
		if (cutEdges.keySet().contains(neighbor))
			if (cutEdges.get(neighbor).contains(curr))
				return true;
		return false;
	}
	
	//temporary public
	// it stores connected components of the graph ???????????
	public class Branch<E> {
		private Forest<E> forest;
		private Set<E> nodes;
		private Branch<E> parent;
		private ArrayList<Branch<E>> children;
		
		
		/*does it need forest?*/
		public Branch(/*Forest<E> forest, */Set<E> nodes, Branch<E> parent) {
			this.nodes = nodes;
			this.parent = parent;
			children = new ArrayList<>(); //a moze null
			/*this.forest = forest;*/
		}
		
		public Branch(Set<E> nodes) {
			this.nodes = nodes;
			parent = null;
			children = new ArrayList<>(); // a moze null jak wyzej
		}
		
		//temporary method
		public Set<E> getNodes() {
			return nodes;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[\n");
			for(E e : nodes) 
				sb.append("- " + e + "\n");
			sb.append("]\n");
			
			return sb.toString();
		}
	}
	
	public static void main (String ... args) {
		HashMap<String, HashMap<String, String>> hm = new HashMap<>();
		//System.out.println(hm.keySet() instanceof Set<?>);
		/*hm.put("bla", null);
		System.out.println	(hm.get("bla").isEmpty());*/
		int b = 1;
		HashMap<Integer, HashMap<Integer, Double>> hmi = RandMap.generate(4, 10, 1); 
		for (Integer i : hmi.keySet())
			System.out.println(i + " {" + hmi.get(i));
		Forest<Integer> f = new Forest<>(hmi);
		try {
			f.growForest();
			for(Forest<Integer>.Branch<Integer> branch : f.trees){
				f.initAllCoeff(branch);
				System.out.format("Minimum coeff for %s branch is %s%n", b++, f.findMinCoeff(branch));
				System.out.println("Branch " + b + " {" + branch + "}");
			}
			for (Integer i : f.coefficients.keySet()) 
				System.out.println(i + " {" + f.coefficients.get(i) + "}");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//f.
	}

}
