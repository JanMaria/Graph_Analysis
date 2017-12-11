import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MyRunner {
	
	public static void main (String... args) {
		String filename = "";
		filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/tiny.json";
		filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/1989-93.json";
		//filename = "C:/Users/Jan/Desktop/Jan/Programowanie/Capstone/data/dblp.json";
		
		HashMap<String, HashMap<String, Double>> graph = new HashMap<>();
		MyParser2 parser = new MyParser2();
		//long strt = System.nanoTime();
		parser.firstParse(filename);
		//System.out.println("run time is : " + (System.nanoTime() - strt) / Math.pow(10,9));
		
		
	
		
		
		
		
		
		graph = parser.getGraph();
		System.out.format("Graph size after parse is: %s nodes", graph.size());
		
		FCore fCore = new FCore(graph);
		
		/*for (String s : graph.keySet()) {
			System.out.println(s + " {" + graph.get(s) + "}");
		}*/
		long strt = System.nanoTime();
		fCore.findFCore();
		System.out.println("run time is : " + (System.nanoTime() - strt) / Math.pow(10,9));
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		/*fCore.setThreshold(3);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(4);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(5);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(6);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(7);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(8);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(9);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(10);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(11);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(12);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(13);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(14);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(15);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(16);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(17);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(18);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(19);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());
		
		fCore.setThreshold(20);
		fCore.findFCore();
		System.out.format("Graph size after f-core analisys is: %s nodes\n", graph.size());*/
		
		Forest<String> f = new Forest<>(graph);
		f.plantForest();
		System.out.println("FORRETST PLANTED");
		
		long start = System.nanoTime();
		f.growWholeBranches();
		System.out.println("GROWING BRANCHES TIME: " + (System.nanoTime() - start) / Math.pow(10, 9));
		
		f.printSample();
		//ArrayList<Forest<String>.Branch<String>> al = f.getFirstCCs();
		//System.out.format("there are %s trees in that forest\n", al.size());
		
		/*int count = 0;
		for (Forest<String>.Branch<String> branch : al) {
			System.out.print(branch.getNodes().size() + " ");
			if (++count%30 == 0)
				System.out.print("\n");
		}
		
		int fullSize = al.stream().mapToInt(b -> b.getNodes().size()).sum();
		System.out.println("\nfull size is " + fullSize);*/
	}

}
