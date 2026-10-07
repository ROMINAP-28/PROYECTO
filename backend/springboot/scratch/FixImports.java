package scratch;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FixImports {
    public static void main(String[] args) throws Exception {
        String baseDir = "d:/ProyectosU/PROYECTO/backend/springboot/src/main/java/com/travelink/controlador";
        String[] dirs = {"admin", "agencia", "turista"};
        
        String extraImports = 
            "import java.nio.file.Files;\n" +
            "import java.nio.file.Path;\n" +
            "import java.nio.file.Paths;\n" +
            "import java.io.File;\n" +
            "import java.io.OutputStream;\n" +
            "import java.nio.charset.StandardCharsets;\n" +
            "import com.travelink.entidades.Usuario;\n";
            
        for (String d : dirs) {
            File folder = new File(baseDir + "/" + d);
            if (!folder.exists()) continue;
            for (File f : folder.listFiles()) {
                if (f.getName().endsWith(".java")) {
                    String content = new String(Files.readAllBytes(f.toPath()));
                    
                    // Replace method calls properly just in case PowerShell missed some
                    content = content.replaceAll("\\b(sendJsonResponse|readRequestBody|parseQueryParams|parseJsonOrFormParams|validarDuplicadosAgencia|enableCORS|jsonEscape|escapar)\\(", "JavaApiServer.$1(");
                    
                    // Fix calificacionControlador and reservaRepositorio variables
                    content = content.replaceAll("calificacionControlador\\.", "JavaApiServer.calificacionControlador.");
                    content = content.replaceAll("reservaRepositorio\\.", "JavaApiServer.reservaRepositorio.");
                    content = content.replaceAll("usuarioControlador\\.", "JavaApiServer.usuarioControlador.");
                    content = content.replaceAll("obtenerParametro\\(", "DisponibilidadAgenciaHandler.obtenerParametro("); // specific to DisponibilidadAgenciaHandler

                    // Add imports below the package declaration
                    int pkgEnd = content.indexOf(";") + 1;
                    content = content.substring(0, pkgEnd) + "\n\n" + extraImports + content.substring(pkgEnd);
                    
                    Files.write(f.toPath(), content.getBytes());
                }
            }
        }
        System.out.println("Imports and methods fixed!");
    }
}
