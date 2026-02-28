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

public class IRcommandConstInt extends IrCommand {
	public Temp t;
	public int value;

	public IRcommandConstInt(Temp t, int value) {
		this.t = t;
		this.value = value;
	}

	@Override
	public void mipsMe() {
		MipsGenerator.getInstance().li(t, value);
	}

	@Override
	public Set<Temp> getDefinedTemps() {
		Set<Temp> s = new HashSet<>();
		if (t != null)
			s.add(t);
		return s;
	}
}
