package ast;

import types.*;
import ir.*;
import temp.*;

public class AstExpNil extends AstExp {
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpNil(int lineNumber) {
		serialNumber = AstNodeSerialNumber.getFresh();
		// System.out.print("====================== exp -> NIL\n");
		this.lineNumber = lineNumber;
	}

	/************************************************/
	/* The printing message for a nil exp AST node */
	/************************************************/
	public void printMe() {
		System.out.print("AST NODE NIL\n");
		AstGraphviz.getInstance().logNode(serialNumber, "NIL");
	}

	/************************************************/
	/* Semantic analysis for nil expression */
	/************************************************/
	public Type semantMe() {
		return TypeNil.getInstance();
	}

	/************************************************/
	/* IR: nil = 0 (null pointer) */
	/************************************************/
	public Temp irMe() {
		Temp t = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(t, 0));
		return t;
	}
}
