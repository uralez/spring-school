document.addEventListener('DOMContentLoaded', function () {
    renderMathInElement(document.body, {
        delimiters: [
            {left: '$$', right: '$$', display: true},
            {left: '$', right: '$', display: false}
        ]
    });

    function fitText(el, minPt) {
        var size = parseFloat(getComputedStyle(el).fontSize);
        var min = minPt * 96 / 72;
        while (el.scrollHeight > el.clientHeight + 1 && size > min) {
            size -= 0.5;
            el.style.fontSize = size + 'px';
        }
    }

    function fitTopic(el) {
        var size = parseFloat(getComputedStyle(el).fontSize);
        var min = 8 * 96 / 72;
        while (el.scrollWidth > el.clientWidth + 1 || el.scrollHeight > el.clientHeight * 2.2) {
            size -= 0.5;
            el.style.fontSize = size + 'px';
            if (size <= min) { break; }
        }
    }

    document.querySelectorAll('.fit').forEach(fitTopic);
    document.querySelectorAll('.fit-body').forEach(function (el) { fitText(el, 6); });
});