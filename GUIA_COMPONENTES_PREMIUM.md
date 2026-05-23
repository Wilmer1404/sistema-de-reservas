# 🎨 GUÍA VISUAL - Componentes Premium SpaceWork

## 1️⃣ COLORES DEL SISTEMA

```
Primario:      #6366f1 (Indigo) - Acciones principales
Secundario:    #8b5cf6 (Púrpura) - Elementos destacados
Accent:        #06b6d4 (Cyan) - Información
Success:       #10b981 (Verde) - Éxito
Warning:       #f59e0b (Amarillo) - Advertencias
Danger:        #ef4444 (Rojo) - Errores
Info:          #0ea5e9 (Azul cielo) - Información
```

## 2️⃣ BOTONES

### Primarios
```html
<button class="btn-primary">Guardar Cambios</button>
<!-- Gradiente indigo → indigo-dark -->
<!-- Sombra elevada en hover -->
<!-- Transform translateY(-2px) -->
```

### Secundarios
```html
<button class="btn-sec">Cancelar</button>
<!-- Fondo gris claro -->
<!-- Borde sutil -->
<!-- Cambia a gris medio en hover -->
```

### De Éxito
```html
<button class="btn-success">✅ Confirmar Pago</button>
<!-- Gradiente verde -->
<!-- Icono emoji integrado -->
```

### De Tabla (Acciones)
```html
<div class="table-actions">
    <button class="btn-action edit" title="Editar">✏️</button>
    <button class="btn-action delete" title="Eliminar">🗑️</button>
    <button class="btn-action view" title="Ver">👁️</button>
</div>
```

## 3️⃣ TARJETAS DE ESTADÍSTICAS

```html
<div class="stat-card blue">
    <div class="stat-icon">📋</div>
    <div>
        <p>Total de Reservas</p>
        <h3>1,250</h3>
    </div>
</div>
```

**Estados disponibles:**
- `stat-card blue` - Información
- `stat-card green` - Éxito
- `stat-card yellow` - Advertencia
- `stat-card purple` - Especial
- `stat-card red` - Crítico

## 4️⃣ NOTIFICACIONES

```javascript
// Éxito
showToast('Pago registrado correctamente', 'ok');

// Error
showToast('Error al procesar el pago', 'err');

// Advertencia
showToast('Por favor, selecciona un método de pago', 'warn');

// Información
showToast('Tu evaluación fue registrada', 'info');
```

## 5️⃣ BADGES DE ESTADO

```html
<!-- Estados de Reserva -->
<span class="badge-estado est-ACTIVO">ACTIVO</span>
<span class="badge-estado est-PENDIENTE">PENDIENTE</span>
<span class="badge-estado est-CONFIRMADA">CONFIRMADA</span>
<span class="badge-estado est-COMPLETADA">COMPLETADA</span>
<span class="badge-estado est-CANCELADA">CANCELADA</span>
<span class="badge-estado est-INACTIVO">INACTIVO</span>
```

## 6️⃣ FORMULARIOS

```html
<div class="field">
    <label>Correo Electrónico</label>
    <input type="email" placeholder="tu@email.com">
</div>

<div class="f2">
    <div class="field">
        <label>Nombre</label>
        <input type="text" placeholder="Tu nombre">
    </div>
    <div class="field">
        <label>Apellido</label>
        <input type="text" placeholder="Tu apellido">
    </div>
</div>
```

**Características:**
- Etiquetas UPPERCASE
- Placeholder descriptivo
- Fondo claro (#f8fafc)
- Focus con borde primario + sombra
- Transición suave

## 7️⃣ TABLAS

```html
<table class="tbl">
    <thead>
        <tr>
            <th>#</th>
            <th>Cliente</th>
            <th>Espacio</th>
            <th>Monto</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>#123</td>
            <td>Juan Pérez</td>
            <td>Sala A</td>
            <td>S/. 500.00</td>
            <td><span class="badge-estado est-COMPLETADA">COMPLETADA</span></td>
            <td>
                <div class="table-actions">
                    <button class="btn-action view">👁️</button>
                    <button class="btn-action edit">✏️</button>
                    <button class="btn-action delete">🗑️</button>
                </div>
            </td>
        </tr>
    </tbody>
</table>
```

## 8️⃣ MODALES

```html
<div class="modal active">
    <div class="modal-content">
        <div class="modal-hd">
            <h3>Procesar Pago</h3>
            <button>✕</button>
        </div>
        <div class="modal-bd">
            <!-- Contenido del modal -->
        </div>
        <div class="modal-ft">
            <button class="btn-sec">Cancelar</button>
            <button class="btn-primary">Confirmar</button>
        </div>
    </div>
</div>
```

## 9️⃣ SIDEBAR

```html
<aside class="sidebar">
    <div class="sidebar-top">
        <div class="sidebar-brand">
            <span class="logo-icon sm">SW</span>
            <span>SpaceWork</span>
        </div>
    </div>
    <nav class="sidebar-menu">
        <span class="menu-label">PRINCIPAL</span>
        <a class="menu-item active" onclick="showSection('reservas')">
            <span class="mi">📋</span> Reservas
            <span class="badge">5</span>
        </a>
        <a class="menu-item" onclick="showSection('pagos')">
            <span class="mi">💳</span> Pagos
        </a>
    </nav>
    <div class="sidebar-bottom">
        <div class="user-pill">
            <div class="avatar">A</div>
            <div>
                <div class="uname">Admin</div>
                <div class="urole">Administrador</div>
            </div>
        </div>
        <button class="btn-exit" onclick="logout()">⏻</button>
    </div>
</aside>
```

## 🔟 HEADER

```html
<header class="app-header">
    <div>
        <h1 class="header-title">Reservas</h1>
        <p class="header-sub">Gestión completa de reservas</p>
    </div>
    <button class="btn-primary" onclick="openAddModal()">+ Nueva</button>
</header>
```

## 1️⃣1️⃣ ANIMACIONES

- **Fade-in**: Secciones al cambiar (0.3s)
- **Slide-up**: Modales al abrir (0.3s)
- **Slide-in**: Notificaciones desde derecha (0.3s)
- **Float**: Círculos de login (6s-10s)
- **Transform**: Botones en hover (translateY -2px)

## 1️⃣2️⃣ RESPONSIVE BREAKPOINTS

```css
/* Desktop (1920px+) */
/* Default styles */

/* Laptop (1366px - 1920px) */
/* Grid adjustments */

/* Tablet (768px - 1024px) */
@media (max-width: 768px) {
    /* Sidebar adaptativo */
    /* Una columna */
}

/* Mobile (320px - 767px) */
@media (max-width: 480px) {
    /* Modal optimizado */
    /* Botones más grandes */
}
```

## 1️⃣3️⃣ VARIABLE CSS PERSONALIZADAS

```css
--primary: #6366f1
--primary-dark: #4f46e5
--primary-light: #818cf8
--secondary: #8b5cf6
--accent: #06b6d4
--danger: #ef4444
--warning: #f59e0b
--success: #10b981
--info: #0ea5e9

--shadow-sm: 0 1px 2px
--shadow-md: 0 4px 6px
--shadow-lg: 0 10px 15px
--shadow-xl: 0 20px 25px
--shadow-2xl: 0 25px 50px

--radius-sm: 8px
--radius-md: 12px
--radius-lg: 16px
--radius-xl: 20px
```

## 🎯 PATRÓN DE USO

1. **Primaria**: Usar `btn-primary` para acciones principales
2. **Secundaria**: Usar `btn-sec` para cancelar/no hacer nada
3. **Éxito**: Usar `btn-success` para completar acciones
4. **Tabla**: Usar `btn-action` con clase (edit, delete, view)
5. **Notificaciones**: `showToast()` con tipo (ok, err, warn, info)
6. **Badges**: `badge-estado est-[ESTADO]` para indicar estado

---

**Versión**: 2.0 Premium  
**Última Actualización**: Mayo 19, 2026  
**Estado**: Production Ready ✅
