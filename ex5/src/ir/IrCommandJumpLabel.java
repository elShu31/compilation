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

public class IrCommandJumpLabel extends IrCommand
{
	public String labelName;

	public IrCommandJumpLabel(String labelName)
	{
		this.labelName = labelName;
	}

	@Override
	public void mipsMe() {
		mips.MipsGenerator.getInstance().jump(labelName);
	}
}
