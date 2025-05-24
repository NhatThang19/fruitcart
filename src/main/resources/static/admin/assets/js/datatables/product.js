$(document).ready(function () {
  let table = $("#productTable").DataTable({
    language: vietnameseLanguage,
    serverSide: true,
    ajax: {
      url: "/api/products",
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
        data: "productImgURL",
        render: function (data, type, row) {
          return '<img src="' + data + '" alt="Avatar" class="cat-thumb" />';
        },
        orderable: false,
        searchable: false,
      },
      {
        data: "name",
        render: function (data, type, row) {
          return `<span class="fw-semibold">${data}</span>`;
        },
      },
      { data: "basePrice" },
      {
        data: "productVariantCount",
        render: function (data, type, row) {
          return `<span class="cr-sub-cat-count" data-bs-toggle="tooltip" data-bs-original-title="Total Sub Categories">${data}</span>`;
        },
        orderable: false,
        searchable: false,
        width: "8%",
      },
      { data: "category", orderable: false, searchable: false },
      {
        data: "active",
        render: function (data, type, row) {
          return data === true
            ? '<span class="badge bg-success">Hoạt động</span>'
            : '<span class="badge bg-danger">Không hoạt động</span>';
        },
        orderable: false,
        searchable: false,
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
              </button>              <div class="dropdown-menu">
                <a class="dropdown-item" href="/admin/products/detail?id=${data}">Xem</a>
                <a class="dropdown-item" href="/admin/products/edit/${data}">Sửa</a>
                <a class="dropdown-item" onclick="showConfirm(event, 'Xác nhận xoá?', 'Bạn chắc chắn muốn xoá danh mục này?', null, '/admin/categories/delete?id=${data}')">Xoá</a>
              </div>
            </div>
          `;
        },
        orderable: false,
        searchable: false,
        width: "8%",
      },
    ],
    order: [[0, "desc"]],
  });
});
