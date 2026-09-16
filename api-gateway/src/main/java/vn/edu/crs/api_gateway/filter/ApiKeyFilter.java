package vn.edu.crs.api_gateway.filter;

import vn.edu.crs.api_gateway.cache.ApiKeyValidationCache;
import vn.edu.crs.api_gateway.client.AuthServiceClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    private final AuthServiceClient authServiceClient;
    private final ApiKeyValidationCache cache;

    private static final String PARTNER_PATH = "/api/public/courses";
    private static final String REQUIRED_SCOPE = "courses:read";

    // Constructor thay thế cho @RequiredArgsConstructor
    public ApiKeyFilter(AuthServiceClient authServiceClient, ApiKeyValidationCache cache) {
        this.authServiceClient = authServiceClient;
        this.cache = cache;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.startsWith(PARTNER_PATH)) {
            return chain.filter(exchange);
        }

        String apiKey = request.getHeaders().getFirst("X-API-KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return reject(exchange);
        }

        String cacheKey = apiKey + ":" + REQUIRED_SCOPE;
        Boolean cached = cache.get(cacheKey);

        if (cached != null) {
            return cached ? chain.filter(exchange) : reject(exchange);
        }

        return authServiceClient.isValidForScope(apiKey, REQUIRED_SCOPE)
                .flatMap(valid -> {
                    cache.put(cacheKey, valid);
                    return valid ? chain.filter(exchange) : reject(exchange);
                });
    }

    private Mono<Void> reject(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}