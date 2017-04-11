.PHONY: docs  

docs: ## generate Sphinx HTML documentation, including API docs
	$(MAKE) -C docs clean
	$(MAKE) -C docs html

pushdocs: docs
	$(MAKE) -C docs github
