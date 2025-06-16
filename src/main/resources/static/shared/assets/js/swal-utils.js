$(document).ready(function () {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has("login_success")) {
    Swal.fire({
      icon: "success",
      title: "Đăng nhập thành công!",
      showConfirmButton: false,
      timer: 3000,
      toast: true,
      timerProgressBar: true,
      position: "top-end",
    });

    const newUrl = window.location.pathname;
    history.replaceState(null, null, newUrl);
  }
});

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
      document.getElementById(formId).submit();
    }
  });
}

function showToast(message, type = "success") {
  const toast = Swal.mixin({
    toast: true,
    position: "top-end",
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
  });
  toast.fire({ icon: type, title: message });
}
