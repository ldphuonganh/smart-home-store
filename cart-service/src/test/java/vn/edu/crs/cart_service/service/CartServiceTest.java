package vn.edu.crs.cart_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.crs.cart_service.client.ProductClient;
import vn.edu.crs.cart_service.dto.CartDTO;
import vn.edu.crs.cart_service.entity.Cart;
import vn.edu.crs.cart_service.entity.CartItem;
import vn.edu.crs.cart_service.repository.CartRepository;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit test CartService: giả lập product-service + DB bằng Mockito. */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(7L);
        lenient().when(cartRepository.findByUserId(7L)).thenReturn(Optional.of(cart));
        lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void addItemReturnsNewLineWithRealPriceAndTotal() {
        ProductClient.ProductInfo den = product(1L, "Đèn LED", "150000", 10);
        when(productClient.getRequired(1L)).thenReturn(den);
        when(productClient.find(1L)).thenReturn(Optional.of(den));

        CartDTO dto = cartService.addItem(7L, 1L, 2);

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getProductName()).isEqualTo("Đèn LED");
        assertThat(dto.getTotalAmount()).isEqualByComparingTo("300000");
        assertThat(dto.getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void addingSameProductAccumulatesQuantity() {
        ProductClient.ProductInfo den = product(1L, "Đèn LED", "150000", 10);
        when(productClient.getRequired(1L)).thenReturn(den);
        when(productClient.find(1L)).thenReturn(Optional.of(den));

        cartService.addItem(7L, 1L, 2);
        CartDTO dto = cartService.addItem(7L, 1L, 3);

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void cannotAddMoreThanStock() {
        when(productClient.getRequired(1L)).thenReturn(product(1L, "Khoá cửa", "3490000", 2));
        assertThatThrownBy(() -> cartService.addItem(7L, 1L, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chi con 2");
    }

    @Test
    void cannotAddOutOfStockProduct() {
        when(productClient.getRequired(1L)).thenReturn(product(1L, "Cảm biến", "159000", 0));
        assertThatThrownBy(() -> cartService.addItem(7L, 1L, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("het hang");
    }

    @Test
    void cannotUpdateItemOfAnotherCart() {
        assertThatThrownBy(() -> cartService.updateItem(7L, 999L, 1))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deletedProductIsShownAsUnavailableAndExcludedFromTotal() {
        CartItem item = new CartItem();
        item.setId(5L);
        item.setCart(cart);
        item.setProductId(42L);
        item.setQuantity(1);
        cart.getItems().add(item);
        when(productClient.find(42L)).thenReturn(Optional.empty());

        CartDTO dto = cartService.getCart(7L);

        assertThat(dto.getItems().get(0).isAvailable()).isFalse();
        assertThat(dto.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static ProductClient.ProductInfo product(Long id, String name, String price, int stock) {
        ProductClient.ProductInfo p = new ProductClient.ProductInfo();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        return p;
    }
}
