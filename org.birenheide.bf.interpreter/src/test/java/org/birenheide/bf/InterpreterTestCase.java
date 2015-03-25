package org.birenheide.bf;

import java.io.InputStream;
import java.util.Scanner;


public class InterpreterTestCase {

	protected String readResource(String name) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
		Scanner sc = new Scanner(is, "UTF-8");
		sc.useDelimiter("\\A");
		try {
			if (sc.hasNext()) {
				return sc.next();
			}
			else {
				return null;
			}
		}
		finally {
			sc.close();
		}
		
		
	}
}
