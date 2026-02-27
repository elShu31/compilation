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

public class IrCommandLabel extends IrCommand {
	public String labelName;

	public IrCommandLabel(String labelName) {
		this.labelName = labelName;
	}
}
