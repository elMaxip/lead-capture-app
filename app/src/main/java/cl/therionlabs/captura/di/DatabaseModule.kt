package cl.therionlabs.captura.di

import android.content.Context
import androidx.room.Room
import cl.therionlabs.captura.data.local.AppDatabase
import cl.therionlabs.captura.data.local.CatalogoDao
import cl.therionlabs.captura.data.local.ProspectoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, json: Json): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NOMBRE)
            .addCallback(AppDatabase.SembrarCatalogo(context, json))
            .build()

    @Provides
    fun provideProspectoDao(db: AppDatabase): ProspectoDao = db.prospectoDao()

    @Provides
    fun provideCatalogoDao(db: AppDatabase): CatalogoDao = db.catalogoDao()
}
