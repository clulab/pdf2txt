# pdf2txt-microsoft

In order to use this converter, you will need an [API key](https://docs.microsoft.com/en-us/azure/search/search-security-api-keys) and an [endpoint](https://docs.microsoft.com/en-us/azure/virtual-network/virtual-network-service-endpoints-overview) for [Microsoft Azure Cognitive Services](https://azure.microsoft.com/en-us/services/cognitive-services/).  They key is a simple string that should be stored in a standard properties file which looks like this:

```properties
key = ...
```

The location of the `.properties` file defaults in `pdf2txt.conf` to `${user.home}/.pdf2txt/microsoft-credentials.properties`.  The value can be overridden with the environment variable `Pdf2txt_microsoft_credentials`.  If need be, you can also change it in the configuration file or just specify a different configuration file (e.g., as a command line argument to `pdf2txt`) that contains a different value for `Pdf2txt.microsoft.credentials` so that it points to your credentials.

Also included in the configuration file is the value for the endpoint.  Default values and the overriding environment variables are as shown.

```
Pdf2txt {
  microsoft {
    credentials = ${user.home}/.pdf2txt/microsoft-credentials.properties
    credentials = ${?Pdf2txt_microsoft_credentials}
    endpoint = ""
    endpoint = ${?Pdf2txt_microsoft_endpoint}
  }
}
```

The `endpoint` should be coordinated with your Azure account.  It is eventually used in a connection string which takes care of the `http[s]://` on the front and `/vision/v3.2/` at the back, so don't include these parts yourself.  The endpoint in the configuration file might look like `myproject.cognitiveservices.azure.com`.