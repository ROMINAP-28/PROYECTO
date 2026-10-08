import os

replacements = {
    'Ã¡': 'á', 'Ã©': 'é', 'Ã­': 'í', 'Ã³': 'ó', 'Ãº': 'ú',
    'Ã ': 'Á', 'Ã‰': 'É', 'Ã': 'Í', 'Ã“': 'Ó', 'Ãš': 'Ú',
    'Ã±': 'ñ', 'Ã‘': 'Ñ', 'Â¡': '¡', 'Â¿': '¿'
}

for root, dirs, files in os.walk('d:/ProyectosU/PROYECTO/frontend'):
    for file in files:
        if file.endswith('.js') or file.endswith('.html'):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                modified = False
                for k, v in replacements.items():
                    if k in content:
                        content = content.replace(k, v)
                        modified = True
                if modified:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(content)
            except Exception as e:
                pass
print('Encoding fixed')
