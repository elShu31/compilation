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

public class IrCommandLabel extends IrCommand
{
	public String labelName;

	public IrCommandLabel(String labelName)
	{
		this.labelName = labelName;
	}

	@Override
	public void mipsMe() {
		mips.MipsGenerator.getInstance().label(labelName);
	}
}
