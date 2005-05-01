package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/** Track the attributes within a scope.  A named scoped has just its list
 *  of attributes.  Each rule has potentially 3 scopes: return values,
 *  parameters, and an implicitly-named scope (i.e., a scope defined in a rule).
 *  Implicitly-defined scopes are named after the rule; rules and scopes then
 *  must live in the same name space--no collisions allowed.
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
	protected String name;

	/** Not a rule scope, but visible to all rules */
	public boolean isGlobal;

	public boolean isParameterScope;

	public boolean isReturnScope;

	/** There is at least one static attribute in this scope */
	public boolean hasStatic = false;

	/** There is at least one normal nonstatic attribute in this scope */
	public boolean hasNonStatic = false;

	/** The list of Attribute objects */
	protected LinkedHashMap attributes = new LinkedHashMap();

	public AttributeScope(String name) {
		this.name = name;
	}

	public String getName() {
		if ( isParameterScope ) {
			return name+"_parameter";
		}
		else if ( isReturnScope ) {
			return name+"_return";
		}
		return name;
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
				hasStatic = true;
				// trim out "static"
				attr.decl = decl.substring("static ".length(),decl.length());
			}
			else {
				hasNonStatic = true;
			}
			attr.name = lastIDInDecl(decl);
			attributes.put(attr.name, attr);
		}
	}

	public Attribute getAttribute(String name) {
		return (Attribute)attributes.get(name);
	}

	public List getAttributes() {
		List a = new ArrayList();
		a.addAll(attributes.values());
		return a;
	}

	/** For decls like "String foo" or "char *foo32[3]" return the last valid
	 *  ID (attribute name) in the decl.
	 */
	protected String lastIDInDecl(String decl) {
		//System.out.println("decl is "+decl);
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
		return (isGlobal?"global ":"")+getName()+":"+attributes;
	}

	public static void main(String[] args) {
		String s = new AttributeScope("test").lastIDInDecl(args[0]);
		System.out.println("name is "+s);
	}
}
