# -*- coding: utf-8 -*-
# Fichero de configuración para Sphinx – Documentación técnica Vendoo
# https://www.sphinx-doc.org/en/master/usage/configuration.html

import os
import sys

# -- Configuración de rutas --------------------------------------------------
# Se añade la raíz del proyecto para que autodoc pueda importar módulos
sys.path.insert(0, os.path.abspath('..'))
sys.path.insert(0, os.path.abspath('../custom_addons'))

# -- Información del proyecto ------------------------------------------------
project = 'Vendoo – Documentación Técnica Módulos Odoo'
copyright = '2025-2026, Adrià & Arnau'
author = 'Adrià & Arnau'
release = '1.0.0'

# -- Configuración general ---------------------------------------------------
extensions = [
    'sphinx.ext.autodoc',        # Genera doc desde docstrings de Python
    'sphinx.ext.napoleon',       # Soporte para docstrings Google/NumPy
    'sphinx.ext.viewcode',       # Añade enlaces al código fuente
    'sphinx.ext.intersphinx',    # Hipervínculos a docs externas (Python…)
    'sphinx_autodoc_typehints',  # Muestra type hints en la documentación
    'myst_parser',               # Permite usar ficheros .md como fuente
]

# Extensiones de ficheros fuente aceptadas
source_suffix = {
    '.rst': 'restructuredtext',
    '.md':  'markdown',
}

templates_path = ['_templates']
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']

language = 'es'  # Español

# -- Configuración del tema HTML ---------------------------------------------
html_theme = 'sphinx_rtd_theme'
html_static_path = ['_static']

html_theme_options = {
    'navigation_depth': 4,
    'collapse_navigation': False,
    'sticky_navigation': True,
    'includehidden': True,
    'titles_only': False,
    'display_version': True,
    'logo_only': False,
    'prev_next_buttons_location': 'bottom',
}

html_logo = '_static/logo3.png'   # Logo de Vendoo
html_favicon = '_static/logo3.png'
html_title = 'Vendoo – Docs Técnica'

# -- Opciones de autodoc -----------------------------------------------------
autodoc_default_options = {
    'members': True,
    'member-order': 'bysource',
    'special-members': '__init__',
    'undoc-members': True,
    'show-inheritance': True,
}
autodoc_typehints = 'description'

# Módulos simulados para que autodoc no falle sin entorno Odoo
autodoc_mock_imports = [
    'odoo',
    'odoo.models',
    'odoo.fields',
    'odoo.api',
    'odoo.http',
    'odoo.exceptions',
    'dateutil',
    'dateutil.relativedelta',
    'jwt',
]

# -- Intersphinx -------------------------------------------------------------
intersphinx_mapping = {
    'python': ('https://docs.python.org/3', None),
}

# -- Opciones de Napoleon ----------------------------------------------------
napoleon_google_docstring = True
napoleon_numpy_docstring = True
napoleon_include_init_with_doc = True
napoleon_include_private_with_doc = False
napoleon_include_special_with_doc = True
napoleon_use_admonition_for_examples = True
napoleon_use_admonition_for_notes = True
