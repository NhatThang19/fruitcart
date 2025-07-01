/**
 * File: cart-global.js
 * Chứa các hàm xử lý giỏ hàng dùng chung trên toàn bộ website.
 */

// Hàm tiện ích để định dạng tiền tệ
function formatCurrency(number) {
    if (typeof number !== 'number' || isNaN(number)) {
        return 'N/A';
    }
    return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(number);
}

// 1. Cập nhật số lượng trên icon giỏ hàng ở header
function updateHeaderCartCount() {
    // Giả sử bạn đang dùng jQuery
    $.ajax({
        url: '/api/v1/cart/count',
        type: 'GET',
        success: function (response) {
            // Dùng class selector để cập nhật tất cả các icon (cả mobile và desktop)
            $('.cr-cart-count').text(response.count || 0);
        },
        error: function () {
            // Nếu có lỗi, hiển thị dấu '!' để người dùng biết
            $('.cr-cart-count').text('!');
        }
    });
}

// 2. Hàm thêm sản phẩm vào giỏ hàng (ĐÂY LÀ HÀM BỊ THIẾU)
function addToCart(variantId, quantity = 1) {
    // Kiểm tra xem variantId có hợp lệ không
    if (!variantId) {
        // Sử dụng SweetAlert2 (dựa trên các file của bạn)
        Swal.fire('Thông báo', 'Sản phẩm này hiện chưa có sẵn để mua.', 'warning');
        return;
    }

    const requestData = {
        productVariantId: variantId,
        quantity: quantity
    };

    // Gửi yêu cầu AJAX để thêm sản phẩm
    $.ajax({
        url: '/api/v1/cart/items',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function (response) {
            // Cập nhật lại số lượng trên header
            updateHeaderCartCount();

            // Hiển thị thông báo thành công
            Swal.fire({
                icon: 'success',
                title: 'Thêm vào giỏ hàng thành công!',
                showConfirmButton: false,
                timer: 1500,
                toast: true,
                position: 'top-end'
            });
        },
        error: function (xhr) {
            // Hiển thị thông báo lỗi
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Đã có lỗi xảy ra, vui lòng thử lại.';
            Swal.fire('Lỗi', errorMessage, 'error');
        }
    });
}

function renderSidebarCart(cartData) {
    const itemList = $('#sidebar-cart-items-list');
    const grandTotalEl = $('#sidebar-cart-grand-total');

    itemList.empty(); // Xóa các item cũ

    if (!cartData || !cartData.items || cartData.items.length === 0) {
        itemList.html('<li><p class="text-center p-3">Giỏ hàng của bạn đang trống.</p></li>');
        grandTotalEl.text(formatCurrency(0));
        return;
    }

    // Lặp qua từng sản phẩm và tạo HTML
    cartData.items.forEach(item => {
        const itemHtml = `
            <li>
                <a href="/san-pham/${item.productSlug}" class="side-pro-img">
                    <img style="width: 20px" src="${item.productImageUrl || '/shared/assets/images/product/default-product.png'}" alt="${item.productName}">
                </a>
                <div class="side-pro-content">
                    <a href="/san-pham/${item.productSlug}" class="cart-pro-title">${item.productName}</a>
                    <p class="cart-pro-variant">${item.variantAttribute}</p>
                    <div class="cart-pro-price">
                        <span class="new-price">${item.quantity} x ${formatCurrency(item.unitPrice)}</span>
                    </div>
                </div>
                <a href="javascript:void(0)" class="remove-cart sidebar-remove-item" data-item-id="${item.cartItemId}" title="Xóa">
                    <i class="ri-close-line"></i>
                </a>
            </li>
        `;
        itemList.append(itemHtml);
    });

    // Cập nhật tổng tiền
    grandTotalEl.text(formatCurrency(cartData.totalPrice));
}


// 2. Hàm mở sidebar và tải dữ liệu
function openAndRenderSidebarCart() {
    // Thêm class 'open' để hiển thị sidebar (dựa trên CSS của template)
    $('.cr-cart-view').addClass('open');

    // Hiển thị loading spinner (tùy chọn)
    $('#sidebar-cart-items-list').html('<li><p class="text-center p-3">Đang tải...</p></li>');

    $.ajax({
        url: '/api/v1/cart',
        type: 'GET',
        success: function (response) {
            renderSidebarCart(response);
        },
        error: function () {
            $('#sidebar-cart-items-list').html('<li><p class="text-center p-3 text-danger">Lỗi khi tải giỏ hàng.</p></li>');
        }
    });
}

// 3. Hàm xóa sản phẩm khỏi sidebar cart
function removeSidebarItem(cartItemId) {
    $.ajax({
        url: `/api/v1/cart/items/${cartItemId}`,
        type: 'DELETE',
        success: function (response) {
            // Sau khi xóa thành công, vẽ lại sidebar và cập nhật header
            renderSidebarCart(response);
            updateHeaderCartCount();
        },
        error: function (xhr) {
            alert(xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi xóa sản phẩm.');
        }
    });
}

// 3. Thực thi các hàm cần thiết khi trang tải xong
$(document).ready(function () {
    // Cập nhật số lượng trên giỏ hàng ngay khi tải trang
    updateHeaderCartCount();

    // Gắn sự kiện click cho nút "Thêm vào giỏ" trên card sản phẩm
    $(document).on('click', '.add-to-cart-btn', function (e) {
        e.preventDefault();
        const variantId = $(this).data('product-variant-id');
        addToCart(variantId, 1);
    });

    // Gắn sự kiện cho nút "Thêm vào giỏ" trong Quick View modal
    $(document).on('click', '#quickViewAddToCartBtn', function () {
        const variantId = $(this).data('variant-id');
        const quantity = parseInt($('#quickViewQuantity').val()) || 1;
        addToCart(variantId, quantity);
        $('#quickview').modal('hide');
    });

    $('.Shopping-toggle').on('click', function (e) {
        e.preventDefault();
        openAndRenderSidebarCart();
    });

    // Đóng sidebar
    $('.close-cart').on('click', function () {
        $('.cr-cart-view').removeClass('open');
    });

    // Xóa sản phẩm khỏi sidebar
    $('#sidebar-cart-items-list').on('click', '.sidebar-remove-item', function () {
        const cartItemId = $(this).data('item-id');
        removeSidebarItem(cartItemId);
    });
});