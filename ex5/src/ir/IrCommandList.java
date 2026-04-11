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

public class IrCommandList {
	public IrCommand head;
	public IrCommandList tail;

	IrCommandList(IrCommand head, IrCommandList tail) {
		this.head = head;
		this.tail = tail;
	}
}
