package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

/**
 * IR command to store array length at base address.
 * base[0] := length
 */
public class IrCommandStoreArrayLength extends IrCommand {
    public Temp base;
    public Temp length;

    public IrCommandStoreArrayLength(Temp base, Temp length) {
        this.base = base;
        this.length = length;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (base != null)
            uses.add(base);
        if (length != null)
            uses.add(length);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().storeArrayLength(base, length);
    }
}
