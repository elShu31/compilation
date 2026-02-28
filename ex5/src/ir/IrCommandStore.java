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

public class IrCommandStore extends IrCommand {
	public VarId varId;
	public Temp src;

	public IrCommandStore(VarId varId, Temp src) {
		this.src = src;
		this.varId = varId;
	}

	/****************************************/
	/* Convenience constructor for backward */
	/* compatibility during transition */
	/****************************************/
	public IrCommandStore(String varName, int scopeOffset, boolean isGlobal, int fpOffset, Temp src) {
		this.src = src;
		this.varId = new VarId(varName, scopeOffset, isGlobal, fpOffset);
	}

	public IrCommandStore(String varName, int scopeOffset, Temp src) {
		this.src = src;
		this.varId = new VarId(varName, scopeOffset);
	}

	@Override
	public void mipsMe() {
		MipsGenerator.getInstance().store(varId, src);
	}
}
