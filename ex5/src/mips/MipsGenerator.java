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

public class MipsGenerator {
	/************************/
	/* Only keep 32 bit words */
	/************************/
	private static final int WORD_SIZE = 4;
	private PrintWriter fileWriter;
	private java.util.Set<String> allocatedGlobalVars = new java.util.HashSet<>();
	private java.util.Map<String, String> stringConstants = new java.util.LinkedHashMap<>();

	public void finalizeFile() {
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.close();
	}

	public void printInt(Temp t) {
		int idx = t.getSerialNumber();
		fileWriter.format("\tmove $a0,$t%d\n", idx);
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}

	public void printString(Temp t) {
		int idx = t.getSerialNumber();
		fileWriter.format("\tmove $a0,$t%d\n", idx);
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	/****************************************/
	/* Allocate a global variable in .data */
	/****************************************/
	public void allocateGlobal(String varName) {
		allocatedGlobalVars.add(varName);
	}

	/****************************************/
	/* Emit the .data section */
	/****************************************/
	public void emitDataSection() {
		fileWriter.print(".data\n");
		fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
		fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
		fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
		for (String varName : allocatedGlobalVars) {
			fileWriter.format("\tglobal_%s: .word 0\n", varName);
		}
		for (java.util.Map.Entry<String, String> entry : stringConstants.entrySet()) {
			fileWriter.format("\t%s: .asciiz %s\n", entry.getKey(), entry.getValue());
		}
		// Emit .text directive after .data section
		fileWriter.print(".text\n");
	}

	/****************************************/
	/* Load from global variable */
	/****************************************/
	public void loadGlobal(Temp dst, String varName) {
		int idxdst = dst.getSerialNumber();
		fileWriter.format("\tlw $t%d,global_%s\n", idxdst, varName);
	}

	/****************************************/
	/* Store to global variable */
	/****************************************/
	public void storeGlobal(String varName, Temp src) {
		int idxsrc = src.getSerialNumber();
		fileWriter.format("\tsw $t%d,global_%s\n", idxsrc, varName);
	}

	/****************************************/
	/* Load from stack (fp-relative) */
	/****************************************/
	public void loadLocal(Temp dst, int fpOffset) {
		int idxdst = dst.getSerialNumber();
		fileWriter.format("\tlw $t%d,%d($fp)\n", idxdst, fpOffset);
	}

	/****************************************/
	/* Store to stack (fp-relative) */
	/****************************************/
	public void storeLocal(int fpOffset, Temp src) {
		int idxsrc = src.getSerialNumber();
		fileWriter.format("\tsw $t%d,%d($fp)\n", idxsrc, fpOffset);
	}

	public void li(Temp t, int value) {
		int idx = t.getSerialNumber();
		fileWriter.format("\tli $t%d,%d\n", idx, value);
	}

	public void add(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		fileWriter.format("\tadd $t%d,$t%d,$t%d\n", dstidx, i1, i2);
	}

	public void mul(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		fileWriter.format("\tmul $t%d,$t%d,$t%d\n", dstidx, i1, i2);
	}

	public void sub(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		fileWriter.format("\tsub $t%d,$t%d,$t%d\n", dstidx, i1, i2);
	}

	public void div(Temp dst, Temp oprnd1, Temp oprnd2) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();
		int dstidx = dst.getSerialNumber();

		// Check for division by zero
		String okLabel = ir.IrCommand.getFreshLabel("DivOk");
		fileWriter.format("\tbne $t%d,$zero,%s\n", i2, okLabel);
		fileWriter.format("\tla $a0,string_illegal_div_by_0\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n"); // exit
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", okLabel);

		fileWriter.format("\tdiv $t%d,$t%d\n", i1, i2);
		fileWriter.format("\tmflo $t%d\n", dstidx);
	}

	public void label(String inlabel) {
		fileWriter.format("%s:\n", inlabel);
	}

	public void jump(String inlabel) {
		fileWriter.format("\tj %s\n", inlabel);
	}

	public void blt(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tblt $t%d,$t%d,%s\n", i1, i2, label);
	}

	public void bge(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbge $t%d,$t%d,%s\n", i1, i2, label);
	}

	public void bgt(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbgt $t%d,$t%d,%s\n", i1, i2, label);
	}

	public void ble(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tble $t%d,$t%d,%s\n", i1, i2, label);
	}

	public void bne(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbne $t%d,$t%d,%s\n", i1, i2, label);
	}

	public void beq(Temp oprnd1, Temp oprnd2, String label) {
		int i1 = oprnd1.getSerialNumber();
		int i2 = oprnd2.getSerialNumber();

		fileWriter.format("\tbeq $t%d,$t%d,%s\n", i1, i2, label);
	}

	public void beqz(Temp oprnd1, String label) {
		int i1 = oprnd1.getSerialNumber();

		fileWriter.format("\tbeq $t%d,$zero,%s\n", i1, label);
	}

	/*******************************************/
	/* Function prologue (Tutorial 10) */
	/* Push $ra, push $fp, set $fp=$sp, */
	/* allocate space for locals */
	/*******************************************/
	public void prologue(String funcName, int numLocals) {
		// Push return address
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $ra,0($sp)\n");
		// Push old frame pointer
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $fp,0($sp)\n");
		// Set frame pointer to current stack pointer
		fileWriter.format("\tmove $fp,$sp\n");
		// Allocate space for local variables
		if (numLocals > 0) {
			fileWriter.format("\tsubu $sp,$sp,%d\n", numLocals * WORD_SIZE);
		}
	}

	/*******************************************/
	/* Function epilogue (Tutorial 10) */
	/* Restore $sp, $fp, $ra, jr $ra */
	/*******************************************/
	public void epilogue() {
		// Restore stack pointer to frame pointer
		fileWriter.format("\tmove $sp,$fp\n");
		// Restore old frame pointer
		fileWriter.format("\tlw $fp,0($sp)\n");
		// Restore return address
		fileWriter.format("\tlw $ra,4($sp)\n");
		// Pop saved $fp and $ra
		fileWriter.format("\taddu $sp,$sp,8\n");
		// Return to caller
		fileWriter.format("\tjr $ra\n");
	}

	/*******************************************/
	/* Return from function */
	/* Move return value to $v0 then epilogue */
	/*******************************************/
	public void returnFromFunc(Temp retVal) {
		if (retVal != null) {
			fileWriter.format("\tmove $v0,$t%d\n", retVal.getSerialNumber());
		}
		epilogue();
	}

	/*******************************************/
	/* Call function (Tutorial 10 convention) */
	/* Push args right-to-left on stack, */
	/* jal, pop args, get return in $v0 */
	/*******************************************/
	public void callFunc(String funcName, java.util.List<Temp> args, Temp retDst) {
		// Push arguments right-to-left onto the stack
		for (int i = args.size() - 1; i >= 0; i--) {
			int tempId = args.get(i).getSerialNumber();
			fileWriter.format("\tsubu $sp,$sp,4\n");
			fileWriter.format("\tsw $t%d,0($sp)\n", tempId);
		}

		// Jump and link
		fileWriter.format("\tjal %s\n", funcName);

		// Pop arguments from stack
		if (args.size() > 0) {
			fileWriter.format("\taddu $sp,$sp,%d\n", args.size() * WORD_SIZE);
		}

		// Store result
		if (retDst != null) {
			fileWriter.format("\tmove $t%d,$v0\n", retDst.getSerialNumber());
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

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static void init(String outputFileName) {
		if (instance == null) {
			instance = new MipsGenerator();

			try {
				instance.fileWriter = new PrintWriter(outputFileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static MipsGenerator getInstance() {
		if (instance == null) {
			init("./output/MIPS.txt");
		}
		return instance;
	}
}
