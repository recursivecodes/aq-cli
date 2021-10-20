package codes.recursive.queue;

import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.jms.annotations.JMSConnectionFactory;
import oracle.jms.AQjmsFactory;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.sql.DataSource;

@Factory
@Introspected
public class AqJmsConfig {

    @ReflectiveAccess
    @Inject
    public DataSource dataSource;

    @Inject
    public AqJmsConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @JMSConnectionFactory("aqConnectionFactory")
    public ConnectionFactory connectionFactory() throws JMSException {
        return AQjmsFactory.getConnectionFactory(dataSource);
    }
}