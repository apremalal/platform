var getTemplateFile = function() {
    return "tmpl/search/template.jag";
};

var initialize = function (jagg) {
    addHeaderJS(global, "search", "search-events", "tmpl/search/js/search-key.js");
};

var getData = function (params) {
    return {};
};

var getParams = function () {
    return {};
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};