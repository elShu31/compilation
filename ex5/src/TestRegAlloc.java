import java.util.*;
import ir.*;
import cfg.*;
import dataflow.*;
import regalloc.*;
import temp.*;
import java.io.*;
import java_cup.runtime.Symbol;
import ast.*;

public class TestRegAlloc {
    public static void main(String[] args) throws Exception {
        FileReader fileReader = new FileReader("../input/Input.txt");
        Lexer l = new Lexer(fileReader);
        Parser p = new Parser(l);
        AstDecList ast = (AstDecList) p.parse().value;
        ast.semantMe();
        ast.irMe();

        CFG cfg = new CFG(Ir.getInstance().getCommands());
        LivenessAnalysis la = new LivenessAnalysis(cfg);
        la.analyze();
        InterferenceGraph ig = new InterferenceGraph(cfg, la);

        System.out.println("Graph has " + ig.getNodes().size() + " nodes");

        // Print out all commands, their defs, uses, and the IN states
        for (int i = 0; i < cfg.size(); i++) {
            IrCommand cmd = cfg.getCommand(i);
            LivenessState state = la.getInState(i);

            System.out.print("[" + i + "] " + cmd.getClass().getSimpleName() + " DEFS:");
            for (Temp t : cmd.getDef())
                System.out.print(" t" + t.getSerialNumber());
            System.out.print(" USES:");
            for (Temp t : cmd.getUse())
                System.out.print(" t" + t.getSerialNumber());

            System.out.print(" | LIVE IN:");
            for (Temp t : state.liveTemps)
                System.out.print(" t" + t.getSerialNumber());
            System.out.println();
        }
    }
}
