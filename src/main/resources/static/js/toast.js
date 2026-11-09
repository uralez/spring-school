document.addEventListener('DOMContentLoaded', function () {
    var box = document.getElementById('flash');
    if (!box) { return; }
    var kind = box.dataset.kind;
    var text = box.dataset.text;
    if (!kind || !text) { return; }

    if (kind === 'success') {
        var t = document.createElement('div');
        t.className = 'toast toast-success';
        t.textContent = text;
        document.body.appendChild(t);
        setTimeout(function () { t.remove(); }, 5000);
        return;
    }

    var d = document.createElement('dialog');
    d.className = 'toast-dialog toast-' + kind;
    var h = document.createElement('h2');
    h.textContent = kind === 'error' ? box.dataset.titleError : box.dataset.titleWarning;
    var p = document.createElement('p');
    p.textContent = text;
    var b = document.createElement('button');
    b.type = 'button';
    b.textContent = 'OK';
    b.addEventListener('click', function () { d.close(); d.remove(); });
    d.append(h, p, b);
    document.body.appendChild(d);
    d.showModal();
});