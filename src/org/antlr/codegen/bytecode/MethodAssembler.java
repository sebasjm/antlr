/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.antlr.codegen.bytecode;

import java.util.*;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;

/** A limited byte code assembler for 1 method.  Assumes that it has access
 *  to the constant pool created by ClassFile.  It reads Jasmin-like
 *  syntax and allows labels (fixed up with back patching).  The syntax
 *  looks like (meaningless code):
 *
 *  .method dfa1 (LIntegerStream;)I
 *        aload 0
 *        invokeinterface IntegerStream.LA 2
 *        ipush 97
 *        if_icmpne label
 *        ireturn
 *  label:
 *        getstatic java/lang/System.err
 *        ldc "error"
 *        invokevirtual java/io/PrintStream.println 2
 *
 *  Each line has an opcode and 1 or more operands separated by space.
 *  The opcode leads to a method that knows how to generate code for it.
 *  In this way, I can convert "aload 0" to "aload_0" instruction.
 *
 *  This only handles bytecodes necessary for my DFA code generation.
 */
public class MethodAssembler {
	// bytecode instructions
	public static final int GOTO_W = 200;
	protected static Map nameToOpcode = new HashMap();
	static {
		nameToOpcode.put("iconst_m1", new Integer(2));
		nameToOpcode.put("iconst_0", new Integer(3));
		nameToOpcode.put("iconst_1", new Integer(4));
		nameToOpcode.put("iconst_2", new Integer(5));
		nameToOpcode.put("iconst_3", new Integer(6));
		nameToOpcode.put("iconst_4", new Integer(7));
		nameToOpcode.put("iconst_5", new Integer(8));
		nameToOpcode.put("lconst_0", new Integer(9));
		nameToOpcode.put("lconst_1", new Integer(10));
		nameToOpcode.put("bipush", new Integer(16));
		nameToOpcode.put("sipush", new Integer(17));
		nameToOpcode.put("ldc", new Integer(18));
		nameToOpcode.put("ldc_w", new Integer(19));
		nameToOpcode.put("iload", new Integer(21));
		nameToOpcode.put("aload", new Integer(25));
		nameToOpcode.put("iload_0", new Integer(26));
		nameToOpcode.put("iload_1", new Integer(27));
		nameToOpcode.put("iload_2", new Integer(28));
		nameToOpcode.put("iload_3", new Integer(29));
		nameToOpcode.put("aload_0", new Integer(42));
		nameToOpcode.put("aload_1", new Integer(43));
		nameToOpcode.put("aload_2", new Integer(44));
		nameToOpcode.put("aload_3", new Integer(45));
		nameToOpcode.put("istore", new Integer(54));
		nameToOpcode.put("astore", new Integer(58));
		nameToOpcode.put("istore_0", new Integer(59));
		nameToOpcode.put("istore_1", new Integer(60));
		nameToOpcode.put("istore_2", new Integer(61));
		nameToOpcode.put("istore_3", new Integer(62));
		nameToOpcode.put("astore_0", new Integer(75));
		nameToOpcode.put("astore_1", new Integer(76));
		nameToOpcode.put("astore_2", new Integer(77));
		nameToOpcode.put("astore_3", new Integer(78));
		nameToOpcode.put("if_icmpeq", new Integer(159));
		nameToOpcode.put("if_icmpne", new Integer(160));
		nameToOpcode.put("if_icmplt", new Integer(161));
		nameToOpcode.put("if_icmpge", new Integer(162));
		nameToOpcode.put("if_icmpgt", new Integer(163));
		nameToOpcode.put("if_icmple", new Integer(164));
		nameToOpcode.put("goto", new Integer(167));
		nameToOpcode.put("jsr", new Integer(168));
		nameToOpcode.put("ret", new Integer(169));
		nameToOpcode.put("ireturn", new Integer(172));
		nameToOpcode.put("return", new Integer(177));
		nameToOpcode.put("getstatic", new Integer(178));
		nameToOpcode.put("invokevirtual", new Integer(182));
		nameToOpcode.put("invokespecial", new Integer(183));
		nameToOpcode.put("invokestatic", new Integer(184));
		nameToOpcode.put("invokeinterface", new Integer(185));
		nameToOpcode.put("goto_w", new Integer(200));
	}

	class Instruction {
		String assemblyCode;
		int address;
		int[] code;
		int opcode;
		public Instruction(int address, String assemblyCode) {
			this.address = address;
			this.assemblyCode = assemblyCode;
		}
		public int[] assemble() {
			code = assembleSingleInstruction(this);
			return code;
		}
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(assemblyCode);
			buf.append("\t\t");
			buf.append('[');
			if ( code!=null ) {
				for (int i = 0; i < code.length; i++) {
					int bc = code[i];
					buf.append(hex((byte)bc));
					buf.append(",");
				}
			}
			buf.append(']');
			return buf.toString();
		}
	}

	static class Label {
		String name;
		int address;
		boolean defined = true;
		/** List of byte code offsets that ref label; walk later
		 *  to update them once this label is defined.
		 */
		List forwardReferences;
		public Label(String name, int pc) {
			this.name = name;
			address = pc;
		}
		public void define(int pc) {
			address = pc;
			defined = true;
		}
	}

	protected List instructions;

	protected Map labels = new HashMap();

	/** Index into bytecodes of current instruction */
	protected int pc = 0;

	protected ClassFile classFile;

	/** The method we are compiling */
	protected ClassFile.Method method;

	public MethodAssembler(ClassFile classFile,
						   ClassFile.Method method)
	{
		this.classFile = classFile;
		this.method = method;
		instructions = new ArrayList();
	}

	public int[] getCode() {
		assemble();
		int n = pc; // number of byte codes is same as pc value
		int[] code = new int[n];
		int loc = 0;
		for (int i = 0; i < instructions.size(); i++) {
			Instruction instr = (Instruction) instructions.get(i);
			System.arraycopy(instr.code,0,code,loc,instr.code.length);
			loc += instr.code.length;
		}
		patchForwardLabelReferences(code);
		dump(code,0,code.length);
		method.code = code;
		return code;
	}

	protected void assemble() {
		// process all but the first line, which is the .method
		for (int i = 1; i < method.assemblyCodeLines.size(); i++) {
			String line = (String) method.assemblyCodeLines.get(i);
			if ( line.indexOf(':')>=0 ) {
				// it's a label, add to labels table
				labelInstruction(line);
				continue;
			}
			Instruction instr = new Instruction(pc, line);
			int[] code = instr.assemble();
			if ( code!=null ) {
				System.out.println(pc+": "+instr);
				instr.opcode = code[0];
				instructions.add(instr);
				pc += code.length;
			}
		}
	}

	protected Label labelInstruction(String assemblyCode) {
		String labelName = getFirstWord(assemblyCode);
		labelName = labelName.substring(0,labelName.length()-1); // kill ':'
		System.out.println("Found def of label "+labelName+"@"+pc);
		Label label = defineLabel(labelName);
		return label;
	}

	protected int[] assembleSingleInstruction(Instruction instr)
	{
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		String instrName = getFirstWord(assemblyCode);
		int numOperands = getNumberOfOperands(assemblyCode);
		if ( numOperands==0 ) {
			// easy 1-to-1 translation if no operands
			return new int[] {getOpcode(instrName)};
		}
		// don't bother being efficient; small list and small amount to compile
		if ( instrName.equals("iconst") ) {code = iconst(instr);}
		else if ( instrName.equals("iload") ) {code = iload_or_store(instr);}
		else if ( instrName.equals("istore") ) {code = iload_or_store(instr);}
		else if ( instrName.equals("aload") ) {code = aload(instr);}
		else if ( instrName.equals("goto") ) {code = branch(instr);}
		else if ( instrName.startsWith("if_icmp") ) {code = branch(instr);}
		else if ( instrName.equals("invokeinterface") ) {code = invokeinterface(instr);}
		else if ( instrName.equals("getstatic") ) {code = getstatic(instr);}
		else if ( instrName.equals("invokevirtual") ) {code = invokevirtual(instr);}
		else if ( instrName.equals("invokespecial") ) {code = invokespecial(instr);}
		else if ( instrName.equals("ldc") ) {code = ldc(instr);}
		else {
			System.err.println("unknown bytecode instruction: "+assemblyCode);
			return null;
		}
		return code;
	}

	// I N S T R U C T I O N S

	protected int[] iconst(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		st.nextToken(); // skip instr name
		String operand = st.nextToken();
		int v = Integer.parseInt(operand);
		if ( v==-1 ) {
			code = new int[] {getOpcode("iconst_m1")};
		}
		else if ( v>=0 && v<=5 ) {
            code = new int[] {getOpcode("iconst_"+v)};
		}
		else if ( v>5 && v<=0xFF ) {
			code = new int[] {getOpcode("bipush"),v};
		}
		else if ( v>5 && v<=0xFFFF ) {
			code = new int[] {getOpcode("sipush"),v};
		}
		else {
			// generic integer constant; ref constant pool
			int intIndex = classFile.indexOfInteger(v);
			code = new int[] {getOpcode("ldc_w"),
							  secondHighByte(intIndex),
							  lowByte(intIndex)};
		}
		return code;
	}

	protected int[] iload_or_store(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		String opcodeStr = st.nextToken();
		String operand = st.nextToken();
		int v = Integer.parseInt(operand);
		if ( v>=0 && v<=3 ) {
			code = new int[] {getOpcode(opcodeStr+"_"+v)};
		}
		else if ( v>3 && v<=255 ) {
			code = new int[] {getOpcode(opcodeStr), v};
		}
		else {
			System.err.println("Ooops...can't handle general iload/istore "+operand);
		}
		return code;
	}

	protected int[] aload(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		st.nextToken(); // skip instr name
		String operand = st.nextToken();
		int v = Integer.parseInt(operand);
		System.out.println("aload operand "+v);
		if ( v>=0 && v<=3 ) {
			code = new int[] {getOpcode("aload_"+v)};
		}
		else if ( v>3 && v<=255 ) {
			code = new int[] {getOpcode("aload"), v};
		}
		else {
			System.err.println("Ooops...can't handle general aload "+operand);
		}
		return code;
	}

	protected int[] ldc(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		String[] elements = getInstructionElements(assemblyCode);
		int[] code = null;
		String operand = elements[1];
		if ( operand.indexOf('"')>=0 ) {
			// it's a "push string" operation; must look up in constant pool
			// first remove quotes
			operand = operand.substring(1,operand.length()-1);
			int literalIndex = classFile.indexOfLiteral(operand);
			if ( literalIndex>=0 && literalIndex<Byte.MAX_VALUE ) {
				code = new int[] {getOpcode("ldc"), literalIndex};
			}
			else if ( literalIndex>=0 && literalIndex<Short.MAX_VALUE ) {
				code = new int[] {getOpcode("ldc_w"),
								  secondHighByte(literalIndex),
								  lowByte(literalIndex)};
			}
		}
		else {
			System.err.println("Ooops...can't handle general ldc "+operand);
		}
		return code;
	}

	protected int[] branch(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		String[] elements = getInstructionElements(assemblyCode);
		int[] code = null;
		String opcodeStr = elements[0];
		String operand = elements[1];
		Label label = (Label)labels.get(operand);
		if ( label==null ) {
			label = referenceLabel(instr, operand, pc+1);
		}

		// goto offset is computed relative to byte 0 of the if_cmpXX instr
		int offset = label.address - pc;

		int fieldIndex = classFile.indexOfField(operand);
		int opcode = getOpcode(opcodeStr);
		if ( opcodeStr.equals("goto") ) {
			// always use 32 bit signed offset since compiler will not
			// care about s2 or s4 when generating native machine code
			// this saves us having to change code size later when we backpatch
			// forward references.
			opcode = getOpcode("goto_w");
			code = new int[] {opcode,0,0,0,0};
			writeInt(code, 1, offset);
		}
		else {
			code = new int[] {opcode,
							  secondHighByte(fieldIndex),
							  lowByte(fieldIndex)};
		}

		return code;
	}

	protected int[] getstatic(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		String[] elements = getInstructionElements(assemblyCode);
		String operand = elements[0];
		int fieldIndex = classFile.indexOfField(operand);
		int[] code = new int[] {getOpcode("getstatic"),
						  secondHighByte(fieldIndex),
					      lowByte(fieldIndex)};
		return code;
	}

	protected int[] invokeinterface(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		st.nextToken(); // skip instr name
		String operand = st.nextToken();
		int methodIndex = classFile.indexOfMethod(operand);
		operand = st.nextToken();
		int numArgs = Integer.parseInt(operand);
		code = new int[] {getOpcode("invokeinterface"),
						  secondHighByte(methodIndex),
						  lowByte(methodIndex),
						  numArgs,
						  0x00};
		return code;
	}

	protected int[] invokevirtual(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		st.nextToken(); // skip instr name
		String operand = st.nextToken();
		int methodIndex = classFile.indexOfMethod(operand);
		code = new int[] {getOpcode("invokevirtual"),
						  secondHighByte(methodIndex),
						  lowByte(methodIndex)};
		return code;
	}

	protected int[] invokespecial(Instruction instr) {
		String assemblyCode = instr.assemblyCode;
		int[] code = null;
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		st.nextToken(); // skip instr name
		String operand = st.nextToken();
		int methodIndex = classFile.indexOfMethod(operand);
		code = new int[] {getOpcode("invokespecial"),
						  secondHighByte(methodIndex),
						  lowByte(methodIndex)};
		return code;
	}

	// L A B E L S

	protected Label defineLabel(String labelName) {
		Label prevLabel = (Label)labels.get(labelName);
		Label label = null;
		if ( prevLabel==null ) { // note seen before
			label = new Label(labelName, pc);
			labels.put(labelName, label);
		}
		else {
			label = prevLabel;
			// this label has been referenced before
			if ( !prevLabel.defined ) {
				prevLabel.define(pc);
			}
			else {
				System.err.println("duplicate label def: "+labelName+"; ignored");
			}

		}
		return label;
	}

	protected Label referenceLabel(Instruction instr,
								   String labelName,
								   int location)
	{
		//System.out.println("Found ref of label "+labelName+"@"+pc);
		Label label = (Label)labels.get(labelName);
		if ( label!=null ) {
			return label;
		}
		label = defineLabel(labelName);
		label.defined = false;
		if ( label.forwardReferences==null ) {
			label.forwardReferences = new LinkedList();
		}
		label.forwardReferences.add(instr);
		//label.forwardReferences.add(new Integer(location));
		return label;
	}

	private void patchForwardLabelReferences(int[] code) {
		// walk all labels, if any have unresolved forward references, patch
		Collection labelObjects = labels.values();
		for (Iterator it = labelObjects.iterator(); it.hasNext();) {
			Label label = (Label) it.next();
			System.out.println("patching label "+label.name);
			if ( label.forwardReferences!=null ) {
				for (int i = 0; i < label.forwardReferences.size(); i++) {
					Instruction instr = (Instruction) label.forwardReferences.get(i);
					int instructionStart = instr.address; // works for goto
					int offset = label.address - instructionStart;
					System.out.println("offset is "+offset);
					System.out.println("label ref at "+instr.address);
					if ( instr.opcode==GOTO_W ) {
						writeInt(code, instr.address+1, offset);
					}
					else {
						code[instr.address+1] = secondHighByte(offset);
						code[instr.address+2] = lowByte(offset);
					}
				}
			}
		}
	}

	// S U P P O R T

    public int getOpcode(String name) {
		Integer opcodeI = (Integer)nameToOpcode.get(name);
		if ( opcodeI==null ) {
			return -1;
		}
		return opcodeI.intValue();
	}

	protected String getFirstWord(String assemblyCode) {
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		if ( st.hasMoreTokens() ) {
			return st.nextToken();
		}
		return null;
	}

	protected int getNumberOfOperands(String assemblyCode) {
		StringTokenizer st = new StringTokenizer(assemblyCode, " ", false);
		if ( !st.hasMoreTokens() ) {
			return 0;
		}
		st.nextToken(); // skip over instruction
		int n = 0;
		while ( st.hasMoreTokens() ) {
			n++;
			st.nextToken();
		}
		return n;
	}

	public static int highByte(int v) {
		// watch out for sign extension, use a long mask
		return (int) ((0xFF000000L&v) >> 24);
	}

	public static int thirdHighByte(int v) {
		return (int) (0x00FF0000L&v) >> 16;
	}

	public static int secondHighByte(int v) {
		return (int) (0x0000FF00L&v) >> 8;
	}

	public static int lowByte(int v) {
		return (int) 0x000000FFL&v;
	}

	/** Write value at index into a byte (int[] actually to avoid sign issues)
	 *  array highest to lowest byte, left to right.
	 */
	public static void writeInt(int[] bytes,
								int address,
								int value)
	{
		bytes[address+0] = (byte)highByte(value); // get highest byte
		bytes[address+1] = (byte)thirdHighByte(value);
		bytes[address+2] = (byte)secondHighByte(value);
		bytes[address+3] = (byte)lowByte(value);
	}

	public static void dump(int[] memory, int offset, int n) {
		for (int i=0; memory!=null && i<n; i++) {
			if ( i%8==0 && i!=0 ) {
				System.out.println();
			}
			if ( i%8==0 ) {
				System.out.print(hex(offset+i)+": ");
			}
			System.out.print(" "+hex((byte)memory[i+offset]));
		}
		System.out.println();
	}

	public static String hex(byte b) {
		String bs = Integer.toString(b&0xFF,16).toUpperCase();
		if ( (b&0xFF)<=(byte)0x0F ) {
			bs = '0'+bs;
		}
		return bs;
	}

	public static String hex(int b) {
		String bs = Integer.toString(b,16).toUpperCase();
		int n = bs.length();
		for (int i=1; i<=(8-n); i++) {
			bs = '0'+bs;
		}
		return bs;
	}

	public static String[] getInstructionElements(String instr) {
		String[] elements = new String[4]; // 4 is max number of elements
		int firstQuoteIndex = instr.indexOf('"');
		if ( firstQuoteIndex>=0 ) {
			// treat specially; has a string argument like ldc "foo"
			StringTokenizer st = new StringTokenizer(instr, " ", false);
			if ( st.hasMoreTokens() ) {
				elements[0] = st.nextToken();
			}
			int lastQuoteIndex = instr.lastIndexOf('"');
			if ( lastQuoteIndex==firstQuoteIndex ) {
				System.err.println("missing final quote in string; instr="+instr);
			}
			// get the string including quotes
			elements[1] = instr.substring(firstQuoteIndex,lastQuoteIndex);
		}
		else {
			StringTokenizer st = new StringTokenizer(instr, " ", false);
			int i = 0;
			while ( st.hasMoreTokens() ) {
				elements[i] = st.nextToken();
				i++;
			}
		}
		return elements;
	}

}
