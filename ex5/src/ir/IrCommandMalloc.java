package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

public class IrCommandMalloc extends IrCommand {
    public Temp dst;
    public Temp sizeBytes;

    public IrCommandMalloc(Temp dst, Temp sizeBytes) {
        this.dst = dst;
        this.sizeBytes = sizeBytes;
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
        if (sizeBytes != null)
            uses.add(sizeBytes);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().malloc(dst, sizeBytes);
    }
}
