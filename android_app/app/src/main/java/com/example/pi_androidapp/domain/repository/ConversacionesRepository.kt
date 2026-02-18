package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Conversacion
import com.example.pi_androidapp.domain.model.Mensaje
import kotlinx.coroutines.flow.Flow

/** Interfaz del repositorio de conversaciones. Define las operaciones de chat. */
interface ConversacionesRepository {

    /**
     * Listar conversaciones del usuario autenticado.
     * @param offset Offset para paginación
     * @param limit Límite de resultados
     * @return Flow con lista de conversaciones
     */
    fun listarConversaciones(offset: Int = 0, limit: Int = 20): Flow<Resource<List<Conversacion>>>

    /**
     * Obtener mensajes de una conversación.
     * @param conversacionId ID de la conversación
     * @param offset Offset para paginación
     * @param limit Límite de resultados
     * @return Flow con lista de mensajes
     */
    fun obtenerMensajes(
            conversacionId: Int,
            offset: Int = 0,
            limit: Int = 50
    ): Flow<Resource<List<Mensaje>>>

    /**
     * Enviar un mensaje en una conversación.
     * @param conversacionId ID de la conversación
     * @param contenido Contenido del mensaje
     * @return Flow con el mensaje enviado
     */
    fun enviarMensaje(conversacionId: Int, contenido: String): Flow<Resource<Mensaje>>

    /**
     * Iniciar un chat sobre un producto.
     * @param productoId ID del producto
     * @return Flow con el ID de la conversación creada/existente
     */
    fun iniciarChat(productoId: Int): Flow<Resource<Int>>
}
