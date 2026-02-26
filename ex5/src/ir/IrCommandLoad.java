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

public class IrCommandLoad extends IrCommand {
	public Temp dst;
	public VarId varId;

	public IrCommandLoad(Temp dst, VarId varId) {
		this.dst = dst;
		this.varId = varId;
	}

	/****************************************/
	/* Convenience constructor for backward */
	/* compatibility during transition */
	/****************************************/
	public IrCommandLoad(Temp dst, String varName, int scopeOffset) {
		this.dst = dst;
		this.varId = new VarId(varName, scopeOffset);
	}

	public IrCommandLoad(Temp dst, String varName, int scopeOffset, VarId.Kind kind, int fpOffset) {
		this.dst = dst;
		this.varId = new VarId(varName, scopeOffset, kind, fpOffset);
	}

	@Override
	public List<Temp> getDef() {
		List<Temp> defs = new ArrayList<>();
		if (dst != null)
			defs.add(dst);
		return defs;
	}

	@Override
	public void mipsMe() {
		if (varId.kind == VarId.Kind.GLOBAL) {
			MipsGenerator.getInstance().loadGlobal(dst, varId.name);
		} else {
			// PARAM or LOCAL — use fp-relative offset
			MipsGenerator.getInstance().loadLocal(dst, varId.fpOffset);
		}
	}
}
