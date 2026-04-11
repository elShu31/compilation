package regalloc;

import temp.Temp;
import java.util.*;

public class RegisterAllocator {
    public static final int K = 10;
    private static final String[] COLORS = {
            "$t0", "$t1", "$t2", "$t3", "$t4",
            "$t5", "$t6", "$t7", "$t8", "$t9"
    };

    public static Map<Integer, String> allocationMap = new HashMap<>();

    public static void allocateRegisters(InterferenceGraph ig) {
        Map<Temp, Set<Temp>> graph = new HashMap<>();
        Map<Temp, Set<Temp>> originalGraph = new HashMap<>();

        // Deep copy the graph
        for (Temp t : ig.getNodes()) {
            Set<Temp> neighbors = new HashSet<>(ig.getNeighbors(t));
            graph.put(t, neighbors);
            originalGraph.put(t, new HashSet<>(neighbors));
        }

        Stack<Temp> stack = new Stack<>();

        // 1. Simplify
        while (!graph.isEmpty()) {
            Temp toRemove = null;
            for (Map.Entry<Temp, Set<Temp>> entry : graph.entrySet()) {
                if (entry.getValue().size() < K) {
                    toRemove = entry.getKey();
                    break;
                }
            }

            if (toRemove == null) {
                // Spill needed
                throw new RegisterAllocationException("Register Allocation Failed");
            }

            stack.push(toRemove);
            Set<Temp> neighbors = graph.get(toRemove);
            graph.remove(toRemove);

            for (Temp neighbor : neighbors) {
                if (graph.containsKey(neighbor)) {
                    graph.get(neighbor).remove(toRemove);
                }
            }
        }

        // 2. Select (Coloring)
        Map<Temp, Integer> colorMap = new HashMap<>();

        while (!stack.isEmpty()) {
            Temp t = stack.pop();
            Set<Integer> usedColors = new HashSet<>();

            for (Temp neighbor : originalGraph.get(t)) {
                if (colorMap.containsKey(neighbor)) {
                    usedColors.add(colorMap.get(neighbor));
                }
            }

            int assignedColor = -1;
            for (int c = 0; c < K; c++) {
                if (!usedColors.contains(c)) {
                    assignedColor = c;
                    break;
                }
            }

            if (assignedColor == -1) {
                throw new RegisterAllocationException("Register Allocation Failed");
            }

            colorMap.put(t, assignedColor);
            allocationMap.put(t.getSerialNumber(), COLORS[assignedColor]);
        }
    }

    public static String getReg(Temp t) {
        if (allocationMap.containsKey(t.getSerialNumber())) {
            return allocationMap.get(t.getSerialNumber());
        }
        return "Temp_" + t.getSerialNumber();
    }
}
