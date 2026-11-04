(function () {
    var dialog = document.getElementById('prompt-dialog');
    if (!dialog) {
        return;
    }
    var subject = document.getElementById('p-subject');
    var cls = document.getElementById('p-class');
    var topic = document.getElementById('p-topic');
    var count = document.getElementById('p-count');
    var lang = document.getElementById('p-lang');
    var generate = document.getElementById('p-generate');
    var text = document.getElementById('p-text');
    var copy = document.getElementById('p-copy');

    function chapterName(t) {
        return (t || '').replace(/\s*\(\d+\s+of\s+\d+\)\s*$/, '');
    }

    function totalLessons(t) {
        var m = (t || '').match(/\((\d+)\s+of\s+(\d+)\)/);
        return m ? m[2] : '';
    }

    function ready() {
        generate.disabled = !(subject.value.trim() && cls.value.trim() && topic.value.trim()
            && parseInt(count.value, 10) > 0);
    }

    function buildPrompt() {
        var main = lang.value === 'RU' ? 'Russian' : 'English';
        var other = lang.value === 'RU' ? 'English' : 'Russian';
        var prev = dialog.dataset.prev;
        var startRule = prev
            ? 'CLASSWORK START: 3 very easy tasks for the first 5 minutes, on the topic of the PREVIOUS lesson. '
              + 'For the first lesson of this series the previous topic is: "' + prev + '". '
              + 'For the other lessons it is the previous lesson of this series.'
            : 'CLASSWORK START: 3 very easy tasks for the first 5 minutes, on the topic of the previous lesson of this series. '
              + 'For the first lesson leave this part empty.';
        return [
            'You are helping a teacher prepare lessons. Subject: ' + subject.value.trim()
                + '. Class: ' + cls.value.trim() + '.',
            'Attached are screenshots of the textbook pages for the topic: ' + topic.value.trim() + '.',
            'Write ' + count.value + ' lessons of 45 minutes on this topic, in order.',
            '',
            'Rules:',
            '1. Use your own words and your own examples. Do not copy sentences, tasks or numbers from the textbook.',
            '2. The main language of the lesson is ' + main + '. Use simple language. Every lecture paragraph is '
                + 'followed by its translation into ' + other + ' as a separate paragraph that starts with "> ". ',
//                + 'The translation must be complete: never write "see the formula", write the formula again.',
            '3. Formulas in LaTeX: $x^2$ inside a line, $$x^2$$ on a separate line. Do not use \\( \\) or \\[ \\]. '
                + 'Write units like $\\text{m/s}$.',
            '4. Output each lesson as a separate code block, so it can be copied.',
            '5. Every lesson has exactly these parts, in this order, and every part starts with its marker line:',
            '',
            '=== TOPIC ===',
            '(one line: the lesson topic)',
            '=== CLASSWORK START ===',
            '=== LECTURE ===',
            '=== CLASSWORK JOINT ===',
            '=== CLASSWORK END ===',
            '=== HOMEWORK ===',
            '',
            '6. ' + startRule,
            '7. LECTURE: the explanation, with a translation after every paragraph.',
            '8. CLASSWORK JOINT: 2-3 tasks to solve together in class, with short worked solutions.',
            '9. CLASSWORK END: 3 short tasks for the last 5 minutes.',
            '10. HOMEWORK: only 2 tasks, about 15-20 minutes in total. No answers.',
            '11. Do not write anything outside the code blocks.'
        ].join('\n');
    }

    document.getElementById('open-prompt').addEventListener('click', function () {
        subject.value = dialog.dataset.subject || '';
        cls.value = dialog.dataset.class || '';
        lang.value = dialog.dataset.lang === 'RU' ? 'RU' : 'EN';
        topic.value = chapterName(dialog.dataset.topic);
        count.value = totalLessons(dialog.dataset.topic);
        text.hidden = true;
        copy.hidden = true;
        ready();
        dialog.showModal();
    });

    [subject, cls, topic, count].forEach(function (el) {
        el.addEventListener('input', ready);
    });

    generate.addEventListener('click', function () {
        text.value = buildPrompt();
        text.hidden = false;
        copy.hidden = false;
    });

    copy.addEventListener('click', function () {
        navigator.clipboard.writeText(text.value);
    });

    document.getElementById('p-close').addEventListener('click', function () {
        dialog.close();
    });
})();