package vn.edu.crs.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "shippingAddress khong duoc de trong")
    private String shippingAddress;

    @NotBlank(message = "phone khong duoc de trong")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9,10}$",
            message = "So dien thoai khong hop le"
    )
    private String phone;

    @NotBlank(message = "paymentMethod khong duoc de trong")
    private String paymentMethod;

    @NotEmpty(message = "Danh sach san pham khong duoc rong")
    @Valid
    private List<OrderItemRequestDTO> items;
}