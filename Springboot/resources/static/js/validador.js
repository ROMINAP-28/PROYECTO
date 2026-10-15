// Travelink - Validador de Formularios y Reglas de Negocio

function getError(msg) {
    var icono = $("<i>", { class: "fa-solid fa-circle-exclamation" });
    var texto = $("<span>", { text: " " + (msg || "Campo obligatorio") });
    return $("<div>", { class: "error-msg", style: "color: #ef4444; font-size: 12px; margin-top: 4px;" }).append(icono).append(texto);
}

function resaltarValido(id) {
    $("#" + id).removeClass("novalido").addClass("valido");
    $("#" + id).parent().children(".error-msg").remove();
}

function resaltarNoValido(id, msg) {
    $("#" + id).removeClass("valido").addClass("novalido");
    if ($("#" + id).parent().children(".error-msg").length === 0) {
        $("#" + id).parent().append(getError(msg));
    }
}

function resaltarFormatoIncorrecto(id, mensajePersonalizado) {
    $("#" + id).removeClass("valido").addClass("novalido");
    $("#" + id).parent().children(".error-msg").remove();
    var msg = mensajePersonalizado || "Formato incorrecto";
    var icono = $("<i>", { class: "fa-solid fa-triangle-exclamation" });
    var texto = $("<span>", { text: " " + msg });
    var mensaje = $("<div>", { class: "error-msg", style: "color: #ef4444; font-size: 12px; margin-top: 4px;" }).append(icono).append(texto);
    $("#" + id).after(mensaje);
}

function limpiarEstiloValidacion(id) {
    $("#" + id).removeClass("novalido valido");
    $("#" + id).parent().children(".error-msg").remove();
}

function isEmail(n) {
    var regExpEmail = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return regExpEmail.test(n) && n.length <= 40;
}

function isTelefonoPeruano(n) {
    // Debe empezar con 9 y tener 9 dígitos
    var regExpTel = /^9\d{8}$/;
    return regExpTel.test(n);
}

function isNombreValido(n) {
    // Solo letras y espacios, máximo 40 caracteres por nombre/apellido
    var regExpNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]{1,40}$/;
    return regExpNombre.test(n.trim());
}

function isUsuarioValido(n) {
    // Máximo 15 caracteres
    return n.trim().length > 0 && n.trim().length <= 15;
}

var validarFormulario = function (clase) {
    var camposPorValidar = 0;
    var camposValidados = 0;

    // Validar No Vacíos
    var listaNoEmpty = clase["noempty"];
    if (listaNoEmpty !== undefined && listaNoEmpty.length > 0) {
        camposPorValidar += listaNoEmpty.length;
        for (var i = 0; i < listaNoEmpty.length; ++i) {
            var valor = $("#" + listaNoEmpty[i]).val();
            if (valor && valor.trim() !== "") {
                limpiarEstiloValidacion(listaNoEmpty[i]);
                resaltarValido(listaNoEmpty[i]);
                camposValidados++;
            } else {
                limpiarEstiloValidacion(listaNoEmpty[i]);
                resaltarNoValido(listaNoEmpty[i], "Campo obligatorio");
            }
        }
    }

    // Validar Emails (Max 40)
    var listaEmails = clase["email"];
    if (listaEmails !== undefined && listaEmails.length > 0) {
        camposPorValidar += listaEmails.length;
        for (var i = 0; i < listaEmails.length; ++i) {
            var valorEmail = $("#" + listaEmails[i]).val();
            if (valorEmail && isEmail(valorEmail.trim())) {
                limpiarEstiloValidacion(listaEmails[i]);
                resaltarValido(listaEmails[i]);
                camposValidados++;
            } else {
                limpiarEstiloValidacion(listaEmails[i]);
                resaltarFormatoIncorrecto(listaEmails[i], "Correo inválido (máx. 40 caracteres)");
            }
        }
    }

    // Validar Teléfono (Inicia con 9, 9 dígitos)
    var listaTel = clase["telefono"];
    if (listaTel !== undefined && listaTel.length > 0) {
        camposPorValidar += listaTel.length;
        for (var i = 0; i < listaTel.length; ++i) {
            var valorTel = $("#" + listaTel[i]).val();
            if (valorTel && isTelefonoPeruano(valorTel.trim())) {
                limpiarEstiloValidacion(listaTel[i]);
                resaltarValido(listaTel[i]);
                camposValidados++;
            } else {
                limpiarEstiloValidacion(listaTel[i]);
                resaltarFormatoIncorrecto(listaTel[i], "Debe empezar con 9 y tener 9 dígitos");
            }
        }
    }

    // Validar Nombres (Sin números ni símbolos, max 40 con espacios)
    var listaNombres = clase["nombre"];
    if (listaNombres !== undefined && listaNombres.length > 0) {
        camposPorValidar += listaNombres.length;
        for (var i = 0; i < listaNombres.length; ++i) {
            var valorNombre = $("#" + listaNombres[i]).val();
            if (valorNombre && isNombreValido(valorNombre.trim())) {
                limpiarEstiloValidacion(listaNombres[i]);
                resaltarValido(listaNombres[i]);
                camposValidados++;
            } else {
                limpiarEstiloValidacion(listaNombres[i]);
                resaltarFormatoIncorrecto(listaNombres[i], "Solo letras (máx. 40 caracteres)");
            }
        }
    }

    return camposPorValidar === camposValidados;
};

// Eventos de entrada en tiempo real
$(document).ready(function () {
    // Restringir ingreso de caracteres en tiempo real
    $('input[type="tel"]').on('input', function () {
        this.value = this.value.replace(/[^0-9]/g, '').slice(0, 9);
    });

    $('input[id*="Nombre"], input[id*="Apellido"], input[id*="Titular"]').on('input', function () {
        this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '').slice(0, 40);
    });

    $('input[id*="Usuario"]').on('input', function () {
        this.value = this.value.slice(0, 15);
    });

    $('input[type="email"]').on('input', function () {
        this.value = this.value.slice(0, 40);
    });
});
