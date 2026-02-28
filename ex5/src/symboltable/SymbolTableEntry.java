/***********/
/* PACKAGE */
/***********/
package symboltable;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;

/**********************/
/* SYMBOL TABLE ENTRY */
/**********************/
public class SymbolTableEntry {
	/*********/
	/* index */
	/*********/
	int index;

	/********/
	/* name */
	/********/
	public String name;

	/******************/
	/* TYPE value ... */
	/******************/
	public Type type;

	/*********************************************/
	/* prevtop and next symbol table entries ... */
	/*********************************************/
	public SymbolTableEntry prevtop;
	public SymbolTableEntry next;

	/*****************/
	/* fpOffset ... */
	/*****************/
	public int fpOffset;

	/****************************************************/
	/* The prevtopIndex is just for debug purposes ... */
	/****************************************************/
	public int prevtopIndex;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public SymbolTableEntry(
			String name,
			Type type,
			int index,
			SymbolTableEntry next,
			SymbolTableEntry prevtop,
			int prevtopIndex,
			int fpOffset) {
		this.index = index;
		this.name = name;
		this.type = type;
		this.next = next;
		this.prevtop = prevtop;
		this.prevtopIndex = prevtopIndex;
		this.fpOffset = fpOffset;
	}

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public SymbolTableEntry(
			String name,
			Type type,
			int index,
			SymbolTableEntry next,
			SymbolTableEntry prevtop,
			int prevtopIndex) {
		this(name, type, index, next, prevtop, prevtopIndex, 0);
	}
}
