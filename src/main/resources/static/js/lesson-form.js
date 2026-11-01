document.addEventListener('DOMContentLoaded', function () {
    var token = document.querySelector('meta[name="_csrf"]').content;
    var header = document.querySelector('meta[name="_csrf_header"]').content;
    var timer = null;

    function refresh(area) {
        var box = area.nextElementSibling;
        if (!area.value.trim()) {
            box.hidden = true;
            box.innerHTML = '';
            return;
        }
        var body = new URLSearchParams({text: area.value});
        var headers = {'Content-Type': 'application/x-www-form-urlencoded'};
        headers[header] = token;
        fetch('/admin/lesson/preview', {method: 'POST', headers: headers, body: body})
            .then(function (r) { return r.text(); })
            .then(function (html) {
                box.innerHTML = html;
                box.hidden = false;
                renderMathInElement(box, {
                    delimiters: [
                        {left: '$$', right: '$$', display: true},
                        {left: '$', right: '$', display: false}
                    ]
                });
            });
    }

    document.querySelectorAll('.md-input').forEach(function (area) {
        refresh(area);
        area.addEventListener('input', function () {
            clearTimeout(timer);
            timer = setTimeout(function () { refresh(area); }, 400);
        });
    });
});