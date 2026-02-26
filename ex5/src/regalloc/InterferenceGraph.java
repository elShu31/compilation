/***********/
/* PACKAGE */
/***********/
package regalloc;

import java.util.*;
import cfg.CFG;
import dataflow.LivenessAnalysis;
import dataflow.LivenessState;
import temp.Temp;

/**
 * Builds an interference graph based on liveness analysis.
 * Two Temps interfere if they are live at the same time.
 */
public class InterferenceGraph {
    // The graph: A map from Temp to its set of interfering Temps
    private Map<Temp, Set<Temp>> adjList;

    public InterferenceGraph(CFG cfg, LivenessAnalysis liveness) {
        adjList = new HashMap<>();
        buildGraph(cfg, liveness);
    }

    private void addNode(Temp t) {
        if (!adjList.containsKey(t)) {
            adjList.put(t, new HashSet<>());
        }
    }

    private void addEdge(Temp t1, Temp t2) {
        if (t1.equals(t2))
            return;
        addNode(t1);
        addNode(t2);
        adjList.get(t1).add(t2);
        adjList.get(t2).add(t1);
    }

    private void buildGraph(CFG cfg, LivenessAnalysis liveness) {
        // For each command, all temps in its IN state interfere with each other
        for (int i = 0; i < cfg.size(); i++) {
            LivenessState state = liveness.getInState(i);
            List<Temp> liveTemps = new ArrayList<>(state.liveTemps);

            // Add all live temps as nodes (even if degree 0)
            for (Temp t : liveTemps) {
                if (t != null)
                    addNode(t);
            }

            // Ensure defined and used temps are also present as nodes
            // even if they are never live. This way they still get colored!
            ir.IrCommand cmd = cfg.getCommand(i);
            for (Temp t : cmd.getDef()) {
                if (t != null)
                    addNode(t);
            }
            for (Temp t : cmd.getUse()) {
                if (t != null)
                    addNode(t);
            }

            // Create cliques of interfering temps
            for (int j = 0; j < liveTemps.size(); j++) {
                for (int k = j + 1; k < liveTemps.size(); k++) {
                    addEdge(liveTemps.get(j), liveTemps.get(k));
                }
            }
        }
    }

    public Set<Temp> getNodes() {
        return new HashSet<>(adjList.keySet());
    }

    public Set<Temp> getNeighbors(Temp t) {
        return adjList.getOrDefault(t, new HashSet<>());
    }

    public int getDegree(Temp t) {
        return getNeighbors(t).size();
    }

    public void printGraph() {
        System.out.println("=== Interference Graph ===");
        for (Map.Entry<Temp, Set<Temp>> entry : adjList.entrySet()) {
            System.out.print("Temp_" + entry.getKey().getSerialNumber() + " interferes with: ");
            for (Temp neighbor : entry.getValue()) {
                System.out.print("Temp_" + neighbor.getSerialNumber() + " ");
            }
            System.out.println();
        }
    }
}
