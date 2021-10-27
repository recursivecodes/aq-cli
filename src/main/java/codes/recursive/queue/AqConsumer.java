package codes.recursive.queue;

import io.micronaut.context.annotation.Requires;
import io.micronaut.jms.annotations.JMSListener;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.messaging.annotation.MessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JMSListener("aqConnectionFactory")
@Requires(property = "consumer.enabled")
@Requires(missingProperty = "aq.help")
public class AqConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(AqConsumer.class);
    @Queue(value = "${aq.queue.name}", concurrency = "1-5")
    public void receive(@MessageBody String body) {
        //System.out.println(body);
        LOG.info(body);
    }
}