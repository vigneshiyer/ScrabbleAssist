package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import dictionary.Dictionary;

public class ScrabbleConsole {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("resources/words.txt"));
		Dictionary dict = new Dictionary();
		String str;
		while ((str = br.readLine()) != null) {
			dict.insert(str);
		}
		br.close();
		//System.out.println("Done");
		br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Enter available characters as a string: ");
			str = br.readLine();
			if (str.equals("")) {
				break;
			}			
			char[] arr = str.toCharArray();
			System.out.println("Available letters: ");
			for (int i = 0; i < arr.length; i++) {
				System.out.print(arr[i]+",");
			}
			System.out.println("\n");			
			System.out.println("Enter the fixed letters as a string. Use a '.' for any character");
			String constraint = br.readLine();
			
			Set<String> words = dict.getPossibleDictionaryWords(str,constraint);
			
			if (words != null) {
				int count = 0;
				List<String> list = new ArrayList<String>();
					list.addAll(words);
					Collections.sort(list);
					int i = 1;
					for (String s : list) {
						System.out.println(i+". "+s);
						i++;
						count++;
					}
					System.out.println("\nTotal words = "+count);
			}
				
			
		}
		
	}
}
