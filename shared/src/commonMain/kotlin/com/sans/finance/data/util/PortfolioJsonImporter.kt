package com.sans.finance.data.util

import com.sans.finance.data.local.entity.PortfolioHoldingEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.datetime.*

@Serializable
data class PortfolioSnapshotJson(
    val metadata: SnapshotMetadata,
    val holdings: List<HoldingJson>
)

@Serializable
data class SnapshotMetadata(
    val date: String,
    @SerialName("exchange_rate") val exchangeRate: Double? = null
)

@Serializable
data class HoldingJson(
    val source: String,
    val category: String,
    val asset: String,
    val currency: String = "IDR",
    val amount: Double? = null,
    val quantity: Double? = null,
    val price: Double? = null,
    @SerialName("value_idr") val valueIdr: Double = 0.0,
    @SerialName("value_usd") val valueUsd: Double = 0.0,
    @SerialName("asset_class") val assetClass: String = "Other",
    val account: String? = null,
    val details: String? = null
)

object PortfolioJsonImporter {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun parse(
        jsonString: String
    ): Triple<Long, List<PortfolioHoldingEntity>, Double?> {
        val snapshot = json.decodeFromString<PortfolioSnapshotJson>(jsonString)

        val snapshotDate = try {
            val date = LocalDate.parse(snapshot.metadata.date)
            date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        } catch (e: Exception) {
            Clock.System.now().toEpochMilliseconds()
        }

        val entities = snapshot.holdings.map { holding ->
            PortfolioHoldingEntity(
                snapshotDate = snapshotDate,
                source = holding.source,
                category = holding.category,
                asset = holding.asset,
                currency = holding.currency,
                quantity = holding.quantity ?: holding.amount ?: 0.0,
                price = holding.price ?: extractPrice(holding.details),
                valueIdr = holding.valueIdr,
                assetClass = holding.assetClass,
                account = holding.account ?: "",
                details = holding.details
            )
        }

        return Triple(snapshotDate, entities, snapshot.metadata.exchangeRate)
    }

    private fun extractPrice(details: String?): Double? {
        if (details == null) return null
        val priceRegex = Regex("""Price:\s*\$?([\d,]+\.?\d*)""")
        val match = priceRegex.find(details)
        return match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }
}
