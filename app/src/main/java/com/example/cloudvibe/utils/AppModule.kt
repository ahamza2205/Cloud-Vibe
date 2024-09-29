package com.example.cloudvibe.utils

import android.content.Context
import com.example.cloudvibe.model.repository.WeatherRepository
import com.example.cloudvibe.model.database.WeatherDao
import com.example.cloudvibe.model.database.WeatherDatabase
import com.example.cloudvibe.model.network.WeatherApiService
import com.example.cloudvibe.model.sharedpreferences.SharedPreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        return RetrofitInstance.api
    }

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext appContext: Context): WeatherDatabase {
        return WeatherDatabase.getDatabase(appContext)
    }

    @Provides
    fun provideWeatherDao(db: WeatherDatabase): WeatherDao {
        return db.weatherDao()
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(api: WeatherApiService, dao: WeatherDao, sharedPreferencesHelper: SharedPreferencesHelper): WeatherRepository {
        return WeatherRepository(api, dao, sharedPreferencesHelper)
    }

    @Provides
    @Singleton
    fun provideSharedPreferencesHelper(@ApplicationContext context: Context): SharedPreferencesHelper {
        return SharedPreferencesHelper(context)
    }
}

