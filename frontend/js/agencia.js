const datosAgencia = JSON.parse(localStorage.getItem("datosAgencia")) || {};

function guardarDatos(datos) {
    Object.assign(datosAgencia, datos);
    localStorage.setItem("datosAgencia", JSON.stringify(datosAgencia));
}

function siguientePaso(pagina) {
    window.location.href = pagina;
}

function mostrarMensaje(elemento, mensaje, tipo = "error") {
    if (!elemento) return;
    elemento.textContent = mensaje;
    elemento.className = `form-message ${tipo}`;
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".password-toggle").forEach(boton => {
        boton.addEventListener("click", () => {
            const input = document.getElementById(boton.dataset.target);
            if (!input) return;
            input.type = input.type === "password" ? "text" : "password";
        });
    });

    const formRegistro = document.getElementById("formRegistroAgencia");
    if (formRegistro) {
        const datos = datosAgencia;
        document.getElementById("nombreComercial").value = datos.nombreComercial || "";
        document.getElementById("correo").value = datos.correo || "";
        document.getElementById("nombreUsuario").value = datos.nombreUsuario || "";

        formRegistro.addEventListener("submit", event => {
            event.preventDefault();

            const nombreComercial = document.getElementById("nombreComercial").value.trim();
            const correo = document.getElementById("correo").value.trim();
            const nombreUsuario = document.getElementById("nombreUsuario").value.trim();
            const contrasena = document.getElementById("contrasena").value;
            const confirmarContrasena = document.getElementById("confirmarContrasena").value;
            const mensaje = document.getElementById("mensajeRegistro");

            if (!nombreComercial || !correo || !nombreUsuario || !contrasena || !confirmarContrasena) {
                mostrarMensaje(mensaje, "Completa todos los campos.");
                return;
            }

            if (contrasena !== confirmarContrasena) {
                mostrarMensaje(mensaje, "Las contraseñas no coinciden.");
                return;
            }

            if (contrasena.length < 8) {
                mostrarMensaje(mensaje, "La contraseña debe tener al menos 8 caracteres.");
                return;
            }

            guardarDatos({
                nombreComercial,
                correo,
                nombreUsuario,
                contrasena
            });

            siguientePaso("R_agencia_empresa.html");
        });
    }

    const formEmpresa = document.getElementById("formAgenciaEmpresa");
    if (formEmpresa) {
        const datos = datosAgencia;
        document.getElementById("razonSocial").value = datos.razonSocial || "";
        document.getElementById("ruc").value = datos.ruc || "";
        document.getElementById("telefonoEmpresa").value = datos.telefonoEmpresa || "";
        document.getElementById("direccion").value = datos.direccion || "";
        document.getElementById("descripcion").value = datos.descripcion || "";

        formEmpresa.addEventListener("submit", event => {
            event.preventDefault();

            const razonSocial = document.getElementById("razonSocial").value.trim();
            const ruc = document.getElementById("ruc").value.trim();
            const telefonoEmpresa = document.getElementById("telefonoEmpresa").value.trim();
            const direccion = document.getElementById("direccion").value.trim();
            const descripcion = document.getElementById("descripcion").value.trim();
            const mensaje = document.getElementById("mensajeEmpresa");

            if (!razonSocial || !ruc) {
                mostrarMensaje(mensaje, "Completa la razón social y el RUC.");
                return;
            }

            guardarDatos({
                razonSocial,
                ruc,
                telefonoEmpresa,
                direccion,
                descripcion
            });

            siguientePaso("R_agencia_responsable.html");
        });
    }

    const formResponsable =
        document.getElementById("formAgenciaResponsable") ||
        document.getElementById("formResponsable");
    document.getElementById("formAgenciaResponsable") ||
    document.getElementById("formResponsable");

    if (formResponsable) {
        const campoNombre = document.getElementById("nombre");
        const campoDocumento = document.getElementById("nroDocumento");
        const campoApellidoPaterno = document.getElementById("apellidoPaterno");
        const campoApellidoMaterno = document.getElementById("apellidoMaterno");
        const campoTelefono = document.getElementById("telefonoResponsable");
        const mensaje = document.getElementById("mensajeResponsable");

        campoNombre.value = datosAgencia.nombre || "";
        campoDocumento.value = datosAgencia.nroDocumento || "";
        campoApellidoPaterno.value = datosAgencia.apellidoPaterno || "";
        campoApellidoMaterno.value = datosAgencia.apellidoMaterno || "";
        campoTelefono.value = datosAgencia.telefonoResponsable || "";

        formResponsable.addEventListener("submit", event => {
            event.preventDefault();

            const nombre = campoNombre.value.trim();
            const nroDocumento = campoDocumento.value.trim();
            const apellidoPaterno = campoApellidoPaterno.value.trim();
            const apellidoMaterno = campoApellidoMaterno.value.trim();
            const telefonoResponsable = campoTelefono.value.trim();

            if (!nombre || !nroDocumento || !apellidoPaterno || !apellidoMaterno) {
                mostrarMensaje(mensaje, "Completa los campos obligatorios.");
                return;
            }

            guardarDatos({
                nombre,
                nroDocumento,
                apellidoPaterno,
                apellidoMaterno,
                telefonoResponsable
            });

            siguientePaso("R_agencia_turismo.html");
        });
    }

    const formTurismo = document.getElementById("formAgenciaTurismo");
    if (formTurismo) {
        const datos = datosAgencia;
        document.getElementById("descripcionTuristica").value = datos.descripcionTuristica || "";
        document.getElementById("observacionesTuristicas").value = datos.observacionesTuristicas || "";

        formTurismo.addEventListener("submit", event => {
            event.preventDefault();

            guardarDatos({
                descripcionTuristica: document.getElementById("descripcionTuristica").value.trim(),
                observacionesTuristicas: document.getElementById("observacionesTuristicas").value.trim()
            });

            siguientePaso("R_agencia_documentos.html");
        });
    }

    const formDocumentos = document.getElementById("formAgenciaDocumentos");
    if (formDocumentos) {
        formDocumentos.addEventListener("submit", event => {
            event.preventDefault();

            const archivos = {
                documentoRuc: document.getElementById("documentoRuc").files[0]?.name || "",
                documentoConstitucion: document.getElementById("documentoConstitucion").files[0]?.name || "",
                documentoAdicional: document.getElementById("documentoAdicional").files[0]?.name || ""
            };

            guardarDatos({ archivos });
            siguientePaso("R_agencia_revision.html");
        });
    }

    const revisionNombreComercial = document.getElementById("revisionNombreComercial");
    if (revisionNombreComercial) {
        document.getElementById("revisionNombreComercial").textContent = datosAgencia.nombreComercial || "-";
        document.getElementById("revisionCorreo").textContent = datosAgencia.correo || "-";
        document.getElementById("revisionUsuario").textContent = datosAgencia.nombreUsuario || "-";
        document.getElementById("revisionRazonSocial").textContent = datosAgencia.razonSocial || "-";
        document.getElementById("revisionRuc").textContent = datosAgencia.ruc || "-";
        document.getElementById("revisionTelefonoEmpresa").textContent = datosAgencia.telefonoEmpresa || "-";
        document.getElementById("revisionDireccion").textContent = datosAgencia.direccion || "-";
        document.getElementById("revisionNombre").textContent = datosAgencia.nombre || "-";
        document.getElementById("revisionApellidoPaterno").textContent = datosAgencia.apellidoPaterno || "-";
        document.getElementById("revisionApellidoMaterno").textContent = datosAgencia.apellidoMaterno || "-";
        document.getElementById("revisionTelefonoResponsable").textContent = datosAgencia.telefonoResponsable || "-";

        const archivos = datosAgencia.archivos || {};
        const documentos = [
            archivos.documentoRuc,
            archivos.documentoConstitucion,
            archivos.documentoAdicional
        ].filter(Boolean);

        document.getElementById("revisionDocumentos").textContent =
            documentos.length ? documentos.join(", ") : "No se han seleccionado documentos.";

        const btnEnviar = document.getElementById("btnEnviarSolicitud");

        if (btnEnviar) {
            btnEnviar.addEventListener("click", async () => {
                const mensaje = document.getElementById("mensajeRevision");

                try {
                    mostrarMensaje(mensaje, "Enviando solicitud...", "success");

                    const respuesta = await fetch("http://localhost:8080/api/agencia/registro", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            nombre: datosAgencia.nombre,
                            apellidoPaterno: datosAgencia.apellidoPaterno,
                            apellidoMaterno: datosAgencia.apellidoMaterno,
                            nroDocumento: datosAgencia.nroDocumento,
                            nombreUsuario: datosAgencia.nombreUsuario,
                            correo: datosAgencia.correo,
                            contrasena: datosAgencia.contrasena,

                            telefonoResponsable: datosAgencia.telefonoResponsable || "",
                            telefonoEmpresa: datosAgencia.telefonoEmpresa || "",

                            razonSocial: datosAgencia.razonSocial,
                            nombreComercial: datosAgencia.nombreComercial,
                            ruc: datosAgencia.ruc,
                            direccion: datosAgencia.direccion,
                            descripcion: datosAgencia.descripcion
                        })
                    });

                    const resultado = await respuesta.json();

                    if (resultado.status === "success") {
                        mostrarMensaje(
                            mensaje,
                            "Solicitud enviada correctamente. Quedará pendiente de validación administrativa.",
                            "success"
                        );

                        localStorage.removeItem("datosAgencia");

                        setTimeout(() => {
                            window.location.href = "R_agencia_login.html";
                        }, 2500);

                    } else {
                        mostrarMensaje(
                            mensaje,
                            resultado.message || "No se pudo registrar la solicitud."
                        );
                    }

                } catch (error) {
                    console.error("ERROR REAL:", error);
                    mostrarMensaje(
                        mensaje,
                        "Error: " + error.message
                    );
                }
            });
        }
    }
});

const formLoginAgencia = document.getElementById("formLoginAgencia");

if (formLoginAgencia) {
    formLoginAgencia.addEventListener("submit", async event => {
        event.preventDefault();

        const usuario = document.getElementById("loginUsuario").value.trim();
        const contrasena = document.getElementById("loginPassword").value;
        const mensaje = document.getElementById("mensajeLogin");

        if (!usuario || !contrasena) {
            mostrarMensaje(mensaje, "Completa el usuario y la contraseña.");
            return;
        }

        try {
            mostrarMensaje(mensaje, "Verificando datos...", "success");

            const respuesta = await fetch("http://localhost:8080/api/agencia/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    usuario,
                    contrasena
                })
            });

            const resultado = await respuesta.json();

            if (resultado.status === "success") {

                localStorage.setItem(
                    "agenciaSesion",
                    JSON.stringify(resultado.agencia)
                );

                mostrarMensaje(
                    mensaje,
                    "Inicio de sesión correcto.",
                    "success"
                );

                setTimeout(() => {
                    window.location.href = "inicio.html";
                }, 800);

            } else {
                mostrarMensaje(
                    mensaje,
                    resultado.message || "Usuario o contraseña incorrectos."
                );
            }

        } catch (error) {
            console.error("ERROR LOGIN AGENCIA:", error);
            mostrarMensaje(
                mensaje,
                "Error: " + error.message
            );
        }
    });
}

const modalServicio = document.getElementById("modalServicio");
const btnNuevoServicio = document.getElementById("btnNuevoServicio");
const cerrarModalServicio = document.getElementById("cerrarModalServicio");
const cancelarServicio = document.getElementById("cancelarServicio");
const formServicio = document.getElementById("formServicio");

if (btnNuevoServicio && modalServicio) {
    btnNuevoServicio.addEventListener("click", () => {
        formServicio.reset();

        document.getElementById("idServicio").value = "";
        idDisponibilidadActual = "";

        document.getElementById("tituloModalServicio").textContent = "Nuevo servicio";
        modalServicio.classList.add("active");
    });
}

if (cerrarModalServicio) {
    cerrarModalServicio.addEventListener("click", () => {
        modalServicio.classList.remove("active");
    });
}

if (cancelarServicio) {
    cancelarServicio.addEventListener("click", () => {
        modalServicio.classList.remove("active");
    });
}

if (modalServicio) {
    modalServicio.addEventListener("click", event => {
        if (event.target === modalServicio) {
            modalServicio.classList.remove("active");
        }
    });
}

const tablaServicios = document.getElementById("tablaServicios");
const cantidadServicios = document.getElementById("cantidadServicios");
const destinoServicio = document.getElementById("destinoServicio");
const formServicioBD = document.getElementById("formServicio");

const ID_AGENCIA_PRUEBA = 5;

async function cargarDestinos() {
    if (!destinoServicio) return;

    try {
        const response = await fetch("http://localhost:8080/api/agencia/destinos");
        const data = await response.json();

        destinoServicio.innerHTML = '<option value="">Seleccionar destino</option>';

        if (data.status === "success") {
            data.destinos.forEach(destino => {
                const option = document.createElement("option");
                option.value = destino.idDestino;
                option.textContent = destino.nombre;
                destinoServicio.appendChild(option);
            });
        }

    } catch (error) {
        console.error("Error al cargar destinos:", error);
    }
}

let serviciosRegistrados = [];
let idDisponibilidadActual = "";
async function cargarServicios() {
    if (!tablaServicios) return;

    try {
        const response = await fetch(
            `http://localhost:8080/api/agencia/servicios?idAgencia=${ID_AGENCIA_PRUEBA}`
        );

        const data = await response.json();
        serviciosRegistrados = data.servicios || [];

        tablaServicios.innerHTML = "";

        if (data.status !== "success" || data.servicios.length === 0) {
            tablaServicios.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-table">
                        No hay servicios registrados.
                    </td>
                </tr>
            `;

            cantidadServicios.textContent = "0 servicios";
            return;
        }

        cantidadServicios.textContent =
            `${data.servicios.length} servicio${data.servicios.length !== 1 ? "s" : ""}`;

        data.servicios.forEach(servicio => {

            const fila = document.createElement("tr");

            fila.innerHTML = `
                <td>${servicio.nombre}</td>
                <td>${servicio.tipoServicio}</td>
                <td>${servicio.destino}</td>
                <td>S/ ${Number(servicio.precio).toFixed(2)}</td>
                <td>${servicio.duracion}</td>
                <td>${servicio.estado}</td>
                <td>
                    <button
                        type="button"
                        class="btn-table btn-editar-servicio"
                        data-id="${servicio.idServicio}">
                        Editar
                    </button>
                </td>
            `;

            tablaServicios.appendChild(fila);
        });

        // Botones Editar de los servicios cargados dinámicamente
        tablaServicios.querySelectorAll(".btn-editar-servicio").forEach(boton => {
            boton.addEventListener("click", () => {
                editarServicio(boton.dataset.id);
            });
        });

    } catch (error) {

        console.error("Error al cargar servicios:", error);

        tablaServicios.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    No se pudieron cargar los servicios.
                </td>
            </tr>
        `;
    }
}

if (formServicioBD) {

    formServicioBD.addEventListener("submit", async event => {

        event.preventDefault();

        const mensaje = document.getElementById("mensajeServicio");

        // ==============================
        // DATOS DEL SERVICIO
        // ==============================

        const datos = new URLSearchParams();

        datos.append(
            "nombre",
            document.getElementById("nombreServicio").value.trim()
        );

        datos.append(
            "tipoServicio",
            document.getElementById("tipoServicio").value
        );

        datos.append(
            "idDestino",
            document.getElementById("destinoServicio").value
        );

        datos.append(
            "precio",
            document.getElementById("precioServicio").value
        );

        datos.append(
            "duracion",
            document.getElementById("duracionServicio").value.trim()
        );

        datos.append(
            "descripcion",
            document.getElementById("descripcionServicio").value.trim()
        );

        datos.append(
            "condiciones",
            document.getElementById("condicionesServicio").value.trim()
        );

        datos.append(
            "estado",
            document.getElementById("estadoServicio").value
        );

        datos.append(
            "idAgencia",
            ID_AGENCIA_PRUEBA
        );
        // Detectar si estamos creando o editando
        const idServicioActual = document.getElementById("idServicio").value.trim();
        const modoEdicion = idServicioActual !== "";
        datos.set("idServicio", idServicioActual);

        console.log("ID DEL SERVICIO:", idServicioActual);
        console.log("MODO EDICIÓN:", modoEdicion);



        // ==============================
        // DATOS DE PRIMERA DISPONIBILIDAD
        // ==============================

        const fechaDisponibilidad =
            document.getElementById("fechaDisponibilidad").value;

        const cuposDisponibilidad =
            document.getElementById("cuposDisponibilidad").value;

        const horaInicioDisponibilidad =
            document.getElementById("horaInicioDisponibilidad").value;

        const horaFinDisponibilidad =
            document.getElementById("horaFinDisponibilidad").value;


        // ==========================================
// VALIDAR PRIMERA DISPONIBILIDAD
// Solo es obligatoria al crear un servicio
// ==========================================

        if (!modoEdicion && (!fechaDisponibilidad || !cuposDisponibilidad)) {

            mostrarMensaje(
                mensaje,
                "Completa la fecha y los cupos de la primera disponibilidad."
            );
            return;
        }

        try {

            mostrarMensaje(
                mensaje,
                "Guardando servicio...",
                "success"
            );


            // ==============================
            // 1. GUARDAR SERVICIO
            // ==============================

            const response = await fetch(
                "http://localhost:8080/api/agencia/servicios",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: datos.toString()
                }
            );


            const data = await response.json();


            // Verificar que el servicio se haya creado

            if (data.status !== "success") {

                mostrarMensaje(
                    mensaje,
                    data.message || "No se pudo guardar el servicio."
                );

                return;
            }


            // ==============================
            // OBTENER ID DEL SERVICIO
            // ==============================

            const idServicio = data.idServicio;


            if (!idServicio) {

                mostrarMensaje(
                    mensaje,
                    "El servicio se creó, pero no se recibió su ID."
                );

                return;
            }


            // ==============================
            // 2. GUARDAR PRIMERA DISPONIBILIDAD
            // ==============================
            if (
                !modoEdicion ||
                idDisponibilidadActual ||
                (fechaDisponibilidad && cuposDisponibilidad)
            ) {

                mostrarMensaje(
                    mensaje,
                    "Guardando primera disponibilidad...",
                    "success"
                );


                const datosDisponibilidad = {
                    fecha: fechaDisponibilidad,
                    horaInicio: horaInicioDisponibilidad,
                    horaFin: horaFinDisponibilidad,
                    cupoTotal: cuposDisponibilidad,
                    idServicio: idServicio,
                    ...(idDisponibilidadActual
                        ? { idDisponibilidad: Number(idDisponibilidadActual) }
                        : {})
                };

                if (
                    idDisponibilidadActual &&
                    (!fechaDisponibilidad || !cuposDisponibilidad)
                ) {
                    mostrarMensaje(
                        mensaje,
                        "Completa la fecha y los cupos de la disponibilidad."
                    );
                    return;
                }

                const responseDisponibilidad = await fetch(
                    "http://localhost:8080/api/agencia/disponibilidad",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify(datosDisponibilidad)
                    }
                );

                const dataDisponibilidad =
                    await responseDisponibilidad.json();
                console.log("RESPUESTA DISPONIBILIDAD:", responseDisponibilidad.status);
                console.log("DATOS DISPONIBILIDAD:", dataDisponibilidad);


                // ==============================
                // VERIFICAR DISPONIBILIDAD
                // ==============================

                if (
                    !responseDisponibilidad.ok ||
                    !dataDisponibilidad.mensaje
                ) {

                    mostrarMensaje(
                        mensaje,
                        "Respuesta del servidor: " +
                        JSON.stringify(dataDisponibilidad)
                    );

                    return;
                }
            }




            mostrarMensaje(
                mensaje,
                modoEdicion
                    ? "Servicio y disponibilidad actualizados correctamente."
                    : "Servicio y primera disponibilidad registrados correctamente.",
                "success"
            );

            // Limpiar formulario

            formServicioBD.reset();
            idDisponibilidadActual = "";


            // Cerrar modal y actualizar tabla

            setTimeout(() => {
                modalServicio.classList.remove("active");
                cargarServicios();
                cargarDisponibilidades();
                idDisponibilidadActual = "";
            }, 1000);


        } catch (error) {

            console.error(
                "ERROR AL REGISTRAR SERVICIO:",
                error
            );

            mostrarMensaje(
                mensaje,
                "No se pudo conectar con el servidor."
            );
        }

    });
}

if (destinoServicio) {
    cargarDestinos();
}

if (tablaServicios) {
    cargarServicios();
}


async function editarServicio(idServicio) {

    const servicio = serviciosRegistrados.find(
        s => Number(s.idServicio) === Number(idServicio)
    );

    if (!servicio) {
        alert("No se encontró el servicio.");
        return;
    }

    // Cargar los datos del servicio
    document.getElementById("idServicio").value = servicio.idServicio;
    document.getElementById("nombreServicio").value = servicio.nombre || "";
    document.getElementById("tipoServicio").value = servicio.tipoServicio || "";
    document.getElementById("destinoServicio").value = servicio.idDestino || "";
    document.getElementById("precioServicio").value = servicio.precio || "";
    document.getElementById("duracionServicio").value = servicio.duracion || "";
    document.getElementById("descripcionServicio").value = servicio.descripcion || "";
    document.getElementById("condicionesServicio").value = servicio.condiciones || "";
    document.getElementById("estadoServicio").value = servicio.estado || "ACTIVO";

    // Reiniciar el identificador de disponibilidad
    idDisponibilidadActual = "";

    // Consultar disponibilidades de la agencia
    try {
        const response = await fetch(
            `http://localhost:8080/api/agencia/disponibilidad?idAgencia=${ID_AGENCIA_PRUEBA}`
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar las disponibilidades.");
        }

        const data = await response.json();

        const lista = Array.isArray(data)
            ? data
            : (data.disponibilidades || []);

        // Seleccionar la primera disponibilidad del servicio
        const disponibilidad = lista.find(
            d => Number(d.idServicio) === Number(servicio.idServicio)
        );

        if (disponibilidad) {
            idDisponibilidadActual =
                String(disponibilidad.idDisponibilidad);

            document.getElementById("fechaDisponibilidad").value =
                disponibilidad.fecha || "";

            document.getElementById("horaInicioDisponibilidad").value =
                disponibilidad.horaInicio || "";

            document.getElementById("horaFinDisponibilidad").value =
                disponibilidad.horaFin || "";

            document.getElementById("cuposDisponibilidad").value =
                disponibilidad.cupoTotal ?? "";
        } else {
            // No hay disponibilidad existente para este servicio
            document.getElementById("fechaDisponibilidad").value = "";
            document.getElementById("horaInicioDisponibilidad").value = "";
            document.getElementById("horaFinDisponibilidad").value = "";
            document.getElementById("cuposDisponibilidad").value = "";
        }

    } catch (error) {
        console.error("Error al cargar disponibilidad del servicio:", error);
        mostrarMensaje(
            document.getElementById("mensajeServicio"),
            "No se pudieron cargar las disponibilidades. Intenta nuevamente."
        );
        return;
    }

    document.getElementById("tituloModalServicio").textContent =
        "Editar servicio";

    modalServicio.classList.add("active");
}

// ==========================================
// DISPONIBILIDAD Y CUPOS
// ==========================================

const tablaDisponibilidad = document.getElementById("tablaDisponibilidad");
const cantidadDisponibilidades = document.getElementById("cantidadDisponibilidades");
const buscarDisponibilidad = document.getElementById("buscarDisponibilidad");
const filtroEstadoDisponibilidad = document.getElementById("filtroEstadoDisponibilidad");
const filtroFechaDisponibilidad = document.getElementById("filtroFechaDisponibilidad");

let disponibilidades = [];

// ==========================================
// CARGAR DISPONIBILIDADES
// ==========================================

async function cargarDisponibilidades() {

    if (!tablaDisponibilidad) return;

    try {

        tablaDisponibilidad.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    Cargando disponibilidades...
                </td>
            </tr>
        `;

        const response = await fetch(
            `http://localhost:8080/api/agencia/disponibilidad?idAgencia=${ID_AGENCIA_PRUEBA}`
        );

        const data = await response.json();

        console.log("DISPONIBILIDADES:", data);

        if (!response.ok) {
            throw new Error("No se pudieron obtener las disponibilidades.");
        }

        disponibilidades = Array.isArray(data)
            ? data
            : (data.disponibilidades || []);

        mostrarDisponibilidades(disponibilidades);

    } catch (error) {

        console.error("Error al cargar disponibilidades:", error);

        tablaDisponibilidad.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    No se pudieron cargar las disponibilidades.
                </td>
            </tr>
        `;

        if (cantidadDisponibilidades) {
            cantidadDisponibilidades.textContent = "0 disponibilidades";
        }
    }
}


// ==========================================
// MOSTRAR DISPONIBILIDADES
// ==========================================

function mostrarDisponibilidades(lista) {

    if (!tablaDisponibilidad) return;

    tablaDisponibilidad.innerHTML = "";

    if (cantidadDisponibilidades) {

        cantidadDisponibilidades.textContent =
            `${lista.length} disponibilidad${lista.length !== 1 ? "es" : ""}`;
    }

    if (lista.length === 0) {

        tablaDisponibilidad.innerHTML = `
            <tr>
                <td colspan="7" class="empty-table">
                    No hay disponibilidades registradas.
                </td>
            </tr>
        `;

        return;
    }

    lista.forEach(disponibilidad => {

        const fila = document.createElement("tr");

        const fecha = disponibilidad.fecha
            ? new Date(disponibilidad.fecha + "T00:00:00")
                .toLocaleDateString("es-PE")
            : "-";

        const horaInicio = disponibilidad.horaInicio || "-";
        const horaFin = disponibilidad.horaFin || "-";

        fila.innerHTML = `
            <td>${disponibilidad.servicio || "-"}</td>

            <td>${fecha}</td>

            <td>
                ${horaInicio} - ${horaFin}
            </td>

            <td>
                ${disponibilidad.cupoTotal ?? 0}
            </td>

            <td>
                ${disponibilidad.cupoDisponible ?? 0}
            </td>

            <td>
                ${disponibilidad.estado || "-"}
            </td>

            <td>
                <button
                    type="button"
                    class="btn-table">
                    Ver
                </button>
            </td>
        `;

        tablaDisponibilidad.appendChild(fila);
    });
}


// ==========================================
// FILTROS
// ==========================================

function filtrarDisponibilidades() {

    const texto = buscarDisponibilidad
        ? buscarDisponibilidad.value.trim().toLowerCase()
        : "";

    const estado = filtroEstadoDisponibilidad
        ? filtroEstadoDisponibilidad.value
        : "";

    const fecha = filtroFechaDisponibilidad
        ? filtroFechaDisponibilidad.value
        : "";

    const resultado = disponibilidades.filter(disponibilidad => {

        const coincideServicio =
            !texto ||
            (disponibilidad.servicio || "")
                .toLowerCase()
                .includes(texto);

        const coincideEstado =
            !estado ||
            disponibilidad.estado === estado;

        const coincideFecha =
            !fecha ||
            disponibilidad.fecha === fecha;

        return coincideServicio &&
            coincideEstado &&
            coincideFecha;
    });

    mostrarDisponibilidades(resultado);
}


// ==========================================
// EVENTOS DE LOS FILTROS
// ==========================================

if (buscarDisponibilidad) {

    buscarDisponibilidad.addEventListener(
        "input",
        filtrarDisponibilidades
    );
}

if (filtroEstadoDisponibilidad) {

    filtroEstadoDisponibilidad.addEventListener(
        "change",
        filtrarDisponibilidades
    );
}

if (filtroFechaDisponibilidad) {

    filtroFechaDisponibilidad.addEventListener(
        "change",
        filtrarDisponibilidades
    );
}


// ==========================================
// CARGAR AL ABRIR DISPONIBILIDAD.HTML
// ==========================================

if (tablaDisponibilidad) {
    cargarDisponibilidades();
}


//RESERVAS


const ID_AGENCIA = 1;

const API_RESERVAS =
    `http://localhost:8080/api/agencia/reservas?idAgencia=${ID_AGENCIA}`;

const tablaReservas = document.getElementById("tablaReservas");
const cantidadReservas = document.getElementById("cantidadReservas");

function escaparHTML(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, caracter => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[caracter]);
}

function formatoFecha(fecha) {
    if (!fecha) return "—";

    const partes = String(fecha).substring(0, 10).split("-");
    if (partes.length !== 3) return escaparHTML(fecha);

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function formatoMonto(monto) {
    return Number(monto ?? 0).toLocaleString("es-PE", {
        style: "currency",
        currency: "PEN"
    });
}

async function cargarReservas() {
    tablaReservas.innerHTML = `
        <tr>
            <td colspan="8" class="empty-table">
                Cargando reservas...
            </td>
        </tr>`;

    try {
        const respuesta = await fetch(API_RESERVAS);
        const datos = await respuesta.json();

        if (!respuesta.ok || datos.status !== "success") {
            throw new Error(datos.message || "No se pudieron cargar las reservas.");
        }

        const reservas = datos.reservas || [];

        cantidadReservas.textContent =
            `${reservas.length} ${reservas.length === 1 ? "reserva" : "reservas"}`;

        if (reservas.length === 0) {
            tablaReservas.innerHTML = `
                <tr>
                    <td colspan="8" class="empty-table">
                        No hay reservas registradas.
                    </td>
                </tr>`;
            return;
        }

        tablaReservas.innerHTML = reservas.map(reserva => `
            <tr>
                <td>${escaparHTML(reserva.codigoReserva)}</td>
                <td>Por identificar</td>
                <td>${escaparHTML(reserva.nombreTour)}</td>
                <td>${formatoFecha(reserva.fechaRegistro)}</td>
                <td>${formatoFecha(reserva.fechaInicio)}</td>
                <td>${formatoFecha(reserva.fechaFin)}</td>
                <td>${formatoMonto(reserva.total)}</td>
                <td>${escaparHTML(reserva.estado)}</td>
            </tr>
        `).join("");

    } catch (error) {
        console.error("Error al cargar reservas:", error);

        cantidadReservas.textContent = "Error";

        tablaReservas.innerHTML = `
            <tr>
                <td colspan="8" class="empty-table">
                    No se pudieron cargar las reservas.
                    Revisa la conexión con el servidor.
                </td>
            </tr>`;
    }
}

document.addEventListener("DOMContentLoaded", cargarReservas);