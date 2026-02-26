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

public class IrCommandBinopSubIntegers extends IrCommand
{
	public Temp t1;
	public Temp t2;
	public Temp dst;
	
	public IrCommandBinopSubIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public List<Temp> getDef() {
		List<Temp> defs = new ArrayList<>();
		if (dst != null) defs.add(dst);
		return defs;
	}

	@Override
	public List<Temp> getUse() {
		List<Temp> uses = new ArrayList<>();
		if (t1 != null) uses.add(t1);
		if (t2 != null) uses.add(t2);
		return uses;
	}

	@Override
	public void mipsMe() {
		mips.MipsGenerator.getInstance().sub(dst, t1, t2);
	}
}
