package types;

public class TypeFunction extends Type
{
	/***********************************/
	/* The return type of the function */
	/***********************************/
	public Type returnType;

	/*************************/
	/* types of input params */
	/*************************/
	public TypeList params;

	/*************************************************/
	/* Byte offset of this method within the class's */
	/* virtual table layout. Computed during parsing */
	/*************************************************/
	public int offset = -1;

	/**************************************************/
	/* Name of the class where this method was        */
	/* originally defined or overridden.              */
	/**************************************************/
	public String originClass;
	
	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeFunction(Type returnType, String name, TypeList params)
	{
		this.name = name;
		this.returnType = returnType;
		this.params = params;
	}
}
