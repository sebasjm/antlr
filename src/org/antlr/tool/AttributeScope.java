package org.antlr.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

/** Track the attributes within a scope.  A named scoped has just its list
 *  of attributes.  An implicitly-named scope (i.e., a scope defined in a rule)
 *  has the list return values, arguments, and any new scope items.  Implicitly-
 *  defined scopes are named after the rule; rules and scopes then must live
 *  in the same name space--no collisions allowed.
 */
public class AttributeScope {

	public static class Attribute {
		/** The entire declaration such as "String foo;" minus "static" */
		public String decl;
		/** The name of the attribute "foo" */
		public String name;
		/** Whether or not the static keyword is present */
		public boolean isStatic;
		public String toString() {
			if ( isStatic ) {
				return "static "+name;
			}
			return name;
		}
	}

	/** The scope name */
	public String name;

	/** The list of Attribute objects */
	public LinkedHashMap attributes = new LinkedHashMap();

	public AttributeScope(String name) {
		this.name = name;
	}

	/** From a chunk of text holding the definitions of the attributes,
	 *  pull them apart and create an Attribute for each one.  Add to
	 *  the list of attributes for this scope.  Pass in the character
	 *  that terminates a definition such as ',' or ';'.  For example,
	 *
	 *  scope symbols {
	 *  	static int n;
	 *  	List names;
	 *  }
	 *
	 *  would pass in definitions equal to the text in between {...} and
	 *  separator=';'.  It results in two Attribute objects.
	 */
	public void addAttributes(String definitions, String separator) {
        StringTokenizer st = new StringTokenizer(definitions,separator);
		while (st.hasMoreElements()) {
			String decl = (String) st.nextElement();
			decl = decl.trim();
			if ( decl.length()==0 ) {
				break; // final bit of whitespace; ignore
			}
			Attribute attr = new Attribute();
			attr.decl = decl;
			if ( decl.startsWith("static ") ) {
				attr.isStatic = true;
				// trim out "static"
				attr.decl = decl.substring("static ".length(),decl.length());
			}
			attr.name = lastIDInDecl(decl);
			System.out.println(attr);
			attributes.put(attr.name, attr);
		}
	}

	/** For decls like "String foo" or "char *foo32[3]" return the last valid
	 *  ID (attribute name) in the decl.
	 */
	protected String lastIDInDecl(String decl) {
		System.out.println("decl is "+decl);
		if ( decl==null ) {
			return "unknown";
		}
		boolean inID = false;
		int start = -1;
		// walk backwards looking for start of an ID
		for (int i=decl.length()-1; i>=0; i--) {
			// if we haven't found the end yet, keep going
			if ( !inID && Character.isLetter(decl.charAt(i)) ) {
			    inID = true;
			}
			else if ( inID && !Character.isLetter(decl.charAt(i)) ) {
				start = i+1;
				break;
			}
		}
		if ( start<0 ) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_FIND_ATTRIBUTE_NAME_IN_DECL,decl);
		}
		// walk forwards looking for end of an ID
		int stop=-1;
		for (int i=start; i<decl.length(); i++) {
			// if we haven't found the end yet, keep going
			if ( !Character.isLetterOrDigit(decl.charAt(i)) ) {
				stop = i;
				break;
			}
			if ( i==decl.length()-1 ) {
				stop = i+1;
			}
		}
		return decl.substring(start,stop);
	}

	public String toString() {
		return name+":"+attributes;
	}

	public static void main(String[] args) {
		String s = new AttributeScope("test").lastIDInDecl(args[0]);
		System.out.println("name is "+s);
	}
}
