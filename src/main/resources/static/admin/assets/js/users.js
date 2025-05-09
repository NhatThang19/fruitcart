$(document).ready(function () {
  let table = $("#userTable").DataTable({
    language: vietnameseLanguage ,
    serverSide: true,
    ajax: {
      url: "/api/users",
      type: "GET",
      data: function (d) {
        if (d.search.value) {
          d.columns[2].search.value = d.search.value;
          d.search.value = "";
        }
      },
    },
    columns: [
      { data: "id" },
      {
        data: "avatar",
        render: function (data, type, row) {
          return '<img src="' + data + '" alt="Avatar" class="cat-thumb" />';
        },
      },
      { data: "email" },
      { data: "username" },
      {
        data: "role.name",
        render: function (data, type, row) {
          if (data === "ADMIN") {
            return '<span class="text-info fw-semibold">ADMIN</span>';
          } else {
            return '<span class="text-success fw-semibold">USER</span>';
          }
        },
      },
      {
        data: "active",
        render: function (data, type, row) {
          if (data === true) {
            return '<span class="text-primary fw-semibold">ACTIVE</span>';
          } else {
            return '<span class="text-danger fw-semibold">BANNED</span>';
          }
        },
      },
      {
        data: "lastModifiedDate",
        render: function (data, type, row) {
          return data ? new Date(data).toLocaleString() : "N/A";
        },
      },
      {
        data: "id",
        render: function (data, type, row) {
          return `
                        <div class="d-flex justify-content-center">
                            <button type="button" class="btn btn-outline-success dropdown-toggle dropdown-toggle-split" 
                                data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" 
                                data-display="static">
                                <span class="sr-only"><i class="ri-settings-3-line"></i></span>
                            </button>
                            <div class="dropdown-menu">
                                <a class="dropdown-item" href="/admin/users/detail/${data}">Xem</a>
                                <a class="dropdown-item" href="#">Sửa</a>
                                <a class="dropdown-item" href="#">Xoá</a>
                            </div>
                        </div>
                    `;
        },
      },
    ],
  });
});
