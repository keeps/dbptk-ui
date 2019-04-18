$(document).ready(function() {
    if(/branding=false/.test(window.location.search)) {
        $("head").append('<link rel="stylesheet" type="text/css" href="api/v1/theme?resource_id=nobranding.css">');
    }
});
