package vn.edu.smarthome.productservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    // Quan hệ N-1: Nhiều Product thuộc 1 Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;
}