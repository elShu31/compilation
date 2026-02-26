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
	/* The scope offset captured during semantic */
	/* analysis for use in IR generation */
	/*************************************************/
	private int scopeOffset = -1;

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

	/*************************************************/
	/* Additional state for implicit fields (this.*) */
	/*************************************************/
	public boolean isImplicitField = false;
	public TypeClass enclosingClass = null;

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
		/* Capture the scope offset */
		/*************************************************/
		this.scopeOffset = SymbolTable.getInstance().getScopeOffset(name);

		// If it's a field, it must be an implicit access to 'this'
		if (t instanceof TypeField) {
			this.isImplicitField = true;
			Type thisType = SymbolTable.getInstance().find("this");
			if (thisType instanceof TypeClass) {
				this.enclosingClass = (TypeClass) thisType;
			}
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

		if (isImplicitField) {
			// 1. Load implicit "this" pointer
			Temp thisTemp = TempFactory.getInstance().getFreshTemp();
			VarId.Kind kind = FunctionContext.getCurrent().getKind("this");
			int fpOffset = FunctionContext.getCurrent().getFpOffset("this");
			// The scope offset for "this" might not be needed for local stack access, but
			// we pass -1 or 1 depending.
			Ir.getInstance().AddIrCommand(new IrCommandLoad(thisTemp, "this", -1, kind, fpOffset));

			// 2. Load field from "this"
			int fieldOffset = ClassLayout.getFieldOffset(enclosingClass, name);
			Ir.getInstance().AddIrCommand(new IrCommandFieldGet(dst, thisTemp, fieldOffset));
		} else if (FunctionContext.isInFunction()) {
			VarId.Kind kind = FunctionContext.getCurrent().getKind(name);
			int fpOffset = FunctionContext.getCurrent().getFpOffset(name);
			Ir.getInstance().AddIrCommand(new IrCommandLoad(dst, name, scopeOffset, kind, fpOffset));
		} else {
			Ir.getInstance().AddIrCommand(new IrCommandLoad(dst, name, scopeOffset));
		}
		return dst;
	}

	public int getScopeOffset() {
		return scopeOffset;
	}
}
