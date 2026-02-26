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
	public IrCommandStore(String varName, int scopeOffset, Temp src) {
		this.src = src;
		this.varId = new VarId(varName, scopeOffset);
	}

	public IrCommandStore(String varName, int scopeOffset, VarId.Kind kind, int fpOffset, Temp src) {
		this.src = src;
		this.varId = new VarId(varName, scopeOffset, kind, fpOffset);
	}

	@Override
	public List<Temp> getUse() {
		List<Temp> uses = new ArrayList<>();
		if (src != null)
			uses.add(src);
		return uses;
	}

	@Override
	public void mipsMe() {
		if (varId.kind == VarId.Kind.GLOBAL) {
			MipsGenerator.getInstance().storeGlobal(varId.name, src);
		} else {
			// PARAM or LOCAL — use fp-relative offset
			MipsGenerator.getInstance().storeLocal(varId.fpOffset, src);
		}
	}
}
