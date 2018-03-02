import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This is a generic class implementing weighted graph, where weights are of Double type and vertices (nodes) can be of any type.  
 * @author Jan Prokop
 *
 * @param <E> Vertices type. 
 */
public class Forest<E> {
	private HashMap<E, HashMap<E, Double>> graph;
	/*
	 * in both of following structures there will be only one entry for one edge
	 * (either node1-node2 or node2-node1 but not both)  
	 */
	private HashMap<E, HashSet<E>> cutEdges;  
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
	
	/*
	 * This method makes a list of connected components in a given graph and creates Branch objects with them.
	 * They become first branches (tree trunks)  
	 */
	public void plantForest(){
		if (!trees.isEmpty()) throw (new RuntimeException("Forest already grows")); 
		
		for (Set<E> treeNodes : findCCs(graph.keySet())) {
			Branch<E> tree = new Branch<E>(treeNodes);
			trees.add(tree);
			branchTips.add(tree);
		}
		initAllCoeffs();
	}
	
	public void growWholeBranches() {
		while (growBranches(1)) {}
	}
	
	//method that helps testing and debugging 
	public void printSample() {
		int i = 0;
		for (Branch<E> branch : branchTips) {
				StringBuilder sb = new StringBuilder();
				sb.append(branch);
				sb.append("\n\tsmallest parent community: " + branch.parent);
				System.out.println(sb);
				if (i++ > 20) break;
		}
	}
	
	//split connected components of the graph as long as they are all smaller than the threshold
	private boolean growBranches(int threshold) {
		boolean nextIterNeeded = false; 
		List<Branch<E>> branchOuts = new LinkedList<>(); 
		Iterator<Branch<E>> it = branchTips.iterator();
		while (it.hasNext()) {
			Branch<E> branch = it.next();
			if (branch.size() > threshold) { 
				for (Set<E> branchOut : branchOut(branch.getNodes())) branchOuts.add(new Branch<E>(branchOut, branch)); 
				it.remove();
			}
		}
		for (Branch<E> branch : branchOuts) {
			if (branch.size() > threshold) nextIterNeeded = true;
			branchTips.add(branch);
		}
		return nextIterNeeded;
	}
	
	/*
	 * for a given connected component cut it's weakest edges as long as it will partition into at least two parts
	 */
	private List<Set<E>> branchOut(Set<E> branch) {
		List<Set<E>> branchOuts = new LinkedList<>();
		do { 
			trimEdges(branch);
			branchOuts = findCCs(branch);
		} while (branchOuts.size() < 2);
		return branchOuts;
	}
	
	// trim weakest edges
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
	
	private double findMinCoeff(Set<E> branch) {
		if (branch.size() < 2) throw (new RuntimeException("Branch has only one node"));
		double min = Double.POSITIVE_INFINITY;
		for(E node : branch)
			if (coefficients.containsKey(node)) {
				for (Map.Entry<E, Double> neighbor : coefficients.get(node).entrySet())
					if (neighbor.getValue() < min) 
						min = neighbor.getValue();
			}
		return min;
	}
	
	//all necessary changes upon edge cut 
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
	
	private void initAllCoeffs() {
		for (E node : graph.keySet())
			for (E neighbor : graph.get(node).keySet())
				if (!hasCoeff(node, neighbor)) {
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
	
	//node degree in the graph that has lost some edges
	private int newDegree(E node) {
		int degree = 0;
		degree += graph.get(node).keySet().stream().filter(other -> !isCut(node, other)).count();
		
		return degree;
	}
	
	//find Connected Components
	private List<Set<E>> findCCs(Set<E> motherSet) {
		List<Set<E>> CCs = new LinkedList<>();
		Set<E> visited = new HashSet<>();
		for(E node : motherSet) 
			if (!visited.contains(node)) {
				Set<E> newCC = DFS(node);
				visited.addAll(newCC);
				CCs.add(newCC);
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
	
	private boolean isCut(E curr, E neighbor) {
		if (cutEdges.keySet().contains(curr))
			if (cutEdges.get(curr).contains(neighbor))
				return true;
		if (cutEdges.keySet().contains(neighbor))
			if (cutEdges.get(neighbor).contains(curr))
				return true;
		return false;
	}
	
	/*
	 * This class is a node class for so called dendrogram which is a tree like structure created by sequential splitting 
	 * of the graph into gradually smaller communities  
	 */
	private class Branch<E> {
		private Forest<E> forest;
		private Set<E> nodes;
		private Branch<E> parent;
		private List<Branch<E>> children; 
		
		
		public Branch(Set<E> nodes, Branch<E> parent) {
			this.nodes = nodes;
			this.parent = parent;
			children = new LinkedList<>(); 
			parent.addChild(this);
			//parent.children.add(this);
		}
		
		public Branch(Set<E> nodes) {
			this.nodes = nodes;
			parent = null;
			children = new LinkedList<>(); 
		}
		
		public Set<E> getNodes() {
			return nodes;
		}
		
		public void addChild(Branch<E> child) {
			children.add(child);
		}
		
		public List<Branch<E>> getChildren() {
			return children;
		}
		
		public Branch<E> getParent() {
			return parent;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			Iterator<E> it = nodes.iterator();
			while (it.hasNext()) {
				sb.append(it.next());
				if (it.hasNext()) sb.append(", ");
			}
			sb.append("]");
			
			return sb.toString();
		}
		
		public int size() {
			return nodes.size();
		}
	}
}
