package codes.recursive;

import ch.qos.logback.classic.Level;
import codes.recursive.queue.AqConsumer;
import codes.recursive.queue.AqProducer;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.cli.CommandLine;
import oracle.jdbc.driver.OracleDriver;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Command(name = "aq-cli", description = "...",
        subcommands = {
            EnqueueCommand.class,
            DequeueCommand.class
        },
        mixinStandardHelpOptions = true)
@TypeHint(value = {
        OracleDriver.class
})
public class AqCliCommand implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AqCliCommand.class);
    private static final String VERSION = "0.0.1";

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output", scope = picocli.CommandLine.ScopeType.INHERIT)
    boolean verbose;
    public String ocid;
    @Option(names = {"-o", "--ocid"}, description = "If provided, the ADB OCID will be used to automatically download Autonomous DB wallet", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setOcid(String ocid) {
        this.ocid = ocid;
    }
    public String walletPassword;
    @Option(names = {"-w", "--wallet-password"}, description = "The ADB Wallet Password. If you do not pass a wallet password, one will be generated for you.", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setWalletPassword(String walletPassword) {
        this.walletPassword = walletPassword;
    }
    public String username;
    @Option(names = {"-u", "--username"}, description = "The database user's username", required = true, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setUsername(String username) {
        this.username = username;
    }
    public String password;
    @Option(names = {"-p", "--password"}, description = "The database user's password", required = true, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setPassword(String password) {
        this.password = password;
    }
    public String queueName;
    @Option(names = {"-q", "--queue-name"}, description = "The AQ queue name", required = true, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String connectString;
    @Option(names = {"-c", "--connect-string"}, description = "The connection string to use to connect to the DB.", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public String host;
    @Option(names = {"-H", "--host"}, description = "The DB host name.", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setHost(String host) {
        this.host = host;
    }

    public String port;
    @Option(names = {"-P", "--port"}, description = "The DB port.", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setPort(String port) {
        this.port = port;
    }

    public String serviceName;
    @Option(names = {"-s", "--service-name"}, description = "The DB service name.", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /* todo - break into connect string and host/port/service */

    public String ociProfile;
    @Option(names = {"-O", "--oci-profile"}, description = "The OCI profile to use when using automatic wallet download", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setOciProfile(String ociProfile) {
        this.ociProfile = ociProfile;
    }
    public String ociProfilePath;
    @Option(names = {"-i", "--oci-profile-path"}, description = "The path to the OCI profile to use when using automatic wallet download", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setOciProfilePath(String ociProfilePath) {
        this.ociProfilePath = ociProfilePath;
    }

    public static void main(String[] args) throws Exception {

        CommandLine parsedArgs = CommandLine.parse(args);
        Map<String, Object> options = parsedArgs.getUndeclaredOptions();

        Boolean isHelp = false;
        if( options.size() == 0 || options.containsKey("h") || options.containsKey("help") ) {
            System.setProperty("aq.help", "true");
            isHelp = true;
        }
        if( options.containsKey("V") || options.containsKey("version") ) {
            System.out.println(VERSION);
            System.exit(0);
        }
        if(!isHelp) {
            System.setProperty("datasources.default.driver-class-name", "oracle.jdbc.driver.OracleDriver");
            System.setProperty("datasources.default.connection-factory-class-name", "oracle.jdbc.pool.OracleDataSource");

            System.setProperty("oracle.jdbc.fanEnabled", "false");
            if(Arrays.asList(args).contains("dequeue")) {
                System.setProperty("consumer.enabled", "true");
                System.setProperty("oracle.jms.maxSleepTime", "1000");
            }

            String jdbcUrl = "jdbc:oracle:thin:@";
            Boolean hasConnectString = false;
            Boolean hasHostPortService = false;
            Boolean hasOcid = false;

            if(options.containsKey("o") || options.containsKey("ocid")) {
                if(options.containsKey("w") && options.containsKey("wallet-password")) {
                    if(options.containsKey("w")) System.setProperty("datasources.default.walletPassword", options.get("w").toString());
                    if(options.containsKey("wallet-password")) System.setProperty("datasources.default.walletPassword", options.get("wallet-password").toString());
                }
                else {
                    // set a default wallet password
                    System.setProperty("datasources.default.walletPassword", "Wallet_" + UUID.randomUUID().toString().replace("-", ""));
                }
                if(options.containsKey("o")) System.setProperty("datasources.default.ocid", options.get("o").toString());
                if(options.containsKey("ocid")) System.setProperty("datasources.default.ocid", options.get("ocid").toString());
                hasOcid = true;
            }

            if(options.containsKey("O")) System.setProperty("oci.config.profile", options.get("O").toString());
            if(options.containsKey("oci-profile")) System.setProperty("oci.config.profile", options.get("oci-profile").toString());
            if(options.containsKey("i")) System.setProperty("oci.config.path", options.get("i").toString());
            if(options.containsKey("oci-profile-path")) System.setProperty("oci.config.path", options.get("oci-profile-path").toString());

            if(options.containsKey("c") || options.containsKey("connect-string")) {
                hasConnectString = true;
                if(options.containsKey("c")) jdbcUrl = jdbcUrl + options.get("c").toString();
                if(options.containsKey("connect-string")) jdbcUrl = jdbcUrl + options.get("connect-string").toString();
            }
            if(
                    (options.containsKey("H") || options.containsKey("host")) &&
                    (options.containsKey("P") || options.containsKey("port")) &&
                    (options.containsKey("s") || options.containsKey("service-name"))
            ) {
                hasHostPortService = true;
                if(options.containsKey("H")) jdbcUrl = jdbcUrl + options.get("H").toString();
                if(options.containsKey("host")) jdbcUrl = jdbcUrl + options.get("host").toString();
                jdbcUrl = jdbcUrl + ":";
                if(options.containsKey("P")) jdbcUrl = jdbcUrl + options.get("P").toString();
                if(options.containsKey("port")) jdbcUrl = jdbcUrl + options.get("port").toString();
                jdbcUrl = jdbcUrl + "/";
                if(options.containsKey("s")) jdbcUrl = jdbcUrl + options.get("s").toString();
                if(options.containsKey("service-name")) jdbcUrl = jdbcUrl + options.get("service-name").toString();
            }

            if(!hasOcid && !hasConnectString && !hasHostPortService) {
                throw new picocli.CommandLine.ParameterException(new picocli.CommandLine(new AqCliCommand()),
                        "To connect, you must use either automatic wallet download by passing an OCID {'-o', '--ocid'} or use a direct connection with either a connect string {'-c', '--connect-string'} or a host {'H', '--host'}, port {'-P', '--port'}, and service name {'s', '--service-name'}.");
            }
            else {
                if(hasConnectString || hasHostPortService) {
                    System.setProperty("datasources.default.url", jdbcUrl);
                }
            }

            if(options.containsKey("u")) System.setProperty("datasources.default.username", options.get("u").toString());
            if(options.containsKey("username")) System.setProperty("datasources.default.username", options.get("username").toString());
            if(options.containsKey("p")) System.setProperty("datasources.default.password", options.get("p").toString());
            if(options.containsKey("password")) System.setProperty("datasources.default.password", options.get("password").toString());
            if(options.containsKey("q")) System.setProperty("aq.queue.name", options.get("q").toString());
            if(options.containsKey("queue-name")) System.setProperty("aq.queue.name", options.get("queue-name").toString());

            LOG.info("Connecting to queue...");
        }

        if(options.containsKey("v") || options.containsKey("verbose")) {
            // do nothing... logging is enabled
        }
        else {
            ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.OFF);
        }
        // always enable log output for CLI classes (for user feedback)
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(EnqueueCommand.class)).setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DequeueCommand.class)).setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AqProducer.class)).setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AqConsumer.class)).setLevel(Level.INFO);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AqCliCommand.class)).setLevel(Level.INFO);

        // disable warnings for OJDBC & UCP
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("oracle.jdbc")).setLevel(Level.ERROR);
        UniversalConnectionPoolManager mgr = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
        mgr.setLogLevel(java.util.logging.Level.SEVERE);

        PicocliRunner.run(AqCliCommand.class, args);
    }

    public void run() {
        if( System.getProperty("aq.help").toString().length() == 0 ) {}
    }
}
