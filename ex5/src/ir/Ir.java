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

		// Output commands inside .text
		for (IrCommand cmd : commands) {
			cmd.mipsMe();
		}
	}
}
