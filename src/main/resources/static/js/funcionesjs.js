const dropdowns = document.querySelectorAll('.dropdown');

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