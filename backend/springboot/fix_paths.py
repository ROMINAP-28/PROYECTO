
import os
import shutil

src_dir = "../../frontend"
dst_dir = "src/main/resources/static"

if os.path.exists(dst_dir):
    shutil.rmtree(dst_dir)
shutil.copytree(src_dir, dst_dir)

def process_file(filepath):
    # Read as Windows-1252 and save as UTF-8
    with open(filepath, "r", encoding="cp1252", errors="ignore") as f:
        content = f.read()
    
    if filepath.endswith(".html"):
        content = content.replace("../index.html", "/index.html")
    
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

for root, dirs, files in os.walk(dst_dir):
    for file in files:
        if file.endswith((".html", ".css", ".js")):
            process_file(os.path.join(root, file))

# Specific fixes
def replace_in_file(path, old, new):
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = c.replace(old, new)
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)

# Fix quotes
replace_in_file(f"{dst_dir}/css/style.css", "url(\"/img/banner-bg.jpg\x27)", "url(\x27/img/banner-bg.jpg\x27)")
replace_in_file(f"{dst_dir}/css/turista/nosotros.css", "url(\"/img/fondo1.png\x27)", "url(\x27/img/fondo1.png\x27)")
replace_in_file(f"{dst_dir}/css/administrador/login.css", "url(\"/img/fondologoadmin.png\x27)", "url(\x27/img/fondologoadmin.png\x27)")

# Fix style.css font
replace_in_file(f"{dst_dir}/css/style.css", "family=Inter:wght@300;400;500;600;700;800&family=Outfit:wght@400;500;600;700;800&display=swap", "family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700&display=swap")
replace_in_file(f"{dst_dir}/css/style.css", "font-family: \x27Inter\x27, -apple-system", "font-family: \x27Plus Jakarta Sans\x27, \x27Inter\x27, -apple-system")
replace_in_file(f"{dst_dir}/css/style.css", "border-bottom: 2px solid #196f3d;\n}", "border-bottom: 2px solid #196f3d;\n}\n.navbar-home-clean .nav-links a { color: #475569; }\n.navbar-home-clean .nav-links a:hover, .navbar-home-clean .nav-links a.active { color: #196f3d; }")

# Fix detalle-agencia.html variable bug
replace_in_file(f"{dst_dir}/html/turista/detalle-agencia.html", "encodeURIComponent(agenciaName)", "encodeURIComponent(nombreAgencia)")

# Fix index.html CSS
index_style = """    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .hide-on-desktop { display: none !important; }
        .menu-toggle { display: none !important; }
        @media (max-width: 992px) {
            .hide-on-desktop { display: block !important; }
            .menu-toggle { display: block !important; }
        }
    </style>
</head>"""
replace_in_file(f"{dst_dir}/index.html", """    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
</head>""", index_style)
replace_in_file(f"{dst_dir}/index.html", "onerror=\"this.onerror=null; this.src=\"/img/logo.png\x27;\"", "onerror=\"this.onerror=null; this.src=\x27/img/logo.png\x27;\"")

# Fix experiencias text
replace_in_file(f"{dst_dir}/js/turista/experiencias.js", "(Cupos: ${cupos}/${totalCupos})", "Disponibles: ${cupos}")

