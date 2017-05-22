package org.opengeoportal.dataingest.api;

import javax.jms.ConnectionFactory;

import org.opengeoportal.dataingest.security.JWTAuthenticationFilter;
import org.opengeoportal.dataingest.security.JWTLoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.cache.annotation.EnableCaching;

/**
 * Creates a RESTfull servrlet application. Introduces a default context.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since 2017-01-11
 */
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
@SpringBootApplication
@EnableCaching
@RestController
@EnableAutoConfiguration
public class DataIngestApplication {
    /**
     * This is the main method which runs the web application.
     *
     * @param args
     *            Unused.
     * @throws Exception
     *             General exception
     */

    public static void main(final String[] args) throws Exception {
        final ConfigurableApplicationContext context = SpringApplication
                .run(DataIngestApplication.class, args);
    }

    /**
     * Container factory.
     *
     * @param connectionFactory
     *            the connection factory
     * @param configurer
     *            the configurer
     * @return the default jms listener container factory
     */
    @Bean
    public JmsListenerContainerFactory<?> myContainerFactory(
            final ConnectionFactory connectionFactory,
            final DefaultJmsListenerContainerFactoryConfigurer configurer) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all boot's default to this factory, including the
        // message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    /**
     * Jackson jms message converter.
     *
     * @return the message converter
     */
    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        final MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    

    @Configuration
    @EnableWebSecurity
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeRequests()
            .antMatchers(HttpMethod.GET, "/datasets").anonymous()
            .antMatchers(HttpMethod.POST, "/datasets").anonymous()
            .antMatchers(HttpMethod.GET, "/allDatasets").anonymous()
            .antMatchers(HttpMethod.POST, "/allDatasets").anonymous()
            .antMatchers(HttpMethod.GET, "/allDatasetsMockup").anonymous()
            .antMatchers(HttpMethod.POST, "/allDatasetsMockup").anonymous()
            .antMatchers(HttpMethod.GET, "/workspaces/*").anonymous()
            .antMatchers(HttpMethod.POST, "/workspaces/*").anonymous()
            .antMatchers(HttpMethod.GET, "/datasets").anonymous()
            .antMatchers(HttpMethod.POST, "/datasets").anonymous()
            .antMatchers(HttpMethod.POST, "/login").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(new JWTLoginFilter("/login", authenticationManager()),
                    UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JWTAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class);
        }    
        
        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth
                .inMemoryAuthentication()
                    .withUser("user").password("password").roles("USER");
        }
    }

}
