package vn.edu.smarthome.productservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.entity.ProductImage;
import vn.edu.smarthome.productservice.repository.CategoryRepository;
import vn.edu.smarthome.productservice.repository.ProductRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Tạo dữ liệu mẫu khi DB còn trống - lấy đúng catalog của SmartHome Store (Laravel):
 * 6 danh mục, 26 sản phẩm (DatabaseSeeder + ExtraCatalogSeeder), file seed/catalog.json.
 *
 * Ảnh mẫu nằm ở frontend (crs-frontend/public/images/...) nên imageUrl dạng "/images/products/x.jpg";
 * ảnh admin upload sau này nằm ở product-service: "/api/products/images/{file}".
 * Không chạy khi test (profile "test").
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }
        JsonNode root;
        try (InputStream in = new ClassPathResource("seed/catalog.json").getInputStream()) {
            root = objectMapper.readTree(in);
        }

        Map<String, Category> bySlug = new HashMap<>();
        for (JsonNode c : root.get("categories")) {
            Category category = new Category();
            category.setName(c.get("name").asText());
            category.setSlug(c.get("slug").asText());
            category.setDescription(text(c, "description"));
            category.setImage("/images/categories/" + c.get("image").asText());
            bySlug.put(category.getSlug(), categoryRepository.save(category));
        }

        for (JsonNode p : root.get("products")) {
            Product product = new Product();
            product.setName(p.get("name").asText());
            product.setSlug(p.get("slug").asText());
            product.setBrand(text(p, "brand"));
            product.setMaterial(text(p, "material"));
            product.setWeight(text(p, "weight"));
            product.setPower(text(p, "power"));
            product.setOrigin(text(p, "origin"));
            product.setDescription(text(p, "description"));
            product.setPrice(new BigDecimal(p.get("price").asText()));
            product.setStock(p.get("stock").asInt());
            product.setActive(true);
            product.setCategory(bySlug.get(p.get("category").asText()));
            int i = 0;
            for (JsonNode img : p.get("images")) {
                product.addImage(new ProductImage("/images/products/" + img.asText(), i == 0, i));
                i++;
            }
            product.syncPrimaryImage();
            productRepository.save(product);
        }
        log.info("Đã tạo dữ liệu mẫu SmartHome Store: {} danh mục, {} sản phẩm",
                categoryRepository.count(), productRepository.count());
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
