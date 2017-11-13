#!/bin/sh

aux_dir=aux
pdfl="pdflatex --output-directory=$aux_dir"

[ -d "$aux_dir" ] || mkdir "$aux_dir"
$pdfl proposal.tex
mv $aux_dir/*.pdf .
