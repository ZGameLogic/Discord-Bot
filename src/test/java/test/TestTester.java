package test;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public class TestTester {
	
	
	public static void main(String[] args) {
		LinkedList<Thing> things = new LinkedList<>();
		things.add(new Thing(1,5,"one"));
		things.add(new Thing(4, 2, "two"));
		
		Collections.sort(things, Comparator.comparing(Thing::getTwo));
		System.out.println(things);
	}
	
	@Getter
	@AllArgsConstructor
	@ToString
	static class Thing {
		int one;
		int two;
		String name;
	}
}
