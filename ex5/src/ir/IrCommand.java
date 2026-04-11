/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;
import java.util.Set;
import java.util.HashSet;
import temp.Temp;

public abstract class IrCommand {
	/*****************/
	/* Label Factory */
	/*****************/
	protected static int labelCounter = 0;

	public static String getFreshLabel(String msg) {
		return String.format("Label_%d_%s", labelCounter++, msg);
	}

	public abstract void mipsMe();

	public Set<Temp> getUsedTemps() {
		return new HashSet<>();
	}

	public Set<Temp> getDefinedTemps() {
		return new HashSet<>();
	}
}
