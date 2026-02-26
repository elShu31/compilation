package ir;

import java.util.ArrayList;
import java.util.List;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandReturn extends IrCommand {
    public Temp retVal; // null for void return

    public IrCommandReturn(Temp retVal) {
        this.retVal = retVal;
    }

    @Override
    public List<Temp> getUse() {
        List<Temp> uses = new ArrayList<>();
        if (retVal != null)
            uses.add(retVal);
        return uses;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().returnFromFunc(retVal);
    }
}
