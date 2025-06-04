/**
 * Hiển thị hộp thoại xác nhận và ngăn submit form mặc định
 * @param {Event} event - Event object từ onclick (bắt buộc nếu dùng với submit button)
 * @param {string} title - Tiêu đề hộp thoại
 * @param {string} text - Nội dung mô tả
 * @param {string} formId - ID của form cần submit (nếu có)
 * @param {string} redirectUrl - URL chuyển hướng (nếu không dùng form)
 */

function confirmDelete(buttonElement) {
  const formId = buttonElement.getAttribute("data-form-id");
  const userName = buttonElement.getAttribute("data-user-name"); // Lấy tên người dùng từ data attribute

  let message = "Bạn có chắc chắn muốn xoá mục này?";
  if (userName) {
    message = `Bạn có chắc chắn muốn xoá người dùng "${userName}"? Hành động này không thể hoàn tác!`;
  } else {
    message =
      "Bạn có chắc chắn muốn thực hiện hành động xoá này? Hành động này không thể hoàn tác!";
  }

  Swal.fire({
    title: "Xác nhận xoá",
    text: message,
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#d33",
    cancelButtonColor: "#3085d6",
    confirmButtonText: "Đồng ý xoá",
    cancelButtonText: "Huỷ bỏ",
  }).then((result) => {
    if (result.isConfirmed) {
      // Nếu người dùng xác nhận, tìm form bằng ID và submit nó
      const formToSubmit = document.getElementById(formId);
      if (formToSubmit) {
        formToSubmit.submit();
      } else {
        console.error("Không tìm thấy form với ID:", formId);
        Swal.fire(
          "Lỗi!",
          "Không thể tìm thấy form để thực hiện hành động xoá.",
          "error"
        );
      }
    }
  });
}

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
