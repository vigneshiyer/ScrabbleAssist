package dictionary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TrieNode {
	private char data;
	public char getData() {
		return data;
	}
	private Map<Character,TrieNode> map = new HashMap<Character,TrieNode>(26);
	private boolean wordEndsHere = false;

	public boolean isWordEndsHere() {
		return wordEndsHere;
	}
	public void setWordEndsHere(boolean wordEndsHere) {
		this.wordEndsHere = wordEndsHere;
	}
	public Map<Character, TrieNode> getMap() {
		return map;
	}
	public TrieNode(char data) {
		this.data = data;
	}
}

public class Dictionary {
	private TrieNode root;

	public Dictionary() {
		// root is null node
		root = new TrieNode('\0');
	}
	
	public Map<Integer,Set<String>> getPossibleDictionaryWords(String availableLetters) {
		return getPossibleDictionaryWords(availableLetters,"");
	}
	

	public Map<Integer,Set<String>> getPossibleDictionaryWords(String availableLetters, String constraint) {
		Map<Integer,Set<String>> result = new HashMap<Integer,Set<String>>();
		if (availableLetters == null || availableLetters.trim().length() == 0) {
			return result;
		}
		
		char[] input = availableLetters.toCharArray();		
		Set<Character> set = new HashSet<Character>();		
		int size = input.length;
		
		Map<Integer,Character> fixedLetters = new HashMap<Integer,Character>();
		char[] fstrarr = constraint.toCharArray();
		
		for (int i = 0; i < fstrarr.length; i++) {
			if (fstrarr[i] != '.') {
				fixedLetters.put(i, fstrarr[i]);
			}
		}

		for (int i = 0; i < size; i++) {
			char ch = input[i];
			if (!set.contains(ch)) {
				set.add(ch);
				StringBuilder s = new StringBuilder();
				Set<Integer> setLetters = new HashSet<Integer>();
				findWordsRecursive(s,i,setLetters,input,fixedLetters,result,constraint.length());
			}
		}
		return result;
	}

	private Map<Integer,Set<String>> findWordsRecursive (StringBuilder s, int startCharIndex, Set<Integer> letters, 
			char[] input, Map<Integer,Character> fixedLetters, Map<Integer,Set<String>> result, int requiredLength) {
		if (s == null) {
			return result;
		}
		
		if (s.length() == 0) {
			if (fixedLetters.containsKey(0)) {
				s.append(fixedLetters.get(0));
			}
			s.append(input[startCharIndex]);
			letters.add(startCharIndex);
		}

		if (s.length() > 0 && !startsWith(s.toString())) {
			return result;
		}
		
		// append the fixed letters in appropriate positions
		int stringSize = s.length();
		
		while (fixedLetters.containsKey(stringSize)) {
			s.append(fixedLetters.get(stringSize));
			stringSize = s.length();
		}

		if (s.length() > 0 && !startsWith(s.toString())) {
			return result;
		}

		if (search(s.toString()) && (fixedLetters.size() == 0 || s.length() == requiredLength)){
			int len = s.length();
			Set<String> set;
			if (result.containsKey(len)) {
				set = result.get(len);
			}
			else {
				set = new HashSet<String>();
			}
			set.add(s.toString());
			result.put(len, set);
		}

		int size = input.length;

		for (int i = 0; i < size; i++) {
			char ch = input[i];
			if (!letters.contains(i)){
				Set<Integer> set = new HashSet<Integer>(letters);
				set.add(i);
				StringBuilder st = new StringBuilder(s);
				st.append(ch);
				findWordsRecursive(st, startCharIndex, set, input, fixedLetters, result,requiredLength);
			}
		}

		return result;

	}

	public void insert(String word) {
		if (word == null || word.trim().length() == 0) {
			return;
		}

		char[] arr = word.toCharArray();
		TrieNode itr = root;
		Map<Character,TrieNode> map = itr.getMap();

		for (int i = 0; i < arr.length - 1; i++) {
			if (!map.containsKey(arr[i])) {
				map.put(arr[i], new TrieNode(arr[i]));
			}
			itr = map.get(arr[i]);
			map = itr.getMap();			
		}

		if (map.containsKey(arr[arr.length-1])) {
			map.get(arr[arr.length-1]).setWordEndsHere(true);
		}
		else {
			TrieNode node = new TrieNode(arr[arr.length-1]);
			node.setWordEndsHere(true);
			map.put(arr[arr.length-1], node);
		}
	}

	public boolean startsWith(String prefix) {
		if (prefix == null || prefix.trim().length() == 0) {
			return false;
		}
		TrieNode itr = root;
		Map<Character,TrieNode> map = itr.getMap();
		char[] arr = prefix.toCharArray();

		for (int i = 0; i < arr.length - 1; i++) {
			if (map.containsKey(arr[i])) {
				itr = map.get(arr[i]);
			}
			else {
				return false;
			}
			map = itr.getMap();
		}

		char last = arr[arr.length-1];
		if (map.containsKey(last)) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean search(String word) {
		if (word == null || word.trim().length() == 0) {
			return false;
		}
		TrieNode itr = root;
		Map<Character,TrieNode> map = itr.getMap();
		char[] arr = word.toCharArray();

		for (int i = 0; i < arr.length - 1; i++) {
			if (map.containsKey(arr[i])) {
				itr = map.get(arr[i]);
			}
			else {
				return false;
			}
			map = itr.getMap();
		}

		char last = arr[arr.length-1];
		if (map.containsKey(last)) {
			TrieNode node = map.get(last);
			return node.isWordEndsHere();
		}
		else {
			return false;
		}
	}
}
