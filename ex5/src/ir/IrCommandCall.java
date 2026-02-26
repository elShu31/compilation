package ir;

import java.util.ArrayList;
import java.util.List;
import temp.Temp;
import mips.MipsGenerator;

public class IrCommandCall extends IrCommand {
    public String funcName;
    public List<Temp> args;
    public Temp retDst;

    public IrCommandCall(String funcName, List<Temp> args, Temp retDst) {
        this.funcName = funcName;
        this.args = args;
        this.retDst = retDst;
    }

    @Override
    public List<Temp> getUse() {
        return new ArrayList<>(args);
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
        MipsGenerator.getInstance().callFunc(funcName, args, retDst);
    }
}
