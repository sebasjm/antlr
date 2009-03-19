package org.antlr.gunit.swingui.model;

import java.io.*;
import java.util.*;
import org.antlr.runtime.*;

public class TestSuite {

    protected List<Rule> rules ;
    protected String grammarName ;
    protected CommonTokenStream tokens;
    protected File testSuiteFile;      

    protected TestSuite(String gname, File testFile) {
        grammarName = gname;
        testSuiteFile = testFile;
        rules = new ArrayList<Rule>();
    }
    
    /* Get the gUnit test suite file name. */
    public File getTestSuiteFile() {
        return testSuiteFile;
    }       

    public void addRule(Rule currentRule) {
        if(currentRule == null) throw new IllegalArgumentException("Null rule");
        rules.add(currentRule);
    }

    // test rule name
    public boolean hasRule(Rule rule) {
        for(Rule r: rules) {
            if(r.getName().equals(rule.getName())) {
                return true;
            }
        }
        return false;
    }

    public int getRuleCount() {
        return rules.size();
    }
    
    public void setRules(List<Rule> newRules) {
        rules.clear();
        rules.addAll(newRules);
    }

    /* GETTERS AND SETTERS */

    public void setGrammarName(String name) { grammarName = name;}

    public String getGrammarName() { return grammarName; }

    public Rule getRule(int index) { return rules.get(index); }

    public CommonTokenStream getTokens() { return tokens; }
    
    public void setTokens(CommonTokenStream ts) { tokens = ts; }

    public Rule getRule(String name) {
        for(Rule rule: rules) {
            if(rule.getName().equals(name)) {
                return rule;
            }
        }
        return null;
    }
    
    // only for stringtemplate use
    public List getRulesForStringTemplate() {return rules;}
    
}
