package codes.recursive;

import codes.recursive.queue.AqProducer;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.inject.Inject;

@Command(name = "enqueue", description = "Enqueues a message to AQ")
@Requires(missingProperty = "aq.help")
public class EnqueueCommand implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(EnqueueCommand.class);
    private String message;

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @Inject
    public AqProducer aqProducer;

    @CommandLine.ParentCommand
    AqCliCommand aqCliCommand;

    @Option(names = {"-m", "--message"},
            description = "The message to enqueue",
            required = true)
    public void setMessage(String message) {
        if (message.length() == 0) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "You must pass a message!");
        }
        this.message = message;
    }

    @Override
    public void run() {
        LOG.info("Enqueuing to '{}'...", aqCliCommand.queueName);
        aqProducer.send(message);
        LOG.info("Message Enqueued!");
    }
}
