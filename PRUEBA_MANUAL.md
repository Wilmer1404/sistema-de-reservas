# 🧪 PRUEBA MANUAL DEL SISTEMA - BOTÓN VER

## ✅ ESTADO ACTUAL DEL SISTEMA
- ✓ Servidor: Corriendo en http://localhost:8080
- ✓ JAR Compilado: CONTIENE "sw-toast" (verificado)
- ✓ Cache-bust: v7 (app.js?v=7, style.css?v=7)
- ✓ Toasts: Renombrados a `.sw-toast` para evitar conflicto con Bootstrap

## 📋 PASOS PARA PROBAR

### PASO 1: ABRIR LA APP EN NAVEGADOR LIMPIO
```
1. Abre: http://localhost:8080
2. Si tienes cookies previas, usa Ctrl+Shift+Del para limpiar localStorage:
   - Abre DevTools (F12)
   - Application -> Local Storage -> Delete "swToken" y "swRole"
3. Recarga: Ctrl+F5 (fuerza recarga completa)
```

### PASO 2: LOGIN
```
Credenciales:
- Usuario: admin
- Contraseña: admin

Espera ver:
1. Página de login desaparece
2. Dashboard aparece
3. Sección "Reservas" carga automáticamente
```

### PASO 3: BUSCAR UNA RESERVA
```
En la sección "Reservas" debe haber una tabla con reservas.
Columnas esperadas:
- ID Reserva
- Cliente
- Espacio
- Fecha/Hora
- Estado
- Botones: [VER] [EDITAR] [ELIMINAR]

Si NO hay reservas:
→ La BD está vacía o sin inicializar
→ PERO el botón VER DEBE FUNCIONAR de todos modos
```

### PASO 4: CLIC EN BOTÓN "VER" 
```
1. Busca cualquier fila en la tabla de Reservas
2. Haz CLIC en el botón verde "VER"
3. MIRA LA ESQUINA SUPERIOR DERECHA (donde aparecen los toasts)

DEBES VER TOASTS EN SECUENCIA:
┌─────────────────────────────────────────┐
│ ℹ️ VER click id=XXX cache=N/0           │
│                                         │
│ ℹ️ Reserva encontrada #XXX, abriendo...  │
│                                         │
│ ✓ Modal abierto - display:flex aplicado │
└─────────────────────────────────────────┘

Luego:
- Un MODAL debe aparecer en el centro con los detalles de la reserva
- El modal debe mostrar: ID, Cliente, Espacio, Fechas, Monto, Estado
```

### PASO 5: SI NO VES TOASTS
```
PROBLEMA 1: Los toasts están ocultos por Bootstrap
→ Abre DevTools (F12)
→ Inspecciona el elemento en esquina superior derecha
→ Ve que dice "No elements match the selector"

SOLUCIÓN: El CSS de `.sw-toast` no está siendo aplicado

PROBLEMA 2: El onclick no disparó
→ En DevTools, abre Console
→ Debería haber mensajes de log
→ Si ves "VER click id=..." → el onclick SÍ se disparó

PROBLEMA 3: El modal no abre
→ Mira en DevTools si ves: "Modal abierto - display:flex"
→ Si ves ese mensaje pero sin modal visual → Bootstrap lo oculta
```

## 🔍 DEBUGGING - CONSOLA DEVTOOLS

Si algo falla, abre DevTools (F12) y copia-pega en la Console:

```javascript
// Ver si los toasts se crean
showToast('TEST MANUAL', 'ok')

// Ver el contenido del cache de reservas
console.log('Admin reservas:', _adminReservas)
console.log('Mis reservas:', _misReservas)

// Ver si el modal está en el DOM
console.log('Modal:', document.getElementById('modalDetalle'))
```

## 📊 RESULTADO ESPERADO

**ÉXITO TOTAL:**
- ✓ Toasts azul/verde aparecen en esquina superior derecha
- ✓ Modal de detalle se abre en el centro con fondo oscuro
- ✓ Puedo ver todos los datos de la reserva
- ✓ Botón Cerrar funciona

**ÉXITO PARCIAL (falla visible):**
- ✓ Toasts aparecen
- ✗ Modal no se abre
→ Significa que `verDetalle()` no abrió el modal

**FALLO TOTAL:**
- ✗ Nada aparece
→ El onclick no disparó o fue bloqueado por JavaScript error

---

**REPORTA EXACTAMENTE QUÉ VES** (toasts, modal, errores en consola)
