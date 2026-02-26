/***********/
/* PACKAGE */
/***********/
package ir;

import mips.MipsGenerator;

import java.util.ArrayList;
import java.util.List;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;

public class IRcommandConstInt extends IrCommand {
	public Temp t;
	public int value;

	public IRcommandConstInt(Temp t, int value) {
		this.t = t;
		this.value = value;
	}

	@Override
	public List<Temp> getDef() {
		List<Temp> defs = new ArrayList<>();
		if (t != null) defs.add(t);
		return defs;
	}

	@Override
	public void mipsMe() {
		mips.MipsGenerator.getInstance().li(t, value);
	}
}
