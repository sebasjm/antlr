BASE=$(CURDIR)/..
include $(BASE)/Makefile.common

test%:
	$(MAKE) -s -C .. $(subst test,test_,$@)


clean:
	rm -f *~ *.pyc *Lexer.py *Parser.py *.tokens *__.g *__.gl *.stamp
