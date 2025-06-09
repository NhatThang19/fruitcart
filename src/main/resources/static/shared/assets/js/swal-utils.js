/**
 * Hàm dùng chung để hiển thị hộp thoại xác nhận của SweetAlert2.
 * @param {string} formId - ID của form cần được submit khi người dùng xác nhận.
 * @param {string} title - Tiêu đề của hộp thoại (ví dụ: 'Bạn có chắc chắn?').
 * @param {string} text - Nội dung mô tả của hộp thoại.
 * @param {string} confirmButtonText - Chữ trên nút xác nhận (ví dụ: 'Vâng, hãy xóa nó!').
 */
function showConfirmDialog(formId, title, text, confirmButtonText) {
  Swal.fire({
    title: title,
    text: text,
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#d33",
    cancelButtonColor: "#6c757d",
    confirmButtonText: confirmButtonText,
    cancelButtonText: "Hủy bỏ",
  }).then((result) => {
    if (result.isConfirmed) {
      // Nếu người dùng xác nhận, tìm form bằng ID và submit nó.
      document.getElementById(formId).submit();
    }
  });
}

/**
 * Hiển thị toast thông báo tự động ẩn
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại thông báo (success, error, warning, info)
 */
function showToast(message, type = "success") {
  const toast = Swal.mixin({
    toast: true,
    position: "top-end",
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
      toast.addEventListener("mouseenter", Swal.stopTimer);
      toast.addEventListener("mouseleave", Swal.resumeTimer);
    },
  });
  toast.fire({ icon: type, title: message });
}
