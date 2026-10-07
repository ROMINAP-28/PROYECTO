
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Splitter {
    public static void main(String[] args) throws Exception {
        String baseDir = "d:/ProyectosU/PROYECTO/backend/springboot/src/main/java/com/travelink";
        String serverFile = baseDir + "/server/JavaApiServer.java";
        String code = new String(Files.readAllBytes(Paths.get(serverFile)));
        
        // Find all static class ...Handler implements HttpHandler { ... }
        Pattern pattern = Pattern.compile("^[ \\t]*static class ([A-Za-z0-9_]+Handler) implements HttpHandler \\{", Pattern.MULTILINE);
        Matcher m = pattern.matcher(code);
        
        List<int[]> classBounds = new ArrayList<>();
        List<String> classNames = new ArrayList<>();
        
        while (m.find()) {
            String className = m.group(1);
            int startIdx = m.start();
            int endIdx = findClosingBrace(code, code.indexOf('{', startIdx));
            classBounds.add(new int[]{startIdx, endIdx});
            classNames.add(className);
        }
        
        System.out.println("Found " + classNames.size() + " handlers");
        
        String adminDir = baseDir + "/controlador/admin";
        String agenciaDir = baseDir + "/controlador/agencia";
        String turistaDir = baseDir + "/controlador/turista";
        Files.createDirectories(Paths.get(adminDir));
        Files.createDirectories(Paths.get(agenciaDir));
        Files.createDirectories(Paths.get(turistaDir));
        
        StringBuilder remainingCode = new StringBuilder(code);
        
        for (int i = classBounds.size() - 1; i >= 0; i--) {
            int start = classBounds.get(i)[0];
            int end = classBounds.get(i)[1] + 1;
            String className = classNames.get(i);
            String classCode = code.substring(start, end);
            
            // Determine package
            String pkg = "com.travelink.controlador.turista";
            String dir = turistaDir;
            if (className.contains("Admin")) {
                pkg = "com.travelink.controlador.admin";
                dir = adminDir;
            } else if (className.contains("Agencia")) {
                pkg = "com.travelink.controlador.agencia";
                dir = agenciaDir;
            }
            
            // Create file
            String newCode = "package " + pkg + ";\n\n" +
                             "import com.sun.net.httpserver.HttpExchange;\n" +
                             "import com.sun.net.httpserver.HttpHandler;\n" +
                             "import java.io.IOException;\n" +
                             "import java.util.Map;\n" +
                             "import java.sql.Connection;\n" +
                             "import java.sql.PreparedStatement;\n" +
                             "import java.sql.ResultSet;\n" +
                             "import java.sql.Statement;\n" +
                             "import com.travelink.config.ConexionDB;\n" +
                             "import com.travelink.server.JavaApiServer;\n" +
                             "import java.util.Locale;\n" +
                             "import java.util.ArrayList;\n" +
                             "import java.util.List;\n" +
                             "import com.travelink.controlador.UsuarioControlador;\n" +
                             "import com.travelink.controlador.CalificacionControlador;\n" +
                             "import com.travelink.repositorio.ReservaRepositorio;\n" +
                             "\n" +
                             classCode.replaceFirst("static class", "public class");
            
            Files.write(Paths.get(dir + "/" + className + ".java"), newCode.getBytes());
            
            // Remove from JavaApiServer
            remainingCode.delete(start, end);
        }
        
        // Replace original file
        Files.write(Paths.get(serverFile), remainingCode.toString().getBytes());
        System.out.println("Splitting complete!");
    }
    
    private static int findClosingBrace(String code, int startBraceIdx) {
        int braces = 1;
        for (int i = startBraceIdx + 1; i < code.length(); i++) {
            if (code.charAt(i) == '{') braces++;
            else if (code.charAt(i) == '}') {
                braces--;
                if (braces == 0) return i;
            }
        }
        return -1;
    }
}
