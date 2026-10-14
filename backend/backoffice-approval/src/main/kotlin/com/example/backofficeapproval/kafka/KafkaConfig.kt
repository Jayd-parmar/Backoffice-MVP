package com.example.backofficeapproval.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import tools.jackson.databind.json.JsonMapper

@Configuration
@EnableKafka
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    private val jsonMapper: JsonMapper,
) {

    @Bean
    fun applicationDecidedTopic() = TopicBuilder.name(ApplicationTopics.DECIDED).partitions(1).replicas(1).build()

    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, Any> {
        val serializer = JacksonJsonSerializer<Any>(jsonMapper)
        serializer.setAddTypeInfo(false)
        val factory = DefaultKafkaProducerFactory<String, Any>(
            mapOf(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers),
        )
        factory.setKeySerializer(StringSerializer())
        factory.setValueSerializer(serializer)
        return factory
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, ApplicationSubmittedEvent> {
        val deserializer = JacksonJsonDeserializer(ApplicationSubmittedEvent::class.java, jsonMapper)
        deserializer.addTrustedPackages("com.example.backofficeapproval.kafka")
        deserializer.setUseTypeHeaders(false)
        return DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "backoffice-approval",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ),
            StringDeserializer(),
            deserializer,
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, ApplicationSubmittedEvent>,
    ): ConcurrentKafkaListenerContainerFactory<String, ApplicationSubmittedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ApplicationSubmittedEvent>()
        factory.setConsumerFactory(consumerFactory)
        return factory
    }
}
