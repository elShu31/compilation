package ir;

import java.util.ArrayList;
import java.util.List;
import temp.Temp;
import mips.MipsGenerator;

public class IrCommandVirtualCall extends IrCommand {
    public Temp objBase;
    public int methodOffset;
    public List<Temp> args;
    public Temp retDst;

    public IrCommandVirtualCall(Temp objBase, int methodOffset, List<Temp> args, Temp retDst) {
        this.objBase = objBase;
        this.methodOffset = methodOffset;
        this.args = args;
        this.retDst = retDst;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>(args);
        if (objBase != null)
            uses.add(objBase);
        return uses;
    }

    @Override
    public List<Temp> getDef() {
        List<Temp> defs = new ArrayList<>();
        if (retDst != null)
            defs.add(retDst);
        return defs;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().virtualCall(objBase, methodOffset, args, retDst);
    }
}
