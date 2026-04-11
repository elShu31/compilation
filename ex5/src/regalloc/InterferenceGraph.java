package regalloc;

import cfg.CFG;
import temp.Temp;
import java.util.*;

public class InterferenceGraph {
    private Map<Temp, Set<Temp>> graph;

    public InterferenceGraph(CFG cfg, LivenessAnalyzer liveness) {
        graph = new HashMap<>();

        // Add all temps that ever appear in IN/OUT/DEF sets as nodes
        for (int i = 0; i < cfg.size(); i++) {
            for (Temp t : liveness.getIn(i)) {
                addNode(t);
            }
            for (Temp t : liveness.getOut(i)) {
                addNode(t);
            }
            for (Temp t : cfg.getCommand(i).getDefinedTemps()) {
                addNode(t);
            }
        }

        // Build edges based on Def + Out[i]
        for (int i = 0; i < cfg.size(); i++) {
            List<Temp> liveTemp = new ArrayList<>(liveness.getOut(i));
            for (Temp d : cfg.getCommand(i).getDefinedTemps()) {
                if (!liveTemp.contains(d)) {
                    liveTemp.add(d);
                }
            }

            for (int j = 0; j < liveTemp.size(); j++) {
                for (int k = j + 1; k < liveTemp.size(); k++) {
                    addEdge(liveTemp.get(j), liveTemp.get(k));
                }
            }
        }
    }

    private void addNode(Temp t) {
        graph.putIfAbsent(t, new HashSet<>());
    }

    private void addEdge(Temp t1, Temp t2) {
        if (t1.equals(t2))
            return;
        graph.get(t1).add(t2);
        graph.get(t2).add(t1);
    }

    public Set<Temp> getNeighbors(Temp t) {
        return graph.getOrDefault(t, Collections.emptySet());
    }

    public Set<Temp> getNodes() {
        return graph.keySet();
    }
}
