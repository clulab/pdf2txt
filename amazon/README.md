# pdf2txt-amazon

In order to use this converter, you will need to have [credentials](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys) for programmatic access from [Amazon AWS](https://aws.amazon.com/).  They will include both an access key and a secret access key which should be recorded in a `.properties` file that looks like this:

```properties
[default]
aws_access_key_id = ...
aws_secret_access_key = ...
```

The location of the `.properties` file defaults in `pdf2txt.conf` to `${user.home}/.pdf2txt/amazon-credentials.properties`.  The value can be overridden with the environment variable `Pdf2txt_amazon_credentials`.  If need be, you can also change it in the configuration file or just specify a different configuration file (e.g., as a command line argument to `pdf2txt`) that contains a different value for `Pdf2txt.amazon.credentials` so that it points to your credentials.

Also included in the configuration file are the profile, the AWS region, and the S3 bucket name to use for documents with more than one page.  Default values and the overriding environment variables are as shown.

```
Pdf2txt {
  amazon {
    credentials = ${user.home}/.pdf2txt/amazon-credentials.properties
    credentials = ${?Pdf2txt_amazon_credentials}
    profile = "default"
    profile = ${?Pdf2txt_amazon_profile}
    region = "us-west-1"
    region = ${?Pdf2txt_amazon_region}
    bucket = ""
    bucket = ${?Pdf2txt_amazon_bucket}
  }
}
```

The `profile` should match the one in the credentials file and the `region` and `bucket` should be coordinated with your AWS account.  If the bucket name is empty, only documents of one page can be processed.  If a non-empty string is used for the bucket name (a requirement for PDFs of more than one page), the program will copy files from the input directory to the bucket and then initiate the conversion from there.  After conversion, the file will be removed from the S3 bucket.  If there is a name clash and the file is already in the bucket, the conversion fails for that file.  These operations on the bucket require permissions that might be defined as such in the bucket policy:

```json
"Action": [
    "s3:DeleteObject",
    "s3:GetObjectAttributes",
    "s3:PutObject"
],
"Resource": "arn:aws:s3:::<bucket>/*"
```
