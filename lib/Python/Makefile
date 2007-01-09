BASE=$(CURDIR)
include $(BASE)/Makefile.common

testantlr3:
	python2.5 testantlr3.py

.PHONY: test
test: testantlr3 $(subst tests/,test_,$(subst .g,,$(wildcard tests/t*.g)))


test_%: tests/%.g
	@echo -e "\n*** Test case $<:"; \
	$(ANTLR) $< \
	&& PYTHONPATH=. python2.5 $(subst .g,.py,$<) \
	&& echo "Ok"
