package ast;

import ir.*;
import temp.*;
import types.*;

public class AstStmtAssign extends AstStmt {
	/***************/
	/* var := exp */
	/***************/
	public AstVar var;
	public AstExp exp;

	/*******************/
	/* CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstVar var, AstExp exp, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		// System.out.print("====================== stmt -> var ASSIGN exp
		// SEMICOLON\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.lineNumber = lineNumber;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe() {
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN STMT\n");

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null)
			var.printMe();
		if (exp != null)
			exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"ASSIGN\nleft := right\n");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
	}

	public Type semantMe() throws SemanticException {
		Type t1 = null;
		Type t2 = null;

		/****************************/
		/* [1] Semant var and exp */
		/****************************/
		if (var != null)
			t1 = var.semantMe();
		if (exp != null)
			t2 = exp.semantMe();

		/****************************/
		/* [2] Check for null types */
		/****************************/
		if (t1 == null) {
			throw new SemanticException("variable has no type", lineNumber);
		}
		if (t2 == null) {
			throw new SemanticException("expression has no type", lineNumber);
		}

		/************************************************/
		/* [3] Check type compatibility for assignment */
		/************************************************/
		if (!TypeUtils.canAssignType(t1, t2)) {
			throw new SemanticException("type mismatch in assignment: cannot assign " + t2.name + " to " + t1.name,
					lineNumber);
		}

		/********************************************************/
		/* [4] Return value is irrelevant for assign statement */
		/********************************************************/
		return null;
	}

	public Temp irMe() {
		// Evaluate left-hand side components before evaluating the right-hand side
		// This strictly enforces left-to-right evaluation order and prevents
		// RHS side-effects from modifying LHS variable resolutions incorrectly.
		
		if (var instanceof AstVarSimple) {
			AstVarSimple simpleVar = (AstVarSimple) var;
			if (simpleVar.isField) {
				// Implicit field assignment - store to 'this'
				Temp thisPtr = TempFactory.getInstance().getFreshTemp();
				Ir.getInstance().AddIrCommand(new IrCommandLoad(thisPtr, "this", -1, false, 8));
				
				Temp src = exp.irMe(); // Evaluate RHS
				
				Ir.getInstance().AddIrCommand(new IrCommandStoreField(thisPtr, simpleVar.fieldByteOffset, src));
			} else {
				/****************************************/
				/* Get the scope offset for this var    */
				/* from the symbol table                */
				/****************************************/
				String varName = simpleVar.name;
				int scopeOffset = simpleVar.getScopeOffset();
				boolean isGlobal = simpleVar.isGlobal;
				int fpOffset = simpleVar.fpOffset;
				
				Temp src = exp.irMe(); // Evaluate RHS
				
				Ir.getInstance().AddIrCommand(new IrCommandStore(varName, scopeOffset, isGlobal, fpOffset, src));
			}
		} else if (var instanceof AstVarSubscript) {
			AstVarSubscript subVar = (AstVarSubscript) var;
			Temp arrayBase = subVar.var.irMe();
			Temp index = subVar.subscript.irMe();

			Temp src = exp.irMe(); // Evaluate RHS AFTER indexing array

			Ir.getInstance().AddIrCommand(new IrCommandStoreArray(arrayBase, index, src));
		} else if (var instanceof AstVarField) {
			AstVarField fieldVar = (AstVarField) var;
			Temp objectBase = fieldVar.var.irMe();
			
			Temp src = exp.irMe(); // Evaluate RHS AFTER loading object base pointer

			Ir.getInstance().AddIrCommand(new IrCommandStoreField(objectBase, fieldVar.fieldByteOffset, src));
		}

		return null;
	}
}
