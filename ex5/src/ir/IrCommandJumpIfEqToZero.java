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
import temp.*;
import mips.*;
import java.util.Set;
import java.util.HashSet;

public class IrCommandJumpIfEqToZero extends IrCommand {
	public Temp t;
	public String labelName;

	public IrCommandJumpIfEqToZero(Temp t, String labelName) {
		this.t = t;
		this.labelName = labelName;
	}

	@Override
	public void mipsMe() {
		MipsGenerator.getInstance().beqz(t, labelName);
	}

	@Override
	public Set<Temp> getUsedTemps() {
		Set<Temp> s = new HashSet<>();
		if (t != null)
			s.add(t);
		return s;
	}
}
