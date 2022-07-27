# pdf2txt-ghostact

This converter makes use of [Ghostscript](https://github.com/ArtifexSoftware/ghostpdl-downloads/releases) and [Tesseract](https://github.com/tesseract-ocr/tesseract), which need to be preinstalled on the computer.  Installation does not automatically make them available, however.  That is the responsibility of configuration.  `Pdf2txt.conf` includes these preconfigured values:

```
Pdf2txt {
  ghostact {
    ghostscript = "gswin64"
    ghostscript = ${?Pdf2txt_ghostact_ghostscript}
    device = "png16m"
    device = ${?Pdf2txt_ghostact_device}
    resolution = 400
    resolution = ${?Pdf2txt_ghostact_resolution}

    tesseract = "tesseract"
    tesseract = ${?Pdf2txt_ghostact_tesseract}
  }
}
```

If the value on the first line of a pair is incorrect, it can be overridden using the environment variable from the second line.  `Pdf2txt_ghostact_ghostscript`, if it exists, specifies where to find the `Ghostscript` executable, for example.

Alternatively, the correct values can be recorded in a substitute configuration file which can be referenced with command line parameters for some of the `Apps` in the project.
