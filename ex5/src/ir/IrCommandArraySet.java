package ir;

import mips.MipsGenerator;
import temp.Temp;

import java.util.ArrayList;
import java.util.List;

public class IrCommandArraySet extends IrCommand {
    public Temp base;
    public Temp index;
    public Temp value;

    public IrCommandArraySet(Temp base, Temp index, Temp value) {
        this.base = base;
        this.index = index;
        this.value = value;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (base != null)
            uses.add(base);
        if (index != null)
            uses.add(index);
        if (value != null)
            uses.add(value);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().arrayStore(base, index, value);
    }
}
