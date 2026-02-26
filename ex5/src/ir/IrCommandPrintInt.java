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

public class IrCommandPrintInt extends IrCommand
{
	public Temp t;

	public IrCommandPrintInt(Temp t)
	{
		this.t = t;
	}


	@Override
	public List<Temp> getUse() {
		List<Temp> uses = new ArrayList<>();
		if (t != null) uses.add(t);
		return uses;
	}

	@Override
	public void mipsMe() {
		mips.MipsGenerator.getInstance().printInt(t);
	}
}
