package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

/**
 * IR command to print a string value (address in a temp register).
 * Calls MipsGenerator.printString() which uses syscall 4.
 */
public class IrCommandPrintString extends IrCommand {
    public Temp src;

    public IrCommandPrintString(Temp src) {
        this.src = src;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (src != null)
            uses.add(src);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().printString(src);
    }
}
