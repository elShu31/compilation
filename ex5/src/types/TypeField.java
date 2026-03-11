package types;

/**
 * Represents a field (data member) in a class.
 * Stores both the field's type and its name.
 */
public class TypeField extends Type
{
	/***********************************/
	/* The type of the field           */
	/***********************************/
	public Type fieldType;
	
	/***********************************/
	/* The memory offset of the field  */
	/***********************************/
	public int offset;
	
	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeField(Type fieldType, String name, int offset)
	{
		this.name = name;
		this.fieldType = fieldType;
		this.offset = offset;
	}
}

