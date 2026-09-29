package vn.edu.crs.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate mặc định (SimpleClientHttpRequestFactory) KHÔNG hỗ trợ PATCH,
     * mà API trừ/hoàn kho của product-service dùng PATCH -> dùng HttpClient của JDK.
     * Có timeout để khi product-service tắt, order-service báo lỗi ngay thay vì treo.
     */
    @Bean
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));
        return new RestTemplate(factory);
    }
}
