# pdf2txt-textract

In order to use this converter, you will need to have [credentials](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys) for programmatic access from [Amazon AWS](https://aws.amazon.com/).  They will include both an access key and a secret access key which should be recorded in a `.properties` file that looks like this:

```properties
[default]
aws_access_key_id = ...
aws_secret_access_key = ...
```

The location of the `.properties` file defaults in `pdf2txt.conf` to `${user.home}/.pdf2txt/aws-credentials.properties`.  If need be, you can change it in that file or just specify a different configuration file (e.g., as a command line argument to `pdf2txt`) that contains a different value for `Pdf2txt.textract.credentials` so that it points to your credentials.

Also included in the configuration file are the profile and the AWS region to use.

```
  textract {
    credentials = ${user.home}/.pdf2txt/aws-credentials.properties
    profile = default
    region = us-west-1
  }
```

The `profile` should match the one in the credentials file and the `region` should be coordinated with your AWS account.
