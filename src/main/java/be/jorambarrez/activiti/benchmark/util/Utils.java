package be.jorambarrez.activiti.benchmark.util;

import java.util.Random;

public class Utils {

	/**
	 * Returns an array with random elements, taken from a given array.
	 * 
	 * @param possibleElements The elements to choose from
	 * @param resultSize The size of the resulting array.
	 */
	public static String[] randomArray(String[] possibleElements, int resultSize) {
		Random random = new Random();
		String[] randomResult = new String[resultSize];
		for (int i = 0; i < resultSize; i++) {
			randomResult[i] = possibleElements[random.nextInt(possibleElements.length)];
		}
		return randomResult;
	}
	
	public static String toString(String[] strings) {
		StringBuilder strb = new StringBuilder();
		for (String s : strings) {
			strb.append(s + "|");
		}
		strb.deleteCharAt(strb.length() - 1);
		return strb.toString();
	}
	
}
