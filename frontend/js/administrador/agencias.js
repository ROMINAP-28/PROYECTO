/**
 * Travelink - Gestión de Agencias
 * 3 Pestañas: Solicitudes (Aceptar/Rechazar), Activas y Suspendidas
 * Filtros compactos, Paginación dinámica (máx 5 registros)
 */

let subTabAgencias = 'solicitudes';
let agenciasList = [];
let agenciasFiltradas = [];
let paginaActual = 1;
const ITEMS_POR_PAGINA = 5;

document.addEventListener('DOMContentLoaded', () => {
    cargarAgencias();
});

async function cargarAgencias() {
    try {
        const res = await fetch(`${API_BASE}/agencias`);
        if (res.ok) {
            const data = await res.json();
            agenciasList = Array.isArray(data) ? data : (data.data || data.agencias || []);
        } else {
            throw new Error('Error al conectar a API');
        }
    } catch (e) {
        console.warn('Cargando fallback para agencias...', e);
        agenciasList = [
            // Solicitudes pendientes
            { id: 101, nombre: 'Andes Expeditions EIRL', ruc: '20789123456', representante: 'Marco Aurelio', email: 'marco@andesexpeditions.pe', telefono: '+51 984 555 123', ciudad: 'Cusco', comision: 10, estado: 'Solicitud', fechaSolicitud: '2026-09-24' },
            { id: 102, nombre: 'Amazon Eco Lodge & Tours', ruc: '20654987321', representante: 'Luciana Silva', email: 'info@amazonecotours.pe', telefono: '+51 965 221 443', ciudad: 'Iquitos', comision: 12, estado: 'Solicitud', fechaSolicitud: '2026-09-25' },
            { id: 103, nombre: 'Colca Canyon Trekkers', ruc: '20556677889', representante: 'Gustavo Flores', email: 'ventas@colcatrekkers.pe', telefono: '+51 954 112 334', ciudad: 'Arequipa', comision: 10, estado: 'Solicitud', fechaSolicitud: '2026-09-26' },
            // Activas
            { id: 1, nombre: 'Andes Tours S.A.C.', ruc: '20456789012', representante: 'Carlos Mendoza', email: 'contacto@andestours.pe', telefono: '+51 984 123 456', ciudad: 'Cusco', comision: 10, estado: 'Activo' },
            { id: 2, nombre: 'Inka Travel Peru EIRL', ruc: '20123456789', representante: 'Jorge Ramírez', email: 'reservas@inkatravel.pe', telefono: '+51 984 765 432', ciudad: 'Cusco', comision: 12, estado: 'Activo' },
            { id: 3, nombre: 'Selva Viva Expeditions', ruc: '20567890123', representante: 'Elena Castillo', email: 'info@selvaviva.com', telefono: '+51 965 332 110', ciudad: 'Iquitos', comision: 10, estado: 'Activo' },
            { id: 4, nombre: 'Ica Travel Sand & Sun', ruc: '20198765432', representante: 'Fernando Morales', email: 'contacto@icatravel.pe', telefono: '+51 956 443 221', ciudad: 'Ica', comision: 10, estado: 'Activo' },
            { id: 5, nombre: 'Chanchamayo Expeditions', ruc: '20445566778', representante: 'Rosa Paucar', email: 'contacto@chanchamayoexp.pe', telefono: '+51 964 121 343', ciudad: 'Junín', comision: 10, estado: 'Activo' },
            { id: 6, nombre: 'Puno Lake Adventures', ruc: '20334455667', representante: 'Raúl Mamani', email: 'info@punolake.pe', telefono: '+51 951 889 001', ciudad: 'Puno', comision: 10, estado: 'Activo' },
            // Suspendidas
            { id: 7, nombre: 'Machupicchu Wonder Tours', ruc: '20678901235', representante: 'Manuel Quispe', email: 'reservas@mpwondertours.com', telefono: '+51 984 990 112', ciudad: 'Cusco', comision: 15, estado: 'Suspendido', motivo: 'Múltiples cancelaciones sin aviso' },
            { id: 8, nombre: 'Arequipa Tours Express', ruc: '20678901299', representante: 'Valeria Díaz', email: 'ventas@arequipaexp.pe', telefono: '+51 954 887 665', ciudad: 'Arequipa', comision: 10, estado: 'Suspendido', motivo: 'Incumplimiento de póliza de seguro' }
        ];
    }
    aplicarFiltros();
}

function cambiarTabAgencias(tab, btn) {
    subTabAgencias = tab;
    document.querySelectorAll('.sub-tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    aplicarFiltros();
}

function aplicarFiltros() {
    const busqueda = (document.getElementById('filtroBusqueda')?.value || '').toLowerCase().trim();
    const ciudad = document.getElementById('filtroCiudad')?.value || 'todos';

    agenciasFiltradas = agenciasList.filter(a => {
        const estadoNorm = (a.estado || '').toLowerCase();
        let coincideTab = false;

        if (subTabAgencias === 'solicitudes') {
            coincideTab = estadoNorm === 'solicitud' || estadoNorm === 'pendiente';
        } else if (subTabAgencias === 'activas') {
            coincideTab = estadoNorm === 'activo' || estadoNorm === 'activa';
        } else if (subTabAgencias === 'suspendidas') {
            coincideTab = estadoNorm === 'suspendido' || estadoNorm === 'suspendida' || estadoNorm === 'inactivo';
        }

        const coincideBusqueda = !busqueda ||
            (a.nombre && a.nombre.toLowerCase().includes(busqueda)) ||
            (a.ruc && a.ruc.toLowerCase().includes(busqueda)) ||
            (a.representante && a.representante.toLowerCase().includes(busqueda)) ||
            (a.email && a.email.toLowerCase().includes(busqueda));

        const coincideCiudad = (ciudad === 'todos') || 
            (a.ciudad && a.ciudad.toLowerCase() === ciudad.toLowerCase());

        return coincideTab && coincideBusqueda && coincideCiudad;
    });

    paginaActual = 1;
    renderizarTabla();
}

function limpiarFiltros() {
    if (document.getElementById('filtroBusqueda')) document.getElementById('filtroBusqueda').value = '';
    if (document.getElementById('filtroCiudad')) document.getElementById('filtroCiudad').value = 'todos';
    aplicarFiltros();
}

function cambiarPaginaAgencias(p) {
    const totalPages = Math.ceil(agenciasFiltradas.length / ITEMS_POR_PAGINA) || 1;
    if (p < 1 || p > totalPages) return;
    paginaActual = p;
    renderizarTabla();
}

function renderizarTabla() {
    const tbody = document.getElementById('tablaAgenciasBody');
    if (!tbody) return;

    if (agenciasFiltradas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 24px; color:#94a3b8;">No se encontraron registros en la pestaña de ${subTabAgencias}.</td></tr>`;
        renderPaginator(0, 1, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaAgencias);
        return;
    }

    const inicio = (paginaActual - 1) * ITEMS_POR_PAGINA;
    const fin = inicio + ITEMS_POR_PAGINA;
    const items = agenciasFiltradas.slice(inicio, fin);

    tbody.innerHTML = items.map(a => {
        return `
            <tr>
                <td><span class="table-id-code">#AG-${String(a.id).padStart(3, '0')}</span></td>
                <td>
                    <div class="agency-cell-meta">
                        <div class="agency-logo-circle">
                            <i class="ti ti-building-store"></i>
                        </div>
                        <div class="agency-text-wrap">
                            <h6>${a.nombre}</h6>
                            <span>RUC: ${a.ruc || 'N/A'}</span>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="contact-info-block">
                        <span><strong>${a.representante || 'N/A'}</strong></span>
                        <small>${a.email || ''} • ${a.telefono || ''}</small>
                    </div>
                </td>
                <td><i class="ti ti-map-pin" style="color:#ef4444; font-size:13px;"></i> ${a.ciudad || 'Perú'}</td>
                <td><strong>${a.comision || 10}%</strong></td>
                <td>
                    <span class="badge-status ${subTabAgencias === 'activas' ? 'activo' : (subTabAgencias === 'solicitudes' ? 'pendiente' : 'suspendido')}">
                        ${subTabAgencias === 'solicitudes' ? 'En espera' : (subTabAgencias === 'activas' ? 'Activa' : 'Suspendida')}
                    </span>
                </td>
                <td>
                    <div class="table-actions">
                        <button class="btn-table-pill outline-blue" title="Ver detalles" onclick="verDetalleAgencia(${a.id})">
                            <i class="ti ti-eye"></i> Ver
                        </button>
                        ${subTabAgencias === 'solicitudes' ? `
                            <button class="btn-table-pill outline-green" title="Aceptar Solicitud" onclick="procesarSolicitud(${a.id}, 'Aceptar')">
                                <i class="ti ti-check"></i> Aceptar
                            </button>
                            <button class="btn-table-pill outline-red" title="Rechazar Solicitud" onclick="procesarSolicitud(${a.id}, 'Rechazar')">
                                <i class="ti ti-x"></i> Rechazar
                            </button>
                        ` : ''}
                        ${subTabAgencias === 'activas' ? `
                            <button class="btn-table-pill outline-red" title="Suspender Agencia" onclick="suspenderAgencia(${a.id})">
                                <i class="ti ti-ban"></i> Suspender
                            </button>
                        ` : ''}
                        ${subTabAgencias === 'suspendidas' ? `
                            <button class="btn-table-pill outline-green" title="Reactivar Agencia" onclick="reactivarAgencia(${a.id})">
                                <i class="ti ti-refresh"></i> Reactivar
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPaginator(agenciasFiltradas.length, paginaActual, ITEMS_POR_PAGINA, 'paginacionControles', 'paginacionInfo', cambiarPaginaAgencias);
}

function verDetalleAgencia(id) {
    const a = agenciasList.find(item => item.id === id);
    if (!a) return;

    Swal.fire({
        title: a.nombre,
        html: `
            <div style="text-align:left; font-size:13.5px; color:#334155; line-height: 1.8;">
                <p><strong>RUC:</strong> ${a.ruc}</p>
                <p><strong>Representante:</strong> ${a.representante}</p>
                <p><strong>Correo electrónico:</strong> ${a.email}</p>
                <p><strong>Teléfono:</strong> ${a.telefono}</p>
                <p><strong>Ciudad / Sede:</strong> ${a.ciudad}</p>
                <p><strong>Comisión Travelink:</strong> ${a.comision}%</p>
                <p><strong>Estado:</strong> ${a.estado}</p>
                ${a.motivo ? `<p style="color:#ef4444;"><strong>Motivo suspensión:</strong> ${a.motivo}</p>` : ''}
            </div>
        `,
        icon: 'info',
        confirmButtonColor: '#1a73e8',
        confirmButtonText: 'Cerrar'
    });
}

function procesarSolicitud(id, decision) {
    const a = agenciasList.find(item => item.id === id);
    if (!a) return;

    const esAceptar = decision === 'Aceptar';
    Swal.fire({
        title: `¿${decision} solicitud?`,
        text: `Se va a ${esAceptar ? 'aprobar y habilitar' : 'rechazar la solicitud de'} ${a.nombre}.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: esAceptar ? '#16a34a' : '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: `Sí, ${decision.toLowerCase()}`,
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            if (esAceptar) {
                a.estado = 'Activo';
            } else {
                agenciasList = agenciasList.filter(item => item.id !== id);
            }
            try {
                await fetch(`${API_BASE}/agencias`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(a)
                });
            } catch (e) {}
            aplicarFiltros();
            Swal.fire(esAceptar ? 'Aprobada' : 'Rechazada', `La solicitud ha sido procesada con éxito.`, 'success');
        }
    });
}

function suspenderAgencia(id) {
    const a = agenciasList.find(item => item.id === id);
    if (!a) return;

    Swal.fire({
        title: '¿Suspender agencia?',
        text: `La agencia "${a.nombre}" dejará de mostrar sus tours en la plataforma.`,
        input: 'text',
        inputPlaceholder: 'Ingresa el motivo de suspensión...',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, suspender',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            a.estado = 'Suspendido';
            a.motivo = result.value || 'Suspensión administrativa';
            try {
                await fetch(`${API_BASE}/agencias`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(a)
                });
            } catch (e) {}
            aplicarFiltros();
            Swal.fire('Suspendida', 'La agencia ha pasado a estado suspendida.', 'success');
        }
    });
}

function reactivarAgencia(id) {
    const a = agenciasList.find(item => item.id === id);
    if (!a) return;

    Swal.fire({
        title: '¿Reactivar agencia?',
        text: `Se reactivará a "${a.nombre}" y sus tours volverán a estar activos.`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#16a34a',
        cancelButtonColor: '#94a3b8',
        confirmButtonText: 'Sí, reactivar',
        cancelButtonText: 'Cancelar'
    }).then(async (result) => {
        if (result.isConfirmed) {
            a.estado = 'Activo';
            delete a.motivo;
            try {
                await fetch(`${API_BASE}/agencias`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(a)
                });
            } catch (e) {}
            aplicarFiltros();
            Swal.fire('Reactivada', 'La agencia está nuevamente activa.', 'success');
        }
    });
}

function exportarAgencias() {
    let csv = 'ID,Nombre,RUC,Representante,Email,Telefono,Ciudad,Comision,Estado\n';
    agenciasFiltradas.forEach(a => {
        csv += `"${a.id}","${a.nombre}","${a.ruc}","${a.representante}","${a.email}","${a.telefono}","${a.ciudad}","${a.comision}%","${a.estado}"\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `agencias_${subTabAgencias}_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
