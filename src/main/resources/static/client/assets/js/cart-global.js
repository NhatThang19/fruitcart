function formatCurrency(number) {
    if (typeof number !== 'number' || isNaN(number)) {
        return 'N/A';
    }
    return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(number);
}

function updateHeaderCartCount() {
    $.ajax({
        url: '/api/v1/cart/count',
        type: 'GET',
        success: function (response) {
            $('.cr-cart-count').text(response.count || 0);
        },
        error: function () {
            $('.cr-cart-count').text('!');
        }
    });
}

function addToCart(variantId, quantity = 1) {
    if (!variantId) {
        return;
    }

    $.ajax({
        url: '/api/v1/cart/items',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({productVariantId: variantId, quantity: quantity}),
        success: function () {
            updateHeaderCartCount();
            openAndRenderSidebarCart();
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
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Đã có lỗi xảy ra.';
            Swal.fire('Lỗi', errorMessage, 'error');
        }
    });
}

function renderSidebarCart(cartData) {
    const itemList = $('#sidebar-cart-items-list');
    const grandTotalEl = $('#sidebar-cart-grand-total');

    itemList.empty();

    if (!cartData || !cartData.items || cartData.items.length === 0) {
        itemList.html('<li><p class="text-center p-3">Giỏ hàng của bạn đang trống.</p></li>');
        grandTotalEl.text(formatCurrency(0));
        return;
    }

    cartData.items.forEach(item => {
        const itemHtml = `
            <li>
                <a href="/san-pham/${item.productSlug}" class="crside_pro_img">
                    <img src="${item.productImageUrl || '/shared/assets/images/product/default-product.png'}" alt="${item.productName}">
                </a>
                <div class="cr-pro-content">
                    <a href="/san-pham/${item.productSlug}" class="cart_pro_title">${item.productName}</a>
                    <span class="cart-price"><span>${formatCurrency(item.unitPrice)}</span> x ${item.quantity}</span>
                     <p class="cart-pro-variant">${item.variantAttribute}</p>
                    <div class="cr-cart-qty">
                        <div class="cart-qty-plus-minus">
                            <button type="button" class="minus sidebar-qty-btn" data-item-id="${item.cartItemId}" data-change="-1">-</button>
                            <input type="text" value="${item.quantity}" class="quantity" readonly>
                            <button type="button" class="plus sidebar-qty-btn" data-item-id="${item.cartItemId}" data-change="1">+</button>
                        </div>
                    </div>
                    <a href="javascript:void(0)" class="remove sidebar-remove-item" data-item-id="${item.cartItemId}">×</a>
                </div>
            </li>
        `;
        itemList.append(itemHtml);
    });

    grandTotalEl.text(formatCurrency(cartData.totalPrice));
}

function updateSidebarItemQuantity(cartItemId, newQuantity) {
    if (newQuantity < 1) {
        return;
    }
    $.ajax({
        url: `/api/v1/cart/items/${cartItemId}`,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({quantity: newQuantity}),
        success: function (response) {
            renderSidebarCart(response);
            updateHeaderCartCount();
        },
        error: function (xhr) {
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi cập nhật số lượng.';
            showErrorToast(errorMessage);
        }
    });
}

function openAndRenderSidebarCart() {
    $('.cr-cart-view').addClass('open');
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

function removeSidebarItem(cartItemId) {
    $.ajax({
        url: `/api/v1/cart/items/${cartItemId}`,
        type: 'DELETE',
        success: function (response) {
            renderSidebarCart(response);
            updateHeaderCartCount();
        },
        error: function (xhr) {
            alert(xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi xóa sản phẩm.');
        }
    });
}


$(document).ready(function () {
    updateHeaderCartCount();

    $(document).on('click', '.add-to-cart-btn', function (e) {
        e.preventDefault();
        const variantId = $(this).data('product-variant-id');
        addToCart(variantId, 1);
    });

    $('.Shopping-toggle').on('click', function (e) {
        e.preventDefault();
        openAndRenderSidebarCart();
    });

    $('.close-cart').on('click', function () {
        $('.cr-cart-view').removeClass('open');
    });

    $('#sidebar-cart-items-list').on('click', '.sidebar-remove-item', function () {
        const cartItemId = $(this).data('item-id');
        removeSidebarItem(cartItemId);
    });

    $('#sidebar-cart-items-list').on('click', '.sidebar-qty-btn', function () {
        const button = $(this);
        const cartItemId = button.data('item-id');
        const change = button.data('change');
        const input = button.siblings('.quantity');
        const currentQuantity = parseInt(input.val());
        const newQuantity = currentQuantity + change;

        updateSidebarItemQuantity(cartItemId, newQuantity);
    });
});