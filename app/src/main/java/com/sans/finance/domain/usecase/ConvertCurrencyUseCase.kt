package com.sans.finance.domain.usecase

import com.sans.finance.data.local.dao.CurrencyDao
import javax.inject.Inject

class ConvertCurrencyUseCase @Inject constructor(
    private val currencyDao: CurrencyDao
) {
    suspend operator fun invoke(amount: Long, from: String, to: String): Long {
        if (from == to) return amount
        
        // Base currency is IDR in our ExchangeRateEntity
        val fromRate = if (from == "IDR") 1.0 else currencyDao.getRate(from)?.rateToIdr ?: 1.0
        val toRate = if (to == "IDR") 1.0 else currencyDao.getRate(to)?.rateToIdr ?: 1.0
        
        if (toRate == 0.0) return amount
        
        // (amount * fromRate) gives IDR value
        // (IDR value / toRate) gives TO value
        val idrValue = amount * fromRate
        return (idrValue / toRate).toLong()
    }
}
