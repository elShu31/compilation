package ast;

import ir.*;
import temp.*;
import types.*;
import symboltable.*;

public class AstVarSimple extends AstVar {
	/************************/
	/* simple variable name */
	/************************/
	public String name;

	/*************************************************/
	/* Is this variable global or local */
	/*************************************************/
	public boolean isGlobal;
	public int fpOffset = 0;

	/*************************************************/
	/* The scope offset captured during semantic */
	/* analysis for use in IR generation */
	/*************************************************/
	private int scopeOffset = -1;

	/*************************************************/
	/* Properties for handling implicit class fields */
	/*************************************************/
	public boolean isField = false;
	public int fieldByteOffset = -1;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSimple(String name, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		// System.out.format("====================== var -> ID( %s )\n",name);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.name = name;
		this.lineNumber = lineNumber;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe() {
		/**********************************/
		/* AST NODE TYPE = AST SIMPLE VAR */
		/**********************************/
		System.out.format("AST NODE SIMPLE VAR( %s )\n", name);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("SIMPLE\nVAR\n(%s)", name));
	}

	/********************************************************/
	/* Semantic analysis for simple variable */
	/* Looks up the variable name in the symbol table */
	/********************************************************/
	public Type semantMe() throws SemanticException {
		Type t = SymbolTable.getInstance().find(name);

		if (t == null) {
			throw new SemanticException("undefined variable " + name, lineNumber);
		}

		/*************************************************/
		/* Capture the scope offset while scope is active */
		/*************************************************/
		this.scopeOffset = SymbolTable.getInstance().getScopeOffset(name);

		/*************************************************/
		/* Capture if the variable is global */
		/*************************************************/
		this.isGlobal = SymbolTable.getInstance().isGlobalVariable(name);

		/*************************************************/
		/* Grab local FP offset if not global */
		/*************************************************/
		if (!this.isGlobal) {
			this.fpOffset = SymbolTable.getInstance().getFpOffset(name);
		}

		// If it's a field, return the field's type, not the TypeField wrapper
		if (t instanceof TypeField) {
			this.isField = true;
			this.fieldByteOffset = ((TypeField) t).offset;
			return ((TypeField) t).fieldType;
		}

		return t;
	}

	/********************************************************/
	/* IR generation for simple variable */
	/* Loads the variable value into a fresh temp */
	/********************************************************/
	public Temp irMe() {
		Temp dst = TempFactory.getInstance().getFreshTemp();

		if (isField) {
			// It's an implicit class field! Load from 'this' pointer -> always at 8($fp) in methods
			Temp thisPtr = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(new IrCommandLoad(thisPtr, "this", -1, false, 8));
			Ir.getInstance().AddIrCommand(new IrCommandLoadField(dst, thisPtr, fieldByteOffset));
			return dst;
		}

		/****************************************/
		/* Use the captured scope offset */
		/****************************************/
		if (scopeOffset == -1) {
			// Fallback if semantMe wasn't called or failed (shouldn't happen in valid flow)
			scopeOffset = SymbolTable.getInstance().getScopeOffset(name);
		}

		Ir.getInstance().AddIrCommand(new IrCommandLoad(dst, name, scopeOffset, isGlobal, fpOffset));
		return dst;
	}

	public int getScopeOffset() {
		return scopeOffset;
	}
}
