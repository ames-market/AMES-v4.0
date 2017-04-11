# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line.
SPHINXOPTS    =
SPHINXBUILD   = sphinx-build
SPHINXPROJ    = AMESV40
SOURCEDIR     = .
BUILDDIR      = _build

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

github: html
	touch $(BUILDDIR)/html/.nojekyll
	git branch -D gh-pages || true
	git push origin --delete gh-pages || true
	git config user.email "noreply@travis.org"; git config user.name "Travis CI"
	ghp-import $(BUILDDIR)/html/
	git push -u origin gh-pages --force
	git config user.email "kdheepak89@gmail.com"; git config user.name "Dheepak Krishnamurthy"
	@echo
	@echo "Published to GitHub Pages"
