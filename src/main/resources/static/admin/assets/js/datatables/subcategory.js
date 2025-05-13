$(document).ready(function () {
  let table = $("#categoryTable").DataTable({
    language: vietnameseLanguage,
    serverSide: true,
    ajax: {
      url: "/api/subcategories",
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
        data: "category.name",
        render: function (data, type, row) {
          return `<span class="cr-sub-cat-list">
                    <span class="cr-sub-cat-tag">${data}</span>
                </span>`;
        },
        orderable: false,
      },
      {
        data: "name",
        render: function (data, type, row) {
          return `<span class="fw-semibold">${data}</span>`;
        },
      },
      {
        data: "slug",
        render: function (data, type, row) {
          return `<code>${data}</code>`;
        },
        orderable: false,
        searchable: false,
      },
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
                <a class="dropdown-item" href="/admin/subcategories/edit?id=${data}">Sửa</a>
                <a class="dropdown-item" onclick="showConfirm(event, 'Xác nhận xoá?', 'Bạn chắc chắn muốn xoá danh mục này?', null, '/admin/subcategories/delete?id=${data}')">Xoá</a>
              </div>
            </div>
          `;
        },
        orderable: false,
        searchable: false,
      },
    ],
    order: [[0, "desc"]],
  });
});
