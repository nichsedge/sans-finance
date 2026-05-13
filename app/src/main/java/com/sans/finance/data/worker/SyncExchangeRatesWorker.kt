package com.sans.finance.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sans.finance.data.local.dao.CurrencyDao
import com.sans.finance.data.local.entity.ExchangeRateEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncExchangeRatesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val currencyDao: CurrencyDao by inject()

    override suspend fun doWork(): Result {
        val client = OkHttpClient()
        val url = "https://open.er-api.com/v6/latest/IDR"
        
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return Result.failure()
                val json = JSONObject(body)
                val rates = json.getJSONObject("rates")
                val exchangeRates = mutableListOf<ExchangeRateEntity>()
                
                val keys = rates.keys()
                while (keys.hasNext()) {
                    val code = keys.next()
                    val rateToBase = rates.getDouble(code)
                    if (rateToBase > 0) {
                        exchangeRates.add(
                            ExchangeRateEntity(
                                code = code,
                                rateToIdr = 1.0 / rateToBase
                            )
                        )
                    }
                }
                currencyDao.insertRates(exchangeRates)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
