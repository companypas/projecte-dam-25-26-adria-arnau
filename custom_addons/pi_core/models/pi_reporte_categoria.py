# -*- coding: utf-8 -*-
from odoo import models, fields, api
from collections import Counter

class PiReporteCategoria(models.TransientModel):
    _name = 'pi.reporte.categoria'
    _description = 'Reporte de Categorías Más Vendidas'
    
    @api.model
    def get_categorias_mas_vendidas(self):
        """Obtiene las categorías más vendidas basándose en las compras confirmadas"""
        compras = self.env['pi.compra'].search([
            ('estado', 'in', ['confirmada', 'valorada_comprador', 'valorada_vendedor'])
        ])
        
        # Contar productos vendidos por categoría
        categorias_ventas = {}
        for compra in compras:
            categoria = compra.producto_id.categoria_id
            if categoria:
                if categoria.id not in categorias_ventas:
                    categorias_ventas[categoria.id] = {
                        'categoria': categoria,
                        'total_ventas': 0,
                        'total_monto': 0.0,
                        'productos_vendidos': []
                    }
                categorias_ventas[categoria.id]['total_ventas'] += 1
                categorias_ventas[categoria.id]['total_monto'] += compra.monto
                categorias_ventas[categoria.id]['productos_vendidos'].append(compra.producto_id)
        
        # Ordenar por total de ventas (más vendidas primero)
        categorias_ordenadas = sorted(
            categorias_ventas.values(),
            key=lambda x: x['total_ventas'],
            reverse=True
        )
        
        return categorias_ordenadas
