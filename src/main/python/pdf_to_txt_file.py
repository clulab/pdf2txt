"""
This script takes a PDF file and converts it into TXT.
It works with python3 in a Unix or Linux or (without alarms) Windows system.

Requirements:
    pdfminer.six

Usage:
    python pdf_to_txt_file.py input_pdf_file output_txt_file
"""

import os
import platform
import sys
import signal
from io import StringIO
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.converter import TextConverter
from pdfminer.layout import LAParams
from pdfminer.pdfpage import PDFPage

def isWindows():
    return platform.system() == "Windows"


def signal_handler(signum, frame):
    raise Exception("Timed out.")


def convert_to_txt(infile, pages=None):
    if not pages:
        pagenums = set()
    else:
        pagenums = set(pages)

    output = StringIO()
    manager = PDFResourceManager()
    converter = TextConverter(manager, output, laparams=LAParams())
    interpreter = PDFPageInterpreter(manager, converter)
    idle_converter = TextConverter(manager, output, laparams=LAParams(boxes_flow=None))
    idle_interpreter = PDFPageInterpreter(manager, idle_converter)

    if not isWindows():
        signal.signal(signal.SIGALRM, signal_handler)
        signal.alarm(1800)

    for page in PDFPage.get_pages(infile, pagenums):
        try:
            interpreter.process_page(page)
        except RecursionError:
            idle_interpreter.process_page(page)
    converter.close()
    idle_converter.close()
    text = output.getvalue()
    output.close

    if not isWindows():
        signal.alarm(0)
    
    return text

if __name__ == "__main__":
    inputfile = sys.argv[1]
    outputfile = sys.argv[2]

    with open(inputfile, 'rb') as filein:
        text = convert_to_txt(filein)
        with open(outputfile, 'w', encoding="utf-8") as outfile:
            outfile.write(text)
