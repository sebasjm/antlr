case <attr.stateNumber>:
	switch (c) {
		<attr.edges:lexer_edge(); separator="\n">
 		case EOF :
			break matching;
		default :
			error(c);
	}
	break;
