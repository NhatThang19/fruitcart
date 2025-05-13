$(document).ready(function () {
  let table = $("#categoryTable").DataTable({
    language: vietnameseLanguage,
    serverSide: true,
    ajax: {
      url: "/api/categories",
      type: "GET",
      data: function (d) {
        if (d.search.value) {
          d.columns[1].search.value = d.search.value;
          d.search.value = "";
        }
      },
    },
    columns: [
      { data: "id" },
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
        data: "subCategoryCount",
        render: function (data, type, row) {
          return `<span class="cr-sub-cat-count" data-bs-toggle="tooltip" data-bs-original-title="Total Sub Categories">${data}</span>`;
        },
        orderable: false,
        searchable: false,
        width: "8%",
      },
      {
        data: "subCategoryNames",
        render: function (data, type, row) {
          if (!data) return '<span class="text-muted">Không có</span>';

          const subCategories = data.split(", ");

          const spans = subCategories
            .map((name) => `<span class="cr-sub-cat-tag">${name.trim()}</span>`)
            .join("");

          return `<div class="d-flex flex-wrap">${spans}</div>`;
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
                <a class="dropdown-item" href="/admin/categories/edit?id=${data}">Sửa</a>
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
