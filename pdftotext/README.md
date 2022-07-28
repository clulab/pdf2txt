# pdf2txt-pdftotext

A pre-compiled executable program provides the conversion and it must be accessible via the `$PATH`.  If it is known by a different name or is installed in a custom directory, the environment variable `Pdf2txt_pdftotext_pdftotext` can be used to override the default value.  The configuration file `Pdf2txt.conf` can also be edited or even replaced by specifying a different configuration as a command line argument.

```
Pdf2txt {
  pdftotext {
    pdftotext = "pdftotext"
    pdftotext = ${?Pdf2txt_pdftotext_pdftotext}
  }
}
```