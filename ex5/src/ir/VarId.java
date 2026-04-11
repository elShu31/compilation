/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.Objects;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;

/**
 * VarId uniquely identifies a variable by its name and scope offset.
 * This is necessary to distinguish between shadowed variables with the same
 * name
 * in different scopes (e.g., global x vs local x in main).
 * 
 * The offset is the index in the symbol table when the variable was entered,
 * which uniquely identifies the variable declaration.
 */
public class VarId {
	/****************/
	/* DATA MEMBERS */
	/****************/
	public final String name;
	public final int scopeOffset;
	public final boolean isGlobal;
	public final int fpOffset;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public VarId(String name, int scopeOffset, boolean isGlobal, int fpOffset) {
		this.name = name;
		this.scopeOffset = scopeOffset;
		this.isGlobal = isGlobal;
		this.fpOffset = fpOffset;
	}

	/****************************************/
	/* Backward compatible constructor */
	/* for global variables */
	/****************************************/
	public VarId(String name, int scopeOffset) {
		this(name, scopeOffset, true, 0);
	}

	/****************************************/
	/* Get the display name for output */
	/* (just the variable name, not offset) */
	/****************************************/
	public String getDisplayName() {
		return name;
	}

	/****************************************/
	/* equals and hashCode for use in Sets */
	/* and Maps during dataflow analysis */
	/****************************************/
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		VarId varId = (VarId) obj;
		return scopeOffset == varId.scopeOffset && isGlobal == varId.isGlobal && fpOffset == varId.fpOffset
				&& Objects.equals(name, varId.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, scopeOffset, isGlobal, fpOffset);
	}

	@Override
	public String toString() {
		return name + "@" + scopeOffset;
	}
}
