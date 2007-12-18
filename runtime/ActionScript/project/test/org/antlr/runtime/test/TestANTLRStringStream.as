package org.antlr.runtime.test {
	import flexunit.framework.Assert;
	import flexunit.framework.TestCase;
	
	import org.antlr.runtime.ANTLRStringStream;
	import org.antlr.runtime.CharStream;
	
	public class TestANTLRStringStream extends TestCase {
		
		public function TestANTLRStringStream()	{
			super();
		}
		
		public function testBasic():void {
			var stream:CharStream = new ANTLRStringStream("abc");
			assertEquals(stream.size, 3);
			
		}
	}
}