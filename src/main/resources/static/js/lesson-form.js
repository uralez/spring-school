document.addEventListener('DOMContentLoaded', function () {
    var token = document.querySelector('meta[name="_csrf"]').content;
    var header = document.querySelector('meta[name="_csrf_header"]').content;

    var FIELDS = {
        'TOPIC': 'topic',
        'CLASSWORK START': 'classworkStart',
        'LECTURE': 'lecture',
        'CLASSWORK JOINT': 'classworkJoint',
        'CLASSWORK END': 'classworkEnd',
        'HOMEWORK': 'homework'
    };

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

    function parseLesson(text) {
        var result = {};
        var current = null;
        text.split(/\r?\n/).forEach(function (line) {
            var m = line.match(/^\s*===\s*([A-Z ]+?)\s*===\s*$/);
            if (m) {
                current = m[1];
                result[current] = [];
            } else if (current) {
                result[current].push(line);
            }
        });
        Object.keys(result).forEach(function (key) {
            result[key] = result[key].join('\n').trim();
        });
        return result;
    }

    document.querySelectorAll('.md-input').forEach(function (area) {
        var timer = null;
        refresh(area);
        area.addEventListener('input', function () {
            clearTimeout(timer);
            timer = setTimeout(function () { refresh(area); }, 400);
        });
    });

    document.getElementById('paste-apply').addEventListener('click', function () {
        var parsed = parseLesson(document.getElementById('paste-lesson').value);
        Object.keys(FIELDS).forEach(function (marker) {
            if (!(marker in parsed)) {
                return;
            }
            var field = document.querySelector('[name="' + FIELDS[marker] + '"]');
            if (!field) {
                return;
            }
            field.value = marker === 'TOPIC' ? parsed[marker].split('\n')[0] : parsed[marker];
            if (field.classList.contains('md-input')) {
                refresh(field);
            }
        });
    });
});