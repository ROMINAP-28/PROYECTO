import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CleanConfig {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "frontend/html/Agencia/inicio.html",
            "frontend/html/Agencia/servicios.html",
            "frontend/html/Agencia/disponibilidad.html",
            "frontend/html/Agencia/paquetes.html",
            "frontend/html/Agencia/reservas.html",
            "frontend/html/Agencia/pagos.html"
        };

        // Compilar las expresiones regulares una sola vez (mejora el rendimiento)
        Pattern sidebarPattern = Pattern.compile("(?s)<a\\s+href=[\"']configuracion\\.html[\"'][^>]*>.*?</a>\\s*");
        Pattern dropdownPattern = Pattern.compile("(?s)<a\\s+href=[\"']configuracion\\.html[\"'][^>]*>.*?Configuración.*?</a>\\s*");

        for (String f : files) {
            File file = new File(f);
            if (!file.exists()) continue;
            String content = Files.readString(Paths.get(f));
            
            // Remove sidebar link for configuracion
            Matcher sidebarMatcher = sidebarPattern.matcher(content);
            content = sidebarMatcher.replaceAll("");
            
            // Remove dropdown link for configuracion
            Matcher dropdownMatcher = dropdownPattern.matcher(content);
            content = dropdownMatcher.replaceAll("");

            Files.writeString(Paths.get(f), content);
            System.out.println("Cleaned configuracion from: " + f);
        }
    }
}
