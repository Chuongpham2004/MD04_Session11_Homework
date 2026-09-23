package org.example.inventoryservice.config;

import org.example.inventoryservice.service.RedisMessageSubscriber;
import org.example.inventoryservice.service.ShippingAlertSubscriber;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    // --- CẤU HÌNH CACHE CHO BÀI TẬP 1 & 2 ---
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    // --- CẤU HÌNH REDIS PUB/SUB ---
    @Bean
    public ChannelTopic alertTopic() {
        return new ChannelTopic("pharmacy-alerts");
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }


    @Bean
    public ChannelTopic shippingTopic() {
        return new ChannelTopic("shipping-alerts");
    }

    @Bean
    public MessageListenerAdapter shippingListenerAdapter(ShippingAlertSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,         // dành cho alertTopic cũ
            ChannelTopic alertTopic,                        // channel cũ
            MessageListenerAdapter shippingListenerAdapter, // dành cho vận chuyển
            ChannelTopic shippingTopic) {                   // shipping-alerts

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Lắng nghe cả 2 channel
        container.addMessageListener(listenerAdapter, alertTopic);
        container.addMessageListener(shippingListenerAdapter, shippingTopic);

        return container;
    }
}