package com.example.cryptoapp.data.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.cryptoapp.data.database.AppDatabase
import com.example.cryptoapp.data.mapper.CoinMapper
import com.example.cryptoapp.data.network.ApiFactory
import kotlinx.coroutines.delay

class RefreshDataWorker(
    context: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {
    private val coinInfoDao = AppDatabase.getInstance(context).coinPriceInfoDao()
    private val mapper = CoinMapper()
    private val apiService = ApiFactory.apiService

    //apply suspend function for coroutine
    override suspend fun doWork(): Result {
        while (true) {
            try {
                val topCoins = apiService.getTopCoinsInfo(limit = 50)
                val fSymbols = mapper.mapNamesListToString(topCoins)
                val jsonContainer = apiService.getFullPriceList(fSyms = fSymbols)
                val coinInfoDtoList = mapper.mapJsonContainerToListCoinInfo(jsonContainer)
                val dbModelList = coinInfoDtoList.map { mapper.mapDtoToDbModel(it) }
                coinInfoDao.insertPriceList(dbModelList)
            } catch (e: Exception) {
                Log.d("MyLog", e.toString())
            }
            delay(10000)
        }
    }

    companion object{
        const val NAME = "RefreshDataWorker"
        fun makeRequest(): OneTimeWorkRequest{
            return OneTimeWorkRequestBuilder<RefreshDataWorker>().build()
        }
    }
}