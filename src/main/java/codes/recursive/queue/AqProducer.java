package codes.recursive.queue;

import io.micronaut.context.annotation.Requires;
import io.micronaut.jms.annotations.JMSProducer;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.messaging.annotation.MessageBody;

@JMSProducer("aqConnectionFactory")
@Requires(missingProperty = "aq.help")
public interface AqProducer {
    @Queue("${aq.queue.name}")
    void send(@MessageBody String body);
}