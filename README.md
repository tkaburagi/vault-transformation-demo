# vault-transformation-demo

This is a Demo for Transformation,  Vault 1.4 Enterprise new feature.

This demo is not for showing how to use Vault token, so policy settings and handling initial token is out of scope.

## Architecture Overview
<kbd>
  <img src="https://raw.githubusercontent.com/tkaburagi/vault-tokenization-demo/master/pic.png">
</kbd>

## UI APP

https://github.com/tkaburagi/vault-tokenization-demo-ui

## App Image
<kbd>
  <img src="https://raw.githubusercontent.com/tkaburagi/vault-tokenization-demo/master/pic2.png">
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
create table users_tokenization (id varchar(50), username varchar(50), password varchar(200), email varchar(200), creditcard varchar(200), flag varchar(20));
```

### Setup Vault

Enable and configure MySQL

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

Enable and Configure Transformation

#### For Credit Card Number

1. Create Alphabet
```
$ vault write transform/alphabet/symbolnumeric \
alphabet="0123456789._%+~#@&/,=$"
```

2. Create Role
```
$ vault write transform/role/payments transformations=creditcard-to-symbolnumeric
```

3. Create Template
```
$ vault write transform/template/creditcard-to-symbolnumeric \
type=regex \
pattern='([0-9A-Z._%+~#@&/,=$]{4})-([0-9A-Z._%+~#@&/,=$]{4})-([0-9A-Z._%+~#@&/,=$]{4})-([0-9A-Z._%+~#@&/,=$]{4})' \
alphabet=symbolnumeric
```

4. Create Transform
```
$ vault write transform/transformation/transform-to-symbolnumeric \
type=fpe \
template=creditcard-to-symbolnumeric \
tweak_source=internal \
allowed_roles=payments
```

5. Test
```
$ vault write transform/encode/payments \
transformation=transform-to-symbolnumeric \
value=1234-4321-5678-4567

$ ault write transform/decode/payments \
transformation=transform-to-symbolnumeric \
value=141@-8@/5-=,+1-064.
```

#### For Email

Encrypt the local part of email, preserv first and last character:

1. Create Alphabet
```
$ vault write transform/alphabet/localemailaddress \
alphabet="0123456789._%+~#@&/,=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

```

2. Create Template
```
$ vault write transform/template/email-template \
type=regex pattern='.(.*).@.*' \
alphabet=localemailaddress

```

3. Create Transform
```
$ vault write transform/transformation/email \
type=fpe template=email-template \
tweak_source=internal allowed_roles=payments

```

4. Allow role
```
$ vault write transform/role/payments transformations=email
```

5. Test
```
$ vault write transform/encode/payments value='citizensmith@gmail.com' transformation=email
$ vault write transform/encode/payments value='citizen.smith@gmail.com' transformation=email
$ vault write transform/encode/payments value='citizen_smith@gmail.com' transformation=email
$ vault write transform/encode/payments value='_citizensmith@gmail.com' transformation=email
$ vault write transform/encode/payments value='citizensmith_@gmail.com' transformation=email
```

### Replace the Vault Token

Unfortunetelly, this app is usign static Vault Token, please replace it to your token. This will be changed when I have time :)

```
$ git clone https://github.com/tkaburagi/vault-tokenization-demo
$ cd vault-tokenization-demo
$ grep -rl s.zTBJe2IgA033w7tPVmQVOYgz . | xargs sed -i 's/zTBJe2IgA033w7tPVmQVOYgz/{{YOUR_TOKEN}}/g'
```

### Build and Run

```
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
  <img src="https://raw.githubusercontent.com/tkaburagi/vault-tokenization-demo/master/pic2.png">
</kbd>
