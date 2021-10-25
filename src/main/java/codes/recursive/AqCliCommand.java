package codes.recursive;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.Configurator;
import codes.recursive.queue.AqConsumer;
import codes.recursive.queue.AqProducer;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.cli.CommandLine;
import oracle.jdbc.driver.OracleDriver;
import oracle.jdbc.driver.OracleLog;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerPermission;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.security.Permission;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output", scope = picocli.CommandLine.ScopeType.INHERIT)
    boolean verbose;

    public String ocid;
    @Option(names = {"-o", "--ocid"}, description = "The ADB OCID", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setOcid(String ocid) {
        this.ocid = ocid;
    }
    public String walletPassword;
    @Option(names = {"-w", "--wallet-password"}, description = "The ADB Wallet Password. If you do not pass a wallet password, one will be generated for you.", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setWalletPassword(String walletPassword) {
        this.walletPassword = walletPassword;
    }
    public String username;
    @Option(names = {"-u", "--username"}, description = "The ADB Username", required = true, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setUsername(String username) {
        this.username = username;
    }
    public String password;
    @Option(names = {"-p", "--password"}, description = "The ADB Password", required = true, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setPassword(String password) {
        this.password = password;
    }
    public String queueName;
    @Option(names = {"-q", "--queue-name"}, description = "The ADB Queue Name", required = true, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    public String url;
    @Option(names = {"-U", "--url"}, description = "The ADB URL", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setUrl(String url) {
        this.url = url;
    }
    public String ociProfile;
    @Option(names = {"-P", "--oci-profile"}, description = "The OCI Profile to use when using automatic wallet download", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setOciProfile(String ociProfile) {
        this.ociProfile = ociProfile;
    }
    public String ociProfilePath;
    @Option(names = {"-i", "--oci-profile-path"}, description = "The path to the OCI Profile to use when using automatic wallet download", required = false, scope = picocli.CommandLine.ScopeType.INHERIT)
    public void setOciProfilePath(String ociProfilePath) {
        this.ociProfilePath = ociProfilePath;
    }

    @Inject
    public DataSource dataSource;

    public static void main(String[] args) throws Exception {
        System.setProperty("oracle.jdbc.fanEnabled", "false");
        if(Arrays.asList(args).contains("dequeue")) {
            System.setProperty("consumer.enabled", "true");
            System.setProperty("oracle.jms.maxSleepTime", "1000");
        }

        CommandLine parsedArgs = CommandLine.parse(args);
        Map<String, Object> options = parsedArgs.getUndeclaredOptions();

        if(options.containsKey("u")) System.setProperty("datasources.default.username", options.get("u").toString());
        if(options.containsKey("username")) System.setProperty("datasources.default.username", options.get("username").toString());
        if(options.containsKey("p")) System.setProperty("datasources.default.password", options.get("p").toString());
        if(options.containsKey("password")) System.setProperty("datasources.default.password", options.get("password").toString());
        if(options.containsKey("q")) System.setProperty("aq.queue.name", options.get("q").toString());
        if(options.containsKey("queue-name")) System.setProperty("aq.queue.name", options.get("queue-name").toString());

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
        }
        if(options.containsKey("U") || options.containsKey("url")) {
            if(options.containsKey("U")) System.setProperty("datasources.default.url", options.get("U").toString());
            if(options.containsKey("url")) System.setProperty("datasources.default.url", options.get("url").toString());
        }

        if(options.containsKey("P")) System.setProperty("oci.config.profile", options.get("P").toString());
        if(options.containsKey("oci-profile")) System.setProperty("oci.config.profile", options.get("oci-profile").toString());
        if(options.containsKey("i")) System.setProperty("oci.config.path", options.get("i").toString());
        if(options.containsKey("oci-profile-path")) System.setProperty("oci.config.path", options.get("oci-profile-path").toString());

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

        LOG.info("Connecting to queue...");
        PicocliRunner.run(AqCliCommand.class, args);
    }

    public void run() {
        Connection connection = null;
        try {
            connection = this.dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select sysdate from dual");
            resultSet.next();
            System.out.println(resultSet.getDate(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
