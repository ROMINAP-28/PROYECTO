function toggleHeart(element) {
    if (element.classList.contains('far')) {
        element.classList.remove('far');
        element.classList.add('fas');
        element.style.color = 'red';
    } else {
        element.classList.remove('fas');
        element.classList.add('far');
        element.style.color = '';
    }
}
