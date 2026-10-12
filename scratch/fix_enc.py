import os

replacements = {
    'Ã¡': 'á', 'Ã©': 'é', 'Ã­': 'í', 'Ã³': 'ó', 'Ãº': 'ú',
    'Ã ': 'Á', 'Ã‰': 'É', 'Ã': 'Í', 'Ã"': 'Ó', 'Ãš': 'Ú',
    'Ã±': 'ñ', 'Ã±': 'ñ', 'Â¡': '¡', 'Â¿': '¿', 'NÂ°': 'N°'
}

files = [
    r'd:\ProyectosU\PROYECTO\backend\springboot\src\main\resources\static\html\administrador\dashboard.html',
    r'd:\ProyectosU\PROYECTO\backend\springboot\src\main\resources\static\js\agencia\agencia.js',
]

for filepath in files:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    original = content
    for k, v in replacements.items():
        content = content.replace(k, v)
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Corregido: {filepath}')
    else:
        print(f'Sin cambios: {filepath}')
