package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

public class IrCommandArrayGet extends IrCommand {
    public Temp dst;
    public Temp base;
    public Temp index;

    public IrCommandArrayGet(Temp dst, Temp base, Temp index) {
        this.dst = dst;
        this.base = base;
        this.index = index;
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
        if (index != null)
            uses.add(index);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().arrayLoad(dst, base, index);
    }
}
