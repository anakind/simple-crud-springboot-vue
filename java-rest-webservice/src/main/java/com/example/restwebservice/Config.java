package com.example.restwebservice;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration class for setup
 */

@Configuration
public class Config implements WebMvcConfigurer {

    public static final String ES_INDEX = "testindex";
    public static final String ES_CONTACT = "contact";

    @Value("${elasticsearch.host:localhost}")
    public String host;
    @Value("${elasticsearch.port:9300}")
    public int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * To avoid CORS policy issue with same address
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD");
    }


    /**
     * Create a TransportClient for ElasticSearch engine
     *
     * @return Client
     */
    @Bean
    public Client client(){
        TransportClient client = null;
        try{
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

            /*final IndicesExistsResponse res = client.admin().indices().prepareExists(ES_INDEX).execute().actionGet();
            if (!res.isExists()) {
                final CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(ES_INDEX);

                // MAPPING GOES HERE
                final XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().startObject().startObject(ES_CONTACT)
                        .startObject("_timestamp")
                        .field("enabled", "true")
                        .field("store", "yes").endObject().endObject()
                        .endObject();
                System.out.println(mappingBuilder.string());
                createIndexRequestBuilder.addMapping(ES_CONTACT, mappingBuilder);

                // MAPPING DONE
                createIndexRequestBuilder.execute().actionGet();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }
}
