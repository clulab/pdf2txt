# pdf2txt-pdfminer

This converter relies on `Python` for the PDF conversion.  By default, it starts the interpreter with `python3`.  If that is not accessible the `$PATH` or is known by a different name, the environment variable `Pdf2txt_pdfminer_python` can be used to override the default value.  The configuration file `Pdf2txt.conf` can also be edited or even replaced by specifying a different configuration as a command line argument.

```
Pdf2txt {
  pdfminer {
    python = "python3"
    python = ${?Pdf2txt_pdfminer_python}
  }
}
```