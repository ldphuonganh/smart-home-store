package vn.edu.crs.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Chặn sớm request thiếu header Authorization cho các API cần đăng nhập.
 * (Mỗi service vẫn TỰ verify chữ ký JWT - Gateway chỉ lọc sớm cho nhẹ tải.)
 */
@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    /** Không cần đăng nhập (mọi method). */
    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/public/"          // Partner: dùng X-API-KEY, ApiKeyFilter kiểm tra
    );

    /** Chỉ cần đăng nhập khi KHÔNG phải GET (khách vẫn xem sản phẩm, danh mục, ảnh). */
    private static final List<String> PUBLIC_READ_PATHS = List.of(
            "/api/products",
            "/api/categories"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        boolean isPreflight = HttpMethod.OPTIONS.equals(method);
        boolean isOpen = OPEN_PATHS.stream().anyMatch(path::startsWith);
        boolean isPublicRead = HttpMethod.GET.equals(method)
                && PUBLIC_READ_PATHS.stream().anyMatch(path::startsWith);

        if (isPreflight || isOpen || isPublicRead) {
            return chain.filter(exchange);
        }

        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return GatewayErrors.write(exchange, HttpStatus.UNAUTHORIZED, "Ban chua dang nhap");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
