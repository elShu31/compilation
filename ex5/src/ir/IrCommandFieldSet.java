package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

/**
 * IR command to store a value into an object field.
 * base[byteOffset] := src
 */
public class IrCommandFieldSet extends IrCommand {
    public Temp base;
    public int byteOffset;
    public Temp src;

    public IrCommandFieldSet(Temp base, int byteOffset, Temp src) {
        this.base = base;
        this.byteOffset = byteOffset;
        this.src = src;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (base != null)
            uses.add(base);
        if (src != null)
            uses.add(src);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().fieldStore(base, byteOffset, src);
    }
}
