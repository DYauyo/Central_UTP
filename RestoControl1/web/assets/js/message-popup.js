(() => {
    const messages = Array.from(document.querySelectorAll('[data-message-popup]'));
    if (messages.length === 0 || typeof HTMLDialogElement === 'undefined') {
        return;
    }

    const language = document.documentElement.lang.split('-')[0];
    const labels = {
        es: ['Mensaje', 'Aceptar'],
        en: ['Message', 'OK'],
        it: ['Messaggio', 'OK'],
        pt: ['Mensagem', 'OK']
    };
    const [titleLabel, closeLabel] = labels[language] || labels.es;

    const dialog = document.createElement('dialog');
    dialog.className = 'message-popup';
    dialog.setAttribute('aria-labelledby', 'message-popup-title');
    dialog.setAttribute('aria-describedby', 'message-popup-body');

    const title = document.createElement('h2');
    title.id = 'message-popup-title';
    title.textContent = titleLabel;

    const body = document.createElement('div');
    body.id = 'message-popup-body';
    for (const message of messages) {
        const copy = message.cloneNode(true);
        copy.querySelectorAll('button').forEach(button => button.remove());
        const paragraph = document.createElement('p');
        paragraph.className = 'message-popup-text';
        paragraph.dataset.tone = message.dataset.messagePopup;
        paragraph.textContent = copy.textContent.trim();
        body.append(paragraph);
    }

    const form = document.createElement('form');
    form.method = 'dialog';
    const close = document.createElement('button');
    close.className = 'btn btn-resto';
    close.type = 'submit';
    close.autofocus = true;
    close.textContent = closeLabel;
    form.append(close);

    dialog.append(title, body, form);
    document.body.append(dialog);
    dialog.addEventListener('close', () => dialog.remove(), { once: true });
    dialog.showModal();
    messages.forEach(message => message.remove());
})();
