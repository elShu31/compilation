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

public class IrCommandJumpIfEqToZero extends IrCommand
{
	public Temp t;
	public String labelName;

	public IrCommandJumpIfEqToZero(Temp t, String labelName)
	{
		this.t          = t;
		this.labelName = labelName;
	}


	@Override
	public List<Temp> getUse() {
		List<Temp> uses = new ArrayList<>();
		if (t != null) uses.add(t);
		return uses;
	}

	@Override
	public void mipsMe() {
		mips.MipsGenerator.getInstance().beqz(t, labelName);
	}
}
