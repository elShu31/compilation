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
		fileWriter.close();
	}

	/**************************/
	/* Library functions */
	/**************************/
	public void printInt(Temp t) {
		fileWriter.format("\t# Print integer\n");
		fileWriter.format("\tmove $a0,%s\n", regalloc.RegisterAllocator.getReg(t));
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\t# Print space\n");

		// 32 is the ASCII code for space
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}

	public void printString(Temp t) {
		fileWriter.format("\t# Print string\n");
		fileWriter.format("\tmove $a0,%s\n", regalloc.RegisterAllocator.getReg(t));
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	/**************************/
	/* Memory allocation */
	/**************************/
	public void malloc(Temp dst, int sizeBytes) {
		fileWriter.format("\t# Memory allocation\n");
		fileWriter.format("\tli $a0,%d\n", sizeBytes);
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove %s,$v0\n", regalloc.RegisterAllocator.getReg(dst));
	}

	public void la(Temp dst, String label) {
		fileWriter.format("\tla %s,%s\n", regalloc.RegisterAllocator.getReg(dst), label);
	}

	public void allocateArray(Temp dst, Temp size) {
		fileWriter.format("\t# Allocate Array\n");
		// 1. Add 1 to size for the length metadata
		fileWriter.format("\taddi $a0,%s,1\n", regalloc.RegisterAllocator.getReg(size));
		// 2. Multiply by 4 (shift left 2) because each array element is a 4-byte word
		fileWriter.format("\tsll $a0,$a0,2\n");
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		// 3. Store result pointer in dst
		fileWriter.format("\tmove %s,$v0\n", regalloc.RegisterAllocator.getReg(dst));
		// 4. Store original size (length) at 0(dst)
		fileWriter.format("\tsw %s,0(%s)\n", regalloc.RegisterAllocator.getReg(size),
				regalloc.RegisterAllocator.getReg(dst));
		// 5. End of allocate array
		fileWriter.format("\t# End of Allocate Array\n");
	}

	public void loadArray(Temp dst, Temp arrayBase, Temp index) {
		fileWriter.format("\t# Load Array\n");
		// 1. Add 1 to index to skip length metadata
		fileWriter.format("\taddi $v1,%s,1\n", regalloc.RegisterAllocator.getReg(index));
		// 2. Multiply by 4 (shift left 2) because each array element is a 4-byte word
		fileWriter.format("\tsll $v1,$v1,2\n");
		// 3. Add offset to base address
		fileWriter.format("\tadd $v1,$v1,%s\n", regalloc.RegisterAllocator.getReg(arrayBase));
		// 4. Load from address
		fileWriter.format("\tlw %s,0($v1)\n", regalloc.RegisterAllocator.getReg(dst));
		// 5. End of load array
		fileWriter.format("\t# End of Load Array\n");
	}

	public void storeArray(Temp arrayBase, Temp index, Temp src) {
		fileWriter.format("\t# Store Array\n");
		// 1. Add 1 to index to skip length metadata
		fileWriter.format("\taddi $v1,%s,1\n", regalloc.RegisterAllocator.getReg(index));
		// 2. Multiply by 4 (shift left 2)
		fileWriter.format("\tsll $v1,$v1,2\n");
		// 3. Add offset to base address
		fileWriter.format("\tadd $v1,$v1,%s\n", regalloc.RegisterAllocator.getReg(arrayBase));
		// 4. Store src at address
		fileWriter.format("\tsw %s,0($v1)\n", regalloc.RegisterAllocator.getReg(src));
		// 5. End of store array
		fileWriter.format("\t# Store Array End\n");
	}

	/**************************/
	/* Global variables */
	/**************************/
	public void allocate(VarId varId) {
		if (varId.isGlobal) {
			fileWriter.format(".data\n");
			fileWriter.format("\tglobal_%s: .word 721\n", varId.name);
		}
	}

	public void load(Temp dst, VarId varId) {
		if (varId.isGlobal) {
			fileWriter.format("\tlw %s,global_%s\n", regalloc.RegisterAllocator.getReg(dst), varId.name);
		} else {
			fileWriter.format("\tlw %s,%d($fp)\n", regalloc.RegisterAllocator.getReg(dst), varId.fpOffset);
		}
	}

	public void store(VarId varId, Temp src) {
		if (varId.isGlobal) {
			fileWriter.format("\tsw %s,global_%s\n", regalloc.RegisterAllocator.getReg(src), varId.name);
		} else {
			fileWriter.format("\tsw %s,%d($fp)\n", regalloc.RegisterAllocator.getReg(src), varId.fpOffset);
		}
	}

	/**************************/
	/* MIPS instructions */
	/**************************/
	public void li(Temp t, int value) {
		fileWriter.format("\tli %s,%d\n", regalloc.RegisterAllocator.getReg(t), value);
	}

	public void add(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tadd %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
	}

	public void mul(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tmul %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
	}

	public void sub(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tsub %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
	}

	public void div(Temp dst, Temp oprnd1, Temp oprnd2) {
		String labelValidDiv = IrCommand.getFreshLabel("ValidDiv");

		fileWriter.format("\tbne %s,$zero,%s\n", regalloc.RegisterAllocator.getReg(oprnd2), labelValidDiv);
		// Branch not taken - there is a zero division error, jump to error handler
		jump("error_illegal_div_by_0");

		// Branch was taken - there is no zero division error
		label(labelValidDiv);
		fileWriter.format("\tdiv %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
	}

	public void label(String label) {
		if (!label.startsWith("Label_")) {
			fileWriter.format(".text\n");
		}
		fileWriter.format("%s:\n", label);
	}

	/************************************/
	/* Jumps and unconditional branches */
	/************************************/
	public void jump(String label) {
		fileWriter.format("\tj %s\n", label);
	}

	/**********************************/
	/* Conditional branches. */
	/**********************************/
	public void blt(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tblt %s,%s,%s\n", regalloc.RegisterAllocator.getReg(oprnd1),
				regalloc.RegisterAllocator.getReg(oprnd2), label);
	}

	public void bge(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbge %s,%s,%s\n", regalloc.RegisterAllocator.getReg(oprnd1),
				regalloc.RegisterAllocator.getReg(oprnd2), label);
	}

	public void bgt(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbgt %s,%s,%s\n", regalloc.RegisterAllocator.getReg(oprnd1),
				regalloc.RegisterAllocator.getReg(oprnd2), label);
	}

	public void ble(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tble %s,%s,%s\n", regalloc.RegisterAllocator.getReg(oprnd1),
				regalloc.RegisterAllocator.getReg(oprnd2), label);
	}

	public void bne(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbne %s,%s,%s\n", regalloc.RegisterAllocator.getReg(oprnd1),
				regalloc.RegisterAllocator.getReg(oprnd2), label);
	}

	public void beq(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbeq %s,%s,%s\n", regalloc.RegisterAllocator.getReg(oprnd1),
				regalloc.RegisterAllocator.getReg(oprnd2), label);
	}

	public void beqz(Temp oprnd1, String label) {
		fileWriter.format("\tbeq %s,$zero,%s\n", regalloc.RegisterAllocator.getReg(oprnd1), label);
	}

	/**************************/
	/* Functions */
	/**************************/
	public void prologue(String funcName, int localVarsSize) {
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $ra,0($sp)\n");

		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $fp,0($sp)\n");

		fileWriter.format("\tmove $fp,$sp\n");

		if (localVarsSize > 0) {
			fileWriter.format("\tsubu $sp,$sp,%d\n", localVarsSize);
		}
	}

	public void epilogue(String funcName) {
		if (funcName.equals("main")) {
			fileWriter.format("\tli $v0,10\n");
			fileWriter.format("\tsyscall\n");
		} else {
			fileWriter.format("\tmove $sp,$fp\n");
			fileWriter.format("\tlw $fp, 0($sp)\n");
			fileWriter.format("\tlw $ra, 4($sp)\n");
			fileWriter.format("\taddu $sp,$sp,8\n");
			fileWriter.format("\tjr $ra\n");
		}
	}

	public void returnFromFunc(String funcName, Temp retVal) {
		if (retVal != null) {
			fileWriter.format("\tmove $v0,%s\n", regalloc.RegisterAllocator.getReg(retVal));
		}
		epilogue(funcName);
	}

	public void pushArg(Temp arg) {
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw %s,0($sp)\n", regalloc.RegisterAllocator.getReg(arg));
	}

	public void callFunc(String funcName, int numArgs, Temp retVal) {
		fileWriter.format("\tjal %s\n", funcName);

		if (numArgs > 0) {
			fileWriter.format("\taddu $sp,$sp,%d\n", numArgs * 4);
		}

		if (retVal != null) {
			fileWriter.format("\tmove %s,$v0\n", regalloc.RegisterAllocator.getReg(retVal));
		}
	}

	public void emitStrings(java.util.List<String> strings) {
		if (strings.isEmpty()) {
			return;
		}

		fileWriter.format(".data\n");
		for (int i = 0; i < strings.size(); i++) {
			fileWriter.format("\tstring_const_%d: .asciiz %s\n", i, strings.get(i));
		}
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

	public void init(PrintWriter fileWriter) {
		this.fileWriter = fileWriter;
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

			if (instance.fileWriter == null) {
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
