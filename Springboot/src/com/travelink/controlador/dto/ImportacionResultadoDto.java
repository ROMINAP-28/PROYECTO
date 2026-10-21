package com.travelink.controlador.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportacionResultadoDto {

    private String status;
    private String mensaje;
    private int totalFilas;
    private int filasImportadas;
    private int filasError;
    private List<FilaError> errores = new ArrayList<>();

    public ImportacionResultadoDto() {}

    public static class FilaError {
        private int fila;
        private String motivo;

        public FilaError(int fila, String motivo) {
            this.fila = fila;
            this.motivo = motivo;
        }

        public int getFila() { return fila; }
        public void setFila(int fila) { this.fila = fila; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }

    public void agregarError(int fila, String motivo) {
        this.errores.add(new FilaError(fila, motivo));
        this.filasError++;
    }

    public void incrementarImportadas() {
        this.filasImportadas++;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getTotalFilas() { return totalFilas; }
    public void setTotalFilas(int totalFilas) { this.totalFilas = totalFilas; }

    public int getFilasImportadas() { return filasImportadas; }
    public void setFilasImportadas(int filasImportadas) { this.filasImportadas = filasImportadas; }

    public int getFilasError() { return filasError; }
    public void setFilasError(int filasError) { this.filasError = filasError; }

    public List<FilaError> getErrores() { return errores; }
    public void setErrores(List<FilaError> errores) { this.errores = errores; }
}
