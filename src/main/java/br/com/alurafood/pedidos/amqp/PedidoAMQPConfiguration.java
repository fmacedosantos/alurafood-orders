package br.com.alurafood.pedidos.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoAMQPConfiguration {

    @Bean
    public RabbitAdmin criaRabbitAdmin(ConnectionFactory conn) {
        return new RabbitAdmin(conn);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> inicializaAdmin(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory conn, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(conn);

        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }

    @Bean
    public Queue filaDlqDetalhesPedidos() {
        return new Queue("pagamentos.detalhes-pedido-dlq", false);
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange("pagamentos.dlx");
    }

    @Bean
    public Binding bindDlxPagamentoPedido() {
        return BindingBuilder.bind(filaDlqDetalhesPedidos())
                .to(deadLetterExchange());
    }

    @Bean
    public Queue filaDetalhesPedidos() {
        return QueueBuilder
                .nonDurable("pagamentos.detalhes-pedido")
                .deadLetterExchange("pagamentos.dlx")
                .build();
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("pagamentos.exchange");
    }

    @Bean
    public Binding bindPagamentoPedido() {
        return BindingBuilder.bind(filaDetalhesPedidos())
                .to(fanoutExchange());
    }
}
