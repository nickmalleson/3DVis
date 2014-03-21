/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spacetime;

import java.util.Scanner;

public class DataFactory {

	private DataFactory() {}
	
	public static Scanner open (String dataSource, boolean stream) {
	
		switch (dataSource.substring(dataSource.indexOf(":"))) {
			case ("http") :
			case ("file") :
				String suffix = dataSource.substring(dataSource.lastIndexOf("."));
				if (suffix.equals("csv")) {
                                    return new Scanner(dataSource);
				} 
				if (suffix.equals("text")) {
				} 
			default :
				return new Scanner(System.in);
			}
	}

}