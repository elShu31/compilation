/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayList;
import java.util.List;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;
import cfg.*;
import regalloc.*;

public class Ir {
	private List<IrCommand> commands = new ArrayList<>();

	/****************************************/
	/* Store strings for .data section */
	/****************************************/
	private List<String> stringTable = new ArrayList<>();

	/******************/
	/* Add Ir command */
	/******************/
	public void AddIrCommand(IrCommand cmd) {
		commands.add(cmd);
	}

	/****************************************/
	/* Get all IR commands for analysis */
	/****************************************/
	public List<IrCommand> getCommands() {
		return commands;
	}

	/****************************************/
	/* Add a string to the string pool */
	/* Returns its assigned data label */
	/****************************************/
	public String addString(String value) {
		int index = stringTable.size();
		stringTable.add(value);
		return "string_const_" + index;
	}

	/****************************************/
	/* Get the number of IR commands */
	/****************************************/
	public int size() {
		return commands.size();
	}

	/****************************************/
	/* Reset the IR (for testing purposes) */
	/****************************************/
	public void reset() {
		commands.clear();
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static Ir instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected Ir() {
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static Ir getInstance() {
		if (instance == null) {
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new Ir();
		}
		return instance;
	}

	public void mipsMe() {
		// Output .data section strings first
		MipsGenerator.getInstance().emitStrings(stringTable);

		List<IrCommand> globalAllocations = new ArrayList<>();
		List<IrCommand> globalInitializations = new ArrayList<>();
		List<IrCommand> functionCommands = new ArrayList<>();

		boolean inGlobalContext = true;

		// Extract global allocations and initializations
		for (int i = 0; i < commands.size(); i++) {
			IrCommand cmd = commands.get(i);

			if (inGlobalContext && cmd instanceof IrCommandLabel && (i + 1 < commands.size())
					&& (commands.get(i + 1) instanceof IrCommandPrologue)) {
				inGlobalContext = false;
			}

			if (inGlobalContext) {
				if (cmd instanceof IrCommandAllocate) {
					globalAllocations.add(cmd);
				} else {
					globalInitializations.add(cmd);
				}
			} else {
				functionCommands.add(cmd);
			}
		}

		// First, do global allocations
		for (IrCommand cmd : globalAllocations) {
			cmd.mipsMe();
		}

		List<IrCommand> currentFunc = new ArrayList<>();
		boolean inFunc = false;

		// Output commands inside .text
		for (IrCommand cmd : functionCommands) {
			if (cmd instanceof IrCommandPrologue) {
				inFunc = true;
				currentFunc.clear();
				IrCommandPrologue pro = (IrCommandPrologue) cmd;
				currentFunc.add(cmd);
				if (pro.funcName.equals("main")) {
					currentFunc.addAll(globalInitializations);
				}
				continue;
			}

			if (inFunc) {
				currentFunc.add(cmd);
			} else {
				cmd.mipsMe();
			}

			if (cmd instanceof IrCommandEpilogue) {
				inFunc = false;

				// Run register allocation for the current function
				CFG funcCfg = new CFG(currentFunc);
				LivenessAnalyzer liveness = new LivenessAnalyzer(funcCfg);
				InterferenceGraph ig = new InterferenceGraph(funcCfg, liveness);

				RegisterAllocator.allocationMap.clear();
				RegisterAllocator.allocateRegisters(ig);

				// Now output the function commands with assigned registers
				for (IrCommand funcCmd : currentFunc) {
					funcCmd.mipsMe();
				}
			}
		}
	}
}
