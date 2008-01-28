package org.antlr.runtime
{
    public class MissingTokenException extends MismatchedTokenException
    {
        public function MissingTokenException(expecting:int, input:IntStream)
        {
            super(expecting, input);
        }
        
        public function get missingType():int {
            return expecting;
        }
        
        public override function toString():String {
            return "MissingTokenException(expected "+expecting+")";
        }
    }
}