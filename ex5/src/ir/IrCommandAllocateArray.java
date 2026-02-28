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

public class IrCommandAllocateArray extends IrCommand {
    public Temp dst;
    public Temp size;

    public IrCommandAllocateArray(Temp dst, Temp size) {
        this.dst = dst;
        this.size = size;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().allocateArray(dst, size);
    }
}
