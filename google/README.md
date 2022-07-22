# pdf2txt-google

In order to use this converter, you will need to have a Google Cloud [project](https://cloud.google.com/resource-manager/docs/creating-managing-projects) configured with the [Cloud Vision API](https://cloud.google.com/vision/) and [Cloud Storage API](https://cloud.google.com/storage/docs/apis).  You will also need a [cloud storage bucket](https://cloud.google.com/storage).  The application must have the credentials needed to access the project.  These are usually provided in a JSON file that contains fields for `project_id` and `private_key_id` among others.  The location of the file is specified in `pdf2txt.conf` and defaults to `${user.home}/.pdf2txt/google-credentials.json`.  However, the value can be overridden with the environment variable `GOOGLE_APPLICATION_CREDENTIALS`.  If need be, you can change the value recorded in the configuration file or just specify a different configuration file (e.g., as a command line argument to `pdf2txt`) that contains a different value for `Pdf2txt.google.credentials`.  Also recorded in the configuration file is the name of the storage bucket.  It defaults to `pdf2txt_pdfs` and can be overridden with the environment variable `Pdf2txt.google.bucket`.  Below are example contents of the files.

pdf2txt.conf:
```
Pdf2txt {
  google {
    credentials = ${user.home}/.pdf2txt/google-credentials.json
    credentials = ${?GOOGLE_APPLICATION_CREDENTIALS}
    bucket = "pdf2txt_pdfs"
    bucket = ${Pdf2txt.google.bucket}
  }
}
```

google-credentials.json:
```json
{
  "type": "...",
  "project_id": "...",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  ...
}
```
