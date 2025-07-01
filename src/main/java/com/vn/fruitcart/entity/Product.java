package com.vn.fruitcart.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.vn.fruitcart.entity.base.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", length = 255, unique = true)
    private String slug;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 0)
    private BigDecimal basePrice;

    @Column(name = "is_new", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isNew = true;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_id", nullable = false)
    private Origin origin;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    @Transient
    public ProductImage getMainImage() {
        if (this.images == null || this.images.isEmpty()) {
            return null;
        }

        return this.images.stream().filter(ProductImage::isMain)
                .findFirst().orElse(this.images.get(0));
    }

    @Transient
    public boolean isAvailable() {
        if (this.getVariants() == null || this.getVariants().isEmpty()) {
            return false;
        }

        return this.getVariants().stream()
                .anyMatch(variant -> variant.getInventory() != null && variant.getInventory().getQuantity() > 0);
    }

    @Transient // Đảm bảo JPA không map phương thức này vào cột DB
    public boolean isAnyVariantOnSale(User user) {
        if (getVariants() == null || getVariants().isEmpty()) {
            return false;
        }
        // Dùng Stream API để kiểm tra: có bất kỳ variant nào thỏa mãn điều kiện không?
        return getVariants().stream().anyMatch(variant -> variant.isOnSaleForUser(user));
    }

    /**
     * Lấy ra tag giảm giá hấp dẫn nhất để hiển thị.
     * Ví dụ: Nếu sản phẩm có biến thể giảm 10% và biến thể giảm 20%,
     * phương thức này sẽ trả về chuỗi "-20%".
     *
     * @param user Người dùng đang xem sản phẩm.
     * @return Chuỗi hiển thị % giảm giá cao nhất, hoặc chuỗi rỗng nếu không có giảm giá.
     */
    @Transient
    public String getDisplayableDiscountTag(User user) {
        if (getVariants() == null || getVariants().isEmpty()) {
            return "";
        }

        // Tìm khuyến mãi có % cao nhất trong tất cả các biến thể
        Optional<Discount> bestDiscount = getVariants().stream()
                .map(variant -> variant.getActiveDiscountForUser(user)) // Lấy Optional<Discount> từ mỗi variant
                .filter(Optional::isPresent)          // Chỉ giữ lại những Optional không rỗng
                .map(Optional::get)                   // Lấy đối tượng Discount ra
                .max(Comparator.comparing(Discount::getDiscountPercentage)); // Tìm cái có % cao nhất

        return bestDiscount
                .map(discount -> "-" + discount.getDiscountPercentage().stripTrailingZeros().toPlainString() + "%")
                .orElse(""); // Nếu không có discount nào, trả về chuỗi rỗng
    }

}
