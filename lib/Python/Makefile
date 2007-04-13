BASE=$(CURDIR)
include $(BASE)/Makefile.common

TESTS=$(subst tests/,test_,$(subst .g,,$(filter-out %__.g,$(wildcard tests/t*.g))))

testantlr3:
	$(PYTHON) testantlr3.py

.PHONY: test
test: clean testantlr3 $(TESTS)


# python <= 2.2 would need a custom set() class
PYSUPPORTED=2.3 2.4 2.5

.PHONY: testAllPy
testAllPy:
	@for PYVER in $(PYSUPPORTED); do \
	  if which python$$PYVER >/dev/null; then \
	    echo -e "++++ Running testsuite with Python V$$PYVER\n\n"; \
	    PYTHON=python$$PYVER make test || exit 1; \
	  else \
	    echo "**** Warning: Python V$$PYVER not installed: not tested"; \
	  fi \
	done

test_%: tests/%.g
	@echo -e "\n*** Test case $<:"; \
	if [ $< -nt $(subst .g,.stamp,$<) \
             -o $(SRC)/build-stamp -nt $(subst .g,.stamp,$<) \
             -o $(SRC)/org/antlr/codegen/templates/Python/Python.stg -nt $(subst .g,.stamp,$<) \
             -o $(SRC)/org/antlr/codegen/templates/Python/AST.stg -nt $(subst .g,.stamp,$<) \
             ]; then \
	  $(ANTLR) -o tests/ $<; \
	  touch $(subst .g,.stamp,$<); \
	fi; \
	PYTHONPATH=. $(PYTHON) $(subst .g,.py,$<)
	@if [ -e $(subst .g,Lexer.py,$<) ]; then ! PYTHONPATH=$(BASE) pylint --rcfile=$(BASE)/pylintrc -e $(subst .g,Lexer.py,$<) | grep '^E'; fi
	@if [ -e $(subst .g,Parser.py,$<) ]; then ! PYTHONPATH=$(BASE) pylint --rcfile=$(BASE)/pylintrc -e $(subst .g,Parser.py,$<) | grep '^E'; fi
	@echo "Ok"

clean:
	$(MAKE) -C tests clean
	rm -f *~ *.pyc
