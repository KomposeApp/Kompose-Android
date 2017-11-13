#!/bin/sh

aux_dir=aux
pdfl="pdflatex --output-directory=$aux_dir"

$pdfl proposal.tex

mv $aux_dir/*.pdf .
