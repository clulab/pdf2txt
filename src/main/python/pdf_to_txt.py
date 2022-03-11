"""
This script takes a folder containing PDF files and converts them into TXT.
It works with python3 in a Unix or Linux or (without alarms) Windows system.

Requirements:
    pdfminer.six

Usage:
    python pdf_to_txt.py input_pdf_folder output_txt_folder
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
    simple_converter = TextConverter(manager, output, laparams=LAParams(boxes_flow=None))
    simple_interpreter = PDFPageInterpreter(manager, simple_converter)

    if not isWindows():
        signal.signal(signal.SIGALRM, signal_handler)
        signal.alarm(1800)

    for pageno, page in enumerate(PDFPage.get_pages(infile, pagenums), 1):
        try:
            interpreter.process_page(page)
        except RecursionError:
            print("(RecursionError in page %s: disabling box_flow)" % pageno, end=" - ")
            simple_interpreter.process_page(page)
    converter.close()
    simple_converter.close()
    text = output.getvalue()
    output.close()

    if not isWindows():
        signal.alarm(0)
    
    return text


if __name__ == "__main__":
    inputdir = sys.argv[1]
    outputdir = sys.argv[2]
    if not os.path.isdir(outputdir):
        os.mkdir(outputdir)
    for pdffile in os.listdir(inputdir):
        filename = os.path.splitext(pdffile)[0]
        filedirin = os.path.join(inputdir, pdffile)
        filedirout = os.path.join(outputdir, filename + ".txt")
        if not os.path.exists(filedirout):
            print("Converting %s" % filedirin, end=" - ")
            try:
                with open(filedirin, 'rb') as filein:
                    text = convert_to_txt(filein)
                    with open(filedirout, 'w', encoding="utf-8") as outfile:
                        outfile.write(text)
                print("Success.")
            except Exception as e:
                print("Fail: %s" % e)

