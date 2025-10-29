# Webhook 通知（例如企业内部 API）
notification.webhook.url=https://api.company.com/notify/task
notification.webhook.token=Bearer abc123


kafka.bootstrap.servers=116.205.190.93:5092
mp.messaging.outgoing.kogito-deadline-events.connector=smallrye-kafka
mp.messaging.outgoing.kogito-deadline-events.topic=kogito-deadline-events
mp.messaging.outgoing.kogito-deadline-events.value.serializer=org.apache.kafka.common.serialization.StringSerializer

mp.messaging.incoming.kogito-deadline-consumer.connector=smallrye-kafka
mp.messaging.incoming.kogito-deadline-consumer.topic=kogito-deadline-events
mp.messaging.incoming.kogito-deadline-consumer.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer