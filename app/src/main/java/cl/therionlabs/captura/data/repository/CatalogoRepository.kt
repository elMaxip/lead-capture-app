package cl.therionlabs.captura.data.repository

import cl.therionlabs.captura.data.local.AreaEntity
import cl.therionlabs.captura.data.local.CatalogoDao
import cl.therionlabs.captura.data.local.CursoEntity
import cl.therionlabs.captura.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogoRepository @Inject constructor(
    private val dao: CatalogoDao,
    private val api: ApiService,
) {

    fun observarAreas(): Flow<List<AreaEntity>> = dao.observarAreas()

    fun observarCursos(areaId: Int): Flow<List<CursoEntity>> = dao.observarCursos(areaId)

    suspend fun refrescar() {
        val catalogo = api.obtenerCatalogo()
        val areas = catalogo.areas.map { AreaEntity(it.id, it.nombre) }
        val cursos = catalogo.areas.flatMap { area ->
            area.cursos.map { CursoEntity(it.id, area.id, it.nombre) }
        }
        dao.reemplazarCatalogo(areas, cursos)
    }
}
