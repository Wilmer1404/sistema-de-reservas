// Funciones de autenticación
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    window.location.href = 'login.html';
}

function obtenerToken() {
    return localStorage.getItem('token');
}

function obtenerUsuario() {
    const user = localStorage.getItem('usuario');
    return user ? JSON.parse(user) : null;
}

function verificarAutenticacion() {
    const token = obtenerToken();
    if (!token) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}
