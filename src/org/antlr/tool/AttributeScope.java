package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.util.*;

/** Track the attributes within a scope.  A named scoped has just its list
 *  of attributes.  Each rule has potentially 3 scopes: return values,
 *  parameters, and an implicitly-named scope (i.e., a scope defined in a rule).
 *  Implicitly-defined scopes are named after the rule; rules and scopes then
 *  must live in the same name space--no collisions allowed.
 */
public class AttributeScope {

	/** All token scopes (token labels) share the same fixed scope of
	 *  of predefined attributes.  I keep this out of the runtime.Token
	 *  object to avoid a runtime space burden.
	 */
	public static AttributeScope tokenScope = new AttributeScope("Token");
	static {
		tokenScope.addAttribute("text", null);
		tokenScope.addAttribute("type", null);
		tokenScope.addAttribute("line", null);
		tokenScope.addAttribute("index", null);
		tokenScope.addAttribute("pos", null);
		tokenScope.addAttribute("channel", null);
	}

	/** The scope name */
	protected String name;

	/** Not a rule scope, but visible to all rules "scope symbols { ...}" */
	public boolean isDynamicGlobalScope;

	/** Visible to all rules, but defined in rule "scope { int i; }" */
	public boolean isDynamicRuleScope;

	public boolean isParameterScope;

	public boolean isReturnScope;

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
			Attribute attr = new Attribute(lastIDInDecl(decl), decl);
			attributes.put(attr.name, attr);
		}
	}

	public void addAttribute(String name, String decl) {
		attributes.put(name, new Attribute(name,decl));
	}

	public Attribute getAttribute(String name) {
		return (Attribute)attributes.get(name);
	}

	public String getAttributeReferenceTemplateName(String scope, String attribute) {
		return "ruleLabelRef";
	}

	/** Used by templates to get all attributes */
	public List getAttributes() {
		List a = new ArrayList();
		a.addAll(attributes.values());
		return a;
	}

	/** Return the set of keys that collide from
	 *  this and other.
	 */
	public Set intersection(AttributeScope other) {
		if ( other==null || other.size()==0 || size()==0 ) {
			return null;
		}
		Set inter = new HashSet();
		Set thisKeys = attributes.keySet();
		for (Iterator it = thisKeys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			if ( other.attributes.get(key)!=null ) {
				inter.add(key);
			}
		}
		if ( inter.size()==0 ) {
			return null;
		}
		return inter;
	}

	public int size() {
		return attributes==null?0:attributes.size();
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
		return (isDynamicGlobalScope?"global ":"")+getName()+":"+attributes;
	}

	public static void main(String[] args) {
		String s = new AttributeScope("test").lastIDInDecl(args[0]);
		System.out.println("name is "+s);
	}
}
