[![Build Status](https://github.com/clulab/pdf2txt/workflows/pdf2txt%20CI/badge.svg)](https://github.com/clulab/pdf2txt/actions)
[![Maven Central](https://img.shields.io/maven-central/v/org.clulab/pdf2txt_2.12?logo=apachemaven)](https://search.maven.org/search?q=g:org.clulab%20a:pdf2txt*)

# pdf2txt

The pdf2txt project combines interfaces to a number of PDF to text converters with text preprocessors that refine the converted text for use in further NLP applications.

## Contents
1. [Library](#library)
2. [Executable](#executable)
3. [PDF Converters](#pdf-converters)
4. [Preprocessors](#preprocessors)
5. [Language Models](#language-models)
6. [Command Line Syntax](#command-line-syntax)
7. [Memory](#memory)

## Library

This project has been published to [maven central](https://search.maven.org/search?q=g:org.clulab%20a:pdf2txt*) and can be used by `sbt` and other build tools as a library dependency.  Include a line like this in `build.sbt` to incorporate the main project along with all the subprojects:
```scala
libraryDependencies += "org.clulab" %% "pdf2txt" % "1.1.2"
```

## Executable

The main `Pdf2txtApp` can be run directly from the [pre-built](https://drive.google.com/file/d/13JPRVsA_B-q3xZK0QVFGqPS-Cw6Go0ES/view?usp=sharing) `jar` file.  The only prerequisite is Java.  Startup is significantly quicker than when it runs via `sbt`.

## PDF Converters

The PDF converters are (in alphabetical order, even though **tika** is the default):

* **adobe**

    This converter provides an interface to Adobe's online PDF Extract service.  The service requires credentials and eventual payment if used beyond the trial limits.  See the adobe subproject's [README.md](./adobe/README.md) for configuration details.  The service returns a zip file containing a desciption of the PDF.  The zip files are saved alongside the PDFs and will be reused if the same PDF is converted again.  Converted text is generated wholly from the zip file and if one is found with the PDF, the call to the service is skipped (and the credentials are not used or needed).

* **pdfminer**
  
    This Python [project](https://pypi.org/project/pdfminer/) is further wrapped in Python code included as a resource with this project.  It gets run as an external process using the `python3` command which must be available on the `$PATH`.  Furthermore, `pdfminer` needs to have been installed in advance, possibly with `pip install pdfminer`.

* **pdftotext**

    This [executable program](https://en.wikipedia.org/wiki/Pdftotext) needs to be installed on the local computer and accessible via the operating system `$PATH` so that the `pdftotext` command can run.  It is started as an external process to perform the conversion.

* **scienceparse**

    [Science Parse](https://github.com/allenai/science-parse) is a Scala library that parses scientific papers.  The pre-built jars are included in this project because recent versions are no longer available in standard repositories (e.g., [maven central](https://search.maven.org/)).  This converter relies on large machine learning models which are downloaded when the converter is first used.

* **text**

    If your text has already been converted from PDF and only needs to be preprocessed. then this is the "converter" to use.  It is implemented directly in this project.  In contrast to the others, it reads files matching *.txt rather than *.pdf.

* **tika**

  [Apache Tika](https://tika.apache.org/) provides a Java library which is included as a dependency for this project.  This is the default converter.

## Preprocessors

Preprocessors can be configured on (true) and off (false) as shown later, but they are by default applied in the order given here.  That can be changed if the project is used as a library, since it is an (ordered) array of preprocessors that gets passed around.  Because actions of one preprocessor can affect how the next might work or the previous might have worked, the list is traversed multiple times until the output no longer changes.

* **line**
 
    This preprocessor removes blank lines that some PDF converters leave between populated lines of text even though there is no paragraph break and usually not even the end of a sentence intervening.  After the blank line is removed, text parsers can usually piece together a sentance that is split across the remaining lines.

* **paragraph**

    Blank lines are otherwise assumed to end paragraphs.  Sentences cannot span paragraphs, so at the end of each paragraph a period is added if necessary.  This prevents parsers from combining things like multiple section headings into a single nonsensical sentence.

* **unicode**

    Conversion of unicode characters is controlled by a translation table which can remove accents, spell out Greek letters, convert to spaces, etc. and a list of accented characters which might be spared from such conversion.  How these are used is controlled by parameters.  In the command line interface, they are hard coded, but the library provides access.

* **case**

  Headers and titles often indicated with words that have been capitalized.  Unfortunately, this can confuse part of speech taggers and named entity recognizers.  Case is restored here so that words appear as they would in normal sentences for more accurate processing.

* **number**
 
    Numbers are sometimes converted so that spaces separate some of the digits or a comma lands after a space as in 123 ,45.  This preprocessor tries to remove unnecessary spaces within numbers.

* **ligature**

    Many PDF converts have difficulties with ligatures, like &ffilig; typeset as single glyphs, resulting in spaces inserted into words.  Such situations are detected and resolved with this preprocessor.  "coe ffi cient" would be corrected to "coefficient".  In order to do so, it must have a fairly good idea of what is a word or not and even whether one word is more probable than another.  Therefore, this preprocessor (and all the remaining ones) makes use of a language model described in the next section.

* **lineBreak**

    Words, particularly in justified text, are often hyphenated and split between lines of text.  Some words already include hyphens that are not optional.  This preprocessor, with the aid from a language model, attempts to find words split across lines and unite the parts.

* **wordBreakByHyphen**
 
    Given the many kinds of dashes (-, &ndash;, &mdash;, etc.) within words, PDF convers sometimes can't tell whether the letters after belong to the same word or the next one and unwanted spaces can get inserted.  Words with hyphens are recombined here.  For example, "left- handed" might be restored to "left-handed" or "two- year- old" to "two-year-old". 
 
* **wordBreakBySpace**

    Finally, sometimes spaces just appear magically within words.  They might be removed here, but by default the Never language model is configured out of an abundance of caution.  Library users can change this.

The [preprocessor unit tests](https://github.com/clulab/pdf2txt/tree/main/src/test/scala/org/clulab/pdf2txt/preprocessor) include illustrative examples of transformations. 

## Language Models

The primary reponsibility of the language models is to determine whether word "parts" should be joined so that a word is whole again.  The parts may have resulted from spaces or hyphens having been inserted between characters of a word.  The programming interface looks like this:

```scala
def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean
```

It decides whether a sentence starting "Wordone wordtwo left right" is OK or should have been "Wordone wordtwo leftright".  This might be calculated based on something like

```
P(Wordone wordtwo leftright | Wordone wordtwo) > P(Wordone wordtwo left | Wordone wordtwo)
```
or even

```
P(leftright) > P(left)
```

The language models below are currently available.  Both the `gigaword` and `glove` use not only vocabulary from their respective dictionaries, but dynamically add to it words from the document they are currently processing.  A novel word such as a product or brand name that is seen without a hyphen in a document can be used to de-hyphenate other instances in the document.

* **always**

    Always join *left* and *right*, which is useful in testing.

* **gigaword**

    Use word frequencies derived from [gigaword](https://catalog.ldc.upenn.edu/LDC2003T05).  Since counts are involved, this is coded as a `BagLanguageModel`

* **glove**

    Use words, without frequencies, derived from [glove](https://nlp.stanford.edu/projects/glove/).  Since these are without counts, this is called a `SetLanguageModel`.

* **never**

    Never join *left* and *right*, which is again useful in testing.

A [HuggingFace](https://huggingface.co/) language model is also anticipated.


## Command Line Syntax

Although this project is intended more as a library, there are several [command line applications](https://github.com/clulab/pdf2txt/tree/main/src/main/scala/org/clulab/pdf2txt/apps) included.  Many read all the PDF files in an input directory, convert them to text, preprocesses them for potential use with other NLP projects, and then write them to an output directory.  They differ mainly in which component converts the PDF to text.   [Pdf2txtApp](https://github.com/clulab/pdf2txt/blob/main/src/main/scala/org/clulab/pdf2txt/apps/Pdf2txtApp.scala) should be noted in particular, since it is the most encompassing.  Here are highlights from its [help text](https://github.com/clulab/pdf2txt/blob/main/src/main/resources/org/clulab/pdf2txt/Pdf2txtApp.syntax.txt).

### Syntax

From the command line with sbt and having the git repo, use
```shell
sbt "run <arguments>"
```
or from the command line after having run "sbt assembly" and
changed directories (target/scala-2.12) or after having downloaded
the jar file,
```shell
java -jar pdf2txt.jar <arguments>
```

### Examples
```
<no_arguments>
```
converts all PDFs in the current directory to text files.
```
-in ./pdfs -out ./txts
```
converts all PDFs in `./pdfs` to text files in `./txts.`
```
-converter pdftotext -wordBreakBySpace false -in doc.pdf -out doc.txt
```
converts `doc.pdf` to `doc.txt` using `pdftotxt` without the
`wordBreakBySpace` preprocessor.
```
-converter text -in file.txt -out file.out.txt
```
preprocesses file.txt resulting in file.out.txt

To get the full [help text](https://github.com/clulab/pdf2txt/blob/main/src/main/resources/org/clulab/pdf2txt/Pdf2txtApp.syntax.txt), use `-h`, `-help`, or `--help`.

## Memory

This software uses lots of memory for multiple large neural network models and dictionaries.  It may not run on machines with less than 16GB of memory, particulary with ScienceParse, and even then, settings may need to be adjusted so that the memory available can also be used.  If you encounter errors indicating memory exhaustion, such as
```
[error] ## Exception when compiling 44 sources to /clulab/pdf2txt-project/pdf2txt/target/scala-2.11/classes
[error] java.lang.OutOfMemoryError: Java heap space
```
or
```
Exception in thread "ModelLoaderThread" java.lang.OutOfMemoryError: Java heap space
```
then here are some tips to try:

* If `sbt` can't complete commands like `compile` or `assembly` for lack of memory, then the `-Xmx` setting in [.jvmopts](https://github.com/clulab/pdf2txt/blob/main/.jvmopts) might be increased.  The Windows version of `sbt` seems to ignore this file, so it may be necessary to instead set the value of the environment variable `_JAVA_OPTIONS`.  Depending on the shell, that might be with `set _JAVA_OPTIONS=-Xmx12g` or `$env:_JAVA_OPTIONS="-Xmx12g"`.

* If `sbt` can't complete the `test` command, then the value for `ThisBuild / Test / javaOptions` in [test.sbt](https://github.com/clulab/pdf2txt/blob/main/test.sbt) needs to be adjusted.

* If the `run` command doesn't work, then use the setting for `run / javaOptions` in [build.sbt](https://github.com/clulab/pdf2txt/blob/main/build.sbt).

* If you execute the jar file from Java and run out of memory, then the environment variable `_JAVA_OPTIONS` is the best place to make the change.  The command for Windows is above.  For other operating systems, it is usually `export _JAVA_OPTIONS=-Xmx12g`.

* If `sbt run` or `java -jar` is problematic, then lowering the value for the `-threads` argument can reduce memory requirements because fewer documents will be processed at the same time.

In each case adjust the number before the `g` (gigabytes) as needed.  

Please note that the startup messages from [fatdynet](https://github.com/clulab/fatdynet) that are printed to `stderr` like the ones below are normal and not indicative of a problem.
```
[error] [dynet] Checking /home/user/pwd for libdynet_swig.so...
[error] [dynet] Checking /home/user for libdynet_swig.so...
[error] [dynet] Extracting resource libdynet_swig.so to /tmp/libdynet_swig-8897097308525612384.so...
[error] [dynet] Loading DyNet from /tmp/libdynet_swig-8897097308525612384.so...
[error] [dynet] random seed: 2522620396
[error] [dynet] allocating memory: 512MB
[error] [dynet] memory allocation done.
```
