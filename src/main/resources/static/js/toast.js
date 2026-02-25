function showToast(message, type = 'success') {
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'fixed bottom-4 right-4 z-[9999] flex flex-col gap-2';
        document.body.appendChild(toastContainer);
    }

    const toast = document.createElement('div');
    const bgClass = type === 'success' ? 'bg-emerald-600/90 text-white' : (type === 'error' ? 'bg-red-600/90 text-white' : 'bg-slate-700/90 text-white');
    toast.className = `min-w-[250px] px-4 py-3 rounded shadow-lg font-medium text-sm transition-all duration-300 transform translate-y-4 opacity-0 ${bgClass}`;
    toast.textContent = message;

    toastContainer.appendChild(toast);

    // Fade in
    requestAnimationFrame(() => {
        toast.classList.remove('translate-y-4', 'opacity-0');
        toast.classList.add('translate-y-0', 'opacity-100');
    });

    // Fade out and remove
    setTimeout(() => {
        toast.classList.remove('translate-y-0', 'opacity-100');
        toast.classList.add('translate-y-4', 'opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
