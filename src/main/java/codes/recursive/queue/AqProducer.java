package codes.recursive.queue;

import io.micronaut.jms.annotations.JMSProducer;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.messaging.annotation.MessageBody;

@JMSProducer("aqConnectionFactory")
public interface AqProducer {
    @Queue("${aq.queue.name}")
    void send(@MessageBody String body);
}