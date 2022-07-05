# pdf2txt-textract

In order to use this converter, you will need to have [credentials](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys) for programmatic access from [Amazon AWS](https://aws.amazon.com/).  They will include both an access key and a secret access key which should be recorded in a `.properties` file that looks like this:

```properties
[default]
aws_access_key_id = ...
aws_secret_access_key = ...
```

The location of the `.properties` file defaults in `pdf2txt.conf` to `${user.home}/.pdf2txt/aws-credentials.properties`.  If need be, you can change it in that file or just specify a different configuration file (e.g., as a command line argument to `pdf2txt`) that contains a different value for `Pdf2txt.textract.credentials` so that it points to your credentials.

Also included in the configuration file are the profile, the AWS region, and the S3 bucket name to use for documents with more than one page.

```
  textract {
    credentials = ${user.home}/.pdf2txt/aws-credentials.properties
    profile = default
    region = us-west-1
    bucket = ""
  }
```

The `profile` should match the one in the credentials file and the `region` and `bucket` should be coordinated with your AWS account.  If the bucket is empty, only documents of one page can be processed.  If a non-empty string is used for the bucket, the program will still use the input directory to collect file names, but it will then use the files in the bucket rather than on disk.  It will complain if the file is not in the bucket.  You'll need to upload the files to S3 manually.
