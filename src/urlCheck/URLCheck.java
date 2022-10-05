package urlCheck;

import java.util.ArrayList;

/**
 * <pre>
 * Service
 * </pre>
 * 
 * @author In-seong Hwang
 * @since 2019. 02.08
 */

public class URLCheck {

	
	public static void main(String[] args) {
/*		CheckThread CT1 = new CheckThread("/a1", "CT1");
		CheckThread CT2 = new CheckThread("/a2", "CT2");
		CheckThread CT3 = new CheckThread("/a3", "CT3");
		CheckThread CT4 = new CheckThread("/a4", "CT4");
		
		Thread a1 = new Thread(CT1);
		Thread a2 = new Thread(CT2);
		Thread a3 = new Thread(CT3);
		Thread a4 = new Thread(CT4);
*/		
		ArrayList<Thread> threadList = new ArrayList<Thread>();
		/*for (int i = 1; i < 3; i++) {
			Thread CT = new Thread(new CheckThread("/a"+i, "CT"+i));
			CT.start();
			threadList.add(CT);
			
		}*/
		Thread CT1 = new Thread(new CheckThread("/a1", "CT1"));
		CT1.start();
		threadList.add(CT1);
		
		for(int j =0 ; j<threadList.size() ; j++) {
			Thread temp = threadList.get(j);
			try {
				temp.join();
			}catch (Exception e) {
				System.out.println("main error : "+e.getMessage());
			}
		}
		System.out.println("main complete : ");
	}
}
