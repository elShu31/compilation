/***********/
/* PACKAGE */
/***********/
package ir;

import java.util.ArrayList;
import java.util.List;
import temp.Temp;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public abstract class IrCommand {
	/*****************/
	/* Label Factory */
	/*****************/
	protected static int labelCounter = 0;

	public static String getFreshLabel(String msg) {
		return String.format("Label_%d_%s", labelCounter++, msg);
	}

	public List<Temp> getDef() {
		return new ArrayList<>();
	}

	public List<Temp> getUse() {
		return new ArrayList<>();
	}

	public void mipsMe() {
		// Default implementation, overridden by subclasses
	}
}
