package ast;

import types.*;
import symboltable.*;
import java.util.*;

public class AstDecClass extends AstNode{
    public String id;
    public String parentId; // can be null
    public AstFieldList fields;

    public AstDecClass(String id, String parentId, AstFieldList fields, int lineNumber){
        serialNumber = AstNodeSerialNumber.getFresh();
        this.id = id;
        this.parentId = parentId;
        this.fields = fields;
        this.lineNumber = lineNumber;
    }

    public void printMe(){
        System.out.format("AST CLASS DEC NODE: %s\n", id);
        if (parentId != null) {
            System.out.format("EXTENDS: %s\n", parentId);
        }
        if (fields != null) fields.printMe();

        String label = (parentId != null) ?
            String.format("CLASS\n%s\nEXTENDS %s", id, parentId) :
            String.format("CLASS\n%s", id);

        AstGraphviz.getInstance().logNode(serialNumber, label);

        if (fields != null) AstGraphviz.getInstance().logEdge(serialNumber, fields.serialNumber);
    }

    public Type semantMe() throws SemanticException
	{
		TypeClass parentClass = null;

		/************************************/
		/* [0a] Check for reserved keyword  */
		/************************************/
		TypeUtils.checkNotReservedKeyword(id, lineNumber);

		/**************************************/
		/* [0b] Check if class name already exists */
		/**************************************/
		if (SymbolTable.getInstance().find(id) != null)
		{
			throw new SemanticException("class " + id + " already exists", lineNumber);
		}

		/**************************************/
		/* [1] Check if parent class exists   */
		/**************************************/
		if (parentId != null)
		{
			Type parentType = SymbolTable.getInstance().find(parentId);
			if (parentType == null)
			{
				throw new SemanticException("parent class " + parentId + " does not exist", lineNumber);
			}
			if (!parentType.isClass())
			{
				throw new SemanticException("parent " + parentId + " is not a class", lineNumber);
			}
			parentClass = (TypeClass) parentType;

			/**************************************/
			/* [2] Check for circular inheritance */
			/**************************************/
			if (checkCircularInheritance(id, parentClass))
			{
				throw new SemanticException("circular inheritance detected for class " + id, lineNumber);
			}
		}

		/********************************************************/
		/* [3] Create a placeholder class type and register it */
		/*     This allows self-referential fields (e.g., IntList tail) */
		/********************************************************/
		TypeClass classType = new TypeClass(parentClass, id, null);
		SymbolTable.getInstance().enter(id, classType);

		/********************************************************/
		/* [4] Build class members list (fields and methods)   */
		/*     Check for overloading, overriding, shadowing    */
		/********************************************************/
		TypeList classMembers = null;
		Set<String> memberNames = new HashSet<>();
		List<AstDecFunc> methodsToProcess = new ArrayList<>();

		for (AstFieldList it = fields; it != null; it = it.tail)
		{
			if (it.head.decVar != null)
			{
				// Field variable
				AstDecVar fieldVar = it.head.decVar;

				// Check if name already exists in current class
				if (memberNames.contains(fieldVar.id))
				{
					throw new SemanticException("duplicate field " + fieldVar.id + " in class " + id, fieldVar.lineNumber);
				}

				// Check for shadowing - field can't have same name as ANY inherited member
				Type inheritedMember = findInParent(parentClass, fieldVar.id);
				if (inheritedMember != null)
				{
					throw new SemanticException("field " + fieldVar.id + " shadows inherited member", fieldVar.lineNumber);
				}

				memberNames.add(fieldVar.id);

				// Check that field type exists
				Type fieldType = SymbolTable.getInstance().find(fieldVar.type.typeName);
				if (fieldType == null)
				{
					throw new SemanticException("non existing type " + fieldVar.type.typeName, fieldVar.lineNumber);
				}

				// Check that field type is not void
				if (fieldType instanceof TypeVoid)
				{
					throw new SemanticException("field cannot have void type", fieldVar.lineNumber);
				}

				TypeField newField = new TypeField(fieldType, fieldVar.id);
				
				// Apply optional assignments
				if (fieldVar.exp != null) {
				    Type expType = fieldVar.exp.semantMe();
				    if (!TypeUtils.canAssignType(fieldType, expType)) {
				        throw new SemanticException("type mismatch in initial value assignment for " + fieldVar.id, fieldVar.lineNumber);
				    }
				    newField.initialValue = fieldVar.exp;
				}

				classMembers = new TypeList(newField, classMembers);
			}
			else if (it.head.decFunc != null)
			{
				// Method
				AstDecFunc method = it.head.decFunc;

				// Check if method name already exists in current class
				// (catches duplicate methods and field-method name conflicts)
				if (memberNames.contains(method.funcName))
				{
					throw new SemanticException("duplicate member name: " + method.funcName, method.lineNumber);
				}

				memberNames.add(method.funcName);

				// Save method for processing later (after class is registered)
				methodsToProcess.add(method);

				// Build method type from signature
				// We need to validate types exist here because we use them for overriding checks
				Type retType = SymbolTable.getInstance().find(method.returnType.typeName);
				if (retType == null)
				{
					throw new SemanticException("non existing return type " + method.returnType.typeName, method.lineNumber);
				}

				// Build parameter type list in correct order
				TypeList paramTypes = TypeUtils.buildParameterTypeList(method.params, lineNumber);

				TypeFunction methodType = new TypeFunction(retType, method.funcName, paramTypes);

				// Check for overriding or shadowing
				Type inheritedMember = findInParent(parentClass, method.funcName);
				if (inheritedMember != null)
				{
					// Check if inherited member is a field (shadowing not allowed)
					if (inheritedMember instanceof TypeField)
					{
						throw new SemanticException("method " + method.funcName + " shadows inherited field", method.lineNumber);
					}

					// It's a method - validate override signature matches exactly
					if (inheritedMember instanceof TypeFunction)
					{
						TypeFunction inheritedMethod = (TypeFunction) inheritedMember;

						// Check return type matches
						if (inheritedMethod.returnType != retType)
						{
							throw new SemanticException("method " + method.funcName + " override has different return type", method.lineNumber);
						}

						// Check parameter types match exactly (same types, same order)
						if (!parameterListsMatch(inheritedMethod.params, paramTypes))
						{
							throw new SemanticException("method " + method.funcName + " override has different parameter types", method.lineNumber);
						}

						// Override is valid - signature matches exactly
					}
				}

				classMembers = new TypeList(methodType, classMembers);
			}
		}

		/************************************************/
		/* [5] Update the class type with members      */
		/************************************************/
		classType.dataMembers = classMembers;

		/************************************************/
		/* [5a] Compute field offsets and class size    */
		/************************************************/
		computeFieldOffsets(classType);

		/************************************************/
		/* [5b] Compute method offsets and vtable       */
		/************************************************/
		computeMethodOffsets(classType);

		/************************************************/
		/* [5c] Add class to global registry for MIPS   */
		/************************************************/
		TypeClass.allClasses.add(classType);

		/********************************************************/
		/* [5d] Now process method bodies in class context      */
		/*     Members can only reference earlier-defined members */
		/********************************************************/
		SymbolTable.getInstance().beginScope();

		// Add inherited members to scope (all inherited members are visible)
		if (parentClass != null)
		{
			addInheritedMembersToScope(parentClass);
		}

		// Process fields and methods in order, adding each to scope before processing next
		// This ensures members can only reference earlier-defined members
		int methodIndex = 0;
		for (AstFieldList it = fields; it != null; it = it.tail)
		{
			if (it.head.decVar != null)
			{
				// Field - add to scope (type already validated above)
				AstDecVar fieldVar = it.head.decVar;
				Type member = TypeUtils.findMemberInClassHierarchy(classType, fieldVar.id);
				SymbolTable.getInstance().enter(fieldVar.id, member);
			}
			else if (it.head.decFunc != null)
			{
				// Method - process body, then add to scope
				AstDecFunc method = methodsToProcess.get(methodIndex++);
				method.semantMe(true);

				// Add method to scope after processing (for later methods to reference)
				Type member = TypeUtils.findMemberInClassHierarchy(classType, method.funcName);
				SymbolTable.getInstance().enter(method.funcName, member);
			}
		}

		SymbolTable.getInstance().endScope();

		/*********************************************************/
		/* [6] Return value is irrelevant for class declarations */
		/*********************************************************/
		return null;
	}

	/*********************************************************/
	/* IR generation for class declarations                  */
	/* Emits IR for all methods in the class                 */
	/*********************************************************/
	public temp.Temp irMe()
	{
		for (AstFieldList it = fields; it != null; it = it.tail)
		{
			if (it.head.decFunc != null)
			{
				AstDecFunc method = it.head.decFunc;
				// Temporarily prefix the function name with the class name
				// so that IrCommandLabel, Prologue, and Epilogue generate CorrectName
				String originalName = method.funcName;
				method.funcName = this.id + "_" + originalName;
				
				method.irMe();
				
				// Restore the original name 
				method.funcName = originalName;
			}
		}
		return null;
	}

	/******************************************************************/
	/* Helper: Check for circular inheritance                        */
	/******************************************************************/
	private boolean checkCircularInheritance(String className, TypeClass parent)
	{
		TypeClass current = parent;
		while (current != null)
		{
			if (current.name.equals(className))
			{
				return true;
			}
			current = current.father;
		}
		return false;
	}

	/******************************************************************/
	/* Helper: Find a member in parent class hierarchy               */
	/******************************************************************/
	private Type findInParent(TypeClass parentClass, String memberName)
	{
		if (parentClass == null)
		{
			return null;
		}

		for (TypeList it = parentClass.dataMembers; it != null; it = it.tail)
		{
			if (it.head.name.equals(memberName))
			{
				return it.head;
			}
		}

		// Recursively search in parent's parent
		return findInParent(parentClass.father, memberName);
	}

	/******************************************************************/
	/* Helper: Check if two parameter lists match exactly            */
	/******************************************************************/
	private boolean parameterListsMatch(TypeList list1, TypeList list2)
	{
		// Both null - match
		if (list1 == null && list2 == null)
		{
			return true;
		}

		// One null, one not - no match
		if (list1 == null || list2 == null)
		{
			return false;
		}

		// Check if head types match
		if (list1.head != list2.head)
		{
			return false;
		}

		// Recursively check tails
		return parameterListsMatch(list1.tail, list2.tail);
	}

	/******************************************************************/
	/* Helper: Add all inherited members to scope recursively        */
	/******************************************************************/
	private void addInheritedMembersToScope(TypeClass parentClass)
	{
		if (parentClass == null)
		{
			return;
		}

		// First add grandparent's members (so they can be overridden)
		addInheritedMembersToScope(parentClass.father);

		// Then add parent's members
		for (TypeList it = parentClass.dataMembers; it != null; it = it.tail)
		{
			SymbolTable.getInstance().enter(it.head.name, it.head);
		}
	}

	/******************************************************************/
	/* Helper: Compute byte offsets for fields in the class layout    */
	/* Layout: [vtable_ptr (4 bytes)] [inherited fields] [own fields] */
	/* Offset 0 is reserved for a future vtable pointer.             */
	/******************************************************************/
	private void computeFieldOffsets(TypeClass classType)
	{
		// Count inherited fields by walking parent chain
		int inheritedFieldCount = countInheritedFields(classType.father);

		// Base offset = 4 (vtable ptr) + inherited fields * 4
		int currentOffset = 4 + inheritedFieldCount * 4;

		// Assign offsets to own fields (dataMembers contains both fields and methods)
		// dataMembers is built in reverse order (prepended), so we need to
		// collect fields in declaration order first
		java.util.List<TypeField> ownFields = new java.util.ArrayList<>();
		for (TypeList it = classType.dataMembers; it != null; it = it.tail)
		{
			if (it.head instanceof TypeField)
			{
				ownFields.add((TypeField) it.head);
			}
		}

		// Reverse to get declaration order (since list was built by prepending)
		java.util.Collections.reverse(ownFields);

		// Assign offsets
		for (TypeField field : ownFields)
		{
			field.offset = currentOffset;
			currentOffset += 4;
		}

		// Set total class size
		classType.classSize = currentOffset;
	}

	/******************************************************************/
	/* Helper: Count total number of fields in parent class chain    */
	/******************************************************************/
	private int countInheritedFields(TypeClass parentClass)
	{
		if (parentClass == null)
		{
			return 0;
		}

		int count = 0;
		for (TypeList it = parentClass.dataMembers; it != null; it = it.tail)
		{
			if (it.head instanceof TypeField)
			{
				count++;
			}
		}
		return count + countInheritedFields(parentClass.father);
	}

	/******************************************************************/
	/* Helper: Compute the VTable layout and method offsets           */
	/******************************************************************/
	private void computeMethodOffsets(TypeClass classType)
	{
		classType.vtable = new java.util.ArrayList<>();
		
		// 1. Copy parent's vtable to inherit existing layouts
		if (classType.father != null && classType.father.vtable != null)
		{
			classType.vtable.addAll(classType.father.vtable);
		}

		// 2. Extract strictly methods from own dataMembers 
		// (remember dataMembers is prepended, so it's reversed relative to declaration)
		java.util.List<TypeFunction> ownMethods = new java.util.ArrayList<>();
		for (TypeList it = classType.dataMembers; it != null; it = it.tail)
		{
			if (it.head instanceof TypeFunction)
			{
				ownMethods.add((TypeFunction) it.head);
			}
		}

		// Restore declaration order
		java.util.Collections.reverse(ownMethods);

		// 3. Overlay own methods onto the vtable
		for (TypeFunction method : ownMethods)
		{
			int existingIndex = -1;
			// Find existing method with the exact same name (Overriding)
			for (int i = 0; i < classType.vtable.size(); i++)
			{
				if (classType.vtable.get(i).name.equals(method.name))
				{
					existingIndex = i;
					break;
				}
			}

			if (existingIndex != -1)
			{
				// Override: Assign the original index/offset, and replace in vtable
				method.offset = existingIndex * 4;
				method.originClass = classType.name; // This is the implementation for this class
				classType.vtable.set(existingIndex, method);
			}
			else
			{
				// New Method: Append at the end of the vtable
				method.offset = classType.vtable.size() * 4;
				method.originClass = classType.name; // Initial implementation
				classType.vtable.add(method);
			}
		}

		// 4. Debug Print (Allows automated and visual testing without executing MIPS)
		System.out.println("[VTABLE LAYOUT] Class: " + classType.name);
		for (TypeFunction method : classType.vtable)
		{
			System.out.println("[VTABLE LAYOUT] Offset: " + method.offset + ", Method: " + method.name);
		}
	}
}