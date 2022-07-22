# pdf2txt-adobe

In order to use this converter, you will need to have [credentials](https://documentcloud.adobe.com/dc-integration-creation-app-cdn/main.html?api=pdf-services-api) from Adobe for the PDF Extract API.  They will include files usually called `pdfservices-api-credentials.json` and `private.key`.  The location of the former defaults in `pdf2txt.conf` to `${user.home}/.pdf2txt/pdfservices-api-credentials.json`.  The value can be overridden with the environment variable `Pdf2txt_adobe_credentials`.  If need be, you can also change it in the configuration file or just specify a different configuration file (e.g., as a command line argument to `pdf2txt`) that contains a different value for `Pdf2txt.adobe.credentials` so that it points to your credentials.  The latter private key file is in turn specified in the credentials file.  Please use an absolute path for the `private_key_file` value.  Below is an outline of the contents of the files mentioned.

pdf2txt.conf:
```
Pdf2txt {
  adobe {
    credentials = ${user.home}/.pdf2txt/pdfservices-api-credentials.json
    credentials = ${?Pdf2txt_adobe_credentials}
  }
}
```

pdfservices-api-credentials.json:
```json
{
 "client_credentials": {
  "client_id": "...",
  "client_secret": "..."
 },
 "service_account_credentials": {
  "organization_id": "...",
  "account_id": "...",
  "private_key_file": "<path to private.key>"
 }
}
```

private.key:
```
-----BEGIN RSA PRIVATE KEY-----
...
-----END RSA PRIVATE KEY-----
```
