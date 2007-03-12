grammar t030specialStates;
options {
  language = Python;
}

// FIXME: when the sempred is evaluated in the DFA.specialState, the NAME
// token is no longer LT(1)!!
r
    : ( {self.cond}? NAME
        | {not self.cond}? NAME WS+ NAME
        )
        ( WS+ NAME )?
        EOF
    ;

NAME: ('a'..'z') ('a'..'z' | '0'..'9')+;
NUMBER: ('0'..'9')+;
WS: ' '+;
