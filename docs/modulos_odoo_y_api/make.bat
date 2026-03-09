@ECHO OFF
pushd %~dp0

REM make.bat para Windows – Vendoo Docs
REM Uso: make.bat html

if "%SPHINXBUILD%" == "" (
    set SPHINXBUILD=sphinx-build
)
set SOURCEDIR=.
set BUILDDIR=_build

%SPHINXBUILD% >NUL 2>NUL
if errorlevel 9009 (
    echo.
    echo.El comando 'sphinx-build' no se encontro. Instala Sphinx:
    echo.    pip install sphinx sphinx-rtd-theme myst-parser sphinx-autodoc-typehints
    exit /b 1
)

if "%1" == "" goto help
if "%1" == "html" goto html
if "%1" == "clean" goto clean

:help
%SPHINXBUILD% -M help %SOURCEDIR% %BUILDDIR% %SPHINXOPTS% %O%
goto end

:html
echo Generando documentacion HTML...
%SPHINXBUILD% -b html %SOURCEDIR% %BUILDDIR%/html %SPHINXOPTS% %O%
echo.
echo Documentacion generada en %BUILDDIR%/html/index.html
goto end

:clean
echo Limpiando _build...
rmdir /s /q %BUILDDIR%
goto end

:end
popd
