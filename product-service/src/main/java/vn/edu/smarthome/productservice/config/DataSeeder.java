package vn.edu.smarthome.productservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.productservice.entity.Category;
import vn.edu.smarthome.productservice.entity.Product;
import vn.edu.smarthome.productservice.repository.CategoryRepository;
import vn.edu.smarthome.productservice.repository.ProductRepository;

import java.math.BigDecimal;

/**
 * Tạo dữ liệu mẫu khi DB còn trống, để demo/test ngay không cần nhập tay.
 * Không chạy khi test (profile "test").
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }
        Category lighting = category("Chiếu sáng thông minh", "Bóng đèn, dải LED điều khiển qua app");
        Category security = category("An ninh", "Camera, khoá cửa, cảm biến");
        Category appliance = category("Thiết bị gia dụng", "Robot hút bụi, máy lọc không khí");
        Category control = category("Điều khiển trung tâm", "Hub, công tắc, ổ cắm thông minh");

        product("Bóng đèn LED thông minh Wi-Fi 9W", "189000", 120, lighting,
                "Đổi 16 triệu màu, hẹn giờ, điều khiển bằng giọng nói.");
        product("Dải đèn LED RGB 5m", "349000", 60, lighting,
                "Dải LED dán tường, đồng bộ theo nhạc.");
        product("Camera an ninh trong nhà 2K", "790000", 40, security,
                "Quay 2K, xoay 360 độ, đàm thoại 2 chiều.");
        product("Khoá cửa vân tay", "3490000", 15, security,
                "Mở khoá bằng vân tay, mật mã, thẻ từ và app.");
        product("Cảm biến cửa", "159000", 0, security,
                "Báo động khi cửa mở bất thường (đang hết hàng để test).");
        product("Robot hút bụi lau nhà", "5990000", 10, appliance,
                "Lập bản đồ bằng Lidar, tự động về dock sạc.");
        product("Máy lọc không khí thông minh", "2490000", 25, appliance,
                "Lọc HEPA H13, theo dõi chất lượng không khí trên app.");
        product("Bộ điều khiển trung tâm Zigbee", "690000", 30, control,
                "Kết nối và điều khiển tập trung các thiết bị Zigbee.");
        product("Ổ cắm thông minh 16A", "229000", 80, control,
                "Bật/tắt từ xa, đo điện năng tiêu thụ.");
        log.info("Đã tạo dữ liệu mẫu cho product-service");
    }

    private Category category(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        return categoryRepository.save(c);
    }

    private void product(String name, String price, int stock, Category category, String description) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setCategory(category);
        p.setDescription(description);
        productRepository.save(p);
    }
}
