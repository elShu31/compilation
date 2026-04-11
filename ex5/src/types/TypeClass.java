package types;

public class TypeClass extends Type
{
	/*********************************************************************/
	/* If this class does not extend a father class this should be null  */
	/*********************************************************************/
	public TypeClass father;

	/**************************************************/
	/* Gather up all data members in one place        */
	/* Note that data members coming from the AST are */
	/* packed together with the class methods         */
	/**************************************************/
	public TypeList dataMembers;

	/**************************************************/
	/* Total object size in bytes, including the      */
	/* reserved vtable pointer slot at offset 0       */
	/**************************************************/
	public int classSize = 0;

	/**************************************************/
	/* Flat representation of this class's vtable,    */
	/* mirroring what will be written to .data        */
	/**************************************************/
	public java.util.List<TypeFunction> vtable;
	
	/**************************************************/
	/* A list of all classes defined in the program.  */
	/**************************************************/
	public static java.util.List<TypeClass> allClasses = new java.util.ArrayList<>();

	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeClass(TypeClass father, String name, TypeList dataMembers)
	{
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
	}

	/*************/
	/* isClass() */
	/*************/
	@Override
	public boolean isClass()
	{
		return true;
	}
}
