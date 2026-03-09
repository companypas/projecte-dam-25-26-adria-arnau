# Makefile mínimo para Sphinx – Vendoo Docs
# Uso: make html   (desde la raíz del proyecto)
#      make -C docs html  (desde un nivel superior)

SPHINXOPTS    ?=
SPHINXBUILD   ?= sphinx-build
SOURCEDIR      = .
BUILDDIR       = _build

.PHONY: help clean html livehtml

help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

clean:
	@echo "Limpiando carpeta _build..."
	rm -rf $(BUILDDIR)

html:
	@echo "Generando documentación HTML..."
	$(SPHINXBUILD) -b html "$(SOURCEDIR)" "$(BUILDDIR)/html" $(SPHINXOPTS) $(O)
	@echo ""
	@echo "Documentación generada en $(BUILDDIR)/html/index.html"

# Requiere: pip install sphinx-autobuild
livehtml:
	sphinx-autobuild "$(SOURCEDIR)" "$(BUILDDIR)/html" $(SPHINXOPTS) $(O)
