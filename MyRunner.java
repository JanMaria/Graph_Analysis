import java.util.ArrayList;
import java.util.HashMap;

public class MyRunner {
	
	public static void main (String... args) {
		//String filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/tiny.json";
		String filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/1989-93.json";
		//String filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/dblp.json";
		
		HashMap<String, HashMap<String, Double>> graph = new HashMap<>();
		MyParser2 parser = new MyParser2();
		//long strt = System.nanoTime();
		parser.firstParse(filename);
		//System.out.println("run time is : " + (System.nanoTime() - strt) / Math.pow(10,9));
		
		Forest<String> f = new Forest<>(parser.getGraph());
		ArrayList<Forest<String>.Branch<String>> al = f.getFirstCCs();
		
		
		/*graph = parser.getGraph();
		
		FCore fCore = new FCore(graph);
		
		for (String s : graph.keySet()) {
			System.out.println(s + " {" + graph.get(s) + "}");
		}
		long strt = System.nanoTime();
		fCore.findFCore();
		System.out.println("run time is : " + (System.nanoTime() - strt) / Math.pow(10,9));*/
	}

}
