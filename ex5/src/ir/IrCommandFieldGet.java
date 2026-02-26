package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

/**
 * IR command to load an object field.
 * dst := base[byteOffset]
 */
public class IrCommandFieldGet extends IrCommand {
    public Temp dst;
    public Temp base;
    public int byteOffset;

    public IrCommandFieldGet(Temp dst, Temp base, int byteOffset) {
        this.dst = dst;
        this.base = base;
        this.byteOffset = byteOffset;
    }

    @Override
    public List<Temp> getDef() {
        List<Temp> defs = new ArrayList<>();
        if (dst != null)
            defs.add(dst);
        return defs;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (base != null)
            uses.add(base);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().fieldLoad(dst, base, byteOffset);
    }
}
