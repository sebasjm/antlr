grammar t013parser;
options {
  language = Python;
}

document:
        t=IDENTIFIER {self.foundIdentifier($t.text)}
        ;

IDENTIFIER: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
