// ============================================================
// MODAL DE CONFIRMACIÓN BOOTSTRAP
// ============================================================
function showConfirm({ message, icon = '⚠️', title = 'Confirmar acción', btnLabel = 'Confirmar', btnClass = 'btn-danger', onConfirm }) {
    document.getElementById('confirmModalIcon').textContent = icon;
    document.getElementById('confirmModalTitle').textContent = title;
    document.getElementById('confirmModalMessage').textContent = message;
    const okBtn = document.getElementById('confirmModalOkBtn');
    okBtn.textContent = btnLabel;
    okBtn.className = `btn ${btnClass}`;
    // Estilo del header según tipo
    const header = document.getElementById('confirmModalHeader');
    header.className = 'modal-header ' + (btnClass === 'btn-danger' ? 'bg-danger text-white' : btnClass === 'btn-warning' ? 'bg-warning' : 'bg-primary text-white');
    // Clonar botón para limpiar eventos anteriores
    const newOkBtn = okBtn.cloneNode(true);
    okBtn.parentNode.replaceChild(newOkBtn, okBtn);
    newOkBtn.className = `btn ${btnClass}`;
    newOkBtn.textContent = btnLabel;
    const modal = getModal('confirmModal');
    newOkBtn.addEventListener('click', () => {
        modal.hide();
        onConfirm();
    });
    modal.show();
}

// SpaceWork API Client
const API_BASE_URL = '/api';
let currentUser = null;

function getActiveUser() {
    if (window.currentUser) return window.currentUser;
    if (currentUser) return currentUser;
    const savedUser = localStorage.getItem('user');
    if (!savedUser) return null;
    try {
        const parsed = JSON.parse(savedUser);
        currentUser = parsed;
        window.currentUser = parsed;
        return parsed;
    } catch (e) {
        return null;
    }
}

// Función para obtener headers autenticados
function getAuthHeaders() {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
}

function getModal(id) {
    const el = document.getElementById(id);
    if (!el) return { show: () => {}, hide: () => {} };
    if (typeof bootstrap !== 'undefined') {
        return bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el);
    }
    // Fallback si Bootstrap no carga
    return {
        show: () => { el.style.display = 'flex'; el.classList.add('show'); document.body.classList.add('modal-open'); },
        hide: () => { el.style.display = 'none'; el.classList.remove('show'); document.body.classList.remove('modal-open'); }
    };
}

// ============================================================
// INICIALIZACIÓN
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('login-form').addEventListener('submit', handleLogin);

    const savedUser = localStorage.getItem('user');
    if (savedUser) {
        try {
            currentUser = JSON.parse(savedUser);
            window.currentUser = currentUser;
            showPage('dashboard');
            if (typeof applyRolePermissions === 'function') applyRolePermissions(currentUser);
            else controlVisibilidadModulosPorRol();
            if (currentUser && currentUser.nombre) {
                document.getElementById('user-display').textContent = currentUser.nombre;
            }
            const seccionInicial = (currentUser.rol === 'CLIENTE') ? 'reservas' : 'dashboard';
            showSection(seccionInicial);
        } catch (e) {
            localStorage.clear();
        }
    }
});

// ============================================================
// AUTENTICACIÓN
// ============================================================
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('login-error');
    errorDiv.classList.add('d-none');

    try {
        // Intentar login como admin primero
        let response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        let data = await response.json();

        // Si falla, intentar como cliente (email o DNI)
        if (!data.success) {
            const resCliente = await fetch(`${API_BASE_URL}/clientes/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            data = await resCliente.json();
        }

        if (data.success) {
            currentUser = data.data ? data.data.user : data.user;
            window.currentUser = currentUser;
            localStorage.setItem('user', JSON.stringify(currentUser));
            if (data.data && data.data.token) {
                localStorage.setItem('token', data.data.token);
            } else if (data.token) {
                localStorage.setItem('token', data.token);
            }
            showPage('dashboard');
            if (typeof applyRolePermissions === 'function') applyRolePermissions(currentUser);
            else controlVisibilidadModulosPorRol();
            const seccion = (currentUser && currentUser.rol === 'CLIENTE') ? 'reservas' : 'dashboard';
            showSection(seccion);
        } else {
            errorDiv.textContent = data.error || data.message || 'Credenciales inválidas';
            errorDiv.classList.remove('d-none');
        }
    } catch (error) {
        errorDiv.textContent = 'Error de conexión. ¿Está el servidor corriendo?';
        errorDiv.classList.remove('d-none');
    }
}

function logout() {
    localStorage.clear();
    currentUser = null;
    showPage('login');
    document.getElementById('login-form').reset();
}

// ============================================================
// CONTROL DE VISIBILIDAD POR ROL (fallback si index.html no tiene applyRolePermissions)
// ============================================================
function controlVisibilidadModulosPorRol() {
    if (!currentUser || !currentUser.rol) return;
    if (typeof applyRolePermissions === 'function') {
        applyRolePermissions(currentUser);
        return;
    }
    const esCliente = currentUser.rol.toUpperCase() === 'CLIENTE';
    const adminOnlyNavIds = ['nav-dashboard','nav-clientes','nav-pagos','nav-descuentos',
                             'nav-horarios','nav-evaluaciones','nav-notificaciones','nav-reportes'];
    adminOnlyNavIds.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = esCliente ? 'none' : '';
    });
    if (esCliente) {
        const adminView = document.querySelector('.reservas-admin-only');
        const clienteView = document.querySelector('.reservas-cliente-only');
        if (adminView) adminView.classList.add('d-none');
        if (clienteView) clienteView.classList.remove('d-none');
    }
}

// ============================================================
// NAVEGACIÓN
// ============================================================
function showPage(pageName) {
    const loginPage = document.getElementById('login-page');
    const dashPage = document.getElementById('dashboard-page');

    if (pageName === 'login') {
        loginPage.classList.remove('d-none');
        dashPage.classList.add('d-none');
    } else {
        loginPage.classList.add('d-none');
        dashPage.classList.remove('d-none');
    }
}

function showSection(sectionName) {
    document.querySelectorAll('.section').forEach(s => s.classList.add('d-none'));
    const sec = document.getElementById(sectionName);
    if (sec) {
        sec.classList.remove('d-none');
        if (sectionName === 'dashboard') loadDashboard();
        if (sectionName === 'reservas') loadReservas();
        if (sectionName === 'espacios') loadEspacios();
        if (sectionName === 'clientes') loadClientes();
        if (sectionName === 'horarios') loadHorariosBloqueados();
        if (sectionName === 'pagos') cargarPagos();
        if (sectionName === 'descuentos') cargarDescuentos();
        if (sectionName === 'evaluaciones') cargarEvaluaciones();
        if (sectionName === 'notificaciones') cargarNotificaciones();
    }
}

// ============================================================
// DASHBOARD
// ============================================================
async function loadDashboard() {
    try {
        const resRes = await fetch(`${API_BASE_URL}/reservas`, { headers: getAuthHeaders() });
        const resData = await resRes.json();
        const espaciosRes = await fetch(`${API_BASE_URL}/espacios`);
        const espaciosData = await espaciosRes.json();
        const clientesRes = await fetch(`${API_BASE_URL}/clientes`);
        const clientesData = await clientesRes.json();

        let totalReservas = 0, confirmadas = 0, completadas = 0, espaciosActivos = 0, totalClientes = 0, totalIngresos = 0;

        if (resData.success && resData.data) {
            totalReservas = resData.data.length;
            confirmadas = resData.data.filter(r => r.estado === 'CONFIRMADA').length;
            completadas = resData.data.filter(r => r.estado === 'COMPLETADA').length;
            totalIngresos = resData.data.filter(r => r.estado === 'COMPLETADA').reduce((sum, r) => sum + (r.montoTotal || 0), 0);
        }

        if (espaciosData.success && espaciosData.data) {
            espaciosActivos = espaciosData.data.filter(e => e.estado === 'ACTIVO').length;
        }

        if (clientesData.success && clientesData.data) {
            totalClientes = clientesData.data.length;
        }

        const ocupacion = espaciosActivos > 0 ? Math.round((confirmadas / espaciosActivos) * 100) : 0;

        document.getElementById('kpi-reservas').textContent = totalReservas;
        document.getElementById('kpi-reservas-detail').textContent = completadas + ' completadas';
        document.getElementById('kpi-confirmadas').textContent = confirmadas;
        document.getElementById('kpi-espacios').textContent = espaciosActivos;
        document.getElementById('kpi-ocupacion').textContent = ocupacion + '%';
        document.getElementById('kpi-clientes').textContent = totalClientes;
        document.getElementById('kpi-ingresos').textContent = 'S/. ' + totalIngresos.toFixed(2);
        
        // Cargar calendario semanal
        await loadCalendarioSemanal();
    } catch (e) {
        console.error('Error loading dashboard:', e);
        showAlert('error', 'Error al cargar dashboard');
    }
}

let _calendario_data = null;

async function loadCalendarioSemanal() {
    try {
        const res = await fetch(`${API_BASE_URL}/calendario/semanal`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            _calendario_data = data.data;
            const selectEspacio = document.getElementById('filtro-calendario-espacio');
            if (selectEspacio && data.data.espacios) {
                let optionsHtml = '<option value="">-- Todos los espacios --</option>';
                data.data.espacios.forEach(e => {
                    optionsHtml += '<option value="' + e.idEspacio + '">' + e.nombre + '</option>';
                });
                selectEspacio.innerHTML = optionsHtml;
            }
            renderCalendarioSemanal(data.data);
        }
    } catch (e) {
        console.error('Error loading calendario:', e);
    }
}

function filtrarCalendarioPorEspacio() {
    if (!_calendario_data) return;
    const idEspacioSeleccionado = document.getElementById('filtro-calendario-espacio').value;
    
    if (idEspacioSeleccionado) {
        const espaciosFiltrados = _calendario_data.espacios.filter(e => e.idEspacio == idEspacioSeleccionado);
        const datosFilterados = {
            espacios: espaciosFiltrados,
            bloques: _calendario_data.bloques
        };
        renderCalendarioSemanal(datosFilterados);
    } else {
        renderCalendarioSemanal(_calendario_data);
    }
}

function renderCalendarioSemanal(calendarioData) {
    const espacios = calendarioData.espacios || [];
    const bloques = calendarioData.bloques || [];
    
    if (espacios.length === 0) {
        document.getElementById('calendario-body').innerHTML = '<tr><td colspan="100" class="text-center text-muted">No hay espacios activos</td></tr>';
        return;
    }
    
    // Agrupar bloques por fecha y hora
    const bloquesByKey = {};
    bloques.forEach(b => {
        const key = b.fecha + '|' + b.hora;
        bloquesByKey[key] = b.espacios || {};
    });
    
    const fechas = [...new Set(bloques.map(b => b.fecha))].sort();
    const horas = [...new Set(bloques.map(b => b.hora))].sort();
    
    if (fechas.length === 0 || horas.length === 0) {
        document.getElementById('calendario-body').innerHTML = '<tr><td colspan="100" class="text-center">Cargando...</td></tr>';
        return;
    }
    
    // Renderizar header (fechas)
    let headerHtml = '<tr><th style="width: 120px; background: #f0f0f0;">Espacio / Hora</th>';
    fechas.forEach(f => {
        const d = new Date(f + 'T00:00:00');
        const formato = d.toLocaleDateString('es-PE', {weekday: 'short', month: 'short', day: 'numeric'});
        headerHtml += '<th style="text-align: center; background: #f0f0f0;"><strong>' + formato + '</strong></th>';
    });
    headerHtml += '</tr>';
    document.getElementById('calendario-header').innerHTML = headerHtml;
    
    // Renderizar body (espacios x fechas/horas)
    let bodyHtml = '';
    
    espacios.forEach(esp => {
        horas.forEach(hora => {
            bodyHtml += '<tr>';
            
            // Encabezado espacio + hora
            bodyHtml += '<td style="font-weight: bold; background: #f9f9f9; font-size: 0.9rem;">' + 
                        esp.nombre + '<br><small style="font-weight: normal; color: #666;">' + hora + '</small></td>';
            
            // Celdas de cada fecha
            fechas.forEach(fecha => {
                const key = fecha + '|' + hora;
                const espaciosEnBloque = bloquesByKey[key] || {};
                const estado = espaciosEnBloque[esp.idEspacio] || 'disponible';
                
                const colorMap = {
                    'disponible': '#d4edda',
                    'ocupado': '#f8d7da',
                    'bloqueado': '#fff3cd'
                };
                const borderColorMap = {
                    'disponible': '#28a745',
                    'ocupado': '#dc3545',
                    'bloqueado': '#ff9800'
                };
                
                const bgColor = colorMap[estado] || '#d4edda';
                const borderColor = borderColorMap[estado] || '#28a745';
                const emoji = estado === 'disponible' ? '✓' : (estado === 'ocupado' ? '✗' : '🔒');
                
                bodyHtml += '<td style="background: ' + bgColor + '; border-left: 3px solid ' + borderColor + 
                           '; text-align: center; cursor: pointer; padding: 8px;" title="' + 
                           esp.nombre + ' - ' + hora + ' - ' + estado + '">' + emoji + '</td>';
            });
            
            bodyHtml += '</tr>';
        });
    });
    
    document.getElementById('calendario-body').innerHTML = bodyHtml;
}

// ============================================================
// RESERVAS
// ============================================================
async function loadReservas() {
    const container = document.getElementById('reservas-list');
    container.innerHTML = '<div class="col-12"><p class="text-muted">Cargando reservas...</p></div>';
    try {
        // Precargar clientes y espacios para rellenar datos en las tarjetas
        if (_allClientes.length === 0) {
            try {
                const resClientes = await fetch(`${API_BASE_URL}/clientes`, { headers: getAuthHeaders() });
                const dataClientes = await resClientes.json();
                if (dataClientes.success) _allClientes = dataClientes.data;
            } catch (e) {
                console.warn('No se pudieron precargar clientes');
            }
        }
        
        if (_allEspacios.length === 0) {
            try {
                const resEspacios = await fetch(`${API_BASE_URL}/espacios`, { headers: getAuthHeaders() });
                const dataEspacios = await resEspacios.json();
                if (dataEspacios.success) _allEspacios = dataEspacios.data;
            } catch (e) {
                console.warn('No se pudieron precargar espacios');
            }
        }
        
        // Si es cliente, filtrar por su ID; si es admin, mostrar todas
        const activeUser = getActiveUser();
        const esCliente = activeUser && activeUser.rol && activeUser.rol.toUpperCase() === 'CLIENTE';
        let url = `${API_BASE_URL}/reservas`;
        
        // Si es cliente, agregar parámetro idCliente (soporta idCliente o idUsuario)
        const idClienteActual = activeUser ? (activeUser.idCliente || activeUser.idUsuario || activeUser.id) : null;
        if (esCliente && idClienteActual) {
            url = `${API_BASE_URL}/reservas?idCliente=${idClienteActual}`;
        }
        
        const res = await fetch(url, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            window._allReservas = data.data;
            // Ordenar por idReserva (descendente para que los nuevos aparezan primero)
            window._allReservas.sort((a, b) => b.idReserva - a.idReserva);
            renderReservas(window._allReservas);
        } else {
            container.innerHTML = '<div class="col-12"><p class="text-danger">Error al cargar reservas</p></div>';
        }
    } catch (e) {
        container.innerHTML = '<div class="col-12"><p class="text-danger">Error de conexión</p></div>';
    }
}

function renderReservas(reservas) {
    const container = document.getElementById('reservas-list');
    container.innerHTML = '';

    if (!reservas || reservas.length === 0) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay reservas registradas</div></div>';
        return;
    }

    reservas.forEach(r => {
        // Cargar datos completos de cliente y espacio desde caché si no están en r
        let clienteCompleto = null;
        let espacioCompleto = null;
        
        if (_allClientes) {
            clienteCompleto = _allClientes.find(c => c.idCliente === r.idCliente);
        }
        if (_allEspacios) {
            espacioCompleto = _allEspacios.find(e => e.idEspacio === r.idEspacio);
        }

        const fechaIni = r.fechaInicio ? new Date(r.fechaInicio) : null;
        const fechaFin = r.fechaFin ? new Date(r.fechaFin) : null;
        const fechaIniStr = fechaIni ? fechaIni.toLocaleString('es-PE') : '-';
        const fechaFinStr = fechaFin ? fechaFin.toLocaleString('es-PE') : '-';
        
        // Calcular duración en horas
        let duracionHoras = '-';
        if (fechaIni && fechaFin) {
            const diffMs = fechaFin - fechaIni;
            duracionHoras = (diffMs / (1000 * 60 * 60)).toFixed(1) + ' hrs';
        }
        
        const badgeColor = r.estado === 'CONFIRMADA' ? 'success' : r.estado === 'CANCELADA' ? 'danger' : r.estado === 'COMPLETADA' ? 'primary' : 'warning';
        const clienteNombre = r.nombreCliente || (clienteCompleto ? clienteCompleto.nombreCompleto : '-');
        const clienteDni = clienteCompleto ? clienteCompleto.dni : '-';
        const clienteEmail = clienteCompleto ? clienteCompleto.email : '-';
        const espacioNombre = r.nombreEspacio || (espacioCompleto ? espacioCompleto.nombre : '-');
        const espacioTipo = espacioCompleto ? espacioCompleto.tipo : '-';
        const precioPorHora = espacioCompleto ? espacioCompleto.precioPorHora || 0 : 0;

        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        let botones = '';
        if (r.estado === 'PENDIENTE') {
            botones = `
                <button class="btn btn-sm btn-success" onclick="confirmarReserva(${r.idReserva})">✅ Confirmar</button>
                <button class="btn btn-sm btn-danger" onclick="cancelarReserva(${r.idReserva})">❌ Cancelar</button>
            `;
        } else if (r.estado === 'CONFIRMADA') {
            botones = `
                <button class="btn btn-sm btn-primary" onclick="completarReserva(${r.idReserva})">🏁 Completada</button>
                <button class="btn btn-sm btn-danger" onclick="cancelarReserva(${r.idReserva})">❌ Cancelar</button>
            `;
        }

        col.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-header bg-light d-flex justify-content-between align-items-center">
                    <h5 class="card-title mb-0">Reserva #${r.idReserva}</h5>
                    <span class="badge bg-${badgeColor}">${r.estado}</span>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <h6 class="text-secondary mb-2">📝 Datos del Cliente</h6>
                        <p class="card-text mb-1"><strong>👤 Nombre:</strong> ${clienteNombre}</p>
                        <p class="card-text mb-1"><strong>🆔 DNI:</strong> ${clienteDni}</p>
                        <p class="card-text mb-2"><strong>📧 Email:</strong> ${clienteEmail}</p>
                    </div>
                    <hr class="my-2">
                    <div class="mb-3">
                        <h6 class="text-secondary mb-2">🏛️ Espacio Reservado</h6>
                        <p class="card-text mb-1"><strong>Nombre:</strong> ${espacioNombre}</p>
                        <p class="card-text mb-1"><strong>Tipo:</strong> ${espacioTipo}</p>
                        <p class="card-text mb-2"><strong>Precio/Hora:</strong> S/. ${precioPorHora.toFixed(2)}</p>
                    </div>
                    <hr class="my-2">
                    <div class="mb-3">
                        <h6 class="text-secondary mb-2">📅 Fechas y Duración</h6>
                        <p class="card-text mb-1"><small><strong>Inicio:</strong> ${fechaIniStr}</small></p>
                        <p class="card-text mb-1"><small><strong>Fin:</strong> ${fechaFinStr}</small></p>
                        <p class="card-text mb-2"><small><strong>Duración:</strong> ${duracionHoras}</small></p>
                    </div>
                    <hr class="my-2">
                    <p class="card-text fw-bold text-success mb-0">💰 Total: S/. ${(r.montoTotal || 0).toFixed(2)}</p>
                </div>
                ${botones ? `<div class="card-footer bg-white d-flex gap-2 flex-wrap">${botones}</div>` : ''}
            </div>
        `;
        container.appendChild(col);
    });
}

// Caché de clientes/espacios para el modal de reservas
let _cacheClientes = [];
let _cacheEspacios = [];

async function abrirFormularioReserva() {
    // Cargar clientes y espacios si no están en caché
    if (_cacheClientes.length === 0) {
        const res = await fetch(`${API_BASE_URL}/clientes`, { headers: getAuthHeaders() });
        const data = await res.json();
        _cacheClientes = data.success ? data.data : [];
    }
    if (_cacheEspacios.length === 0) {
        const res = await fetch(`${API_BASE_URL}/espacios`, { headers: getAuthHeaders() });
        const data = await res.json();
        _cacheEspacios = data.success ? data.data : [];
    }

    // Poblar selects
    const selCliente = document.getElementById('reservaIdCliente');
    const selEspacio = document.getElementById('reservaIdEspacio');
    selCliente.innerHTML = '<option value="">-- Seleccione un cliente --</option>';
    selEspacio.innerHTML = '<option value="">-- Seleccione un espacio --</option>';

    _cacheClientes.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.idCliente;
        opt.textContent = (c.nombreCompleto || `${c.nombre} ${c.apellido}`) + ` (DNI: ${c.dni})`;
        selCliente.appendChild(opt);
    });

    _cacheEspacios.filter(e => e.estado === 'ACTIVO').forEach(e => {
        const opt = document.createElement('option');
        opt.value = e.idEspacio;
        opt.textContent = `${e.nombre} — S/. ${(e.precioPorHora||0).toFixed(2)}/hr`;
        opt.dataset.precio = e.precioPorHora || 0;
        selEspacio.appendChild(opt);
    });

    document.getElementById('reservaForm').reset();
    document.getElementById('monto-preview').classList.add('d-none');
    getModal('reservaModal').show();
}

function calcularMonto() {
    const selEspacio = document.getElementById('reservaIdEspacio');
    const fechaIni = document.getElementById('reservaFechaInicio').value;
    const fechaFin = document.getElementById('reservaFechaFin').value;
    const preview = document.getElementById('monto-preview');

    if (!selEspacio.value || !fechaIni || !fechaFin) { preview.classList.add('d-none'); return; }

    const opt = selEspacio.selectedOptions[0];
    const precio = parseFloat(opt.dataset.precio || 0);
    const ini = new Date(fechaIni);
    const fin = new Date(fechaFin);
    const horas = (fin - ini) / (1000 * 60 * 60);

    if (horas <= 0) { preview.classList.add('d-none'); return; }

    const monto = (precio * horas).toFixed(2);
    document.getElementById('monto-valor').textContent = `S/. ${monto} (${horas.toFixed(1)} horas × S/. ${precio.toFixed(2)}/hr)`;
    preview.classList.remove('d-none');
}

async function guardarReserva() {
    const idCliente  = document.getElementById('reservaIdCliente').value;
    const idEspacio  = document.getElementById('reservaIdEspacio').value;
    const fechaInicio = document.getElementById('reservaFechaInicio').value;
    const fechaFin   = document.getElementById('reservaFechaFin').value;

    if (!idCliente || !idEspacio || !fechaInicio || !fechaFin) {
        showAlert('error', 'Complete todos los campos');
        return;
    }
    if (new Date(fechaFin) <= new Date(fechaInicio)) {
        showAlert('error', 'La fecha fin debe ser posterior a la fecha inicio');
        return;
    }

    // Validar horarios bloqueados
    const sinBloqueo = await validarDisponibilidadEspacio(idEspacio, fechaInicio, fechaFin);
    if (!sinBloqueo) {
        showAlert('error', '⚠️ El espacio tiene un horario bloqueado en esa fecha. Selecciona otro horario.');
        return;
    }

    // Validar que no exista otra reserva en el mismo rango
    const sinConflicto = await validarSinReservaExistente(idEspacio, fechaInicio, fechaFin);
    if (!sinConflicto) {
        showAlert('error', '⚠️ El espacio ya tiene una reserva en ese rango de horario. Selecciona otra sala u otro horario.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/reservas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idCliente: parseInt(idCliente), idEspacio: parseInt(idEspacio), fechaInicio, fechaFin })
        });
        const data = await res.json();
        if (data.success) {
            showAlert('success', `✅ Reserva creada. Monto: S/. ${(data.monto||0).toFixed(2)}`);
            getModal('reservaModal').hide();
            loadReservas();
        } else {
            showAlert('error', data.error || data.message || 'Error al crear reserva');
        }
    } catch (e) {
        showAlert('error', 'Error de conexión al crear reserva');
    }
}

async function confirmarReserva(id) {
    showConfirm({
        message: '¿Confirmar esta reserva?',
        icon: '✅',
        title: 'Confirmar Reserva',
        btnLabel: 'Sí, confirmar',
        btnClass: 'btn-success',
        onConfirm: async () => {
            const res = await fetch(`${API_BASE_URL}/reservas/${id}/confirmar`, { method: 'PUT', headers: getAuthHeaders() });
            const data = await res.json();
            showAlert(data.success ? 'success' : 'error', data.success ? '✅ Reserva confirmada' : data.error);
            if (data.success) loadReservas();
        }
    });
}

async function completarReserva(id) {
    showConfirm({
        message: '¿Marcar esta reserva como completada? Se creará un pago pendiente.',
        icon: '🏁',
        title: 'Completar Reserva',
        btnLabel: 'Sí, completar',
        btnClass: 'btn-primary',
        onConfirm: async () => {
            const res = await fetch(`${API_BASE_URL}/reservas/${id}/completar`, { method: 'PUT', headers: getAuthHeaders() });
            const data = await res.json();
            showAlert(data.success ? 'success' : 'error', data.success ? '✅ Reserva completada - Ve a Pagos para pagar' : data.error);
            if (data.success) loadReservas();
        }
    });
}

async function cancelarReserva(id) {
    showConfirm({
        message: '¿Cancelar esta reserva? Esta acción no se puede deshacer.',
        icon: '🚫',
        title: 'Cancelar Reserva',
        btnLabel: 'Sí, cancelar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            const res = await fetch(`${API_BASE_URL}/reservas/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
            const data = await res.json();
            showAlert(data.success ? 'success' : 'error', data.success ? '❌ Reserva cancelada' : data.error);
            if (data.success) loadReservas();
        }
    });
}

// ============================================================
// ESPACIOS
// ============================================================
async function loadEspacios() {
    const container = document.getElementById('espacios-grid');
    container.innerHTML = '<div class="col-12"><p class="text-muted">Cargando espacios...</p></div>';
    try {
        const res = await fetch(`${API_BASE_URL}/espacios`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            _allEspacios = data.data;
            renderEspacios(data.data);
        }
    } catch (e) {
        container.innerHTML = '<div class="col-12"><p class="text-danger">Error de conexión</p></div>';
    }
}

function renderEspacios(espacios) {
    const container = document.getElementById('espacios-grid');
    container.innerHTML = '';

    if (!espacios || espacios.length === 0) {
        container.innerHTML = '<p class="text-muted" style="grid-column:1/-1;text-align:center;padding:40px">No hay espacios registrados</p>';
        return;
    }

    espacios.forEach(e => {
        const card = document.createElement('div');
        card.className = 'espacio-card-item';

        // Valida que la imagen sea un data URL base64 válido antes de renderizarla
        const esImagenValida = e.urlImagen && e.urlImagen.startsWith('data:image') && e.urlImagen.length > 100;
        const imgHtml = esImagenValida
            ? `<img src="${e.urlImagen}" alt="${e.nombre}" class="espacio-card-img" onerror="this.parentElement.innerHTML='<div class=\\'espacio-card-no-img\\'><i class=\\'fas fa-image\\'></i><span>Sin imagen</span></div>'">`
            : `<div class="espacio-card-no-img"><i class="fas fa-door-open"></i><span>Sin imagen</span></div>`;

        card.innerHTML = `
            <div class="card h-100 shadow-sm espacio-card">
                <div class="espacio-card-img-wrap">${imgHtml}</div>
                <div class="card-body">
                    <h5 class="card-title">${e.nombre}</h5>
                    <p class="card-text mb-1"><span class="badge bg-secondary">${e.tipo}</span></p>
                    <p class="card-text mb-1"><i class="fas fa-users text-muted me-1"></i><strong>${e.capacidad}</strong> personas</p>
                    <p class="card-text mb-1"><i class="fas fa-map-marker-alt text-muted me-1"></i>${e.ubicacion}</p>
                    <p class="card-text mb-2"><i class="fas fa-dollar-sign text-muted me-1"></i><strong>S/. ${(e.precioPorHora || 0).toFixed(2)}</strong>/hora</p>
                    <span class="badge bg-${e.estado === 'ACTIVO' ? 'success' : 'danger'}">${e.estado}</span>
                </div>
                <div class="card-footer bg-white border-top d-flex gap-2">
                    <button class="btn btn-sm btn-primary flex-fill" onclick="editarEspacio(${e.idEspacio})"><i class="fas fa-edit"></i> Editar</button>
                    <button class="btn btn-sm btn-danger flex-fill" onclick="eliminarEspacio(${e.idEspacio})"><i class="fas fa-trash"></i> Eliminar</button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

function abrirFormularioEspacio() {
    document.getElementById('espacioId').value = '';
    document.getElementById('espacioForm').reset();
    document.getElementById('espacioImagenFile').dataset.base64 = '';
    document.getElementById('espacioImgPreview').src = '';
    document.getElementById('espacioImgPreview').style.display = 'none';
    const ph = document.getElementById('espacioImgPlaceholder');
    if (ph) ph.style.display = 'flex';
    const btnQ = document.getElementById('btnQuitarImagen');
    if (btnQ) btnQ.classList.add('d-none');
    document.getElementById('espacioModalTitle').innerHTML = '<i class="fas fa-plus-circle me-2"></i>Nuevo Espacio';
    getModal('espacioModal').show();
}

async function editarEspacio(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/espacios/${id}`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (!data.success || !data.data) {
            showAlert('error', 'No se pudo cargar el espacio');
            return;
        }
        const e = data.data;
        document.getElementById('espacioId').value = e.idEspacio;
        document.getElementById('espacioNombre').value = e.nombre || '';
        document.getElementById('espacioTipo').value = e.tipo || '';
        document.getElementById('espacioCapacidad').value = e.capacidad || '';
        document.getElementById('espacioUbicacion').value = e.ubicacion || '';
        document.getElementById('espacioPrecio').value = e.precioPorHora || '';
        // Cargar imagen actual
        const previewImg = document.getElementById('espacioImgPreview');
        const placeholder = document.getElementById('espacioImgPlaceholder');
        const btnQuitar = document.getElementById('btnQuitarImagen');
        const esValida = e.urlImagen && e.urlImagen.startsWith('data:image') && e.urlImagen.length > 100;
        if (esValida) {
            previewImg.src = e.urlImagen;
            previewImg.style.display = 'block';
            if (placeholder) placeholder.style.display = 'none';
            if (btnQuitar) btnQuitar.classList.remove('d-none');
        } else {
            previewImg.src = '';
            previewImg.style.display = 'none';
            if (placeholder) placeholder.style.display = 'flex';
            if (btnQuitar) btnQuitar.classList.add('d-none');
        }
        document.getElementById('espacioImagenFile').value = '';
        document.getElementById('espacioImagenFile').dataset.base64 = esValida ? e.urlImagen : '';
        document.getElementById('espacioModalTitle').innerHTML = '<i class="fas fa-edit me-2"></i>Editar Espacio';
        getModal('espacioModal').show();
    } catch (err) {
        showAlert('error', 'Error de conexión');
    }
}

async function eliminarEspacio(id) {
    showConfirm({
        message: '¿Eliminar este espacio? Ya no estará disponible para reservas.',
        icon: '🗑️',
        title: 'Eliminar Espacio',
        btnLabel: 'Sí, eliminar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/espacios/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Espacio eliminado correctamente');
                    loadEspacios();
                } else {
                    showAlert('error', data.error || 'No se pudo eliminar');
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

function previewEspacioImagen(input) {
    const file = input.files[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
        showAlert('error', 'La imagen no puede superar 2MB');
        input.value = '';
        return;
    }
    const reader = new FileReader();
    reader.onload = function(ev) {
        input.dataset.base64 = ev.target.result;
        const previewImg = document.getElementById('espacioImgPreview');
        const placeholder = document.getElementById('espacioImgPlaceholder');
        const btnQuitar = document.getElementById('btnQuitarImagen');
        previewImg.src = ev.target.result;
        previewImg.style.display = 'block';
        if (placeholder) placeholder.style.display = 'none';
        if (btnQuitar) btnQuitar.classList.remove('d-none');
    };
    reader.readAsDataURL(file);
}

function quitarImagenEspacio() {
    const input = document.getElementById('espacioImagenFile');
    const previewImg = document.getElementById('espacioImgPreview');
    const placeholder = document.getElementById('espacioImgPlaceholder');
    const btnQuitar = document.getElementById('btnQuitarImagen');
    input.value = '';
    input.dataset.base64 = '';
    previewImg.src = '';
    previewImg.style.display = 'none';
    if (placeholder) placeholder.style.display = 'flex';
    if (btnQuitar) btnQuitar.classList.add('d-none');
}

async function guardarEspacio() {
    const id = document.getElementById('espacioId').value;
    const nombre = document.getElementById('espacioNombre').value;
    const tipo = document.getElementById('espacioTipo').value;
    const capacidad = parseInt(document.getElementById('espacioCapacidad').value);
    const ubicacion = document.getElementById('espacioUbicacion').value;
    const precio = parseFloat(document.getElementById('espacioPrecio').value);
    const fileInput = document.getElementById('espacioImagenFile');
    const urlImagen = fileInput.dataset.base64 || null;

    try {
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/espacios/${id}` : `${API_BASE_URL}/espacios`;
        
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, tipo, capacidad, ubicacion, precioPorHora: precio, urlImagen })
        });

        const data = await res.json();
        if (data.success) {
            showAlert('success', id ? 'Espacio actualizado' : 'Espacio creado');
            const modal = getModal('espacioModal');
            modal.hide();
            loadEspacios();
        } else {
            showAlert('error', data.message || 'Error');
        }
    } catch (e) {
        showAlert('error', 'Error al guardar');
    }
}

// ============================================================
// CLIENTES
// ============================================================
async function loadClientes() {
    const container = document.getElementById('clientes-tbody');
    container.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>';
    try {
        const res = await fetch(`${API_BASE_URL}/clientes`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            _allClientes = data.data;
            renderClientes(data.data);
        }
    } catch (e) {
        container.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Error de conexión</td></tr>';
    }
}

function renderClientes(clientes) {
    const tbody = document.getElementById('clientes-tbody');
    tbody.innerHTML = '';

    if (!clientes || clientes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay clientes registrados</td></tr>';
        return;
    }

    clientes.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${c.nombreCompleto || c.nombre + ' ' + (c.apellido || '')}</td>
            <td>${c.dni || '-'}</td>
            <td>${c.email || '-'}</td>
            <td>${c.telefono || '-'}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="editarCliente(${c.idCliente})">✏️</button>
                <button class="btn btn-sm btn-danger" onclick="eliminarCliente(${c.idCliente})">🗑️</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function abrirFormularioCliente() {
    document.getElementById('clienteId').value = '';
    document.getElementById('clienteForm').reset();
    document.getElementById('clienteModalTitle').textContent = 'Nuevo Cliente';
    const modal = getModal('clienteModal');
    modal.show();
}

async function editarCliente(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/clientes/${id}`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (!data.success || !data.data) {
            showAlert('error', 'No se pudo cargar el cliente');
            return;
        }
        const c = data.data;
        document.getElementById('clienteId').value = c.idCliente;
        document.getElementById('clienteNombre').value = c.nombre || '';
        document.getElementById('clienteApellido').value = c.apellido || '';
        document.getElementById('clienteDni').value = c.dni || '';
        document.getElementById('clienteEmail').value = c.email || '';
        document.getElementById('clienteTelefono').value = c.telefono || '';
        document.getElementById('clienteModalTitle').textContent = 'Editar Cliente';
        getModal('clienteModal').show();
    } catch (err) {
        showAlert('error', 'Error de conexión');
    }
}

async function eliminarCliente(id) {
    showConfirm({
        message: '¿Eliminar este cliente? Se desactivará de la plataforma.',
        icon: '👤',
        title: 'Eliminar Cliente',
        btnLabel: 'Sí, eliminar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/clientes/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Cliente eliminado');
                    loadClientes();
                } else {
                    showAlert('error', data.message || 'Error');
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

async function guardarCliente() {
    const id = document.getElementById('clienteId').value;
    const nombre = document.getElementById('clienteNombre').value;
    const apellido = document.getElementById('clienteApellido').value;
    const dni = document.getElementById('clienteDni').value;
    const email = document.getElementById('clienteEmail').value;
    const telefono = document.getElementById('clienteTelefono').value;

    try {
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/clientes/${id}` : `${API_BASE_URL}/clientes`;
        
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, apellido, dni, email, telefono })
        });

        const data = await res.json();
        if (data.success) {
            showAlert('success', id ? 'Cliente actualizado' : 'Cliente creado');
            const modal = getModal('clienteModal');
            modal.hide();
            loadClientes();
        } else {
            showAlert('error', data.message || 'Error');
        }
    } catch (e) {
        showAlert('error', 'Error al guardar');
    }
}

// ============================================================
// HORARIOS BLOQUEADOS
// ============================================================
async function loadHorariosBloqueados() {
    const container = document.getElementById('horarios-list');
    container.innerHTML = '<div class="col-12"><p class="text-muted">Cargando horarios...</p></div>';
    try {
        const res = await fetch(`${API_BASE_URL}/horarios`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            _allHorarios = data.data;
            renderHorariosBloqueados(data.data);
        } else {
            container.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay horarios bloqueados</div></div>';
        }
    } catch (e) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-warning">No hay horarios bloqueados</div></div>';
    }
}

function renderHorariosBloqueados(horarios) {
    const container = document.getElementById('horarios-list');
    container.innerHTML = '';

    if (!horarios || horarios.length === 0) {
        container.innerHTML = `<div class="col-12">
            <div style="text-align:center;padding:60px 24px;color:#64748b;background:#f8fafc;border-radius:16px;border:2px dashed #e2e8f0">
                <div style="width:64px;height:64px;background:#f1f5f9;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 16px">
                    <i class="fas fa-lock-open" style="font-size:26px;color:#94a3b8"></i>
                </div>
                <p style="font-size:16px;font-weight:700;color:#374151;margin-bottom:6px">Sin bloqueos activos</p>
                <p style="font-size:13px;color:#9ca3af">Todos los espacios están disponibles. Usa "Bloquear Horario" para restringir disponibilidad.</p>
            </div></div>`;
        return;
    }

    horarios.forEach(h => {
        const fmtFecha = (str) => {
            if (!str) return '-';
            const d = new Date(str);
            const fecha = d.toLocaleDateString('es-PE', { day:'2-digit', month:'short', year:'numeric' });
            const hora  = d.toLocaleTimeString('es-PE', { hour:'2-digit', minute:'2-digit' });
            return { fecha, hora };
        };
        const ini = fmtFecha(h.fechaInicio);
        const fin = fmtFecha(h.fechaFin);

        // Calcular duración en horas
        let duracion = '';
        if (h.fechaInicio && h.fechaFin) {
            const ms = new Date(h.fechaFin) - new Date(h.fechaInicio);
            const hrs = Math.floor(ms / 3600000);
            const min = Math.floor((ms % 3600000) / 60000);
            duracion = hrs > 0 ? `${hrs}h ${min > 0 ? min + 'min' : ''}` : `${min}min`;
        }

        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-6 mb-3';
        col.innerHTML = `
            <div class="bloqueo-card" style="
                border-radius:14px;
                overflow:hidden;
                box-shadow:0 4px 20px rgba(220,38,38,0.10);
                background:#fff;
                border:1px solid #fee2e2;
                height:100%;
                display:flex;
                flex-direction:column;
                transition:transform .2s,box-shadow .2s;
            " onmouseover="this.style.transform='translateY(-3px)';this.style.boxShadow='0 8px 28px rgba(220,38,38,0.16)'"
               onmouseout="this.style.transform='';this.style.boxShadow='0 4px 20px rgba(220,38,38,0.10)'">

                <!-- Cabecera -->
                <div style="background:linear-gradient(135deg,#b91c1c 0%,#dc2626 60%,#ef4444 100%);padding:16px 18px;display:flex;align-items:center;justify-content:space-between">
                    <div style="display:flex;align-items:center;gap:12px">
                        <div style="width:38px;height:38px;background:rgba(255,255,255,0.18);border-radius:10px;display:flex;align-items:center;justify-content:center;flex-shrink:0">
                            <i class="fas fa-ban" style="color:#fff;font-size:16px"></i>
                        </div>
                        <div>
                            <div style="color:#fff;font-weight:700;font-size:14px;line-height:1.2">${h.nombreEspacio || 'Espacio #' + h.idEspacio}</div>
                            <div style="color:rgba(255,255,255,0.75);font-size:11px;margin-top:2px">
                                <i class="fas fa-hashtag" style="font-size:9px"></i> Bloqueo ${h.idHorarioBloqueado}
                            </div>
                        </div>
                    </div>
                    ${duracion ? `<span style="background:rgba(0,0,0,0.2);color:#fff;font-size:11px;font-weight:700;padding:4px 10px;border-radius:20px">${duracion}</span>` : ''}
                </div>

                <!-- Razón destacada -->
                <div style="background:#fef2f2;padding:10px 18px;border-bottom:1px solid #fee2e2;display:flex;align-items:center;gap:8px">
                    <i class="fas fa-comment-alt" style="color:#dc2626;font-size:12px;flex-shrink:0"></i>
                    <span style="font-size:13px;color:#7f1d1d;font-weight:600">${(h.razon || 'Sin razón especificada').toUpperCase()}</span>
                </div>

                <!-- Timeline de fechas -->
                <div style="padding:16px 18px;flex:1">
                    <div style="position:relative;padding-left:20px">
                        <!-- Línea vertical -->
                        <div style="position:absolute;left:7px;top:16px;bottom:16px;width:2px;background:linear-gradient(to bottom,#dc2626,#fca5a5)"></div>

                        <!-- Inicio -->
                        <div style="position:relative;margin-bottom:16px">
                            <div style="position:absolute;left:-20px;top:3px;width:14px;height:14px;border-radius:50%;background:#dc2626;border:2px solid #fff;box-shadow:0 0 0 2px #dc2626"></div>
                            <div style="font-size:10px;color:#94a3b8;font-weight:700;text-transform:uppercase;letter-spacing:.6px;margin-bottom:2px">
                                <i class="fas fa-play-circle" style="color:#dc2626;margin-right:3px"></i>Inicio
                            </div>
                            <div style="font-size:13px;font-weight:700;color:#1e293b">${ini.hora}</div>
                            <div style="font-size:11px;color:#64748b">${ini.fecha}</div>
                        </div>

                        <!-- Fin -->
                        <div style="position:relative">
                            <div style="position:absolute;left:-20px;top:3px;width:14px;height:14px;border-radius:50%;background:#fca5a5;border:2px solid #fff;box-shadow:0 0 0 2px #fca5a5"></div>
                            <div style="font-size:10px;color:#94a3b8;font-weight:700;text-transform:uppercase;letter-spacing:.6px;margin-bottom:2px">
                                <i class="fas fa-stop-circle" style="color:#fca5a5;margin-right:3px"></i>Fin
                            </div>
                            <div style="font-size:13px;font-weight:700;color:#1e293b">${fin.hora}</div>
                            <div style="font-size:11px;color:#64748b">${fin.fecha}</div>
                        </div>
                    </div>
                </div>

                <!-- Botón desbloquear -->
                <div style="padding:12px 18px;background:#fafafa;border-top:1px solid #f1f5f9">
                    <button onclick="desbloquearHorario(${h.idHorarioBloqueado})"
                        style="width:100%;padding:9px 16px;border:1.5px solid #dc2626;border-radius:8px;background:#fff;color:#dc2626;font-weight:700;font-size:13px;cursor:pointer;transition:all .2s;display:flex;align-items:center;justify-content:center;gap:7px"
                        onmouseover="this.style.background='#dc2626';this.style.color='#fff'"
                        onmouseout="this.style.background='#fff';this.style.color='#dc2626'">
                        <i class="fas fa-lock-open"></i> Desbloquear Horario
                    </button>
                </div>
            </div>`;
        container.appendChild(col);
    });
}

async function abrirBloqueoDialog() {
    // Load espacios into select
    const select = document.getElementById('bloqueoIdEspacio');
    select.innerHTML = '<option value="">-- Seleccione un espacio --</option>';
    try {
        const res = await fetch(`${API_BASE_URL}/espacios`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success && data.data) {
            data.data.forEach(e => {
                const opt = document.createElement('option');
                opt.value = e.idEspacio;
                opt.textContent = `${e.nombre} (${e.tipo})`;
                select.appendChild(opt);
            });
        }
    } catch (err) { /* ignore, select stays empty */ }

    document.getElementById('bloqueoForm').reset();
    getModal('bloqueoModal').show();
}

async function guardarBloqueo() {
    const idEspacio = document.getElementById('bloqueoIdEspacio').value;
    const fechaInicioRaw = document.getElementById('bloqueoFechaInicio').value;
    const fechaFinRaw = document.getElementById('bloqueoFechaFin').value;
    const razon = document.getElementById('bloqueoRazon').value;

    if (!idEspacio || !fechaInicioRaw || !fechaFinRaw || !razon) {
        showAlert('error', 'Complete todos los campos');
        return;
    }

    // Convert datetime-local "2025-03-15T10:00" → "2025-03-15 10:00"
    const fechaInicio = fechaInicioRaw.replace('T', ' ');
    const fechaFin = fechaFinRaw.replace('T', ' ');

    try {
        const res = await fetch(`${API_BASE_URL}/horarios`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idEspacio: parseInt(idEspacio), fechaInicio, fechaFin, razon })
        });
        const data = await res.json();
        if (data.success) {
            showAlert('success', 'Horario bloqueado correctamente');
            getModal('bloqueoModal').hide();
            loadHorariosBloqueados();
        } else {
            showAlert('error', data.message || 'No se pudo bloquear');
        }
    } catch (err) {
        showAlert('error', 'Error de conexión');
    }
}

async function desbloquearHorario(id) {
    showConfirm({
        message: '¿Desbloquear este horario? El espacio volverá a estar disponible.',
        icon: '🔓',
        title: 'Desbloquear Horario',
        btnLabel: 'Sí, desbloquear',
        btnClass: 'btn-warning',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/horarios/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Horario desbloqueado');
                    loadHorariosBloqueados();
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

// ============================================================
// REPORTES
// ============================================================
function exportarReservasCSV() {
    exportarDatos(`${API_BASE_URL}/reservas`, 'reservas', ['idReserva', 'estado', 'montoTotal', 'fechaInicio', 'fechaFin']);
}

function exportarEspaciosCSV() {
    exportarDatos(`${API_BASE_URL}/espacios`, 'espacios', ['idEspacio', 'nombre', 'tipo', 'capacidad', 'precioPorHora', 'estado']);
}

function exportarClientesCSV() {
    exportarDatos(`${API_BASE_URL}/clientes`, 'clientes', ['idCliente', 'nombreCompleto', 'dni', 'email', 'telefono']);
}

async function exportarDatos(url, nombreArchivo, columnas) {
    try {
        const res = await fetch(url);
        const data = await res.json();
        
        if (!data.success || !data.data) {
            showAlert('error', 'Error al obtener datos');
            return;
        }

        let csv = columnas.join(',') + '\n';
        data.data.forEach(row => {
            let fila = columnas.map(col => {
                let valor = row[col] || '';
                if (typeof valor === 'string') {
                    valor = '"' + valor.replace(/"/g, '""') + '"';
                }
                return valor;
            }).join(',');
            csv += fila + '\n';
        });

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url2 = URL.createObjectURL(blob);
        link.href = url2;
        link.download = nombreArchivo + '_' + new Date().toISOString().split('T')[0] + '.csv';
        link.click();
        
        showAlert('success', 'Archivo descargado');
    } catch (e) {
        showAlert('error', 'Error al generar CSV');
    }
}

// ============================================================
// UTILIDADES
// ============================================================
function showAlert(type, message) {
    // Crear alerta dinámica
    const alertHtml = `
        <div class="alert alert-${type === 'error' ? 'danger' : type === 'success' ? 'success' : 'info'} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    // Crear contenedor si no existe
    let alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alert-container';
        alertContainer.style.position = 'fixed';
        alertContainer.style.top = '20px';
        alertContainer.style.right = '20px';
        alertContainer.style.zIndex = '9999';
        alertContainer.style.maxWidth = '400px';
        document.body.appendChild(alertContainer);
    }
    
    const alertDiv = document.createElement('div');
    alertDiv.innerHTML = alertHtml;
    alertContainer.appendChild(alertDiv.firstElementChild);
    
    // Auto-remove después de 4 segundos
    setTimeout(() => {
        const alerts = alertContainer.querySelectorAll('.alert');
        if (alerts.length > 0) {
            alerts[0].remove();
        }
    }, 4000);
}

// ============================================================
// FILTROS
// ============================================================
window._allReservas = [];
let _allEspacios = [];
let _allClientes = [];
let _allHorarios = [];

function filtrarReservas() {
    const filtro = document.getElementById('filtroReservas').value.toLowerCase();
    const activeUser = getActiveUser();

    if (activeUser && activeUser.rol === 'CLIENTE' && typeof renderReservasCliente === 'function') {
        const clienteId = activeUser.idCliente || activeUser.idUsuario || activeUser.id;
        let base = window._allReservas;
        if (clienteId) {
            base = window._allReservas.filter(r => r.idCliente == clienteId);
        }
        const filtradosCliente = base.filter(r => {
            const espacio = r.nombreEspacio || '';
            const estado = r.estado || '';
            const texto = (espacio + ' ' + estado + ' ' + (r.idReserva || '')).toLowerCase();
            return texto.includes(filtro);
        });
        renderReservasCliente(filtradosCliente);
        return;
    }

    const filtrados = window._allReservas.filter(r => {
        const cliente = r.nombreCliente || (r.cliente ? (r.cliente.nombreCompleto || r.cliente.nombre || '') : '');
        const espacio = r.nombreEspacio || (r.espacio ? r.espacio.nombre : '');
        const texto = (cliente + ' ' + espacio).toLowerCase();
        return texto.includes(filtro);
    });
    renderReservas(filtrados);
}

function filtrarEspacios() {
    const filtro = document.getElementById('filtroEspacios').value.toLowerCase();
    const filtrados = _allEspacios.filter(e => {
        const texto = (e.nombre + ' ' + e.tipo).toLowerCase();
        return texto.includes(filtro);
    });
    renderEspacios(filtrados);
}

function filtrarClientes() {
    const filtro = document.getElementById('filtroClientes').value.toLowerCase();
    const filtrados = _allClientes.filter(c => {
        const nombre = c.nombreCompleto || c.nombre + ' ' + (c.apellido || '');
        const texto = (nombre + ' ' + (c.dni || '') + ' ' + (c.email || '')).toLowerCase();
        return texto.includes(filtro);
    });
    renderClientes(filtrados);
}

function filtrarHorarios() {
    const filtro = document.getElementById('filtroHorarios').value.toLowerCase();
    const filtrados = _allHorarios.filter(h => {
        const espacio = h.nombreEspacio || '';
        const razon = h.razon || '';
        const texto = (espacio + ' ' + razon).toLowerCase();
        return texto.includes(filtro);
    });
    renderHorariosBloqueados(filtrados);
}

// ============================================================
// VALIDACIÓN DE HORARIOS BLOQUEADOS
// ============================================================
async function validarDisponibilidadEspacio(idEspacio, fechaInicio, fechaFin) {
    try {
        const res = await fetch(`${API_BASE_URL}/horarios`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (!data.success || !data.data) return true;

        const inicio = new Date(fechaInicio).getTime();
        const fin = new Date(fechaFin).getTime();

        for (const h of data.data) {
            if (h.idEspacio === parseInt(idEspacio)) {
                const hInicio = new Date(h.fechaInicio).getTime();
                const hFin = new Date(h.fechaFin).getTime();
                
                // Verificar solapamiento
                if (!(fin <= hInicio || inicio >= hFin)) {
                    return false; // Hay conflicto
                }
            }
        }
        return true; // Sin conflictos
    } catch (e) {
        console.error('Error validando disponibilidad:', e);
        return true; // Si hay error, permitir continuar
    }
}

async function validarSinReservaExistente(idEspacio, fechaInicio, fechaFin) {
    try {
        const res = await fetch(`${API_BASE_URL}/reservas`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (!data.success || !data.data) return true;

        const inicio = new Date(fechaInicio).getTime();
        const fin = new Date(fechaFin).getTime();

        for (const r of data.data) {
            if (r.idEspacio === parseInt(idEspacio) && r.estado !== 'CANCELADA') {
                const rInicio = new Date(r.fechaInicio).getTime();
                const rFin = new Date(r.fechaFin).getTime();
                if (!(fin <= rInicio || inicio >= rFin)) {
                    return false; // Hay conflicto con reserva existente
                }
            }
        }
        return true;
    } catch (e) {
        console.error('Error validando reservas existentes:', e);
        return true;
    }
}

// ============================================================
// CRUD DE PAGOS
// ============================================================
window._allPagos = [];

async function cargarPagos() {
    try {
        const res = await fetch(`${API_BASE_URL}/pagos`, { headers: getAuthHeaders() });
        const data = await res.json();
        window._allPagos = data.success ? (data.data || []) : [];
        renderPagos(window._allPagos);
    } catch (e) {
        console.error('Error cargando pagos:', e);
        window._allPagos = [];
    }
}

function renderPagos(pagos) {
    const tbody = document.getElementById('pagos-tbody');
    if (!tbody) return;
    if (!pagos || pagos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-3">No hay pagos registrados</td></tr>';
        return;
    }
    tbody.innerHTML = pagos.map(p => {
        const estadoBadge = p.estadoPago === 'COMPLETADO'
            ? 'success' : p.estadoPago === 'RECHAZADO' ? 'danger' : 'warning';
        const metodoBadge = p.estadoPago === 'COMPLETADO' ? 'success' : 'secondary';
        const fechaCreacionStr = p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleDateString('es-PE') : '-';
        const fechaPagoStr = p.fechaPago ? new Date(p.fechaPago).toLocaleDateString('es-PE') : '-';
        const pagarBtn = p.estadoPago !== 'COMPLETADO'
            ? `<button class="btn btn-sm btn-success" onclick="procesarPago(${p.idPago}, ${p.monto}, '${(p.nombreCliente||'').replace(/'/g,'')}', '${(p.emailCliente||'').replace(/'/g,'')}')">💳 Pagar</button>`
            : `<span class="text-success fw-bold">✅ Pagado</span>`;
        return `
        <tr>
            <td><strong>#${p.idPago}</strong></td>
            <td><span class="badge bg-primary">R#${p.idReserva}</span></td>
            <td>${p.nombreCliente || '-'}</td>
            <td class="fw-bold text-success">S/. ${(p.monto || 0).toFixed(2)}</td>
            <td><span class="badge bg-${metodoBadge}">${p.metodoPago || 'Pendiente'}</span></td>
            <td><span class="badge bg-${estadoBadge}">${p.estadoPago || 'PENDIENTE'}</span></td>
            <td>${fechaCreacionStr}</td>
            <td>${fechaPagoStr}</td>
            <td>${pagarBtn}</td>
        </tr>`;
    }).join('');
}

async function procesarPago(idPago, monto, nombreCliente, emailCliente) {
    await seleccionarMetodoPago(idPago, monto, nombreCliente, emailCliente);
}

function seleccionarMetodoPago(idPago, monto, nombreCliente, emailCliente) {
    return new Promise((resolve) => {
        const viejo = document.getElementById('pagoMetodoModal');
        if (viejo) { const inst = bootstrap.Modal.getInstance(viejo); if (inst) inst.dispose(); viejo.remove(); }

        let montoFinal = typeof monto === 'number' ? monto : parseFloat(monto);
        let idDescuentoAplicado = null;
        const activeUser = getActiveUser();
        const esCliente = activeUser && activeUser.rol === 'CLIENTE';

        // ── CAMPOS DE TARJETA ─────────────────────────────────────────
        const camposTarjeta = `
        <div id="pm-card-fields" style="margin-top:16px">
            <div style="margin-bottom:12px">
                <label style="font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.5px;margin-bottom:4px;display:block">Número de tarjeta</label>
                <div style="position:relative">
                    <input type="text" id="pm-numero-tarjeta" maxlength="19" placeholder="0000 0000 0000 0000"
                        style="width:100%;padding:11px 44px 11px 12px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:15px;font-family:monospace;letter-spacing:2px;outline:none;box-sizing:border-box"
                        oninput="this.value=this.value.replace(/[^0-9]/g,'').replace(/(.{4})/g,'$1 ').trim().substring(0,19)"
                        onfocus="this.style.borderColor='#6366f1'" onblur="this.style.borderColor='#e2e8f0'">
                    <span style="position:absolute;right:12px;top:50%;transform:translateY(-50%);font-size:20px" id="pm-card-brand">💳</span>
                </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
                <div>
                    <label style="font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.5px;margin-bottom:4px;display:block">Vencimiento</label>
                    <input type="text" id="pm-expiracion" maxlength="5" placeholder="MM/AA"
                        style="width:100%;padding:11px 12px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:14px;font-family:monospace;outline:none;box-sizing:border-box"
                        oninput="let v=this.value.replace(/[^0-9]/g,'');if(v.length>=2)v=v.substring(0,2)+'/'+v.substring(2);this.value=v"
                        onfocus="this.style.borderColor='#6366f1'" onblur="this.style.borderColor='#e2e8f0'">
                </div>
                <div>
                    <label style="font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.5px;margin-bottom:4px;display:block">CVV</label>
                    <input type="password" id="pm-cvv" maxlength="4" placeholder="•••"
                        style="width:100%;padding:11px 12px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:14px;font-family:monospace;outline:none;box-sizing:border-box"
                        oninput="this.value=this.value.replace(/[^0-9]/g,'')"
                        onfocus="this.style.borderColor='#6366f1'" onblur="this.style.borderColor='#e2e8f0'">
                </div>
            </div>
        </div>`;

        // ── SELECTOR MÉTODO (solo admin) ──────────────────────────────
        const selectorMetodo = esCliente ? '' : `
        <div style="margin-bottom:14px">
            <label style="font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.5px;margin-bottom:6px;display:block">Método de pago</label>
            <select id="pm-metodo" style="width:100%;padding:10px 12px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:14px;outline:none;background:#fff"
                onchange="toggleCamposTarjeta(this.value)">
                <option value="EFECTIVO">💵 Efectivo</option>
                <option value="TRANSFERENCIA">🏦 Transferencia Bancaria</option>
            </select>
            <div id="pm-card-fields-wrapper" style="display:none">${camposTarjeta}</div>
        </div>`;

        const modalEl = document.createElement('div');
        modalEl.id = 'pagoMetodoModal';
        modalEl.className = 'modal fade';
        modalEl.tabIndex = -1;
        modalEl.innerHTML = `
        <div class="modal-dialog modal-dialog-centered" style="max-width:400px">
          <div class="modal-content" style="border-radius:16px;overflow:hidden;border:none;box-shadow:0 20px 60px rgba(0,0,0,0.2)">

            <!-- HEADER -->
            <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:20px 24px;display:flex;align-items:center;gap:12px">
                <div style="background:rgba(255,255,255,0.2);border-radius:10px;padding:8px;font-size:22px">💳</div>
                <div>
                    <div style="color:#fff;font-weight:800;font-size:17px">Pasarela de Pagos</div>
                    <div style="color:rgba(255,255,255,0.75);font-size:12px">Transacción segura</div>
                </div>
                <button onclick="document.getElementById('pagoMetodoModal').querySelector('[data-bs-dismiss]').click()"
                    style="margin-left:auto;background:rgba(255,255,255,0.2);border:none;border-radius:8px;padding:6px 10px;color:#fff;cursor:pointer;font-size:16px">✕</button>
            </div>

            <!-- BODY -->
            <div style="padding:20px 24px">

                <!-- Monto destacado -->
                <div style="text-align:center;margin-bottom:18px;padding:14px;background:#f5f3ff;border-radius:12px">
                    <div style="font-size:11px;color:#7c3aed;font-weight:700;text-transform:uppercase;letter-spacing:.5px;margin-bottom:4px">Total a pagar</div>
                    <div style="font-size:28px;font-weight:800;color:#6366f1">S/. <span id="pm-monto">${montoFinal.toFixed(2)}</span></div>
                    <div style="font-size:12px;color:#94a3b8;margin-top:2px" id="pm-cliente-label"></div>
                </div>

                <!-- Descuento (solo admin) -->
                ${!esCliente ? `
                <div style="margin-bottom:14px">
                    <label style="font-size:11px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.5px;margin-bottom:6px;display:block">🎁 Código de descuento</label>
                    <div style="display:flex;gap:8px">
                        <input type="text" id="pm-codigo-descuento" placeholder="Ej: PROMO25"
                            style="flex:1;padding:9px 12px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:13px;outline:none;text-transform:uppercase"
                            onfocus="this.style.borderColor='#6366f1'" onblur="this.style.borderColor='#e2e8f0'">
                        <button id="pm-validar-codigo"
                            style="padding:9px 14px;background:#6366f1;color:#fff;border:none;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;white-space:nowrap">Validar</button>
                    </div>
                    <div id="pm-descuento-msg" style="margin-top:6px;font-size:12px"></div>
                </div>` : ''}

                <!-- Selector método (admin) o campos tarjeta directo (cliente) -->
                ${selectorMetodo}
                ${esCliente ? camposTarjeta : ''}

                <!-- Logos tarjetas -->
                <div style="display:flex;gap:6px;justify-content:center;margin-top:16px;opacity:.5;font-size:11px;color:#94a3b8;align-items:center">
                    <span>VISA</span><span>•</span><span>MASTERCARD</span><span>•</span><span>AMEX</span>
                </div>
            </div>

            <!-- FOOTER -->
            <div style="padding:0 24px 20px">
                <button id="pm-confirmar"
                    style="width:100%;padding:14px;background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;border:none;border-radius:10px;font-size:16px;font-weight:700;cursor:pointer;letter-spacing:.3px">
                    Pagar S/. <span id="pm-btn-monto">${montoFinal.toFixed(2)}</span>
                </button>
                <button data-bs-dismiss="modal" id="pm-cancelar"
                    style="width:100%;padding:10px;background:transparent;color:#94a3b8;border:none;font-size:13px;cursor:pointer;margin-top:8px">
                    Cancelar
                </button>
            </div>

          </div>
        </div>`;
        document.body.appendChild(modalEl);

        const nombreModal = nombreCliente || (activeUser && activeUser.nombre) || '';
        const emailModal  = emailCliente  || (activeUser && activeUser.email)  || '';
        document.getElementById('pm-cliente-label').textContent = nombreModal + (emailModal ? ' · ' + emailModal : '');

        const modal = new bootstrap.Modal(modalEl);
        modal.show();

        // Toggle campos tarjeta para admin
        window.toggleCamposTarjeta = function(metodo) {
            const w = document.getElementById('pm-card-fields-wrapper');
            if (w) w.style.display = metodo === 'TARJETA' ? 'block' : 'none';
        };

        // Detectar marca de tarjeta
        const numInput = document.getElementById('pm-numero-tarjeta');
        if (numInput) {
            numInput.addEventListener('input', function() {
                const v = this.value.replace(/\s/g,'');
                const brand = document.getElementById('pm-card-brand');
                if (!brand) return;
                if (/^4/.test(v)) brand.textContent = '💳 VISA';
                else if (/^5[1-5]/.test(v)) brand.textContent = '💳 MC';
                else if (/^3[47]/.test(v)) brand.textContent = '💳 AMEX';
                else brand.textContent = '💳';
            });
        }

        // Validar descuento (admin)
        const btnDesc = document.getElementById('pm-validar-codigo');
        if (btnDesc) {
            btnDesc.onclick = async () => {
                const codigo = document.getElementById('pm-codigo-descuento').value.trim().toUpperCase();
                const msgEl  = document.getElementById('pm-descuento-msg');
                if (!codigo) { msgEl.innerHTML = '<span style="color:#ef4444">Ingrese un código</span>'; return; }
                try {
                    const res  = await fetch(`${API_BASE_URL}/descuentos/validar`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ codigo, monto: montoFinal }) });
                    const data = await res.json();
                    if (data.success && data.data) {
                        const porcentaje = data.data.porcentaje;
                        const desc = (montoFinal * porcentaje) / 100;
                        montoFinal -= desc;
                        idDescuentoAplicado = data.data.idDescuento;
                        document.getElementById('pm-monto').textContent    = montoFinal.toFixed(2);
                        document.getElementById('pm-btn-monto').textContent = montoFinal.toFixed(2);
                        msgEl.innerHTML = `<span style="color:#10b981;font-weight:600">✅ ${porcentaje}% aplicado — ahorras S/. ${desc.toFixed(2)}</span>`;
                        btnDesc.disabled = true;
                        document.getElementById('pm-codigo-descuento').disabled = true;
                    } else {
                        msgEl.innerHTML = `<span style="color:#ef4444">❌ ${data.message || 'Código inválido o vencido'}</span>`;
                    }
                } catch(e) { msgEl.innerHTML = '<span style="color:#ef4444">Error de conexión</span>'; }
            };
        }

        document.getElementById('pm-cancelar').onclick = () => { modal.hide(); resolve(null); };

        document.getElementById('pm-confirmar').onclick = async () => {
            const metodo = esCliente ? 'TARJETA' : (document.getElementById('pm-metodo') ? document.getElementById('pm-metodo').value : 'TARJETA');
            const payload = { metodoPago: metodo, montoFinal, idDescuento: idDescuentoAplicado };

            // Si es tarjeta, incluir datos
            if (metodo === 'TARJETA') {
                const numEl = document.getElementById('pm-numero-tarjeta');
                const expEl = document.getElementById('pm-expiracion');
                const cvvEl = document.getElementById('pm-cvv');
                const num = numEl ? numEl.value.replace(/\s/g,'') : '';
                const exp = expEl ? expEl.value : '';
                const cvv = cvvEl ? cvvEl.value : '';
                if (!num || num.length < 16) { showAlert('danger', '❌ Número de tarjeta inválido (debe tener 16 dígitos)'); return; }
                if (!exp || !/^\d{2}\/\d{2}$/.test(exp)) { showAlert('danger', '❌ Fecha de vencimiento inválida (MM/AA)'); return; }
                if (!cvv || cvv.length < 3)  { showAlert('danger', '❌ CVV inválido'); return; }
                payload.numeroTarjeta = num;
                payload.expiracion    = exp;
                payload.cvv           = cvv;
            }

            modal.hide();
            try {
                const res  = await fetch(`${API_BASE_URL}/pagos/${idPago}/pagar`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', `✅ Pago procesado. Se envió confirmación a ${emailModal || 'tu correo'}.`);
                    await cargarPagos();
                    if (window._allReservas && window._allReservas.length > 0) {
                        await loadReservas();
                        if (currentUser && currentUser.rol === 'CLIENTE' && typeof renderClienteHistorial === 'function') renderClienteHistorial();
                    }
                } else {
                    showAlert('danger', '❌ ' + (data.error || 'No se pudo procesar el pago'));
                }
            } catch(e) { showAlert('danger', '❌ Error de conexión al procesar el pago'); }
            resolve(metodo);
        };
    });
}

function filtrarPagos() {
    const filtro = document.getElementById('filtroPagos').value.toLowerCase();
    // Filtrar solo pagos PENDIENTES que coincidan con el término de búsqueda
    const filtrados = window._allPagos.filter(p => {
        if (p.estadoPago === 'COMPLETADO') return false;
        const texto = ((p.idReserva || '') + ' ' + (p.monto || '') + ' ' + (p.nombreCliente || '') + ' ' + (p.estadoPago || '')).toLowerCase();
        return texto.includes(filtro);
    });
    renderPagos(filtrados);
}

/* DESHABILITADO: Los pagos se crean automáticamente cuando se crea una reserva
function abrirFormularioPago() {
    const form = `
        <div class="card">
            <div class="card-header bg-warning">Registrar Nuevo Pago</div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label>ID Reserva</label>
                        <input type="number" class="form-control" id="pago-reserva" min="1" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label>Monto (S/.)</label>
                        <input type="number" class="form-control" id="pago-monto" min="0.01" step="0.01" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label>Método de Pago</label>
                    <select class="form-select" id="pago-metodo" required>
                        <option value="">Seleccionar...</option>
                        <option value="TARJETA">Tarjeta de Crédito</option>
                        <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                        <option value="EFECTIVO">Efectivo</option>
                    </select>
                </div>
                <div class="d-flex gap-2">
                    <button class="btn btn-primary" onclick="guardarPago()">💾 Guardar</button>
                    <button class="btn btn-secondary" onclick="cerrarFormulario()">Cancelar</button>
                </div>
            </div>
        </div>
    `;
    mostrarModal('Nuevo Pago', form);
}
*/

/* DESHABILITADO: Los pagos se crean automáticamente
async function guardarPago() {
    const idReserva = document.getElementById('pago-reserva').value;
    const monto = document.getElementById('pago-monto').value;
    const metodo = document.getElementById('pago-metodo').value;
    
    if (!idReserva || !monto || !metodo) {
        alert('Complete todos los campos');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/pagos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idReserva: parseInt(idReserva), monto: parseFloat(monto), metodoPago: metodo })
        });
        const data = await res.json();
        if (data.success) {
            alert('✓ Pago registrado exitosamente');
            cerrarFormulario();
            cargarPagos();
        } else {
            alert('Error: ' + (data.error || 'No se pudo registrar el pago'));
        }
    } catch (e) {
        alert('Error de conexión: ' + e.message);
    }
}
*/

function editarPago(id) {
    alert('Edición en desarrollo');
}

function eliminarPago(id) {
    if (confirm('¿Eliminar este pago?')) {
        alert('Eliminación en desarrollo');
    }
}

// ============================================================
// CRUD DE DESCUENTOS
// ============================================================
let _allDescuentos = [];

async function cargarDescuentos() {
    try {
        const res = await fetch(`${API_BASE_URL}/descuentos`, { headers: getAuthHeaders() });
        const data = await res.json();
        _allDescuentos = data.success ? (data.data || []) : [];
        renderDescuentos(_allDescuentos);
    } catch (e) {
        console.error('Error cargando descuentos:', e);
        _allDescuentos = [];
    }
}

function renderDescuentos(descuentos) {
    const tbody = document.getElementById('descuentos-tbody') || {};
    if (!tbody) return;
    if (!descuentos || descuentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-3">No hay descuentos registrados</td></tr>';
        return;
    }
    tbody.innerHTML = descuentos.map(d => {
        const fIni = d.fechaInicio ? d.fechaInicio.substring(0,10) : '-';
        const fFin = d.fechaFin ? d.fechaFin.substring(0,10) : '-';
        return `
        <tr>
            <td><strong>${d.codigo}</strong></td>
            <td>${d.descripcion || '-'}</td>
            <td><span class="badge bg-success">${d.porcentaje}%</span></td>
            <td>S/. ${(d.montoMinimo || 0).toFixed(2)}</td>
            <td><small>${fIni} → ${fFin}</small></td>
            <td>${d.usosActuales}/${d.usosMaximos || '∞'}</td>
            <td><span class="badge bg-${d.estado === 'ACTIVO' ? 'success' : 'secondary'}">${d.estado}</span></td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editarDescuento(${d.idDescuento})">✏️</button>
                <button class="btn btn-sm btn-danger" onclick="eliminarDescuento(${d.idDescuento})">🗑️</button>
            </td>
        </tr>
        `;
    }).join('');
}

function filtrarDescuentos() {
    const filtro = document.getElementById('filtroDescuentos').value.toLowerCase();
    const filtrados = _allDescuentos.filter(d => {
        const texto = (d.codigo + ' ' + d.descripcion).toLowerCase();
        return texto.includes(filtro);
    });
    renderDescuentos(filtrados);
}

function abrirFormularioDescuento() {
    _abrirDescuentoModal(null);
}

async function editarDescuento(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/descuentos/${id}`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (!data.success || !data.data) { showAlert('error', 'No se pudo cargar el descuento'); return; }
        _abrirDescuentoModal(data.data);
    } catch (e) {
        showAlert('error', 'Error de conexión');
    }
}

function _abrirDescuentoModal(d) {
    const esEdicion = d !== null && d !== undefined;
    const today = new Date().toISOString().split('T')[0];
    const futuro = new Date(Date.now() + 30*24*60*60*1000).toISOString().split('T')[0];

    // Eliminar instancia previa
    const viejo = document.getElementById('descuentoModal');
    if (viejo) {
        const inst = bootstrap.Modal.getInstance(viejo);
        if (inst) inst.dispose();
        viejo.remove();
    }

    const estadoHtml = esEdicion
        ? `<div class="mb-3">
                <label class="form-label">Estado</label>
                <select class="form-select" id="desc-estado">
                    <option value="ACTIVO" ${d.estado === 'ACTIVO' ? 'selected' : ''}>ACTIVO</option>
                    <option value="INACTIVO" ${d.estado === 'INACTIVO' ? 'selected' : ''}>INACTIVO</option>
                </select>
           </div>`
        : '';

    const modalEl = document.createElement('div');
    modalEl.id = 'descuentoModal';
    modalEl.className = 'modal fade';
    modalEl.tabIndex = -1;
    modalEl.innerHTML = `
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title">${esEdicion ? '✏️ Editar' : '➕ Nuevo'} Código de Descuento</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" id="desc-id" value="${esEdicion ? d.idDescuento : ''}">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-bold">Código *</label>
                            <input type="text" class="form-control text-uppercase" id="desc-codigo"
                                placeholder="Ej: PROMO25" value="${esEdicion ? (d.codigo || '') : ''}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-bold">Porcentaje (%) *</label>
                            <input type="number" class="form-control" id="desc-porcentaje"
                                min="1" max="100" value="${esEdicion ? d.porcentaje : ''}">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-bold">Descripción *</label>
                            <input type="text" class="form-control" id="desc-descripcion"
                                value="${esEdicion ? (d.descripcion || '') : ''}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Monto Mínimo (S/.)</label>
                            <input type="number" class="form-control" id="desc-monto"
                                min="0" step="0.01" value="${esEdicion ? d.montoMinimo : '0'}">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label fw-bold">Fecha Inicio *</label>
                            <input type="date" class="form-control" id="desc-fecha-inicio"
                                value="${esEdicion ? (d.fechaInicio ? d.fechaInicio.split('T')[0] : today) : today}">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label fw-bold">Fecha Fin *</label>
                            <input type="date" class="form-control" id="desc-fecha-fin"
                                value="${esEdicion ? (d.fechaFin ? d.fechaFin.split('T')[0] : futuro) : futuro}">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label fw-bold">Usos Máximos *</label>
                            <input type="number" class="form-control" id="desc-usos"
                                min="1" value="${esEdicion ? d.usosMaximos : '100'}">
                        </div>
                    </div>
                    ${estadoHtml}
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn btn-success" id="desc-btn-guardar">💾 Guardar</button>
                </div>
            </div>
        </div>`;
    document.body.appendChild(modalEl);
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    document.getElementById('desc-btn-guardar').onclick = async () => {
        const id = document.getElementById('desc-id').value;
        const codigo = (document.getElementById('desc-codigo').value || '').toUpperCase().trim();
        const porcentaje = document.getElementById('desc-porcentaje').value;
        const descripcion = (document.getElementById('desc-descripcion').value || '').trim();
        const monto = document.getElementById('desc-monto').value || 0;
        const fechaInicio = document.getElementById('desc-fecha-inicio').value;
        const fechaFin = document.getElementById('desc-fecha-fin').value;
        const usosMaximos = document.getElementById('desc-usos').value || 100;
        const estadoEl = document.getElementById('desc-estado');
        const estado = estadoEl ? estadoEl.value : 'ACTIVO';

        if (!codigo || !porcentaje || !descripcion || !fechaInicio || !fechaFin) {
            showAlert('error', 'Complete todos los campos obligatorios'); return;
        }
        if (parseFloat(porcentaje) <= 0 || parseFloat(porcentaje) > 100) {
            showAlert('error', 'El porcentaje debe estar entre 1 y 100'); return;
        }
        if (fechaFin < fechaInicio) {
            showAlert('error', 'La fecha fin debe ser mayor a la fecha inicio'); return;
        }

        try {
            const method = id ? 'PUT' : 'POST';
            const url = id ? `${API_BASE_URL}/descuentos/${id}` : `${API_BASE_URL}/descuentos`;
            const res = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    codigo, descripcion,
                    porcentaje: parseFloat(porcentaje),
                    montoMinimo: parseFloat(monto),
                    fechaInicio, fechaFin,
                    usosMaximos: parseInt(usosMaximos),
                    estado
                })
            });
            const data = await res.json();
            if (data.success) {
                showAlert('success', id ? '✅ Descuento actualizado' : '✅ Descuento creado');
                modal.hide();
                cargarDescuentos();
            } else {
                showAlert('error', data.error || 'No se pudo guardar');
            }
        } catch (e) {
            showAlert('error', 'Error de conexión: ' + e.message);
        }
    };
}

async function eliminarDescuento(id) {
    showConfirm({
        message: '¿Desactivar este código de descuento?',
        icon: '🎁',
        title: 'Desactivar Descuento',
        btnLabel: 'Sí, desactivar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/descuentos/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Descuento desactivado');
                    cargarDescuentos();
                } else {
                    showAlert('error', data.error || 'No se pudo desactivar');
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

// ============================================================
// CRUD DE EVALUACIONES
// ============================================================
let _allEvaluaciones = [];

async function cargarEvaluaciones() {
    try {
        const res = await fetch(`${API_BASE_URL}/evaluaciones`, { headers: getAuthHeaders() });
        const data = await res.json();
        _allEvaluaciones = data.success ? (data.data || []) : [];
        renderEvaluaciones(_allEvaluaciones);
    } catch (e) {
        console.error('Error cargando evaluaciones:', e);
        _allEvaluaciones = [];
    }
}

function renderEvaluaciones(evaluaciones) {
    const tbody = document.getElementById('evaluaciones-tbody') || {};
    if (!tbody) return;
    tbody.innerHTML = evaluaciones.map(e => {
        const stars = '⭐'.repeat(e.calificacion) + '☆'.repeat(5 - e.calificacion);
        const nombreCliente = e.nombreCliente || '-';
        const emailCliente = e.emailCliente || '-';
        const comentario = e.comentario ? e.comentario.substring(0, 50) : '-';
        return `
        <tr>
            <td><strong>#${e.idReserva || '-'}</strong></td>
            <td>${nombreCliente}</td>
            <td>${emailCliente}</td>
            <td>${stars} (${e.calificacion}/5)</td>
            <td title="${e.comentario || ''}"><small>${comentario}${e.comentario && e.comentario.length > 50 ? '...' : ''}</small></td>
            <td><small>${new Date(e.fechaEvaluacion).toLocaleDateString('es-PE') || '-'}</small></td>
        </tr>
    `}).join('');
}

function filtrarEvaluaciones() {
    const filtro = document.getElementById('filtroEvaluaciones').value.toLowerCase();
    const filtrados = _allEvaluaciones.filter(e => {
        const texto = ((e.nombreCliente || '') + ' ' + (e.emailCliente || '')).toLowerCase();
        return texto.includes(filtro);
    });
    renderEvaluaciones(filtrados);
}

function abrirFormularioEvaluacion() {
    const form = `
        <div class="card">
            <div class="card-header bg-info text-white">Nueva Evaluación</div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label>ID Reserva</label>
                        <input type="number" class="form-control" id="eval-reserva" min="1" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label>ID Usuario</label>
                        <input type="number" class="form-control" id="eval-usuario" min="1" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label>Calificación (1-5 ⭐)</label>
                    <select class="form-select" id="eval-calificacion" required>
                        <option value="">Seleccionar...</option>
                        <option value="5">⭐⭐⭐⭐⭐ Excelente (5)</option>
                        <option value="4">⭐⭐⭐⭐☆ Muy Bueno (4)</option>
                        <option value="3">⭐⭐⭐☆☆ Bueno (3)</option>
                        <option value="2">⭐⭐☆☆☆ Regular (2)</option>
                        <option value="1">⭐☆☆☆☆ Malo (1)</option>
                    </select>
                </div>
                <div class="mb-3">
                    <label>Comentario</label>
                    <textarea class="form-control" id="eval-comentario" rows="3" placeholder="Comparte tu experiencia..."></textarea>
                </div>
                <div class="d-flex gap-2">
                    <button class="btn btn-info text-white" onclick="guardarEvaluacion()">💾 Guardar</button>
                    <button class="btn btn-secondary" onclick="cerrarFormulario()">Cancelar</button>
                </div>
            </div>
        </div>
    `;
    mostrarModal('Nueva Evaluación', form);
}

async function guardarEvaluacion() {
    const idReserva = document.getElementById('eval-reserva').value;
    const idUsuario = document.getElementById('eval-usuario').value;
    const calificacion = document.getElementById('eval-calificacion').value;
    const comentario = document.getElementById('eval-comentario').value;
    
    if (!idReserva || !idUsuario || !calificacion) {
        alert('Complete los campos obligatorios');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/evaluaciones`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idReserva: parseInt(idReserva), idUsuario: parseInt(idUsuario), calificacion: parseInt(calificacion), comentario })
        });
        const data = await res.json();
        if (data.success) {
            alert('✓ Evaluación registrada exitosamente');
            cerrarFormulario();
            cargarEvaluaciones();
        } else {
            alert('Error: ' + (data.error || 'No se pudo guardar'));
        }
    } catch(e) {
        alert('Error de conexión');
    }
}

// ============================================================
// CALENDARIO DISPONIBILIDAD SEMANAL EN MODAL RESERVA
// ============================================================
let _calSemanaOffset = 0;

let _calSlotInicio = null; // Para tracking de slot seleccionado

async function selectSlotReserva(slotIniISO, slotFinISO) {
    const slotIni = new Date(slotIniISO);
    const slotFin = new Date(slotFinISO);
    
    const fIniEl = document.getElementById('reservaFechaInicio');
    const fFinEl = document.getElementById('reservaFechaFin');
    
    const fIniVal = fIniEl.value ? new Date(fIniEl.value) : null;
    
    if (!fIniVal) {
        // Si no hay inicio, este slot es el inicio
        fIniEl.value = slotIni.toISOString().slice(0, 16);
    } else if (slotIni.getTime() >= fIniVal.getTime()) {
        // Si hay inicio y este slot es posterior, es el fin
        fFinEl.value = slotFin.toISOString().slice(0, 16);
    } else {
        // Si este slot es anterior al inicio, intercambiar
        fFinEl.value = fIniEl.value;
        fIniEl.value = slotIni.toISOString().slice(0, 16);
    }
    
    calcularMonto();
    renderCalendarioDisponibilidad();
}

async function renderCalendarioDisponibilidad() {
    const idEspacio = document.getElementById('reservaIdEspacio').value;
    const container = document.getElementById('calendario-disponibilidad');
    if (!idEspacio) {
        container.innerHTML = '<div class="text-muted text-center py-5"><span style="font-size:2rem">📅</span><p class="mt-2 mb-0">Selecciona un espacio para ver<br>la disponibilidad semanal</p></div>';
        return;
    }

    // Calcular lunes de la semana actual + offset
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7) + _calSemanaOffset * 7);

    // Obtener reservas existentes
    let reservas = [];
    try {
        const res = await fetch(`${API_BASE_URL}/reservas`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (data.success) reservas = (data.data || []).filter(r => r.idEspacio === parseInt(idEspacio) && r.estado !== 'CANCELADA');
    } catch(e) {}

    // Días de la semana (Lun-Dom)
    const dias = [];
    for (let i = 0; i < 7; i++) {
        const d = new Date(lunes);
        d.setDate(lunes.getDate() + i);
        dias.push(d);
    }

    const nombresDia = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];
    const hoyStr = hoy.toDateString();

    // Fechas seleccionadas en el form
    const fIniVal = document.getElementById('reservaFechaInicio').value;
    const fFinVal = document.getElementById('reservaFechaFin').value;
    const selIni = fIniVal ? new Date(fIniVal).getTime() : null;
    const selFin = fFinVal ? new Date(fFinVal).getTime() : null;

    // Rango de horas a mostrar (6:00 - 22:00)
    const HORA_INI = 6, HORA_FIN = 22;

    let html = `<div class="cal-header">
        <h6>📅 Disponibilidad Semanal</h6>
        <div class="cal-week-nav">
            <button class="btn btn-outline-secondary btn-sm" onclick="_calSemanaOffset--; renderCalendarioDisponibilidad()">‹</button>
            <button class="btn btn-outline-primary btn-sm" onclick="_calSemanaOffset=0; renderCalendarioDisponibilidad()">Hoy</button>
            <button class="btn btn-outline-secondary btn-sm" onclick="_calSemanaOffset++; renderCalendarioDisponibilidad()">›</button>
        </div>
    </div>
    <div class="cal-scroll">
    <table class="cal-table">
    <thead><tr><th class="cal-time-col"></th>`;

    dias.forEach((d, i) => {
        const esHoy = d.toDateString() === hoyStr;
        html += `<th class="${esHoy ? 'cal-today' : ''}">${nombresDia[i]}<br><span style="font-weight:400">${d.getDate()}/${d.getMonth()+1}</span></th>`;
    });
    html += '</tr></thead><tbody>';

    for (let h = HORA_INI; h < HORA_FIN; h++) {
        html += `<tr><td class="cal-time-col">${String(h).padStart(2,'0')}:00</td>`;
        dias.forEach(d => {
            const slotIni = new Date(d); slotIni.setHours(h, 0, 0, 0);
            const slotFin = new Date(d); slotFin.setHours(h + 1, 0, 0, 0);
            const siMs = slotIni.getTime(), sfMs = slotFin.getTime();

            // ¿Hay reserva que solapa este slot?
            const reserva = reservas.find(r => {
                const ri = new Date(r.fechaInicio).getTime();
                const rf = new Date(r.fechaFin).getTime();
                return ri < sfMs && rf > siMs;
            });

            // ¿Está dentro del rango seleccionado?
            const enSeleccion = selIni && selFin && selIni < sfMs && selFin > siMs;

            let cls, title;
            if (reserva) {
                cls = 'ocupado';
                title = `Ocupado: ${reserva.nombreCliente || 'Reservado'}`;
            } else if (enSeleccion) {
                cls = 'seleccionado';
                title = 'Tu selección';
            } else {
                cls = 'libre';
                title = 'Disponible';
            }
            const onclick = (reserva || !slotIni) ? '' : `onclick="selectSlotReserva('${slotIni.toISOString()}', '${slotFin.toISOString()}')"`;
            html += `<td><span class="cal-slot ${cls}" title="${title}" ${onclick} style="${!reserva ? 'cursor:pointer' : ''}"></span></td>`;
        });
        html += '</tr>';
    }

    html += `</tbody></table></div>
    <div class="cal-legend">
        <span class="cal-legend-item"><span class="cal-legend-dot" style="background:#d4edda"></span> Libre</span>
        <span class="cal-legend-item"><span class="cal-legend-dot" style="background:#f8d7da"></span> Ocupado</span>
        <span class="cal-legend-item"><span class="cal-legend-dot" style="background:#cce5ff"></span> Tu selección</span>
    </div>`;

    container.innerHTML = html;
}

async function validarSinReservaExistente(idEspacio, fechaInicio, fechaFin) {
    try {
        const res = await fetch(`${API_BASE_URL}/reservas`, { headers: getAuthHeaders() });
        const data = await res.json();
        if (!data.success || !data.data) return true;

        const inicio = new Date(fechaInicio).getTime();
        const fin = new Date(fechaFin).getTime();

        for (const r of data.data) {
            if (r.idEspacio === parseInt(idEspacio) && r.estado !== 'CANCELADA') {
                const rInicio = new Date(r.fechaInicio).getTime();
                const rFin = new Date(r.fechaFin).getTime();
                if (!(fin <= rInicio || inicio >= rFin)) {
                    return false;
                }
            }
        }
        return true;
    } catch (e) {
        console.error('Error validando reservas existentes:', e);
        return true;
    }
}



function editarEvaluacion(id) {
    alert('Edición en desarrollo');
}

function eliminarEvaluacion(id) {
    if (confirm('¿Eliminar esta evaluación?')) {
        alert('Eliminación en desarrollo');
    }
}

// ============================================================
// NOTIFICACIONES
// ============================================================
let _allNotificaciones = [];

async function cargarNotificaciones() {
    try {
        const res = await fetch(`${API_BASE_URL}/notificaciones`, { headers: getAuthHeaders() });
        const data = await res.json();
        _allNotificaciones = data.success ? (data.data || []) : [];
        renderNotificaciones(_allNotificaciones);
    } catch (e) {
        console.error('Error cargando notificaciones:', e);
        _allNotificaciones = [];
    }
}

function renderNotificaciones(notificaciones) {
    const container = document.getElementById('notificaciones-list');
    container.innerHTML = '';

    if (!notificaciones || notificaciones.length === 0) {
        container.innerHTML = `<div class="col-12">
            <div style="text-align:center;padding:64px 24px;background:#f8fafc;border-radius:16px;border:2px dashed #e2e8f0">
                <div style="width:64px;height:64px;background:#e2e8f0;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 16px">
                    <i class="fas fa-bell-slash" style="font-size:26px;color:#94a3b8"></i>
                </div>
                <p style="font-size:16px;font-weight:700;color:#374151;margin-bottom:4px">Sin notificaciones</p>
                <p style="font-size:13px;color:#9ca3af">El sistema no tiene notificaciones pendientes.</p>
            </div></div>`;
        return;
    }

    const tipoConfig = {
        'EVALUACION':   { fa:'fa-star',        color:'#f59e0b', bg:'#fffbeb', border:'#fcd34d', label:'Evaluación'   },
        'PAGO':         { fa:'fa-credit-card', color:'#10b981', bg:'#f0fdf4', border:'#6ee7b7', label:'Pago'         },
        'RESERVA':      { fa:'fa-calendar-check', color:'#3b82f6', bg:'#eff6ff', border:'#93c5fd', label:'Reserva'   },
        'RECORDATORIO': { fa:'fa-clock',       color:'#8b5cf6', bg:'#f5f3ff', border:'#c4b5fd', label:'Recordatorio' },
        'PROMOCION':    { fa:'fa-tag',         color:'#ec4899', bg:'#fdf2f8', border:'#f9a8d4', label:'Promoción'    },
        'SISTEMA':      { fa:'fa-cog',         color:'#64748b', bg:'#f8fafc', border:'#cbd5e1', label:'Sistema'      },
    };

    // Contador de no leídas
    const noLeidas = notificaciones.filter(n => n.leida != 1).length;

    let html = `<div class="col-12">
        <!-- Barra de resumen -->
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;padding:14px 20px;background:#fff;border-radius:12px;box-shadow:0 1px 6px rgba(0,0,0,0.06);border:1px solid #e2e8f0">
            <div style="display:flex;align-items:center;gap:10px">
                <div style="width:36px;height:36px;background:#f1f5f9;border-radius:10px;display:flex;align-items:center;justify-content:center">
                    <i class="fas fa-bell" style="color:#3b82f6;font-size:15px"></i>
                </div>
                <div>
                    <div style="font-size:14px;font-weight:700;color:#1e293b">${notificaciones.length} notificaciones</div>
                    <div style="font-size:11px;color:#64748b">${noLeidas} sin leer</div>
                </div>
            </div>
            ${noLeidas > 0 ? `<span style="background:#3b82f6;color:#fff;font-size:11px;font-weight:700;padding:4px 12px;border-radius:20px">${noLeidas} nuevas</span>` : '<span style="background:#f0fdf4;color:#16a34a;font-size:11px;font-weight:700;padding:4px 12px;border-radius:20px"><i class="fas fa-check" style="margin-right:4px"></i>Todo leído</span>'}
        </div>

        <!-- Lista de notificaciones -->
        <div style="display:flex;flex-direction:column;gap:8px">`;

    notificaciones.forEach(function(n) {
        const cfg   = tipoConfig[n.tipo] || { fa:'fa-info-circle', color:'#64748b', bg:'#f8fafc', border:'#e2e8f0', label: n.tipo };
        const leida = n.leida == 1;

        const fecha = n.fechaCreacion
            ? new Date(n.fechaCreacion).toLocaleDateString('es-PE', { day:'2-digit', month:'short', year:'numeric' })
            : '';
        const hora = n.fechaCreacion
            ? new Date(n.fechaCreacion).toLocaleTimeString('es-PE', { hour:'2-digit', minute:'2-digit' })
            : '';

        let detalle = n.asunto || 'Sin asunto';
        let subdetalle = '';
        if (n.tipo === 'EVALUACION' && n.mensaje) {
            const emailMatch   = n.mensaje.match(/Email:\s*([\w.@+-]+)/);
            const clienteMatch = n.mensaje.match(/Cliente:\s*([^|]+)/);
            if (clienteMatch) subdetalle = clienteMatch[1].trim();
            if (emailMatch)   subdetalle += (subdetalle ? ' · ' : '') + emailMatch[1].trim();
        }

        let accionHtml = '';
        if (n.tipo === 'EVALUACION' && !leida) {
            accionHtml = `<button id="btn-eval-${n.idNotificacion}" onclick="enviarEvaluacion(${n.idNotificacion}, this)"
                style="padding:7px 14px;border:1.5px solid #16a34a;border-radius:8px;background:#fff;color:#16a34a;
                       font-weight:700;font-size:12px;cursor:pointer;white-space:nowrap;transition:all .2s;
                       display:flex;align-items:center;gap:6px"
                onmouseover="this.style.background='#16a34a';this.style.color='#fff'"
                onmouseout="this.style.background='#fff';this.style.color='#16a34a'">
                <i class="fas fa-paper-plane"></i> Enviar email
            </button>`;
        } else if (n.tipo === 'EVALUACION' && leida) {
            accionHtml = `<span style="display:flex;align-items:center;gap:5px;font-size:12px;color:#16a34a;font-weight:600;padding:7px 14px;background:#f0fdf4;border-radius:8px">
                <i class="fas fa-check-circle"></i> Enviado
            </span>`;
        }

        html += `
            <div style="
                display:flex;align-items:center;gap:14px;
                padding:14px 18px;
                background:${leida ? '#f9fafb' : '#fff'};
                border-radius:12px;
                border:1px solid ${leida ? '#e2e8f0' : cfg.border};
                border-left:4px solid ${leida ? '#cbd5e1' : cfg.color};
                box-shadow:${leida ? 'none' : '0 2px 8px rgba(0,0,0,0.06)'};
                opacity:${leida ? '0.7' : '1'};
                transition:box-shadow .2s;
            " onmouseover="this.style.boxShadow='0 4px 16px rgba(0,0,0,0.10)'"
               onmouseout="this.style.boxShadow='${leida ? 'none' : '0 2px 8px rgba(0,0,0,0.06)'}'"
            >
                <!-- Icono circular -->
                <div style="width:44px;height:44px;border-radius:12px;background:${cfg.bg};
                            display:flex;align-items:center;justify-content:center;flex-shrink:0;
                            border:1.5px solid ${cfg.border}">
                    <i class="fas ${cfg.fa}" style="color:${cfg.color};font-size:17px"></i>
                </div>

                <!-- Contenido -->
                <div style="flex:1;min-width:0">
                    <div style="display:flex;align-items:center;gap:8px;margin-bottom:3px">
                        <span style="font-size:10px;font-weight:800;padding:2px 9px;border-radius:20px;
                                     background:${cfg.bg};color:${cfg.color};border:1px solid ${cfg.border};
                                     text-transform:uppercase;letter-spacing:.5px">${cfg.label}</span>
                        ${!leida ? '<span style="width:7px;height:7px;border-radius:50%;background:#3b82f6;display:inline-block"></span>' : ''}
                    </div>
                    <div style="font-size:13px;font-weight:${leida ? '500' : '700'};color:${leida ? '#64748b' : '#1e293b'};
                                white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:480px">
                        ${detalle}
                    </div>
                    ${subdetalle ? `<div style="font-size:11px;color:#94a3b8;margin-top:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${subdetalle}</div>` : ''}
                </div>

                <!-- Fecha -->
                <div style="text-align:right;flex-shrink:0;margin:0 12px">
                    <div style="font-size:12px;font-weight:600;color:#374151">${fecha}</div>
                    <div style="font-size:11px;color:#94a3b8">${hora}</div>
                </div>

                <!-- Acción -->
                ${accionHtml ? `<div style="flex-shrink:0">${accionHtml}</div>` : ''}
            </div>`;
    });

    html += '</div></div>';
    container.innerHTML = html;
}

function filtrarNotificaciones() {
    const filtro = document.getElementById('filtroNotificaciones').value;
    let filtrados = _allNotificaciones;
    if (filtro) {
        filtrados = _allNotificaciones.filter(n => n.tipo === filtro);
    }
    renderNotificaciones(filtrados);
}

async function enviarEvaluacion(idNotificacion, btn) {
    // Deshabilitar botón inmediatamente para evitar doble envío
    if (btn) { btn.disabled = true; btn.textContent = '⏳ Enviando...'; }
    try {
        const res = await fetch(`${API_BASE_URL}/evaluaciones/enviar/${idNotificacion}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const data = await res.json();
        if (data.success) {
            // Marcar como leída en BD
            await fetch(`${API_BASE_URL}/notificaciones/${idNotificacion}/leida`, { method: 'PUT' });
            // Actualizar dato local (sin recargar del servidor) para que el re-render muestre "Ya enviado"
            const notif = _allNotificaciones.find(n => n.idNotificacion === idNotificacion);
            if (notif) notif.leida = 1;
            // Re-renderizar usando datos locales ya actualizados
            filtrarNotificaciones();
        } else {
            alert('❌ Error: ' + (data.error || 'No se pudo enviar el email'));
            if (btn) { btn.disabled = false; btn.textContent = '📧 Enviar Evaluación'; }
        }
    } catch (e) {
        alert('❌ Error de conexión: ' + e.message);
        if (btn) { btn.disabled = false; btn.textContent = '📧 Enviar Evaluación'; }
    }
}

// ============================================================
// FUNCIONES AUXILIARES
// ============================================================
function mostrarModal(titulo, contenido) {
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.style.display = 'block';
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title">${titulo}</h5>
                    <button type="button" class="btn-close btn-close-white" onclick="cerrarFormulario()"></button>
                </div>
                <div class="modal-body">
                    ${contenido}
                </div>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
    modal.classList.add('show');
}

function cerrarFormulario() {
    const modales = document.querySelectorAll('.modal');
    modales.forEach(m => m.remove());
}

// Cargar datos al cambiar de sección
const _originalShowSection = window.showSection;
window.showSection = function(section) {
    _originalShowSection(section);
    if (section === 'pagos') cargarPagos();
    else if (section === 'descuentos') cargarDescuentos();
    else if (section === 'evaluaciones') cargarEvaluaciones();
};
