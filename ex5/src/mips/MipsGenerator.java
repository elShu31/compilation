/***********/
/* PACKAGE */
/***********/
package mips;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;
import ir.*;

public class MipsGenerator {
	private static final int WORD_SIZE = 4;
	private PrintWriter fileWriter;

	public void finalizeFile() {
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.close();
	}

	/**************************/
	/* Library functions */
	/**************************/
	public void printInt(Temp t) {
		int idx = t.getSerialNumber();
		fileWriter.format("\t# Print integer\n");
		fileWriter.format("\tmove $a0,Temp_%d\n", idx);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
	}

	public void printString(Temp t) {
		int idx = t.getSerialNumber();
		fileWriter.format("\t# Print string\n");
		fileWriter.format("\tmove $a0,Temp_%d\n", idx);
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	/**************************/
	/* Memory allocation */
	/**************************/
	public void malloc(Temp dst, int sizeBytes) {
		int dstIdx = dst.getSerialNumber();
		fileWriter.format("\t# Memory allocation\n");
		fileWriter.format("\tli $a0,%d\n", sizeBytes);
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove Temp_%d,$v0\n", dstIdx);
	}

	/**************************/
	/* Global variables */
	/**************************/
	public void allocate(String varName) {
		fileWriter.format(".data\n");
		fileWriter.format("\tglobal_%s: .word 721\n", varName);
	}

	public void load(Temp dst, String varName) {
		int idxdst = dst.getSerialNumber();
		fileWriter.format("\tlw Temp_%d,global_%s\n", idxdst, varName);
	}

	public void store(String varName, Temp src) {
		int idxsrc = src.getSerialNumber();
		fileWriter.format("\tsw Temp_%d,global_%s\n", idxsrc, varName);
	}

	/**************************/
	/* MIPS instructions */
	/**************************/
	public void li(Temp t, int value) {
		int idx = t.getSerialNumber();
		fileWriter.format("\tli Temp_%d,%d\n", idx, value);
	}

	public void add(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		fileWriter.format("\tadd Temp_%d,Temp_%d,Temp_%d\n", dstidx, i1, i2);
	}

	public void mul(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		fileWriter.format("\tmul Temp_%d,Temp_%d,Temp_%d\n", dstidx, i1, i2);
	}

	public void sub(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		fileWriter.format("\tsub Temp_%d,Temp_%d,Temp_%d\n", dstidx, i1, i2);
	}

	public void div(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		String labelValidDiv = IrCommand.getFreshLabel("ValidDiv");

		fileWriter.format("\tbne Temp_%d,$zero,%s\n", i2, labelValidDiv);
		// Branch not taken - there is a zero division error, jump to error handler
		jump("error_illegal_div_by_0");

		// Branch was taken - there is no zero division error
		label(labelValidDiv);
		fileWriter.format("\tdiv Temp_%d,Temp_%d,Temp_%d\n", dstidx, i1, i2);
	}

	public void label(String label) {
		if (label.equals("main")) {
			fileWriter.format(".text\n");
			fileWriter.format("%s:\n", label);
		} else {
			fileWriter.format("%s:\n", label);
		}
	}

	/**************************/
	/* Jumps */
	/**************************/
	public void jump(String label) {
		fileWriter.format("\tj %s\n", label);
	}

	/**************************/
	/* Branches */
	/**************************/
	public void blt(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tblt Temp_%d,Temp_%d,%s\n", i1, i2, label);
	}

	public void bge(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbge Temp_%d,Temp_%d,%s\n", i1, i2, label);
	}

	public void bgt(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbgt Temp_%d,Temp_%d,%s\n", i1, i2, label);
	}

	public void ble(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tble Temp_%d,Temp_%d,%s\n", i1, i2, label);
	}

	public void bne(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbne Temp_%d,Temp_%d,%s\n", i1, i2, label);
	}

	public void beq(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbeq Temp_%d,Temp_%d,%s\n", i1, i2, label);
	}

	public void beqz(Temp oprnd1, String label) {
		int i1 = oprnd1.getSerialNumber();

		fileWriter.format("\tbeq Temp_%d,$zero,%s\n", i1, label);
	}

	/**************************/
	/* Functions */
	/**************************/
	public void prologue() {
		// Store $ra into the stack
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $ra,0($sp)\n");

		// Store $fp into the stack
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $fp,0($sp)\n");

		// move $sp to $fp
		fileWriter.format("\tmove $fp,$sp\n");
	}

	public void epilogue() {
		// mov $fp to $sp
		fileWriter.format("\tmove $sp,$fp\n");

		// load $fp from stack
		fileWriter.format("\tlw $fp, 0($sp)\n");

		// load $ra from stack
		fileWriter.format("\tlw $ra, 4($sp)\n");

		// add 8 to $sp
		fileWriter.format("\taddu $sp,$sp,8\n");

		// jump to $ra
		fileWriter.format("\tjr $ra\n");
	}

	public void returnFromFunc(Temp retVal) {
		if (retVal != null) {
			fileWriter.format("\tmove $v0,$t%d\n", retVal.getSerialNumber());
		}
		epilogue();
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static MipsGenerator instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected MipsGenerator() {
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MipsGenerator getInstance() {
		if (instance == null) {
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new MipsGenerator();

			try {
				/*********************************************************************************/
				/*
				 * [1] Open the MIPS text file and write data section with error message strings
				 */
				/*********************************************************************************/
				String dirname = "./output/";
				String filename = String.format("MIPS.txt");

				/***************************************/
				/* [2] Open MIPS text file for writing */
				/***************************************/
				instance.fileWriter = new PrintWriter(dirname + filename);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*****************************************************/
			/* [3] Print data section with error message strings */
			/*****************************************************/
			instance.fileWriter.print(".data\n");
			instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
			instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
			instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");

			/*********************************************************/
			/* [4] Emit the MIPS code for those errors */
			/*********************************************************/
			instance.fileWriter.print(".text\n");
			instance.fileWriter.print("error_string_access_violation:\n");
			instance.fileWriter.print("\tli $v0,4\n");
			instance.fileWriter.print("\tla $a0,string_access_violation\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("\tli $v0,10\n");
			instance.fileWriter.print("\tsyscall\n");

			instance.fileWriter.print("error_illegal_div_by_0:\n");
			instance.fileWriter.print("\tli $v0,4\n");
			instance.fileWriter.print("\tla $a0,string_illegal_div_by_0\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("\tli $v0,10\n");
			instance.fileWriter.print("\tsyscall\n");

			instance.fileWriter.print("error_invalid_ptr_dref:\n");
			instance.fileWriter.print("\tli $v0,4\n");
			instance.fileWriter.print("\tla $a0,string_invalid_ptr_dref\n");
			instance.fileWriter.print("\tsyscall\n");
			instance.fileWriter.print("\tli $v0,10\n");
			instance.fileWriter.print("\tsyscall\n");
		}
		return instance;
	}
}
