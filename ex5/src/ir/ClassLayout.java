package ir;

import types.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to compute class memory layout.
 * Fields are laid out contiguously, 4 bytes each.
 * Inherited fields come first (in declaration order),
 * then the class's own fields.
 */
public class ClassLayout {

    /**
     * Get all fields of a class (including inherited), in memory order.
     * Parent fields first, then own fields.
     */
    public static List<String> getFieldsInOrder(TypeClass tc) {
        List<String> fields = new ArrayList<>();
        collectFields(tc, fields);
        return fields;
    }

    private static void collectFields(TypeClass tc, List<String> fields) {
        if (tc == null)
            return;
        collectFields(tc.father, fields);

        // Members are stored in reverse order in TypeList
        List<Type> ownMembers = new ArrayList<>();
        for (TypeList it = tc.dataMembers; it != null; it = it.tail) {
            ownMembers.add(it.head);
        }

        for (int i = ownMembers.size() - 1; i >= 0; i--) {
            Type m = ownMembers.get(i);
            if (m instanceof TypeField) {
                if (!fields.contains(m.name)) {
                    fields.add(m.name);
                }
            }
        }
    }

    /**
     * Get the byte offset of a field in a class.
     * Offset 0 is the vtable pointer. Fields start at offset 4.
     */
    public static int getFieldOffset(TypeClass tc, String fieldName) {
        List<String> fields = getFieldsInOrder(tc);
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).equals(fieldName)) {
                return (i + 1) * 4; // +1 for vptr
            }
        }
        return -1;
    }

    /**
     * Get total object size in bytes (vptr + fields).
     */
    public static int getObjectSize(TypeClass tc) {
        return (getFieldsInOrder(tc).size() + 1) * 4;
    }

    /**
     * Represents a method in a vtable, tracking its name and the class that defined
     * it.
     */
    public static class MethodEntry {
        public String name;
        public TypeClass definingClass;

        public MethodEntry(String name, TypeClass definingClass) {
            this.name = name;
            this.definingClass = definingClass;
        }
    }

    /**
     * Get all methods of a class in vtable order.
     * Parent methods first (overridden properly), then new methods.
     */
    public static List<MethodEntry> getMethodsInOrder(TypeClass tc) {
        List<MethodEntry> vtable = new ArrayList<>();
        collectMethods(tc, vtable);
        return vtable;
    }

    private static void collectMethods(TypeClass tc, List<MethodEntry> vtable) {
        if (tc == null)
            return;
        collectMethods(tc.father, vtable);

        List<Type> ownMembers = new ArrayList<>();
        for (TypeList it = tc.dataMembers; it != null; it = it.tail) {
            ownMembers.add(it.head);
        }

        for (int i = ownMembers.size() - 1; i >= 0; i--) {
            Type m = ownMembers.get(i);
            if (m instanceof TypeFunction) {
                String methodName = m.name;

                // Check if overriding existing method
                boolean overridden = false;
                for (int j = 0; j < vtable.size(); j++) {
                    if (vtable.get(j).name.equals(methodName)) {
                        vtable.set(j, new MethodEntry(methodName, tc));
                        overridden = true;
                        break;
                    }
                }

                if (!overridden) {
                    vtable.add(new MethodEntry(methodName, tc));
                }
            }
        }
    }

    /**
     * Get the byte offset of a method in the vtable.
     */
    public static int getMethodOffset(TypeClass tc, String methodName) {
        List<MethodEntry> vtable = getMethodsInOrder(tc);
        for (int i = 0; i < vtable.size(); i++) {
            if (vtable.get(i).name.equals(methodName)) {
                return i * 4;
            }
        }
        return -1;
    }
}
