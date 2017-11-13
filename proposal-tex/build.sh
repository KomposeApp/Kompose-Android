#!/bin/sh

pdfl="pdflatex"

$pdfl proposal.tex && bibtex proposal && $pdfl proposal.tex && $pdfl proposal.tex
