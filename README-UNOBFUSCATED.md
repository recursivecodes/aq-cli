# AQ CLI

This is a tool to quickly test Oracle Advanced Queuing (AQ) via the command line.

There are two ways to use the tool. The first way is to build and use it as Java Jar file. The second is to build and use it as a native image binary.

The CLI can enqueue/dequeue to AQ running on Autonomous Database. If you have the OCI CLI configured on your machine and you pass the OCID of the Autonomous DB instance, the CLI will automatically download and use your Autonomous DB wallet to make the connection. If you have [enabled TLS connections](https://recursive.codes/blog/post/2026), you can pass the JDBC URL to avoid using a wallet for the connection. See examples below.

## The Jar Method

### Building

To build a Jar, run `./gradlew assemble`. The Jar file will be build to the `build/libs/` directory.

### Running the CLI via the Jar

Once the Jar has been built, you can use the CLI with `java -jar /path/to/the.jar <command> <options>`. 

### Examples

Enqueue a message with automatic wallet download.

```shell
java -jar build/libs/aq-cli-0.1-all.jar \
  enqueue -m "{\"id\":1, \"wallet\": true}" \
  -o ocid1.autonomousdatabase.oc1.phx.abyhqljrp22gdi2vqky6qn3z7s2gi7yklxa2t3yhvqlrs7h6ak665uvxctia \
  -u aqdemouser \
  -p Str0ngPassword! \
  -w Wall3tPassw3rd! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -P DEFAULT \
  -i ~/.oci/config 
```
Enqueue a message via a TLS connection.

```shell
java -jar build/libs/aq-cli-0.1-all.jar \
  enqueue -m "{\"id\":1, \"wallet\": false}" \
  -u aqdemouser \
  -p Str0ngPassword! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -U 'jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=hvg9nd7xibsaegv_demodb_low.atp.oraclecloud.com))(security=(ssl_server_cert_dn="CN=adwc.uscom-east-1.oraclecloud.com, OU=Oracle BMCS US, O=Oracle Corporation, L=Redwood City, ST=California, C=US")))'
```

Dequeue messages with automatic wallet download.

```shell
java -jar build/libs/aq-cli-0.1-all.jar \
  dequeue \
  -o ocid1.autonomousdatabase.oc1.phx.abyhqljrp22gdi2vqky6qn3z7s2gi7yklxa2t3yhvqlrs7h6ak665uvxctia \
  -u aqdemouser \
  -p Str0ngPassword! \
  -w Wall3tPassw3rd! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -P DEFAULT \
  -i ~/.oci/config 
```
Enqueue a message via a TLS connection.

```shell
java -jar build/libs/aq-cli-0.1-all.jar \
  dequeue \
  -u aqdemouser \
  -p Str0ngPassword! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -U 'jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=hvg9nd7xibsaegv_demodb_low.atp.oraclecloud.com))(security=(ssl_server_cert_dn="CN=adwc.uscom-east-1.oraclecloud.com, OU=Oracle BMCS US, O=Oracle Corporation, L=Redwood City, ST=California, C=US")))'
```

## The Native Image Method

### Building

Assemble the Jar (with `./gradlew assemble`) and then build the native image with `./gradlew nativeImage`. The resulting binary will be in `build/native-images/`.

### Running the CLI via the Native Image

Run it like you would any other binary.

### Examples

Enqueue a message with automatic wallet download.

```shell
./aq enqueue -m "{\"id\":1, \"wallet\": true}" \
  -o ocid1.autonomousdatabase.oc1.phx.abyhqljrp22gdi2vqky6qn3z7s2gi7yklxa2t3yhvqlrs7h6ak665uvxctia \
  -u aqdemouser \
  -p Str0ngPassword! \
  -w Wall3tPassw3rd! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -P DEFAULT \
  -i ~/.oci/config 
```
Enqueue a message via a TLS connection.

```shell
./aq enqueue -m "{\"id\":1, \"wallet\": false}" \
  -u aqdemouser \
  -p Str0ngPassword! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -U 'jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=hvg9nd7xibsaegv_demodb_low.atp.oraclecloud.com))(security=(ssl_server_cert_dn="CN=adwc.uscom-east-1.oraclecloud.com, OU=Oracle BMCS US, O=Oracle Corporation, L=Redwood City, ST=California, C=US")))'
```

Dequeue messages with automatic wallet download.

```shell
./aq dequeue \
  -o ocid1.autonomousdatabase.oc1.phx.abyhqljrp22gdi2vqky6qn3z7s2gi7yklxa2t3yhvqlrs7h6ak665uvxctia \
  -u aqdemouser \
  -p Str0ngPassword! \
  -w Wall3tPassw3rd! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -P DEFAULT \
  -i ~/.oci/config 
```
Enqueue a message via a TLS connection.

```shell
./aq dequeue \
  -u aqdemouser \
  -p Str0ngPassword! \
  -q AQDEMOADMIN.EVENT_QUEUE \
  -U 'jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=hvg9nd7xibsaegv_demodb_low.atp.oraclecloud.com))(security=(ssl_server_cert_dn="CN=adwc.uscom-east-1.oraclecloud.com, OU=Oracle BMCS US, O=Oracle Corporation, L=Redwood City, ST=California, C=US")))'
```

## CLI Commands and Options

To get help, execute the CLI with `-h`. It will produce the following output:

```shell
Usage: aq-cli [-hvV] [-i=<ociProfilePath>] [-o=<ocid>] -p=<password>
              [-P=<ociProfile>] -q=<queueName> -u=<username> [-U=<url>]
              [-w=<walletPassword>] [COMMAND]
...
  -h, --help          Show this help message and exit.
  -i, --oci-profile-path=<ociProfilePath>
                      The path to the OCI Profile to use when using automatic
                        wallet download
  -o, --ocid=<ocid>   The ADB OCID
  -p, --password=<password>
                      The ADB Password
  -P, --oci-profile=<ociProfile>
                      The OCI Profile to use when using automatic wallet
                        download
  -q, --queue-name=<queueName>
                      The ADB Queue Name
  -u, --username=<username>
                      The ADB Username
  -U, --url=<url>     The ADB URL
  -v, --verbose       Enable verbose output
  -V, --version       Print version information and exit.
  -w, --wallet-password=<walletPassword>
                      The ADB Wallet Password. If you do not pass a wallet
                        password, one will be generated for you.
Commands:
  enqueue  Enqueues a message to AQ
  dequeue  Dequeues messages from AQ (until interrupted with CTRL+C)
```