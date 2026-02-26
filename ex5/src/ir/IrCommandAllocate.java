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

public class IrCommandAllocate extends IrCommand {
	public VarId varId;

	public IrCommandAllocate(VarId varId) {
		this.varId = varId;
	}

	/****************************************/
	/* Convenience constructor for backward */
	/* compatibility during transition */
	/****************************************/
	public IrCommandAllocate(String varName, int scopeOffset) {
		this.varId = new VarId(varName, scopeOffset);
	}

	public IrCommandAllocate(String varName, int scopeOffset, VarId.Kind kind, int fpOffset) {
		this.varId = new VarId(varName, scopeOffset, kind, fpOffset);
	}

	@Override
	public void mipsMe() {
		// Only global variables need .data allocation
		if (varId.kind == VarId.Kind.GLOBAL) {
			MipsGenerator.getInstance().allocateGlobal(varId.name);
		}
		// Local and param variables live on the stack — no .data entry needed
	}
}
