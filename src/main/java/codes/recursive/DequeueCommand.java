package codes.recursive;

import codes.recursive.queue.AqConsumer;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Introspected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "dequeue", description = "Dequeues messages from AQ (until interrupted with CTRL+C)")
@Requires(property = "consumer.enabled")
@Introspected
public class DequeueCommand implements Runnable  {
    private static final Logger LOG = LoggerFactory.getLogger(DequeueCommand.class);

    @CommandLine.ParentCommand
    public AqCliCommand aqCliCommand;

    @Override
    public void run() {
        LOG.info("Dequeuing from '{}'...", aqCliCommand.queueName);
        while(true) {/*wait*/}
    }
}
