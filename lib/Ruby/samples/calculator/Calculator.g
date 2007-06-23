grammar Calculator;
options {
  language = Ruby;
}

evaluate returns [result]: r=expression { $result = $r.result };

expression returns [result]: r=mult { $result = $r.result } (
    '+' r2=mult {
        $result += $r2.result
    }
  | '-' r2=mult {
        $result -= $r2.result
    }
  )*;

mult returns [result]: r=log { $result = $r.result } (
    '*' r2=log {
        $result *= $r2.result
    }
  | '/' r2=log {
        $result /= $r2.result
    }
  | '%' r2=log {
        $result %= $r2.result
    }
  )*;

log returns [result]: 'ln' r=exp { $result = Math::log($r.result) }
    | r=exp { $result = $r.result }
    ;

exp returns [result]: r=atom { $result = $r.result } ('^' r2=atom { $result = $result ** $r2.result } )? 
    ;

atom returns [result]:
    n=INTEGER { $result = $n.text.to_i }
  | n=DECIMAL { $result = $n.text.to_f } 
  | '(' r=expression { $result = r.result } ')'
  | 'PI' { $result = Math::PI }
  | 'E' { $result = Math::E }
  ;

INTEGER: DIGIT+;

DECIMAL: DIGIT+ '.' DIGIT+;

fragment
DIGIT: '0'..'9';

WS: (' ' | '\n' | '\t')+ { $channel = :hidden };
