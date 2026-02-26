package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

/**
 * IR command to load the address of a string constant into a temp.
 * The string is registered in MipsGenerator's .data section.
 */
public class IRcommandConstString extends IrCommand {
    public Temp dst;
    public String label; // label in .data section

    public IRcommandConstString(Temp dst, String value) {
        this.dst = dst;
        // Register the string constant and get its label
        this.label = MipsGenerator.getInstance().addStringConstant(value);
    }

    @Override
    public List<Temp> getDef() {
        List<Temp> defs = new ArrayList<>();
        if (dst != null)
            defs.add(dst);
        return defs;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().loadStringConstant(dst, label);
    }
}
