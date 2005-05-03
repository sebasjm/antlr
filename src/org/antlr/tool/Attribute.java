package org.antlr.tool;

/** Track the names of attributes define in arg lists, return values,
 *  scope blocks etc...
 */
public class Attribute {
	/** The entire declaration such as "String foo;" minus "static" */
	public String decl;

	/** The name of the attribute "foo" */
	public String name;

	public Attribute(String name, String decl) {
		this.name = name;
		this.decl = decl;
	}

	public String toString() {
		return name;
	}
}

