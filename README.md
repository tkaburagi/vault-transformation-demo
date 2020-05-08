# vault-transformation-demo

This is a Demo for Transformation,  Vault 1.4 Enterprise new feature.

This demo is not for showing how to use Vault token, so policy settings and handling initial token is out of scope.

## Architecture Overview
<kbd>
  <img src="https://github-image-tkaburagi.s3-ap-northeast-1.amazonaws.com/my-github-repo/transform-1.png">
</kbd>

## UI APP

https://github.com/tkaburagi/vault-tokenization-demo-ui

## App Image
<kbd>
  <img src="https://github-image-tkaburagi.s3-ap-northeast-1.amazonaws.com/my-github-repo/transform-2.png">
</kbd>

## Pre-requisite

* Starting MySQL
* Installing Java 8
* Running Vault 1.4 Ent

### MySQL Setup

should be `jdbc:mysql://127.0.0.1:3306/handson`

After login,

```
create database handson;
use handson;
create table users_tokenization (id varchar(50), username varchar(50), password varchar(200), email varchar(200), creditcard varchar(200), flag varchar(30));
```

### Setup Vault

1. Enable and configure MySQL

```
$ vault secrets enable database;
$ vault write database/roles/role-demoapp \
  db_name=mysql-handson-db \
  creation_statements="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT,INSERT,UPDATE ON handson.users TO '{{name}}'@'%';" \
  default_ttl="5h" \
  max_ttl="5h"

$ vault write database/config/mysql-handson-db 
  plugin_name=mysql-legacy-database-plugin \
  connection_url="{{username}}:{{password}}@tcp(127.0.0.1:3306)/" \
  allowed_roles="role-demoapp" \
  username="root" \
  password="rooooot"
```

2. Set the policy and create token
```
$ cat << EOF > payments.hcl
path "transform/encode/payments" {
  capabilities = [ "create", "update" ]
}

path "transform/decode/payments" {
  capabilities = [ "create", "update" ]
}
EOF

$ vault policy write payments configs/policies/payments.hcl

$ export VAULT_TOKEN_TRANS=$(vault token create -format=json -policy=payments | jq -r '.auth.client_token')
```

Enable and Configure Transformation

#### For Credit Card Number(simplest)

1. Create Tranform
```
vault write transform/transformation/creditcard-numeric \
type=fpe
template=builtin/creditcardnumber \
allowed_roles=payments \
tweak_source=internal
```

#### For Credit Card Number(simple)

1. Create Template
```
vault write transform/template/creditcard-to-numericandupper \
type=regex \
pattern='([0-9A-Z]{4})-([0-9A-Z]{4})-([0-9A-Z]{4})-([0-9A-Z]{4})' \
alphabet=builtin/alphanumericupper
```

2. Create Transform
```
vault write transform/transformation/creditcard-numericupper \
type=fpe \
template=creditcard-to-numericandupper \
tweak_source=internal \
allowed_roles=payments
```

#### For Credit Card Number

1. Create Alphabet
```
vault write transform/alphabet/symbolnumericalpha \
alphabet="0123456789._%+~#@&/,=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
```

2. Create Template
```
vault write transform/template/creditcard-to-symbolnumericalpha \
type=regex \
pattern='([0-9A-Z._%+~#@&/,=$]{4})-([0-9A-Z._%+~#@&/,=$]{4})-([0-9A-Z._%+~#@&/,=$]{4})-([0-9A-Z._%+~#@&/,=$]{4})' \
alphabet=symbolnumeric
```

3. Create Transform
```
vault write transform/transformation/creditcard-symbolnumericalpha \
type=fpe \
template=creditcard-to-symbolnumericalpha \
tweak_source=internal \
allowed_roles=payments
```

5. Test
```
vault write transform/encode/payments \
transformation=creditcard-numeric \
value=1234-4321-5678-4567

vault write transform/decode/payments \
transformation=creditcard-numeric \
value=8450-6698-5151-9691
```

```
vault write transform/encode/payments \
transformation=creditcard-numericupper \
value=1234-4321-5678-4567

vault write transform/decode/payments \
transformation=creditcard-numericupper \
value=B7Y9-YMEZ-GO4J-H7QG
```


```
vault write transform/encode/payments \
transformation=transform-to-symbolnumeric \
value=1234-4321-5678-4567

vault write transform/decode/payments \
transformation=transform-to-symbolnumeric \
value="141@-8@/5-=,+1-064."
```

#### For Email (ex. domain)

1. Create Alphabet
```
vault write transform/alphabet/symbolnumericalpha \
alphabet=".@0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
```

2. Create Template
```
vault write transform/template/email-exdomain \
type=regex \
pattern='.([0-9A-Za-z]{1,100})@.*'
alphabet=localemailaddress
```

3. Create Transform
```
vault write transform/transformation/email-exdomain \
type=fpe \
template=email-exdomain \
allowed_roles=payments \
tweak_source=internal
```

4. Test
```
vault write transform/encode/payments \
transformation=email-exdomain \
value=email@kabctl.run
```

#### For Email (ex. entire)

1. Create Template
```
vault write transform/template/email-template \
alphabet=localemailaddress \
pattern='.([0-9A-Za-z]{1,100})@(.*)\.(.*)' \
type=regex
```

2. Create Transform
```
vault write transform/transformation/email \
type=fpe \
template=email-template \
allowed_roles=payments \
tweak_source=internal
```

3. Test
```
vault write transform/encode/payments \
transformation=email \
value=email@kabctl.run
```

#### For Masking

1. Create Template
```
vault write transform/template/ccn-masking \
type=regex \
pattern='\d\d\d\d-\d\d(\d{2}|[A-Z]{4})-(\d{4}|[A-Z]{4})-\d\d\d\d' \
alphabet=builtin/alphanumericupper
```

2. Create Transform
```
vault write transform/transformation/ccn-masking \
allowed_roles=payments-masking \
templates=ccn-masking \
type=masking
```

3. Test
```
vault write transform/encode/payments-masking \
transformation=ccn-masking \
value=1122-3344-8822-5913
```

### Build and Run

```
$ echo ${VAULT_TOKEN_TRANS}
$ git clone https://github.com/tkaburagi/vault-tokenization-demo
$ ./mvnw clean package -DskipTests
$ java -jar /target/vault-tokenization-demo-0.0.1-SNAPSHOT.jar
$ cd ..
$ git clone https://github.com/tkaburagi/vault-tokenization-demo-ui
$ cd vault-tokenization-demo-ui 
$ ./mvnw clean package -DskipTests
$ java -jar /target/vault-tokenization-demo-ui-0.0.1-SNAPSHOT.jar
```

`http://127.0.0.1:7070`

<kbd>
  <img src="https://github-image-tkaburagi.s3-ap-northeast-1.amazonaws.com/my-github-repo/transform-2.png">
</kbd>
