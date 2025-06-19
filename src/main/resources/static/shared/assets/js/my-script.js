var FruitCart = FruitCart || {};

// MODULE 1: XỬ LÝ CHỌN ĐỊA CHỈ (ADDRESS SELECTOR)
FruitCart.AddressSelector = {
  host: "https://provinces.open-api.vn/api/",
  provinceEl: "#province",
  districtEl: "#district",
  wardEl: "#ward",
  provinceNameEl: "#provinceName",
  districtNameEl: "#districtName",
  wardNameEl: "#wardName",

  init: function (config) {
    const self = this;
    const savedData = config || {};
    self.initSelect2();
    self.bindEvents(savedData);

    self
      .callAPI(self.host + "?depth=1")
      .done(function (provinces) {
        self.renderData(self.provinceEl, provinces, savedData.provinceName);
      })
      .fail(function () {
        console.error("Lỗi khi tải danh sách tỉnh/thành phố.");
      });
  },

  initSelect2: function () {
    $(this.provinceEl + ", " + this.districtEl + ", " + this.wardEl).select2({
      theme: "bootstrap-5",
      placeholder: "Vui lòng chọn...",
    });
  },

  bindEvents: function (savedData) {
    const self = this;

    $(self.provinceEl).change(function () {
      self.resetSelect(self.districtEl, "");
      self.resetSelect(self.wardEl, "");

      const provinceName = $(self.provinceEl + " option:selected").text();
      $(self.provinceNameEl).val(provinceName);

      const provinceCode = $(this).val();
      if (provinceCode) {
        self
          .callAPI(self.host + "p/" + provinceCode + "?depth=2")
          .done(function (data) {
            self.renderData(
              self.districtEl,
              data.districts,
              savedData.districtName
            );
          });
      }
    });

    $(self.districtEl).change(function () {
      self.resetSelect(self.wardEl, "");

      const districtName = $(self.districtEl + " option:selected").text();
      $(self.districtNameEl).val(districtName);

      const districtCode = $(this).val();
      if (districtCode) {
        self
          .callAPI(self.host + "d/" + districtCode + "?depth=2")
          .done(function (data) {
            self.renderData(self.wardEl, data.wards, savedData.wardName);
          });
      }
    });

    $(self.wardEl).change(function () {
      const wardName = $(self.wardEl + " option:selected").text();
      $(self.wardNameEl).val(wardName);
    });
  },

  callAPI: function (api) {
    return $.ajax({
      url: api,
      method: "GET",
      dataType: "json",
    });
  },

  renderData: function (selectId, array, selectedName) {
    let select = $(selectId);
    select
      .empty()
      .append('<option value="" disabled selected>Vui lòng chọn...</option>');

    let selectedCode = null;
    if (array && Array.isArray(array)) {
      array.forEach((element) => {
        const isSelected = element.name === selectedName;
        if (isSelected) {
          selectedCode = element.code;
        }
        select.append(
          `<option value="${element.code}" ${isSelected ? "selected" : ""}>${
            element.name
          }</option>`
        );
      });
    }

    if (selectedCode) {
      select.val(selectedCode).trigger("change");
    }
  },

  resetSelect: function (selectId, placeholder) {
    let select = $(selectId);
    select.empty();
    select.append(`<option value="" disabled selected>${placeholder}</option>`);
    select.trigger("change");
  },
};

$(document).ready(function () {
  if ($("#province").length) {
    FruitCart.AddressSelector.init();
  }
});
