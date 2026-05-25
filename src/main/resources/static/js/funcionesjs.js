const dropdowns = document.querySelectorAll('.dropdown');

if (localStorage.getItem('salmaColorMode') === 'accessible') {
    document.body.classList.add('color-accessible');
}

const colorModeButtons = document.querySelectorAll('[data-color-mode]');

colorModeButtons.forEach(button => {
    button.setAttribute('aria-pressed', document.body.classList.contains('color-accessible').toString());
    button.addEventListener('click', () => {
        document.body.classList.toggle('color-accessible');
        const enabled = document.body.classList.contains('color-accessible');
        localStorage.setItem('salmaColorMode', enabled ? 'accessible' : 'default');
        document.querySelectorAll('[data-color-mode]').forEach(item => item.setAttribute('aria-pressed', enabled.toString()));
    });
});

dropdowns.forEach(dropdown => {

    dropdown.addEventListener('mouseenter', () => {

        // cerrar todos
        dropdowns.forEach(d => {
            d.classList.remove('show');
            d.querySelector('.dropdown-menu').classList.remove('show');
        });

        // abrir el actual
        dropdown.classList.add('show');
        dropdown.querySelector('.dropdown-menu').classList.add('show');
    });

    dropdown.addEventListener('mouseleave', () => {
        dropdown.classList.remove('show');
        dropdown.querySelector('.dropdown-menu').classList.remove('show');
    });

});

const cartDrawer = document.querySelector('#cartDrawer');
const cartOpenButtons = document.querySelectorAll('[data-cart-open]');
const cartCloseButtons = document.querySelectorAll('[data-cart-close]');

function openCartDrawer() {
    if (!cartDrawer) {
        return;
    }
    cartDrawer.classList.add('open');
    cartDrawer.setAttribute('aria-hidden', 'false');
    document.body.classList.add('cart-open');
}

function closeCartDrawer() {
    if (!cartDrawer) {
        return;
    }
    cartDrawer.classList.remove('open');
    cartDrawer.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('cart-open');
}

cartOpenButtons.forEach(button => {
    button.addEventListener('click', openCartDrawer);
});

cartCloseButtons.forEach(button => {
    button.addEventListener('click', closeCartDrawer);
});

document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        closeCartDrawer();
    }
});

if (new URLSearchParams(window.location.search).get('cart') === 'open') {
    openCartDrawer();
}

const cartSelectionForms = document.querySelectorAll('[data-cart-selection-form]');
const cartTotal = document.querySelector('[data-cart-selected-total]');
const checkoutButton = document.querySelector('[data-checkout-button]');
const freeShippingMessage = document.querySelector('[data-free-shipping-message]');

function formatCop(value) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
    }).format(value).replace('COP', '$').trim();
}

function refreshCartTotal() {
    if (!cartTotal) {
        return;
    }
    const subtotal = Array.from(document.querySelectorAll('.cart-row')).reduce((sum, row) => {
        const checkbox = row.querySelector('[data-cart-selection-checkbox]');
        const lineTotal = Number(row.dataset.unitPrice || 0) * Number(row.dataset.quantity || 0);
        return checkbox && checkbox.checked ? sum + lineTotal : sum;
    }, 0);
    const threshold = Number(freeShippingMessage?.dataset.threshold || 100000);
    const shipping = subtotal > 0 && subtotal < threshold ? Number(freeShippingMessage?.dataset.shippingCost || 12000) : 0;
    const selectedTotal = subtotal + shipping;
    cartTotal.textContent = formatCop(selectedTotal);
    if (freeShippingMessage) {
        if (subtotal <= 0) {
            freeShippingMessage.textContent = `Compra minimo ${formatCop(threshold)} para obtener envio gratis.`;
        } else if (shipping === 0) {
            freeShippingMessage.textContent = `Envio gratis aplicado por compra desde ${formatCop(threshold)}.`;
        } else {
            freeShippingMessage.textContent = `Compra minimo ${formatCop(threshold)} para obtener envio gratis. Te faltan ${formatCop(threshold - subtotal)}.`;
        }
    }
    if (checkoutButton) {
        checkoutButton.disabled = selectedTotal <= 0;
    }
}

cartSelectionForms.forEach(form => {
    const checkbox = form.querySelector('[data-cart-selection-checkbox]');
    if (!checkbox) {
        return;
    }
    checkbox.addEventListener('change', async () => {
        refreshCartTotal();
        const csrfInput = form.querySelector('input[name^="_csrf"]');
        const itemIdInput = form.querySelector('input[name="itemId"]');
        const body = new URLSearchParams();
        if (csrfInput) {
            body.set(csrfInput.name, csrfInput.value);
        }
        if (itemIdInput) {
            body.set('itemId', itemIdInput.value);
        }
        body.set('selected', checkbox.checked ? 'true' : 'false');
        try {
            await fetch(form.action, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest'},
                body
            });
        } catch (error) {
            checkbox.checked = !checkbox.checked;
            refreshCartTotal();
        }
    });
});

refreshCartTotal();

const cardNumberInput = document.querySelector('[data-card-number]');
const cardNameInput = document.querySelector('[data-card-name]');
const cardExpInput = document.querySelector('[data-card-exp]');
const cardPreviewNumber = document.querySelector('[data-card-preview-number]');
const cardPreviewName = document.querySelector('[data-card-preview-name]');
const cardPreviewExp = document.querySelector('[data-card-preview-exp]');

function groupCardNumber(value) {
    const limit = Number(cardNumberInput?.dataset.maxDigits || 16);
    return value.replace(/\D/g, '').slice(0, limit).replace(/(.{4})/g, '$1 ').trim();
}

if (cardNumberInput) {
    cardNumberInput.addEventListener('input', () => {
        cardNumberInput.value = groupCardNumber(cardNumberInput.value);
        if (cardPreviewNumber) {
            cardPreviewNumber.textContent = cardNumberInput.value || '•••• •••• •••• ••••';
        }
    });
}

if (cardNameInput) {
    cardNameInput.addEventListener('input', () => {
        if (cardPreviewName) {
            cardPreviewName.textContent = cardNameInput.value.toUpperCase() || 'NOMBRE EN TARJETA';
        }
    });
}

if (cardExpInput) {
    cardExpInput.addEventListener('input', () => {
        let value = cardExpInput.value.replace(/\D/g, '').slice(0, 4);
        if (value.length > 2) {
            value = value.slice(0, 2) + '/' + value.slice(2);
        }
        cardExpInput.value = value;
        if (cardPreviewExp) {
            cardPreviewExp.textContent = value || 'MM/AA';
        }
    });
}

const paymentMethodInputs = document.querySelectorAll('[data-payment-method]');
const paymentPanels = document.querySelectorAll('[data-method-panel]');

function refreshPaymentPanels() {
    const selected = document.querySelector('[data-payment-method]:checked')?.value || 'card';
    paymentPanels.forEach(panel => {
        panel.hidden = !panel.dataset.methodPanel.split(' ').includes(selected);
    });
}

paymentMethodInputs.forEach(input => input.addEventListener('change', refreshPaymentPanels));
refreshPaymentPanels();
