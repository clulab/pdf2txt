[![Build Status](https://github.com/clulab/pdf2txt/workflows/pdf2txt%20CI/badge.svg)](https://github.com/clulab/pdf2txt/actions)

# pdf2txt

The pdf2txt project implements the org.clulab.pdf2txt package including the Pdf2txt class.

## Convert a directory of PDFs to text

Several of the included "Apps" read all the PDF files in an input directory, convert them to text, preprocesses them for potential use with other CLU Lab projects, and then write them to an output directory.  These differ mainly in which component converts the PDF to text.  Here is how they might be executed:

```shell
# Use the Tika library for the conversion.
$ sbt "runMain org.clulab.pdf2txt.apps.TikaApp <input directory> <output directory>"
```

```shell
# Use the ScienceParse library from Allen AI for the conversion.
$ sbt "runMain org.clulab.pdf2txt.apps.ScienceParseApp <input directory> <output directory>"
```

```shell
# Use the pdftotext program for the conversion.
# It should be available on the $PATH.
$ sbt "runMain org.clulab.pdf2txt.apps.PdfToTextApp <input directory> <output directory>"
```

```shell
# Use the PdfMiner Python library for the conversion.
# The python3 command must be available on the $PATH.
# The pdfminer project needs to have been installed a la "pip install pdfminer".
$ sbt "runMain org.clulab.pdf2txt.apps.PdfMinerApp <input directory> <output directory>"
```

```shell
# Just preprocess files that have already been converted to text.
$ sbt "runMain org.clulab.pdf2txt.apps.TextApp <input directory> <output directory>"
```

## Convert a single PDF file to text file

To convert a single file, call the convert method.

```scala
val pdfConverter = new TikaConverter() // for example
Pdf2txt(pdfConverter).convert(inputFile, outputFile)
```

## Preprocessors

* line - Remove extraneous blank lines within a paragraph.
* paragraph - Make sure paragraphs end with complete sentences.
* unicode - Clean up some unicode characters.
* number - Remove extra spaces around commas within numbers.
* ligature - Remove extra spaces around ligatures if it makes sense.
* lineBreak - Join lines that end with hyphens (if it makes sense).
* wordBreakByHyphen - Join word parts that have been hyphenated.
* wordBreakBySpace - Join word parts separated by spaces.
