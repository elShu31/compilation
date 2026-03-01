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
import regalloc.RegisterAllocator;

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

	public void stringConcat(Temp dst, Temp src1, Temp src2) {
		fileWriter.format("\t# Inline String Concatenation\n");
		String labelLen1 = ir.IrCommand.getFreshLabel("sc_len1");
		String labelLen2Decide = ir.IrCommand.getFreshLabel("sc_len2_decide");
		String labelLen2 = ir.IrCommand.getFreshLabel("sc_len2");
		String labelAlloc = ir.IrCommand.getFreshLabel("sc_alloc");
		String labelCopy1 = ir.IrCommand.getFreshLabel("sc_copy1");
		String labelCopy2Decide = ir.IrCommand.getFreshLabel("sc_copy2_decide");
		String labelCopy2 = ir.IrCommand.getFreshLabel("sc_copy2");
		String labelEnd = ir.IrCommand.getFreshLabel("sc_end");

		String rSrc1 = regalloc.RegisterAllocator.getReg(src1);
		String rSrc2 = regalloc.RegisterAllocator.getReg(src2);
		String rDst = regalloc.RegisterAllocator.getReg(dst);

		// Push registers we are about to use to preserve their values
		// We use $s0-$s4 for internal logic so we don't clobber any $tX registers
		// that might be assigned to rSrc1, rSrc2, or rDst by the RegisterAllocator!
		fileWriter.format("\tsubu $sp, $sp, 32\n");
		fileWriter.format("\tsw $s0, 0($sp)\n");
		fileWriter.format("\tsw $s1, 4($sp)\n");
		fileWriter.format("\tsw $s2, 8($sp)\n");
		fileWriter.format("\tsw $s3, 12($sp)\n");
		fileWriter.format("\tsw $s4, 16($sp)\n");
		fileWriter.format("\tsw $a0, 20($sp)\n");
		fileWriter.format("\tsw $v1, 24($sp)\n");
		fileWriter.format("\tsw $v0, 28($sp)\n");

		// Length of string 1
		fileWriter.format("\tli $s1, 0\n");
		fileWriter.format("\tmove $s2, %s\n", rSrc1);
		fileWriter.format("%s:\n", labelLen1);
		fileWriter.format("\tlb $s3, 0($s2)\n");
		fileWriter.format("\tbeqz $s3, %s\n", labelLen2Decide);
		fileWriter.format("\taddi $s1, $s1, 1\n");
		fileWriter.format("\taddi $s2, $s2, 1\n");
		fileWriter.format("\tj %s\n", labelLen1);

		// Length of string 2
		fileWriter.format("%s:\n", labelLen2Decide);
		fileWriter.format("\tli $s4, 0\n");
		fileWriter.format("\tmove $s2, %s\n", rSrc2);
		fileWriter.format("%s:\n", labelLen2);
		fileWriter.format("\tlb $s3, 0($s2)\n");
		fileWriter.format("\tbeqz $s3, %s\n", labelAlloc);
		fileWriter.format("\taddi $s4, $s4, 1\n");
		fileWriter.format("\taddi $s2, $s2, 1\n");
		fileWriter.format("\tj %s\n", labelLen2);

		// Allocate space (len1 + len2 + 1)
		fileWriter.format("%s:\n", labelAlloc);
		fileWriter.format("\tadd $a0, $s1, $s4\n");
		fileWriter.format("\taddi $a0, $a0, 1\n");
		fileWriter.format("\tli $v0, 9\n"); // syscall sbrk
		fileWriter.format("\tsyscall\n");

		// $v0 has new pointer.
		fileWriter.format("\tmove $s0, $v0\n"); // $s0 = writing cursor
		fileWriter.format("\tmove $v1, $v0\n"); // $v1 = base pointer backup

		// Copy String 1
		fileWriter.format("\tmove $s2, %s\n", rSrc1);
		fileWriter.format("%s:\n", labelCopy1);
		fileWriter.format("\tlb $s3, 0($s2)\n");
		fileWriter.format("\tbeqz $s3, %s\n", labelCopy2Decide);
		fileWriter.format("\tsb $s3, 0($s0)\n");
		fileWriter.format("\taddi $s2, $s2, 1\n");
		fileWriter.format("\taddi $s0, $s0, 1\n");
		fileWriter.format("\tj %s\n", labelCopy1);

		// Copy String 2
		fileWriter.format("%s:\n", labelCopy2Decide);
		fileWriter.format("\tmove $s2, %s\n", rSrc2);
		fileWriter.format("%s:\n", labelCopy2);
		fileWriter.format("\tlb $s3, 0($s2)\n");
		fileWriter.format("\tbeqz $s3, %s\n", labelEnd);
		fileWriter.format("\tsb $s3, 0($s0)\n");
		fileWriter.format("\taddi $s2, $s2, 1\n");
		fileWriter.format("\taddi $s0, $s0, 1\n");
		fileWriter.format("\tj %s\n", labelCopy2);

		// End string
		fileWriter.format("%s:\n", labelEnd);
		fileWriter.format("\tli $s3, 0\n");
		fileWriter.format("\tsb $s3, 0($s0)\n");

		// Map return value to proper mapped string destination register before popping
		fileWriter.format("\tmove %s, $v1\n", rDst);

		// Pop registers to restore values
		fileWriter.format("\tlw $s0, 0($sp)\n");
		fileWriter.format("\tlw $s1, 4($sp)\n");
		fileWriter.format("\tlw $s2, 8($sp)\n");
		fileWriter.format("\tlw $s3, 12($sp)\n");
		fileWriter.format("\tlw $s4, 16($sp)\n");
		fileWriter.format("\tlw $a0, 20($sp)\n");
		fileWriter.format("\tlw $v1, 24($sp)\n");
		fileWriter.format("\tlw $v0, 28($sp)\n");
		fileWriter.format("\taddu $sp, $sp, 32\n");

		fileWriter.format("\t# Inline String Concatenation End\n");
	}

	public void stringCompareEq(Temp dst, Temp src1, Temp src2) {
		fileWriter.format("\t# Inline String Equality Compare\n");
		String labelLoop = ir.IrCommand.getFreshLabel("seq_loop");
		String labelDiff = ir.IrCommand.getFreshLabel("seq_diff");
		String labelAssignOne = ir.IrCommand.getFreshLabel("seq_AssignOne");
		String labelAssignZero = ir.IrCommand.getFreshLabel("seq_AssignZero");
		String labelEnd = ir.IrCommand.getFreshLabel("seq_end");

		String rSrc1 = regalloc.RegisterAllocator.getReg(src1);
		String rSrc2 = regalloc.RegisterAllocator.getReg(src2);
		String rDst = regalloc.RegisterAllocator.getReg(dst);

		// Push $s registers to preserve state
		fileWriter.format("\tsubu $sp, $sp, 16\n");
		fileWriter.format("\tsw $s0, 0($sp)\n");
		fileWriter.format("\tsw $s1, 4($sp)\n");
		fileWriter.format("\tsw $s2, 8($sp)\n");
		fileWriter.format("\tsw $s3, 12($sp)\n");

		// $s0 = cursor 1, $s1 = cursor 2
		fileWriter.format("\tmove $s0, %s\n", rSrc1);
		fileWriter.format("\tmove $s1, %s\n", rSrc2);

		fileWriter.format("%s:\n", labelLoop);
		fileWriter.format("\tlb $s2, 0($s0)\n"); // Load byte from string 1
		fileWriter.format("\tlb $s3, 0($s1)\n"); // Load byte from string 2

		// If bytes are not equal, the strings are not equal
		fileWriter.format("\tbne $s2, $s3, %s\n", labelDiff);

		// If bytes are equal AND equal to 0 (end of string), the strings are equal
		fileWriter.format("\tbeqz $s2, %s\n", labelAssignOne);

		// Otherwise, move to next byte and repeat
		fileWriter.format("\taddi $s0, $s0, 1\n");
		fileWriter.format("\taddi $s1, $s1, 1\n");
		fileWriter.format("\tj %s\n", labelLoop);

		// Different bytes -> return 0
		fileWriter.format("%s:\n", labelDiff);
		fileWriter.format("\tj %s\n", labelAssignZero);

		// They are equal -> return 1
		fileWriter.format("%s:\n", labelAssignOne);
		fileWriter.format("\tli %s, 1\n", rDst);
		fileWriter.format("\tj %s\n", labelEnd);

		// They are not equal -> return 0
		fileWriter.format("%s:\n", labelAssignZero);
		fileWriter.format("\tli %s, 0\n", rDst);
		fileWriter.format("\tj %s\n", labelEnd);

		fileWriter.format("%s:\n", labelEnd);

		// Pop registers to restore values
		fileWriter.format("\tlw $s0, 0($sp)\n");
		fileWriter.format("\tlw $s1, 4($sp)\n");
		fileWriter.format("\tlw $s2, 8($sp)\n");
		fileWriter.format("\tlw $s3, 12($sp)\n");
		fileWriter.format("\taddu $sp, $sp, 16\n");

		fileWriter.format("\t# Inline String Equality Compare End\n");
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
		// 3. Store original size (length) at 0($v0) BEFORE moving to dst
		// This protects the size register even if RegisterAllocator mapped dst and size
		// to the same register
		fileWriter.format("\tsw %s,0($v0)\n", regalloc.RegisterAllocator.getReg(size));
		// 4. Store result pointer in dst
		fileWriter.format("\tmove %s,$v0\n", regalloc.RegisterAllocator.getReg(dst));
		// 5. End of allocate array
		fileWriter.format("\t# End of Allocate Array\n");
	}

	private void checkArrayBounds(Temp arrayBase, Temp index) {
		String rBase = regalloc.RegisterAllocator.getReg(arrayBase);
		String rIndex = regalloc.RegisterAllocator.getReg(index);

		fileWriter.format("\t# Array bounds check\n");

		// 0. Store $v1 to stack
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw $v1,0($sp)\n");

		// 1. Check if index < 0
		fileWriter.format("\tbltz %s,error_access_violation\n", rIndex);

		// 2. Load array size (from 0 offset of base)
		fileWriter.format("\tlw $v1,0(%s)\n", rBase);

		// 3. Check if index >= size
		fileWriter.format("\tbge %s,$v1,error_access_violation\n", rIndex);

		// 4. Restore $v1 from stack, load/store is valid
		fileWriter.format("\tlw $v1,0($sp)\n");
		fileWriter.format("\taddu $sp,$sp,4\n");
		fileWriter.format("\t# End bounds check\n");
	}

	public void loadArray(Temp dst, Temp arrayBase, Temp index) {
		fileWriter.format("\t# Load Array\n");
		checkArrayBounds(arrayBase, index);

		// 1. Add 1 to index to skip length metadata
		fileWriter.format("\taddi $v1,%s,1\n", regalloc.RegisterAllocator.getReg(index));
		// 2. Multiply by 4 (shift left 2) because each array element is a 4-byte word
		fileWriter.format("\tsll $v1,$v1,2\n");
		// 3. Add offset to base address
		fileWriter.format("\tadd $v1,$v1,%s\n", regalloc.RegisterAllocator.getReg(arrayBase));
		// 4. Load from address
		fileWriter.format("\tlw %s,0($v1)\n", regalloc.RegisterAllocator.getReg(dst));
		// 5. End of Load Array
		fileWriter.format("\t# End of Load Array\n");
	}

	public void storeArray(Temp arrayBase, Temp index, Temp src) {
		fileWriter.format("\t# Store Array\n");
		checkArrayBounds(arrayBase, index);

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
		clampInt(t);
	}

	private void clampInt(Temp dst) {
		String rDst = regalloc.RegisterAllocator.getReg(dst);
		String lSkipUpper = ir.IrCommand.getFreshLabel("clamp_skip_upper");
		String lSkipLower = ir.IrCommand.getFreshLabel("clamp_skip_lower");

		fileWriter.format("\t# Clamp %s to [-32768, 32767]\n", rDst);
		// Upper bound
		fileWriter.format("\tli $v1, 32767\n");
		fileWriter.format("\tble %s, $v1, %s\n", rDst, lSkipUpper);
		fileWriter.format("\tli %s, 32767\n", rDst);
		fileWriter.format("%s:\n", lSkipUpper);

		// Lower bound
		fileWriter.format("\tli $v1, -32768\n");
		fileWriter.format("\tbge %s, $v1, %s\n", rDst, lSkipLower);
		fileWriter.format("\tli %s, -32768\n", rDst);
		fileWriter.format("%s:\n", lSkipLower);
	}

	public void add(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tadd %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
		clampInt(dst);
	}

	public void mul(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tmul %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
		clampInt(dst);
	}

	public void sub(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tsub %s,%s,%s\n", regalloc.RegisterAllocator.getReg(dst),
				regalloc.RegisterAllocator.getReg(oprnd1), regalloc.RegisterAllocator.getReg(oprnd2));
		clampInt(dst);
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
		clampInt(dst);
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
		// Push registers to protect them before entering jal
		// RegisterAllocator assigns $t0-$t9. So we preserve any active $t register.
		fileWriter.format("\t# Preserve caller registers\n");
		fileWriter.format("\tsubu $sp,$sp,%d\n", RegisterAllocator.K * WORD_SIZE);
		for (int i = 0; i < RegisterAllocator.K; i++) {
			fileWriter.format("\tsw $t%d,%d($sp)\n", i, i * WORD_SIZE);
		}

		fileWriter.format("\tjal %s\n", funcName);

		// After jal, pop all preserved $t0-$t9
		fileWriter.format("\t# Restore caller registers\n");
		for (int i = RegisterAllocator.K - 1; i >= 0; i--) {
			fileWriter.format("\tlw $t%d,%d($sp)\n", i, i * WORD_SIZE);
		}
		fileWriter.format("\taddu $sp,$sp,%d\n", RegisterAllocator.K * WORD_SIZE);

		if (numArgs > 0) {
			fileWriter.format("\taddu $sp,$sp,%d\n", numArgs * WORD_SIZE);
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

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static MipsGenerator getInstance() {
		if (instance == null) {
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new MipsGenerator();
		}
		return instance;
	}

	public void init(PrintWriter fileWriter) {
		this.fileWriter = fileWriter;

		/*****************************************************/
		/* [3] Print data section with error message strings */
		/*****************************************************/
		this.fileWriter.print(".data\n");
		this.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
		this.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
		this.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");

		/*********************************************************/
		/* [4] Emit the MIPS code for those errors */
		/*********************************************************/
		this.fileWriter.print(".text\n");
		this.fileWriter.print("error_access_violation:\n");
		this.fileWriter.print("\tli $v0,4\n");
		this.fileWriter.print("\tla $a0,string_access_violation\n");
		this.fileWriter.print("\tsyscall\n");
		this.fileWriter.print("\tli $v0,10\n");
		this.fileWriter.print("\tsyscall\n");

		this.fileWriter.print("error_illegal_div_by_0:\n");
		this.fileWriter.print("\tli $v0,4\n");
		this.fileWriter.print("\tla $a0,string_illegal_div_by_0\n");
		this.fileWriter.print("\tsyscall\n");
		this.fileWriter.print("\tli $v0,10\n");
		this.fileWriter.print("\tsyscall\n");

		this.fileWriter.print("error_invalid_ptr_dref:\n");
		this.fileWriter.print("\tli $v0,4\n");
		this.fileWriter.print("\tla $a0,string_invalid_ptr_dref\n");
		this.fileWriter.print("\tsyscall\n");
		this.fileWriter.print("\tli $v0,10\n");
		this.fileWriter.print("\tsyscall\n");
	}
}
