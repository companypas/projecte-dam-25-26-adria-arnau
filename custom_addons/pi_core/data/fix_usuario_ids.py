# Script para ejecutar desde la consola de Odoo o shell
# Este script corrige todos los usuarios que tienen id_usuario = 'Nuevo'

# Para ejecutar desde el shell de Odoo:
# python odoo-bin shell -c odoo.conf -d nombre_base_datos
# Luego ejecutar:
# exec(open('ruta/a/este/archivo.py').read())

# O simplemente ejecutar este código en la consola de Odoo:
env['pi.usuario']._fix_existing_nuevo_ids()
print("IDs de usuarios corregidos exitosamente")
