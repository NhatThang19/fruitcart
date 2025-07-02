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

// MODULE 2: XỬ LÝ QUILL EDITOR - Module mới được thêm vào
FruitCart.QuillEditor = {
    quill: null,

    init: function () {
        const self = this;
        const toolbarOptions = [
            [{'header': [1, 2, 3, 4, false]}],
            ['bold', 'italic', 'underline', 'strike'],
            [{'list': 'ordered'}, {'list': 'bullet'}],
            [{'indent': '-1'}, {'indent': '+1'}],
            [{'color': []}, {'background': []}],
            [{'align': []}],
            ['link', 'video', 'image'],
            ['clean']
        ];

        self.quill = new Quill('#quill-editor', {
            modules: {
                toolbar: toolbarOptions
            },
            theme: 'snow'
        });

        self.bindFormSubmit();
    },

    bindFormSubmit: function () {
        const self = this;
        $("#createProductForm, #editProductForm").on('submit', function () {
            var $descriptionTextarea = $('textarea[name=description]');

            if (self.quill.getLength() > 1) {
                var quillHtml = self.quill.root.innerHTML;
                $descriptionTextarea.val(quillHtml);
            } else {
                $descriptionTextarea.val("");
            }
            return true;
        });
    }
};

/**
 * Định dạng các ô input có class 'format-as-money' thành dạng tiền tệ
 * và định dạng lại giá trị ban đầu của chúng.
 */
function setupCurrencyFormatting() {

    const moneyInputs = $('input.format-as-money');

    function formatValue(value) {
        if (!value) return '';
        let rawValue = value.toString().replace(/\D/g, '');
        return rawValue.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    }

    moneyInputs.each(function () {
        $(this).val(formatValue($(this).val()));
    });

    moneyInputs.on('input', function () {
        let originalCursorPos = this.selectionStart;
        let originalLength = this.value.length;

        let formattedValue = formatValue($(this).val());
        $(this).val(formattedValue);

        let newLength = this.value.length;
        let newCursorPos = originalCursorPos + (newLength - originalLength);
        this.setSelectionRange(newCursorPos, newCursorPos);
    });

    const form = moneyInputs.closest('form');

    if (form.length) {
        form.on('submit', function () {
            moneyInputs.each(function () {
                let formattedValue = $(this).val();
                if (formattedValue) {
                    let rawValue = formattedValue.replace(/\./g, '');
                    $(this).val(rawValue);
                }
            });
            return true;
        });
    }
}

$(document).ready(function () {
    if ($("#province").length) {
        FruitCart.AddressSelector.init();
    }

    if ($("#quill-editor").length) {
        FruitCart.QuillEditor.init();
    }

    setupCurrencyFormatting();
});
