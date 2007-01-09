BASE=$(CURDIR)/..
include $(BASE)/Makefile.common

test%:
	$(MAKE) -s -C .. $(subst test,test_,$@)
