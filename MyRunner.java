import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/*
 * This class just runs other classes methods in a proper order. 
 */
public class MyRunner {
	
	public static void main (String... args) {
		String filename = "";
		filename = "C:/*****************/data/dblp.json"; //gzipped file available at: http://projects.csail.mit.edu/dnd/DBLP/
		
		HashMap<String, HashMap<String, Double>> graph = new HashMap<>();
		MyParser parser = new MyParser();
		parser.firstParse(filename);
		
		graph = parser.getGraph();
		System.out.format("Graph size after parse is: %s nodes", graph.size());
		
		FCore fCore = new FCore(graph);
		
		/*
		 * this loop gradually trim the F-Core to prevent stack overflow 
		 * although on some systems it might not be enough - it depends on
		 * VM stack size  
		 */
		for (int i = 2; i <= 20; i++) {
			fCore.setThreshold(i);
			fCore.findFCore();
		}
		
		Forest<String> f = new Forest<>(graph);
		f.plantForest();
		f.growWholeBranches();
		
		System.out.println("This is just a sample of the outcome: ");
		f.printSample();
	}

}
