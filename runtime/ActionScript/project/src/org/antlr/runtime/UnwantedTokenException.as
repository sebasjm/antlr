package org.antlr.runtime
{
    public class UnwantedTokenException extends MismatchedTokenException
    {
        public function UnwantedTokenException(expecting:int, input:IntStream)
        {
            super(expecting, input);
        }
     
        public function get unexpectedToken():Token {
            return token;
        }
        
        public override function toString():String {
    		return "UnwantedTokenException(found="+token.text+", expected "+
    			   expecting+")";
        }
    }
}