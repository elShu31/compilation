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
	private int stringConstantCounter = 0;

	public PrintWriter getFileWriter() {
		return fileWriter;
	}

	/****************************************/
	/* Register a string constant and */
	/* return its label name */
	/****************************************/
	public String addStringConstant(String value) {
		// Check if this exact string already has a label
		for (java.util.Map.Entry<String, String> entry : stringConstants.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		String label = "string_const_" + (stringConstantCounter++);
		stringConstants.put(label, value);
		return label;
	}

	/****************************************/
	/* Load address of a string constant */
	/****************************************/
	public void loadStringConstant(Temp dst, String label) {
		int idx = dst.getSerialNumber();
		fileWriter.format("\tla $t%d,%s\n", idx, label);
	}

	public void finalizeFile() {
		fileWriter.print("\tli $v0,10\n");
		fileWriter.print("\tsyscall\n");

		fileWriter.print("error_null_ptr:\n");
		fileWriter.print("\tla $a0, string_invalid_ptr_dref\n");
		fileWriter.print("\tli $v0, 4\n");
		fileWriter.print("\tsyscall\n");
		fileWriter.print("\tli $v0, 10\n");
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
	/* Allocate memory on heap via sbrk */
	/****************************************/
	public void malloc(Temp dst, Temp sizeBytes) {
		int sizeIdx = sizeBytes.getSerialNumber();
		int dstIdx = dst.getSerialNumber();
		fileWriter.format("\tmove $a0,$t%d\n", sizeIdx);
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove $t%d,$v0\n", dstIdx);
	}

	/****************************************/
	/* Store to array element */
	/* base[(index+1)*4] := value */
	/****************************************/
	public void arrayStore(Temp base, Temp index, Temp value) {
		int baseIdx = base.getSerialNumber();
		int indexIdx = index.getSerialNumber();
		int valueIdx = value.getSerialNumber();
		fileWriter.format("\tbeq $t%d,$zero,error_null_ptr\n", baseIdx);
		fileWriter.format("\taddu $v1,$t%d,1\n", indexIdx);
		fileWriter.format("\tsll $v1,$v1,2\n");
		fileWriter.format("\taddu $v1,$t%d,$v1\n", baseIdx);
		// Bounds check: index >= 0
		String okLower = ir.IrCommand.getFreshLabel("ArrOkLo");
		fileWriter.format("\tbge $t%d,$zero,%s\n", indexIdx, okLower);
		fileWriter.format("\tla $a0,string_access_violation\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", okLower);
		// Bounds check: index < length
		String okUpper = ir.IrCommand.getFreshLabel("ArrOkHi");
		fileWriter.format("\tlw $a0,0($t%d)\n", baseIdx);
		fileWriter.format("\tblt $t%d,$a0,%s\n", indexIdx, okUpper);
		fileWriter.format("\tla $a0,string_access_violation\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", okUpper);
		fileWriter.format("\tsw $t%d,0($v1)\n", valueIdx);
	}

	/****************************************/
	/* Load from array element */
	/* dst := base[(index+1)*4] */
	/****************************************/
	public void arrayLoad(Temp dst, Temp base, Temp index) {
		int dstIdx = dst.getSerialNumber();
		int baseIdx = base.getSerialNumber();
		int indexIdx = index.getSerialNumber();
		fileWriter.format("\tbeq $t%d,$zero,error_null_ptr\n", baseIdx);
		fileWriter.format("\taddu $v1,$t%d,1\n", indexIdx);
		fileWriter.format("\tsll $v1,$v1,2\n");
		fileWriter.format("\taddu $v1,$t%d,$v1\n", baseIdx);
		// Bounds check: index >= 0
		String okLower = ir.IrCommand.getFreshLabel("ArrOkLo");
		fileWriter.format("\tbge $t%d,$zero,%s\n", indexIdx, okLower);
		fileWriter.format("\tla $a0,string_access_violation\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", okLower);
		// Bounds check: index < length
		String okUpper = ir.IrCommand.getFreshLabel("ArrOkHi");
		fileWriter.format("\tlw $a0,0($t%d)\n", baseIdx);
		fileWriter.format("\tblt $t%d,$a0,%s\n", indexIdx, okUpper);
		fileWriter.format("\tla $a0,string_access_violation\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", okUpper);
		fileWriter.format("\tlw $t%d,0($v1)\n", dstIdx);
	}

	/****************************************/
	/* Store length at array base addr */
	/* base[0] := length */
	/****************************************/
	public void storeArrayLength(Temp base, Temp length) {
		int baseIdx = base.getSerialNumber();
		int lengthIdx = length.getSerialNumber();
		fileWriter.format("\tsw $t%d,0($t%d)\n", lengthIdx, baseIdx);
	}

	/****************************************/
	/* String concatenation */
	/* dst = str1 + str2 */
	/* Computes lengths, allocates buffer, */
	/* copies both strings */
	/****************************************/
	public void stringConcat(Temp dst, Temp str1, Temp str2) {
		int dstIdx = dst.getSerialNumber();
		int s1Idx = str1.getSerialNumber();
		int s2Idx = str2.getSerialNumber();

		// Use $a1 = len1, $a2 = len2, $a3 = temp for copy
		// $v1 = pointer during copy

		// --- Compute strlen(str1) -> $a1 ---
		String len1Start = ir.IrCommand.getFreshLabel("StrLen1");
		String len1End = ir.IrCommand.getFreshLabel("StrLen1End");
		fileWriter.format("\tli $a1,0\n");
		fileWriter.format("\tmove $v1,$t%d\n", s1Idx);
		fileWriter.format("%s:\n", len1Start);
		fileWriter.format("\tlb $a3,0($v1)\n");
		fileWriter.format("\tbeq $a3,$zero,%s\n", len1End);
		fileWriter.format("\taddu $a1,$a1,1\n");
		fileWriter.format("\taddu $v1,$v1,1\n");
		fileWriter.format("\tj %s\n", len1Start);
		fileWriter.format("%s:\n", len1End);

		// --- Compute strlen(str2) -> $a2 ---
		String len2Start = ir.IrCommand.getFreshLabel("StrLen2");
		String len2End = ir.IrCommand.getFreshLabel("StrLen2End");
		fileWriter.format("\tli $a2,0\n");
		fileWriter.format("\tmove $v1,$t%d\n", s2Idx);
		fileWriter.format("%s:\n", len2Start);
		fileWriter.format("\tlb $a3,0($v1)\n");
		fileWriter.format("\tbeq $a3,$zero,%s\n", len2End);
		fileWriter.format("\taddu $a2,$a2,1\n");
		fileWriter.format("\taddu $v1,$v1,1\n");
		fileWriter.format("\tj %s\n", len2Start);
		fileWriter.format("%s:\n", len2End);

		// --- Allocate len1 + len2 + 1 bytes via sbrk ---
		fileWriter.format("\taddu $a0,$a1,$a2\n");
		fileWriter.format("\taddu $a0,$a0,1\n"); // +1 for null terminator
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		// $v0 now has the new buffer pointer
		fileWriter.format("\tmove $t%d,$v0\n", dstIdx);

		// --- Copy str1 into new buffer ---
		String cp1Start = ir.IrCommand.getFreshLabel("StrCp1");
		String cp1End = ir.IrCommand.getFreshLabel("StrCp1End");
		fileWriter.format("\tmove $v1,$t%d\n", s1Idx); // src = str1
		fileWriter.format("\tmove $a0,$t%d\n", dstIdx); // dest = new buffer
		fileWriter.format("%s:\n", cp1Start);
		fileWriter.format("\tlb $a3,0($v1)\n");
		fileWriter.format("\tbeq $a3,$zero,%s\n", cp1End);
		fileWriter.format("\tsb $a3,0($a0)\n");
		fileWriter.format("\taddu $v1,$v1,1\n");
		fileWriter.format("\taddu $a0,$a0,1\n");
		fileWriter.format("\tj %s\n", cp1Start);
		fileWriter.format("%s:\n", cp1End);

		// --- Copy str2 into buffer (continuing from where str1 ended) ---
		String cp2Start = ir.IrCommand.getFreshLabel("StrCp2");
		String cp2End = ir.IrCommand.getFreshLabel("StrCp2End");
		fileWriter.format("\tmove $v1,$t%d\n", s2Idx); // src = str2
		// $a0 already points to end of str1 in buffer
		fileWriter.format("%s:\n", cp2Start);
		fileWriter.format("\tlb $a3,0($v1)\n");
		fileWriter.format("\tbeq $a3,$zero,%s\n", cp2End);
		fileWriter.format("\tsb $a3,0($a0)\n");
		fileWriter.format("\taddu $v1,$v1,1\n");
		fileWriter.format("\taddu $a0,$a0,1\n");
		fileWriter.format("\tj %s\n", cp2Start);
		fileWriter.format("%s:\n", cp2End);

		// --- Null terminate ---
		fileWriter.format("\tsb $zero,0($a0)\n");
	}

	/****************************************/
	/* Load a field from an object */
	/* dst := base[byteOffset] */
	/****************************************/
	public void fieldLoad(Temp dst, Temp base, int byteOffset) {
		int dstIdx = dst.getSerialNumber();
		int baseIdx = base.getSerialNumber();
		// Null pointer check
		String ok = ir.IrCommand.getFreshLabel("NullOk");
		fileWriter.format("\tbne $t%d,$zero,%s\n", baseIdx, ok);
		fileWriter.format("\tla $a0,string_invalid_ptr_dref\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", ok);
		fileWriter.format("\tlw $t%d,%d($t%d)\n", dstIdx, byteOffset, baseIdx);
	}

	/****************************************/
	/* Store a field in an object */
	/* base[byteOffset] := src */
	/****************************************/
	public void fieldStore(Temp base, int byteOffset, Temp src) {
		int baseIdx = base.getSerialNumber();
		int srcIdx = src.getSerialNumber();
		// Null pointer check
		String ok = ir.IrCommand.getFreshLabel("NullOk");
		fileWriter.format("\tbne $t%d,$zero,%s\n", baseIdx, ok);
		fileWriter.format("\tla $a0,string_invalid_ptr_dref\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("%s:\n", ok);
		fileWriter.format("\tsw $t%d,%d($t%d)\n", srcIdx, byteOffset, baseIdx);
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
		fileWriter.print("\n");
		fileWriter.print("\t.align 2\n");
		for (java.util.Map.Entry<String, java.util.List<String>> entry : vtables.entrySet()) {
			fileWriter.format("vtable_%s:\n", entry.getKey());
			if (entry.getValue().isEmpty()) {
				fileWriter.print("\t.word 0\n");
			} else {
				for (String method : entry.getValue()) {
					fileWriter.format("\t.word %s\n", method);
				}
			}
			fileWriter.print("\n");
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
	public void callFunc(String name, java.util.List<Temp> args, Temp retDst) {
		// Save caller-save registers (not implemented fully here, assuming simple reg
		// alloc)

		// Push arguments to stack (right-to-left)
		for (int i = args.size() - 1; i >= 0; i--) {
			int argIdx = args.get(i).getSerialNumber();
			fileWriter.format("\t\t# push arg %d\n", i);
			fileWriter.format("\tsub $sp,$sp,4\n");
			fileWriter.format("\tsw $t%d,0($sp)\n", argIdx);
		}

		// Call function
		fileWriter.format("\tjal %s\n", name);

		// Pop arguments from stack
		if (args.size() > 0) {
			fileWriter.format("\tadd $sp,$sp,%d\n", args.size() * 4);
		}

		// Move return value from $v0 to destination register
		if (retDst != null) {
			int retIdx = retDst.getSerialNumber();
			fileWriter.format("\tmove $t%d,$v0\n", retIdx);
		}
	}

	public void virtualCall(Temp objBase, int methodOffset, java.util.List<Temp> args, Temp retDst) {
		int baseIdx = objBase.getSerialNumber();

		// Null check object
		fileWriter.format("\t\t# virtual method call, null check\n");
		fileWriter.format("\tbeq $t%d,$zero,error_null_ptr\n", baseIdx);

		// Push arguments to stack (right-to-left)
		// Last argument first. Object base is technically the first (implicit) argument
		// 'this'.
		for (int i = args.size() - 1; i >= 0; i--) {
			int argIdx = args.get(i).getSerialNumber();
			fileWriter.format("\t\t# push arg %d\n", i);
			fileWriter.format("\tsub $sp,$sp,4\n");
			fileWriter.format("\tsw $t%d,0($sp)\n", argIdx);
		}

		// Push 'this' as the first argument
		fileWriter.format("\t\t# push 'this'\n");
		fileWriter.format("\tsub $sp,$sp,4\n");
		fileWriter.format("\tsw $t%d,0($sp)\n", baseIdx);

		// Load vtable address from objBase[0]
		fileWriter.format("\t\t# load vtable ptr\n");
		fileWriter.format("\tlw $a0,0($t%d)\n", baseIdx);

		// Load method address from vtable[methodOffset]
		fileWriter.format("\t\t# load method address\n");
		fileWriter.format("\tlw $t0,%d($a0)\n", methodOffset);

		// Call function via register
		fileWriter.format("\tjalr $t0\n");

		// Pop arguments from stack ('this' + explicit args)
		int totalArgs = args.size() + 1;
		fileWriter.format("\tadd $sp,$sp,%d\n", totalArgs * 4);

		// Move return value from $v0 to destination register
		if (retDst != null) {
			int retIdx = retDst.getSerialNumber();
			fileWriter.format("\tmove $t%d,$v0\n", retIdx);
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

	private java.util.Map<String, java.util.List<String>> vtables = new java.util.LinkedHashMap<>();

	public void emitVTable(String className, java.util.List<String> methods) {
		vtables.put(className, methods);
	}
}
