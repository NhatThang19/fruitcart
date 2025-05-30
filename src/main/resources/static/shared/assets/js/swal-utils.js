/**
 * Hiển thị hộp thoại xác nhận và ngăn submit form mặc định
 * @param {Event} event - Event object từ onclick (bắt buộc nếu dùng với submit button)
 * @param {string} title - Tiêu đề hộp thoại
 * @param {string} text - Nội dung mô tả
 * @param {string} formId - ID của form cần submit (nếu có)
 * @param {string} redirectUrl - URL chuyển hướng (nếu không dùng form)
 */

function showConfirm(
  event = null,
  title,
  text,
  formId = null,
  redirectUrl = null
) {
  // Ngăn hành vi mặc định nếu event được truyền vào
  if (event) {
    event.preventDefault();
  }

  Swal.fire({
    title: title || "Bạn chắc chắn?",
    text: text || "Thao tác này không thể hoàn tác!",
    icon: "warning", // Sẽ đổi thành 'question' hoặc 'info' cho trường hợp lưu
    showCancelButton: true,
    confirmButtonColor: "#3085d6", // Màu cho nút xác nhận chung
    cancelButtonColor: "#d33",
    confirmButtonText: "Xác nhận", // Text nút xác nhận chung
    cancelButtonText: "Huỷ",
  }).then((result) => {
    if (result.isConfirmed) {
      if (formId) {
        const formToSubmit = document.getElementById(formId);
        if (formToSubmit) {
          formToSubmit.submit();
        } else {
          console.error('Form with id "' + formId + '" not found.');
          Swal.fire("Lỗi!", "Không tìm thấy form để gửi đi.", "error");
        }
      } else if (redirectUrl) {
        window.location.href = redirectUrl;
      }
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
