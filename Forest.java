import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

public class Forest<E> {
	private HashMap<E, HashMap<E, Double>> graph;
	/*
	 * in both of following structures there will be only one entry for one edge
	 * (either node1-node2 or node2-node1 but not both)  
	 */
	private HashMap<E, HashSet<E>> cutEdges; //this is useless when coefficients has the same nodes 
	private HashMap<E, HashMap<E, Double>> coefficients;
	
	private LinkedList<Branch<E>> branchTips;
	// Isn't a tree, in essence, a big branch? ;) 
	private LinkedList<Branch<E>> trees; 
	
	
	public Forest(HashMap<E, HashMap<E, Double>> graph) {
		if (graph == null || graph.isEmpty()) throw (new IllegalArgumentException("Cannot pass empty graph"));
		this.graph = graph;
		this.cutEdges = new HashMap<>();
		this.coefficients = new HashMap<>();
		this.branchTips = new LinkedList<>();
		this.trees = new LinkedList<>();
		
	}
	
	public void plantForest(){
		if (!trees.isEmpty()) throw (new RuntimeException("Forest already grows")); 
		
		for (Set<E> tree : findBranchOuts(graph.keySet())) {
			trees.add(new Branch<E>(tree));
			branchTips.add(new Branch<E> (tree));
		}
		initAllCoeffs();
	}
	
	public void growWholeBranches() {
		while (growBranches(1000)) {}
	}
	
	//temporary testing method
	public void printSample() {
		int i = 0;
		for (Branch<E> branch : branchTips) {
			if (branch.size() < 20) {
				StringBuilder sb = new StringBuilder();
				sb.append(branch);
				if (branch.parent != null) {sb.append(" parent: " + branch.parent.getNodes());}
				else {sb.append(" parent: " + branch.parent);}
				/*if (branch.parent.parent != null) sb.append("\nPARRENT parent: " + branch.parent.parent.getNodes());
				else sb.append("\nPARRENT parent: " + branch.parent.parent);*/
				System.out.println(sb);
				//if (i++ > 10) break;
			}
		}
	}
	
	//jakas inna nazwa chyba and this method is useless for larger graph because it iterates only once
	private void growBranches() {
		List<Branch<E>> branchOuts = new LinkedList<>(); 
		Iterator<Branch<E>> it = branchTips.iterator();
		while (it.hasNext()) {
			Branch<E> branch = it.next();
			if (branch.size() > 5) { /////////////// this is threshold... should be changed somehow
				for (Set<E> branchOut : branchOut(branch.getNodes())) branchOuts.add(new Branch(branchOut, branch)); 
				/*branchOuts.addAll(branchOut(branch.getNodes()));*/
				it.remove();
			}
		}
		for (Branch<E> branch : branchOuts) branchTips.add(branch);
	}
	
	private boolean growBranches(int threshold) {
		long start = System.nanoTime();
		boolean nextIterNeeded = false; 
		List<Branch<E>> branchOuts = new LinkedList<>(); 
		Iterator<Branch<E>> it = branchTips.iterator();
		while (it.hasNext()) {
			Branch<E> branch = it.next();
			if (branch.size() > threshold) { 
				for (Set<E> branchOut : branchOut(branch.getNodes())) branchOuts.add(new Branch(branchOut, branch)); 
				
				it.remove();
				
			}
		}
		for (Branch<E> branch : branchOuts) {
			if (branch.size() > threshold) nextIterNeeded = true;
			branchTips.add(branch);
		}
		System.out.format("\nONE ITERATION ENDED IN TIME: %s%n", (System.nanoTime() - start) / Math.pow(10,9));
		return nextIterNeeded;
	}
	
	private List<Set<E>> branchOut(Set<E> branch) {
		List<Set<E>> branchOuts = new LinkedList<>();
		do { 
			trimEdges(branch);
			branchOuts = findBranchOuts(branch);
		} while (branchOuts.size() < 2);
		return branchOuts;
	}
	
	/*private void trimEdges(Set<E> branch){
		double min = findMinCoeff(branch);
		for (E node : branch) 
			for (E neighbor : graph.get(node).keySet()) 
				if (!isCut(node, neighbor) && graph.get(node).get(neighbor) == min) //tu chyba powinno byc coefficients a nie graph
					cutEdge(node,neighbor);
	}*/
	
	private void trimEdges(Set<E> branch) {
		double min = findMinCoeff(branch);
		List<E> dumpster = new ArrayList<>();
		for (E node : branch)
			for (E neighbor : graph.get(node).keySet())
				if (!isCut(node, neighbor) && getCoeff(node, neighbor) == min) {
					dumpster.add(node);
					dumpster.add(neighbor);
				}
		emptyDumpster(dumpster);
	}
	
	private void emptyDumpster(List<E> dumpster) {
		Iterator<E> it = dumpster.iterator();
		while(it.hasNext()) {
			cutEdge(it.next(), it.next());
		}
	}
	
	private double getCoeff(E start, E end) {
		if (coefficients.containsKey(start))
			if (coefficients.get(start).containsKey(end))
				return coefficients.get(start).get(end);
		return coefficients.get(end).get(start);
	}
	
	/*private void trimForest() {
		trimEdges(graph.keySet());
	}*/
	
	//this method is optimized to work better with the whole graph 
	private void trimForest() {
		double min = findMinCoeff(graph.keySet());
		for (E node : coefficients.keySet()) {
			for (Map.Entry<E, Double> neighbor : coefficients.get(node).entrySet()) // what happens if coefficients.get(node).isEmpty() == true?
				if (neighbor.getValue() == min)
					cutEdge(node, neighbor.getKey());
		}
			
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
	
	//this is useful if I decide to split only one branch (e.g. giant component)
	private double findMinCoeff(Set<E> branch) /*throws Exception */{
		if (branch.size() < 2) throw (new RuntimeException("Branch has only one node"));
		double min = Double.POSITIVE_INFINITY;
		//double partialMin = Double.POSITIVE_INFINITY;
		for(E node : branch)
			if (coefficients.containsKey(node)) {
				for (Map.Entry<E, Double> neighbor : coefficients.get(node).entrySet())
					if (neighbor.getValue() < min) 
						min = neighbor.getValue();
				/*try {
				partialMin = coefficients.get(node).entrySet().stream()
						.filter(e -> branch.contains(e.getKey()) && !isCut(node, e.getKey()))
						.mapToDouble(e -> e.getValue()).min().getAsDouble();
				} catch (NoSuchElementException err) {
					partialMin = Double.POSITIVE_INFINITY;
					System.out.println("No such element catched");
				}
				min = (partialMin < min) ? partialMin : min;*/
			}
		return min;
	}
	
	private double findMinCoeff() {
		double min = Double.POSITIVE_INFINITY;
		double partialMin = Double.POSITIVE_INFINITY;
		for(E node : graph.keySet())
			if (coefficients.containsKey(node)) {
				//try {
				partialMin = coefficients.get(node).entrySet().stream()
						/*.filter(e -> !isCut(node, e.getKey()))*/
						.mapToDouble(e -> e.getValue()).min().getAsDouble();
				/*} catch (NoSuchElementException err) {
					partialMin = Double.POSITIVE_INFINITY;
					System.out.println("No such element catched");
				}*/
				min = partialMin < min ? partialMin : min;
			}
		return min;
	}
	
	private void cutEdge(E start, E end) {
		if (!cutEdges.containsKey(start)) cutEdges.put(start, new HashSet<>()); 
		cutEdges.get(start).add(end);
		removeFromCoeff(start, end);
		recalcCoeffs(start);
		recalcCoeffs(end);
	}
	
	private void removeFromCoeff(E start, E end) {
		if (coefficients.containsKey(start))
			if (!coefficients.get(start).keySet().remove(end))
				coefficients.get(end).keySet().remove(start);
	}
	
	//this is probably going to be deleted 
	private void initAllCoeff(Branch<E> branch) {
		for (E node : branch.getNodes())
			for (E neighbor : graph.get(node).keySet())
				if (!hasCoeff(node, neighbor)/* && !isCut(node, neighbor)*/) { //it's happening at the beginning so there are no cut edges
					if (!coefficients.containsKey(node)) coefficients.put(node, new HashMap<>());  
					coefficients.get(node).put(neighbor, ECC(node, neighbor));
				}
	}
	
	private void initAllCoeffs() {
		for (E node : graph.keySet())
			for (E neighbor : graph.get(node).keySet())
				if (!hasCoeff(node, neighbor)) {
					if (!coefficients.containsKey(node)) coefficients.put(node, new HashMap<>());  
					coefficients.get(node).put(neighbor, ECC(node, neighbor));
				}
	}
	
	//this is actually equivalent of !isCut
	private boolean hasCoeff(E start, E end) {
		if (coefficients.keySet().contains(start))
			if (coefficients.get(start).keySet().contains(end)) return true;
		if (coefficients.keySet().contains(end))
			if (coefficients.get(end).keySet().contains(start)) return true;
		return false;
	}
	
	/**
	 * Edge Clustering Coefficient is a ratio between the number of all triangles one edge creates, and the number 
	 * it could create, considering the smaller degree of it's two nodes. The numerator is incremented by one 
	 * to remedy zero numerator
	 * @param start First node of the edge
	 * @param end Second node of the edge
	 * @return Edge Clustering Coefficient
	 */
	private double ECC (E start, E end) {
		double sum = 1.0; //so as to avoid 0 in the numerator
		sum += getNumTriangles(start, end); 
		int maxTriangles = (newDegree(start) < newDegree(end)) ? newDegree(start) - 1 : newDegree(end) - 1;
		
		return sum / maxTriangles;
	}
	
	private int getNumTriangles(E start, E end) {
		int sum = 0;
		for (E node : graph.get(start).keySet())
			if (!isCut(start, node) && node != end)
				for (E neighbor : graph.get(node).keySet())
					if (neighbor == end && !isCut(node, neighbor))
						sum++;
		return sum;
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
	private ArrayList<Set<E>> findBranchOuts(Set<E> motherSet) {
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
		private ArrayList<Branch<E>> children;  // nie sa inicjowane na razie nigdy
		
		
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
			sb.append("]");
			
			return sb.toString();
		}
		
		public int size() {
			return nodes.size();
		}
	}
	
	/*public static void main (String ... args) {
		HashMap<String, HashMap<String, String>> hm = new HashMap<>();
		//System.out.println(hm.keySet() instanceof Set<?>);
		hm.put("bla", null);
		System.out.println	(hm.get("bla").isEmpty());
		int b = 0;
		HashMap<Integer, HashMap<Integer, Double>> hmi = RandMap.generate(4, 10, 1); 
		for (Integer i : hmi.keySet())
			System.out.println(i + " {" + hmi.get(i));
		Forest<Integer> f = new Forest<>(hmi);
		try {
			f.plantForest();
			for(Forest<Integer>.Branch<Integer> branch : f.trees){
				if (branch.size() > 1) System.out.format("Minimum coeff for %s branch is %s%n", ++b, f.findMinCoeff(branch.getNodes()));
				System.out.println("Branch " + b + " {" + branch + "}");
				for (Integer i : f.graph.get(1).keySet()) {
					System.out.println("jblklkbdlkfskdf" + i);
				}
				if (branch.size() > 1) f.trimEdges(branch.getNodes());
				for (Set s : f.branchOut(branch.getNodes()))
					System.out.println("BRANCHOUT: " + b + s);
				//if (branch.size() > 1) System.out.println("BRANCHOUT: " + b + f.branchOut(branch.getNodes()));
				
			}
			f.growWholeBranches();
			int in = 0;
			for (Forest<Integer>.Branch<Integer> branch : f.branchTips) 
				System.out.println("BRANCH TIP: " + ++in + branch + " parrent: " + branch.parent);
			
			for (Forest<Integer>.Branch<Integer>  tree : f.trees)
				System.out.println(tree);
			for (Integer i : f.coefficients.keySet()) 
				System.out.println(i + " {" + f.coefficients.get(i) + "}");
			
			
			
		} catch (Exception e) {
			System.err.println("ERRRRRRRRR");
			e.printStackTrace();
		}
		
		//f.
	}*/

}
