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
import temp.*;
import mips.*;

public class IrCommandPrintString extends IrCommand {
    public Temp t;

    public IrCommandPrintString(Temp t) {
        this.t = t;
    }

    public void mipsMe() {
        MipsGenerator.getInstance().printString(t);
    }
}
