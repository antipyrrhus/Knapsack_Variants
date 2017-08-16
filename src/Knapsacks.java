/** Class: Knapsacks.java
 *  @author Yury Park
 *  This class - Various methods for solving the knapsack problem.
 *  ==========================================================================================================================
 *  We are given a text data file that has the following format:
 *
 *  [knapsack_size][number_of_items]
 *  [value_1] [weight_1]
 *  [value_2] [weight_2]
 *  ...
 *  For example, if the third line of the file says "50074 659", it means that the
 *  second item has value 50074 and size 659, respectively.
 *  We can assume that all numbers are positive integers.
 *  
 *  This class presents three algorithmic approaches to solving the knapsack problem:
 *  1) Recursive solution with memoization
 *  2) Iterative solution via dynamic programming
 *  3) Approximate solution in accordance with the level of desired accuracy
 *     that the user inputs (e.g. 90% accuracy) 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Knapsacks {

	private String filename;						//Name of text file for reading in data
	private int W;									//total weight capacity of the knapsack
	private int N;									//total no. of items
	private Item[] itemArr;							//Array of Item objects. Item is a custom inner class.
	private HashMap<ArrayList<Integer>, Integer> K;	//Optimal total knapsack value, mapped by weight. Used by the recursive method only.
	private int totalValue;
	private static boolean debugOn;					//If set to true, prints out extra (verbose) info to the console.

	/**
	 * 2-arg constructor.
	 * @param filename Name of file to read data from
	 * @param debug if set to true, prints out verbose info to the console
	 */
	public Knapsacks (String filename, boolean debug) {
		this.K = new HashMap<ArrayList<Integer>, Integer>();
		this.filename = filename;
		this.totalValue = 0;
		debugOn = debug;
		build();
	}

	 /**
     * Method: build
     * 1. Read in the text file
     * 2. Fill up the Item[] array.
     */
    private void build(){
        try {
            BufferedReader rd = new BufferedReader(new FileReader(new File(filename)));

            //Read the first line
            String line = rd.readLine();

            StringTokenizer tokenizer = new StringTokenizer(line);
            W = Integer.parseInt(tokenizer.nextToken());  	//Read in the total weight capacity on the first line
            N = Integer.parseInt(tokenizer.nextToken());	//Read in the total no. of items on the first line

            /* Initalize Item array (set size as N + 1 because we'll denote itemArr[0] as a dummy Item.)
             * So the actual Items will be stored from itemArr[1] all the way to itemArr[N]. */
            itemArr = new Item[N + 1];
            itemArr[0] = new Item(0, 0, 0);		//dummy item
            int index = 1;					//Actual Item object will be added starting at index 1.

            while ((line = rd.readLine()) != null) {
                tokenizer = new StringTokenizer(line);
                int value = Integer.parseInt(tokenizer.nextToken());
                int weight = Integer.parseInt(tokenizer.nextToken());
                this.totalValue += value;
                //Add each Item (inner class object) to the array with its value and weight
                itemArr[index] = new Item(index, value, weight);
                index++;
            }
            rd.close();	//close the reader when done.

            //Testing
            if (debugOn) {
            	for (Item i : itemArr) System.out.println(i);	//Print out each item
            	System.out.printf("Total no. of items (excluding the dummy item at index 0): %s\n", N);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //end private void build()

    /**
	 * Method: knapsackRecur
	 *         Uses recursion and memoization.
	 *         Clearly faster than brute force, not quite as fast as optimized iteration (see further below).
	 *
	 * @param i the index number of the current Item.
	 *          Remember that for convenience, the index begins from 1 all the way to N. (as opposed to from 0 to N-1)
	 * @param w the running weight capacity left in the knapsack.
	 * @return  the maximum value achievable using a knapsack of capacity w and items 1 thru i.
	 */
	private int knapsackRecur(int i, int w) {
		/* We use memoization in this recursive method. The key will consist of an ArrayList (that serves
		 * the same function as a tuple for this purpose) consisting of the current weight, w, and the current item index, i. */
		ArrayList<Integer> key = new ArrayList<>(Arrays.asList(new Integer[]{w, i}));

		/* Base case. See if the HashSet, K, contains the above key. If so, we don't need to re-compute this value again.
		 * So just return that value. This is what saves so much time over the naive brute force method.
		 *
		 * NOTE: Recall that the HashSet, K, is supposed to store the maximum value achievable using a knapsack of
		 * capacity w and items 1 thru i. */
		if (K.containsKey(key)) {
			if (debugOn) System.out.printf("Key found! %s\n", key);
			return K.get(key);
		}

		/* Another Base case. If index number is less than 1, there are no more items to consider, so the total value is 0
		 * regardless of the knapsack's capacity w. */
		if (i < 1) {
			K.put(key, 0);		//Save this value in the hashset.
			return K.get(key);
		}

		/* If we get this far, base cases are over and done with.
		 * If the weight of the current item is greater than the residual capacity the knapsack can handle,
		 * we "skip" this item. That is, The max. value achievable given capacity w and items 1 thru i is the same
		 * as the max. value achievable given capacity w and items 1 thru i-1. */
		if (itemArr[i].weight > w) {
			if (debugOn) System.out.println("item weight is greater than w.");
			K.put(key, knapsackRecur(i - 1, w));	//Save to HashSet
			return K.get(key);
		/* Else, we consider two cases: we either choose to include the item, or choose not to.
		 * Do recursive method call for each of the two cases, and return whichever results
		 * in the greatest total value.
		 * Note that if we do include the item, we decrement the knapsack's available weight
		 * accordingly, and we also add the value of the item. */
		} else {
			if (debugOn) System.out.println("item weight is less than or equal to w.");
			K.put(key, Math.max(knapsackRecur(i - 1, w - itemArr[i].weight) + itemArr[i].value,	//include the item. or...
					            knapsackRecur(i - 1, w))										//don't include the item.
				  );
			return K.get(key);
		}
		//end if/else
	}
	//end private int knapsackRecur(int i, int w)


	/**
	 * Method: getItemsInKnapsack
	 *         Assumes that you have already run the method knapsackRecur(). If not, this method won't work.
	 *         NOTE: it's also possible to make another version of this method corresponding to an iterative version
	 *         of knapsack. But we'll skip that for now.
	 *         
	 * @return all the items in the knapsack after running knapsackRecur().
	 */
	public ArrayList<Item> getItemsInKnapsack() {
		/* Remember what we did in knapsackRecur() method. We either included a given item into the knapsack or did not,
		 * maximizing the value every step of the way. Specifically:
		 *
		 * If we did not include an Item at some given index i, then we set the HashSet K(w, i) = K(w, i-1).
		 * If we did include the Item, then we set K(w, i) = K(w - item's weight, i-1) + item's value.
		 *
		 * So now, to figure out which items are in the knapsack, we will start from the value of K(W,N) --
		 * the return value of knapsackRecur() -- and make our way backwards all the way to the first Item.
		 * 
		 * In effect, we're backtracking to find out which items were included in the knapsack.
		 * */
		ArrayList<Integer> key = new ArrayList<>(Arrays.asList(new Integer[]{W, N}));
		ArrayList<Integer> possiblePrevKey;
		ArrayList<Item> retArr = new ArrayList<Item>();		//initialize the ArrayList to return

		/* We'll begin by looking at the last Item and making our way backwards all the way to the first Item.
		 * Remember that index 1, not 0, is the first Item. */
		for (int i = N; i >= 1; i--) {
			/* We must check if the current key, K(w, i), equals the previous key, K(w, i-1).
			 * So we'll initialize this variable as a possible previous key */
			possiblePrevKey = new ArrayList<>(Arrays.asList(new Integer[]{key.get(0), i - 1}));

			if (debugOn) System.out.println(K.get(key) + " " + K.get(possiblePrevKey));

			/* If K(w, i) equals K(w, i-1), then it means this item was not included in the knapsack.
			 * So let key = previous key and go on to next loop iteration. */
			if (K.get(key).equals(K.get(possiblePrevKey))) { //must use .equals() instead of ==, because the HashSet stores Integer object, not int.
				if (debugOn) System.out.println("Keys are equal. Skipping item...");
				key = new ArrayList<>(Arrays.asList(new Integer[]{key.get(0), i - 1}));
			}

			/* Else, if K(w, i) does not equal K(w, i-1), then it means this Item WAS included in the knapsack.
			 * So we add the item to the ArrayList to return, and also update K(w, i) to K(w - itemweight, i-1) */
			else {
				if (debugOn) System.out.println("Keys aren't equal! Adding item...");
				retArr.add(this.itemArr[i]);
				key = new ArrayList<>(Arrays.asList(new Integer[]{key.get(0) - itemArr[i].weight, i - 1}));
			}
		}
		//end for i

		//Prints out the contents of the knapsack to the console for testing, and confirms the total value and weight.
		int sumValue = 0, sumWeight = 0;
		for (Item i : retArr) {
			sumValue += i.value;
			sumWeight += i.weight;
		}
		System.out.printf("Knapsack's total capacity: %s. Maximized value of items in knapsack: %s. Total weight: %s\n",
				this.W, sumValue, sumWeight);

		//Finally return the AL
		return retArr;
	}
	//end public ArrayList<Item> getItemsInKnapsack()

	/**
	 * Method: knapsackRecurBruteForce
	 *         Commented out naive recursive method. NOT efficient. For educational / comparison purposes only.
	 *         For actual problems, use either knapsackRecur() or knapsack() method instead.
	 * @param i the index number of the current Item.
	 *          Remember that for convenience, the index begins from 1 all the way to N. (as opposed to from 0 to N-1)
	 * @param w the running weight capacity left in the knapsack.
	 * @return  the maximum value achievable using a knapsack of capacity w and items 1 thru i.
	 */
//	private int knapsackRecurBruteForce (int i, int w) {
//		if (i < 1) return 0;
//		if (itemArr[i].weight > w) return knapsackRecurBruteForce(i-1, w);
//		return Math.max(knapsackRecurBruteForce(i - 1, w - itemArr[i].weight) + itemArr[i].value,
//			 		    knapsackRecurBruteForce(i - 1, w));
//	}

	/**
	 * Method: knapsack. Dynamic programming iterative method.
	 *         UPDATE: commented out since it's suboptimal. See the other knapsack() method below.
	 *         Unoptimized dynamic programming / iteration. Compare with knapsackRecur() method.
	 *
	 * @return the maximum value achievable using a knapsack of capacity W and items 1 thru N.
	 *         Note that W and N are global variables indicating the total capacity of the knapsack and
	 *         the total number of items, respectively.
	 */
//	private long knapsack() {
//
//		/* Create a Value matrix. Items are in rows and weight are in columns. Add +1 on each side
//		 * since we're going to create a dummy row0 and col0 which will be filled with all zeroes.
//		 * So remember that in knapsackRecur() method, we saved each key in a HashSet as follows:
//		 * K(w, i) = the maximum value achievable using a knapsack of capacity w and items 1 thru i.
//		 *
//		 * Now, instead of using HashSet, we use a 2-D array, such that:
//		 * A[i][w] = the maximum value achievable using a knapsack of capacity w and items 1 thru i.
//		 *
//		 * Additionally, instead of recursing from the end to the beginning as we did in knapsackRecur(),
//	     * we will iterate from the beginning to the end, filling up the 2-D array as we go. */
//		int[][] A = new int[N + 1][W + 1];
//
//		/* Let's go thru the Value array row by row (that is, item by item) and populate it...
//		 * NOTE: we're going from item 1 TO AND INCLUDING N. So by default, the 0th index will be all set to zeroes.  */
//		for (int i = 1; i <= N; i++) {
//			for (int x = 0; x <= W; x++){
//				if (itemArr[i].weight <= x) {
//					A[i][x] = Math.max(A[i-1][x-itemArr[i].weight] + itemArr[i].value,
//							           A[i-1][x]);
//				} else {
//					//If the current item's weight is more than the running weight, just carry forward the value without the current item
//					//by assigning the previous row, same column's value to the current row, column.
//					A[i][x] = A[i-1][x];
//				}
//			}
//			//end for x
//		}
//		//end for i
//
//		return A[N][W];	//return the value on the last row and last column
//	}

	/**
	 * Method: knapsack
	 *         Better optimized dynamic programming / iteration. Compare with knapsackRecur() as well as the
	 *         now-defunct (commented out) knapsack() method above.
	 *
	 * @return the maximum value achievable using a knapsack of capacity W and items 1 thru N.
	 *         Note that W and N are global variables indicating the total capacity of the knapsack and
	 *         the total number of items, respectively.
	 */
	private int knapsack() {
		/* Compare with the now defunct (commented-out) knapsack() method, where we initialize A = new int[N + 1][W + 1].
		 * Here we just do [2][W + 1] instead. Why? We don't actually need N + 1 rows because during the iterative process,
		 * the algorithm only needs to look at the previous row. So we only need 2 rows.
		 * This saves space and prevents possible stack overflow error from having too big of an array size. */
		int[][] A = new int[2][W + 1];

		for (int i = 1; i <= N; i++) {
			for (int x = 0; x <= W; x++){
				if (itemArr[i].weight <= x) {
					/* Note that we now have A[1][x] and A[0][x] as opposed to A[i][x] and A[i-1][x], as we
					 * used to in the now-defunct knapsack() method. */
					A[1][x] = Math.max(A[0][x-itemArr[i].weight] + itemArr[i].value,
							           A[0][x]);
				} else {
					A[1][x] = A[0][x];
				}
			}
			//end for x

			/* Now we update the previous row with the current row's values before the next outer loop iteration. */
			for (int x = 0; x <= W; x++) {
				A[0][x] = A[1][x];
			}
		}
		//end for i

		return A[1][W];	//return the value on the last row and last column.
	}

	/**
	 * Method: knapsack_approx
	 * 
	 * @param accuracyPercentage the desired accuracy level of the output.
	 * @return an APPROXIMATE solution consisting of the
	 *         maximum total value that can be fit into the knapsack.
	 *         This method demonstrates the trade-off between efficiency and accuracy.
	 *         When you don't mind an approximate result that is "good enough", but want
	 *         more efficiency, consider using this method.
	 *         
	 *         NOTE:   This algorithm works even when the weight of each item is NOT an integer
	 *                 (this is the key advantage of this method over previous methods, which
	 *                 require integer weights), but does require that the value of each item either be an integer or
	 *                 (if not) be rounded down to a small integer.
	 */
	private int knapsack_approx(double accuracyPercentage) {
		double epsilon = (100 - accuracyPercentage) / 100.0;
		 /* m is the divider. Used to round down each item's value to the nearest multiple of m. */
		int m = (int)((epsilon * this.totalValue) / N);
		if (m == 0) {
			System.out.println("\tNOTE: With the given accuracyPercentage goal, the computed m = 0.\n\t"
					+ "That's not good as we can't divide by zero. We'll use m = 1 instead and stipulate that\n\t"
					+ "the accuracyPercentage goal MAY NOT BE MET.");
			m = 1;
		}

		int totalValueAfterDivisionByM = 0;
		for (int i = 1; i <= N; i++) {
			itemArr[i].value /= m;
			totalValueAfterDivisionByM += itemArr[i].value;
		}

		//base case.
		int[][] A = new int[N+1][totalValueAfterDivisionByM+1];
		for (int x = 0; x <= totalValueAfterDivisionByM; x++) {
			A[0][x] = (x == 0 ? 0 : Integer.MAX_VALUE);
//			System.out.printf("A[%s][%s] = %s\n", 0, x, A[0][x]);
		}

		//Dynamic programming
		for (int i = 1; i <= N; i++) {
			for (int x = 0; x <= totalValueAfterDivisionByM; x++) {
				int tempWeight = (x-itemArr[i].value < 0 ? 0 : A[i-1][x-itemArr[i].value]);
				if (tempWeight == Integer.MAX_VALUE) {
					A[i][x] = A[i-1][x];
				}
				else
					A[i][x] = (int)Math.min(A[i-1][x], itemArr[i].weight + tempWeight);
//				System.out.printf("A[%s][%s] (min. weight needed to achieve value >= %s while using at most the first %s items) = %s\n", i, x, x, i, A[i][x]);
			}
		}
		//end for i

		//Find the largest total value (x) such that their min. weight <= total capacity of knapsack.
		for (int x = totalValueAfterDivisionByM; x >= 0; x--) {
			if (A[N][x] <= this.W) return x * m;	//x * m restores the original total value.
		}
		return 0;
	}

	/**
	 * Class: Item. Inner class that represents item that may or may not be chosen for inclusion in knapsack.
	 */
	private class Item {
		int lbl, value, weight;

		/**
		 * 3-arg constructor.
		 * @param lbl The Item's label number
		 * @param value
		 * @param weight
		 */
		Item (int lbl, int value, int weight) {
			this.lbl = lbl;
			this.value = value;
			this.weight = weight;
		}

		/**
		 * Method: toString
		 * @return this Item's label no., value and weight.
		 */
		@Override
		public String toString() {
			return String.format("Item{#%s/$%s/%skg}", lbl, value, weight);
		}
	}

	/**
	 * Method: printResults. Prints out test data results to console. 
	 */
	public void printResults() {
		System.out.printf("================================================================\n"
				          + "Now running methods for filename %s...\n", this.filename);
		printResultsHelper(); //invoke helper method
	}
	
	/**
	 * Method: printResults. Overloaded method.
	 * @param expectedAnswer the expected answer
	 */
	public void printResults(int expectedAnswer) {
		System.out.printf("================================================================\n"
				          + "Expected answer for file %s: %s. Now running methods...\n", this.filename, expectedAnswer);
		printResultsHelper();
	}
	
	/**
	 * Method: printResultsHelper. Invoked by printResults() methods above
	 */
	private void printResultsHelper() {
		long startTime = System.currentTimeMillis();
		System.out.println("Actual answer after running recursive method: " + this.knapsackRecur(this.N, this.W));
		System.out.println("Actual answer after running iterative method: " + this.knapsack());
		System.out.println("Actual answer (within 90% accuracy) after running approximate algorithm: "
						   + this.knapsack_approx(90));
		System.out.printf("Time elapsed (in millisecs): %s\n", System.currentTimeMillis() - startTime);
		System.out.println("\nNow we'll print out the actual items in the knapsack...");
		System.out.println(this.getItemsInKnapsack());
	}

	/**
	 * Method: main
	 * @param args
	 */
	public static void main(String[] args) {
		Knapsacks k = new Knapsacks("knapsack_Q1.txt", false);
		k.printResults(2493893);

		k = new Knapsacks("knapsack_small01.txt", false);
		k.printResults(4);

		k = new Knapsacks("knapsack_Q2.txt", false);
		k.printResults(4243395);
	}
}
