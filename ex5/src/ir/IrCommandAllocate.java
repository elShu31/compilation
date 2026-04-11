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

public class IrCommandAllocate extends IrCommand {
	public VarId varId;

	public IrCommandAllocate(VarId varId) {
		this.varId = varId;
	}

	/****************************************/
	/* Convenience constructor for backward */
	/* compatibility during transition */
	/****************************************/
	public IrCommandAllocate(String varName, int scopeOffset, boolean isGlobal, int fpOffset) {
		this.varId = new VarId(varName, scopeOffset, isGlobal, fpOffset);
	}

	public IrCommandAllocate(String varName, int scopeOffset) {
		this.varId = new VarId(varName, scopeOffset);
	}

	@Override
	public void mipsMe() {
		MipsGenerator.getInstance().allocate(varId);
	}
}
