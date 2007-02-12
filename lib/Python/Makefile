BASE=$(CURDIR)
include $(BASE)/Makefile.common


testantlr3:
	$(PYTHON) testantlr3.py

.PHONY: test
test: testantlr3 $(subst tests/,test_,$(subst .g,,$(wildcard tests/t*.g)))


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
	$(ANTLR) $< \
	&& PYTHONPATH=. $(PYTHON) $(subst .g,.py,$<) \
	&& echo "Ok"


clean:
	$(MAKE) -C tests clean
	rm -f *~ *.pyc
