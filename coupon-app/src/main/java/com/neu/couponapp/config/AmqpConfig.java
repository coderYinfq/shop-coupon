package com.neu.couponapp.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，在这里声明交换机、队列等
 */

@Configuration
public class AmqpConfig {
    //注入交换机
    @Bean
    public DirectExchange myChange(){
        return new DirectExchange("BootDirectExchange",true,false);
    }
    //注入队列
    @Bean
    public Queue myQueue(){
        return new Queue("bootDirectQueue",true);
    }
    //绑定
    @Bean
    public Binding directBinding(Queue myQueue,DirectExchange myChange){
        return BindingBuilder.bind(myQueue).to(myChange).with("BootRouting");
    }


}
