import java.util.HashMap;
import java.util.Random;

/* 
 * This class is for debugging and testing purposes.   
 */

public class RandMap {
	
	//s is seed for Random class object, n is a number of vertices and for each vertex this function generates 1 to max new edges
	public static HashMap<Integer, HashMap<Integer, Double>> generate (int s, int n, int max) {
		Random r = new Random(s);
		HashMap<Integer, HashMap<Integer, Double>> hm = new HashMap<>();
		for (int i = 0; i < n; i++)
			hm.put(i, new HashMap<>());
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < r.nextInt(max)+1; j++) {
				int k = r.nextInt(n);
				double d = r.nextDouble();
				if (i != k) {
					hm.get(i).putIfAbsent(k, d);
					hm.get(k).putIfAbsent(i, d);
				}
			}
		}
		
		return hm;
	}

}
