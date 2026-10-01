package vn.edu.smarthome.productservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.smarthome.productservice.repository.CategoryRepository;
import vn.edu.smarthome.productservice.repository.ProductRepository;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test tích hợp product-service (chạy với H2, không cần MySQL).
 * Chạy trong IntelliJ: chuột phải file này -> Run, hoặc: mvnw test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductServiceApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String secret;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        adminToken = token("ADMIN");
        customerToken = token("CUSTOMER");
    }

    // ==================== PHÂN QUYỀN ====================

    @Test
    void guestCanViewProductsAndCategories() throws Exception {
        long categoryId = createCategory("Chiếu sáng");
        createProduct("Đèn LED", 150000, 10, categoryId);

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryName").value("Chiếu sáng"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productCount").value(1));
    }

    @Test
    void createProductWithoutTokenReturns401WithCommonErrorFormat() throws Exception {
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/products"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void customerCannotCreateProduct() throws Exception {
        long categoryId = createCategory("An ninh");
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Camera", 500000, 5, categoryId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken + "x")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CRUD + VALIDATION ====================

    @Test
    void adminCrudProduct() throws Exception {
        long categoryId = createCategory("Gia dụng");
        long id = createProduct("Robot hút bụi", 5990000, 3, categoryId);

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Robot hút bụi"))
                .andExpect(jsonPath("$.stock").value(3));

        mvc.perform(put("/products/{id}", id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Robot hút bụi X", 5490000, 7, categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Robot hút bụi X"))
                .andExpect(jsonPath("$.price").value(5490000))
                .andExpect(jsonPath("$.stock").value(7));

        mvc.perform(delete("/products/{id}", id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void duplicateProductNameReturns409() throws Exception {
        long categoryId = createCategory("Điều khiển");
        createProduct("Ổ cắm", 200000, 5, categoryId);
        long otherId = createProduct("Công tắc", 300000, 5, categoryId);

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("ổ CẮM", 1, 1, categoryId)))
                .andExpect(status().isConflict());

        // Sửa sản phẩm khác thành tên đã tồn tại cũng phải bị chặn
        mvc.perform(put("/products/{id}", otherId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Ổ cắm", 1, 1, categoryId)))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidProductReturns400WithFieldErrors() throws Exception {
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-1,\"stock\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists());
    }

    @Test
    void createProductWithUnknownCategoryReturns404() throws Exception {
        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson("Đèn", 1000, 1, 9999)))
                .andExpect(status().isNotFound());
    }

    // ==================== TÌM KIẾM / LỌC ====================

    @Test
    void searchAndFilterProducts() throws Exception {
        long light = createCategory("Chiếu sáng");
        long security = createCategory("An ninh");
        createProduct("Đèn LED phòng khách", 150000, 10, light);
        createProduct("Đèn ngủ", 90000, 0, light);
        createProduct("Camera ngoài trời", 1200000, 4, security);

        mvc.perform(get("/products").param("keyword", "đèn"))
                .andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(get("/products").param("categoryId", String.valueOf(security)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Camera ngoài trời"));

        mvc.perform(get("/products").param("minPrice", "100000").param("maxPrice", "200000"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Đèn LED phòng khách"));

        mvc.perform(get("/products").param("inStock", "true"))
                .andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(get("/products").param("sort", "price,asc").param("size", "2"))
                .andExpect(jsonPath("$.content[0].name").value("Đèn ngủ"))
                .andExpect(jsonPath("$.totalPages").value(2));

        mvc.perform(get("/products").param("minPrice", "500").param("maxPrice", "100"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/products").param("sort", "khongTonTai,asc"))
                .andExpect(status().isBadRequest());
    }

    // ==================== DANH MỤC ====================

    @Test
    void cannotDeleteCategoryThatStillHasProducts() throws Exception {
        long categoryId = createCategory("Chiếu sáng");
        long productId = createProduct("Đèn", 100000, 1, categoryId);

        mvc.perform(delete("/categories/{id}", categoryId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("1 sản phẩm")));

        // Sản phẩm vẫn còn (trước đây bị xoá dây chuyền)
        mvc.perform(get("/products/{id}", productId)).andExpect(status().isOk());

        mvc.perform(delete("/products/{id}", productId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/categories/{id}", categoryId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicateCategoryNameReturns409() throws Exception {
        createCategory("An ninh");
        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"an ninh\"}"))
                .andExpect(status().isConflict());
    }

    // ==================== API NỘI BỘ: TỒN KHO ====================

    @Test
    void reserveAndReleaseStock() throws Exception {
        long categoryId = createCategory("An ninh");
        long id = createProduct("Khoá cửa", 3490000, 5, categoryId);

        mvc.perform(get("/internal/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(5))
                .andExpect(jsonPath("$.price").value(3490000));

        mvc.perform(patch("/internal/products/{id}/reserve-stock", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(2));

        mvc.perform(patch("/internal/products/{id}/reserve-stock", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("không đủ hàng")));

        mvc.perform(patch("/internal/products/{id}/release-stock", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(5));

        mvc.perform(patch("/internal/products/{id}/reserve-stock", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/internal/products/{id}/reserve-stock", 9999)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    // ==================== PARTNER API ====================

    @Test
    void publicProductApiIsReadOnly() throws Exception {
        long categoryId = createCategory("Chiếu sáng");
        long id = createProduct("Đèn", 100000, 1, categoryId);

        mvc.perform(get("/public/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        mvc.perform(get("/public/products/{id}", id)).andExpect(status().isOk());
        mvc.perform(post("/public/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== ẢNH ====================

    @Test
    void uploadAndServeImage() throws Exception {
        long categoryId = createCategory("Chiếu sáng");
        long id = createProduct("Đèn", 100000, 1, categoryId);
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

        String body = mvc.perform(multipart("/products/{id}/image", id)
                        .file(new MockMultipartFile("file", "den.png", "image/png", png))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", startsWith("/api/products/images/")))
                .andReturn().getResponse().getContentAsString();

        String imageUrl = objectMapper.readTree(body).get("imageUrl").asText();
        String fileName = imageUrl.substring("/api/products/images/".length());

        mvc.perform(get("/products/images/{fileName}", fileName))
                .andExpect(status().isOk())
                .andExpect(content().bytes(png));

        mvc.perform(multipart("/products/{id}/image", id)
                        .file(new MockMultipartFile("file", "virus.exe", "application/octet-stream", png))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/products/images/{fileName}", "khong-co.png"))
                .andExpect(status().isNotFound());
    }

    // ==================== HÀM PHỤ ====================

    private String token(String role) {
        return Jwts.builder()
                .subject(role.toLowerCase() + "@smarthome.vn")
                .claims(Map.of("userId", 1, "email", role.toLowerCase() + "@smarthome.vn", "role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private long createCategory(String name) throws Exception {
        String body = mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createProduct(String name, long price, int stock, long categoryId) throws Exception {
        String body = mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(name, price, stock, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode node = objectMapper.readTree(body);
        return node.get("id").asLong();
    }

    private String productJson(String name, long price, int stock, long categoryId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", name,
                "price", price,
                "stock", stock,
                "categoryId", categoryId,
                "description", "Mô tả " + name));
    }
}
