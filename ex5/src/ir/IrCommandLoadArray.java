/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import mips.*;
import temp.*;

public class IrCommandLoadArray extends IrCommand {
    public Temp dst;
    public Temp arrayBase;
    public Temp index;

    public IrCommandLoadArray(Temp dst, Temp arrayBase, Temp index) {
        this.dst = dst;
        this.arrayBase = arrayBase;
        this.index = index;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().loadArray(dst, arrayBase, index);
    }
}
