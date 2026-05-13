package com.sans.finance.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migrations {
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
            connection.execSQL("""
                CREATE TABLE IF NOT EXISTS `expense_tag_ref` (
                    `expenseId` INTEGER NOT NULL,
                    `tagId` INTEGER NOT NULL,
                    PRIMARY KEY(`expenseId`, `tagId`),
                    FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_tag_ref_expenseId` ON `expense_tag_ref` (`expenseId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_expense_tag_ref_tagId` ON `expense_tag_ref` (`tagId`)")
            connection.execSQL("INSERT OR IGNORE INTO tags (name) SELECT DISTINCT platform FROM expenses WHERE platform IS NOT NULL AND platform != ''")
            connection.execSQL("INSERT INTO expense_tag_ref (expenseId, tagId) SELECT e.id, t.id FROM expenses e JOIN tags t ON e.platform = t.name WHERE e.platform IS NOT NULL AND e.platform != ''")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE categories ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
            connection.execSQL("ALTER TABLE tags ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` INTEGER NOT NULL, `currency` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
            val now = com.sans.finance.core.util.DateTimeUtils.getNow()
            connection.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `balance`, `currency`, `created_at`, `updated_at`) VALUES (1, 'Cash', 'Cash', 0, 'IDR', $now, $now)")
            connection.execSQL("ALTER TABLE expenses ADD COLUMN account_id INTEGER NOT NULL DEFAULT 1")
            connection.execSQL("ALTER TABLE expenses ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
            connection.execSQL("ALTER TABLE categories ADD COLUMN type TEXT NOT NULL DEFAULT 'EXPENSE'")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `net_worth_snapshots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `totalAssets` INTEGER NOT NULL, `totalLiabilities` INTEGER NOT NULL, `netWorth` INTEGER NOT NULL)")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `currentAmount` INTEGER NOT NULL, `currency` TEXT NOT NULL, `deadline` INTEGER, `accountId` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `categoryId` INTEGER, `accountId` INTEGER, `period` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE expenses ADD COLUMN recurrence_interval TEXT")
            connection.execSQL("ALTER TABLE expenses ADD COLUMN next_due_date INTEGER")
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override suspend fun migrate(connection: SQLiteConnection) {
            // Simplified rename for KMP if RENAME COLUMN is not available in old SQLite, 
            // but modern Room/SQLite should handle it.
            connection.execSQL("ALTER TABLE expenses RENAME COLUMN item_name TO note")
            connection.execSQL("ALTER TABLE expenses RENAME COLUMN merchant TO description")
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE expenses ADD COLUMN to_account_id INTEGER")
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE expenses ADD COLUMN currency TEXT NOT NULL DEFAULT 'USD'")
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("""
                CREATE TABLE IF NOT EXISTS `portfolio_snapshots` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `snapshot_date` INTEGER NOT NULL,
                    `source` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `asset` TEXT NOT NULL,
                    `currency` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `price` REAL,
                    `value_idr` REAL NOT NULL,
                    `value_usd` REAL NOT NULL,
                    `account` TEXT NOT NULL,
                    `details` TEXT,
                    `created_at` INTEGER NOT NULL
                )
            """.trimIndent())
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_portfolio_snapshots_snapshot_date` ON `portfolio_snapshots` (`snapshot_date`)")
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `portfolio_snapshot_headers` (`snapshotDate` INTEGER PRIMARY KEY NOT NULL, `exchangeRateUsd` REAL NOT NULL, `totalValueIdr` REAL NOT NULL, `totalValueUsd` REAL NOT NULL, `createdAt` INTEGER NOT NULL)")
            connection.execSQL("CREATE TABLE IF NOT EXISTS `portfolio_holdings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `snapshot_date` INTEGER NOT NULL, `source` TEXT NOT NULL, `category` TEXT NOT NULL, `asset` TEXT NOT NULL, `currency` TEXT NOT NULL, `amount` REAL NOT NULL, `price` REAL, `value_idr` REAL NOT NULL, `account` TEXT NOT NULL, `details` TEXT, FOREIGN KEY(`snapshot_date`) REFERENCES `portfolio_snapshot_headers`(`snapshotDate`) ON UPDATE NO ACTION ON DELETE CASCADE)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_portfolio_holdings_snapshot_date` ON `portfolio_holdings` (`snapshot_date`)")
            connection.execSQL("INSERT OR IGNORE INTO portfolio_snapshot_headers (snapshotDate, exchangeRateUsd, totalValueIdr, totalValueUsd, createdAt) SELECT snapshot_date, AVG(value_idr / NULLIF(value_usd, 0)), SUM(value_idr), SUM(value_usd), MIN(created_at) FROM portfolio_snapshots GROUP BY snapshot_date")
            connection.execSQL("INSERT INTO portfolio_holdings (snapshot_date, source, category, asset, currency, amount, price, value_idr, account, details) SELECT snapshot_date, source, category, asset, currency, amount, price, value_idr, account, details FROM portfolio_snapshots")
            connection.execSQL("DROP TABLE portfolio_snapshots")
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE portfolio_holdings ADD COLUMN asset_class TEXT NOT NULL DEFAULT 'Other'")
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE goals RENAME TO goals_old")
            connection.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `targetType` TEXT NOT NULL, `targetName` TEXT, `currency` TEXT NOT NULL, `deadline` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
            connection.execSQL("INSERT INTO goals (id, name, targetAmount, targetType, targetName, currency, deadline, createdAt, updatedAt) SELECT id, name, CAST(targetAmount AS REAL), 'TOTAL', NULL, currency, deadline, createdAt, updatedAt FROM goals_old")
            connection.execSQL("DROP TABLE goals_old")
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override suspend fun migrate(connection: SQLiteConnection) {
            // SQLite doesn't support DROP COLUMN in older versions, but if we assume modern, we can try.
            // However, Room usually handles this by recreating the table if needed.
            // For KMP, we should be careful.
            try {
                connection.execSQL("ALTER TABLE expenses DROP COLUMN platform")
            } catch (e: Exception) {
                // Fallback or ignore if already gone
            }
        }
    }

    val MIGRATION_20_21 = object : Migration(20, 21) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("DROP TABLE IF EXISTS `expenses_fts`")
            connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `expenses_fts` USING fts4(content=`expenses`, `description`, `note`)")
            connection.execSQL("INSERT INTO expenses_fts(rowid, description, note) SELECT id, description, note FROM expenses")
        }
    }

    val MIGRATION_21_22 = object : Migration(21, 22) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS exchange_rates (code TEXT PRIMARY KEY NOT NULL, rateToIdr REAL NOT NULL, updatedAt INTEGER NOT NULL)")
        }
    }

    val MIGRATION_22_23 = object : Migration(22, 23) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE portfolio_holdings RENAME COLUMN amount TO quantity")
        }
    }

    val MIGRATION_23_24 = object : Migration(23, 24) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE TABLE IF NOT EXISTS `expenses_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `title` TEXT NOT NULL, `details` TEXT, `amount` INTEGER NOT NULL, `category_id` INTEGER NOT NULL, `account_id` INTEGER NOT NULL DEFAULT 1, `to_account_id` INTEGER, `type` TEXT NOT NULL DEFAULT 'EXPENSE', `currency` TEXT NOT NULL DEFAULT 'USD', `status` TEXT NOT NULL, `is_recurring` INTEGER NOT NULL, `is_installment` INTEGER NOT NULL DEFAULT 0, `recurrence_interval` TEXT, `next_due_date` INTEGER, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
            connection.execSQL("INSERT INTO expenses_new (id, date, title, details, amount, category_id, account_id, to_account_id, type, currency, status, is_recurring, is_installment, recurrence_interval, next_due_date, created_at, updated_at) SELECT id, date, note, description, final_price, category_id, account_id, to_account_id, type, currency, status, is_recurring, is_installment, recurrence_interval, next_due_date, created_at, updated_at FROM expenses")
            connection.execSQL("DROP TABLE expenses")
            connection.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
            connection.execSQL("DROP TABLE IF EXISTS `expenses_fts`")
            connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `expenses_fts` USING fts4(content=`expenses`, `title`, `details`)")
            connection.execSQL("INSERT INTO expenses_fts(rowid, title, details) SELECT id, title, details FROM expenses")
            connection.execSQL("CREATE TABLE IF NOT EXISTS `installments_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `expense_id` INTEGER NOT NULL, `status` TEXT NOT NULL, `duration_months` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, FOREIGN KEY(`expense_id`) REFERENCES `expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
            connection.execSQL("INSERT INTO installments_new (id, expense_id, status, duration_months, created_at) SELECT id, expense_id, status, duration_months, created_at FROM installments")
            connection.execSQL("DROP TABLE installments")
            connection.execSQL("ALTER TABLE installments_new RENAME TO installments")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_installments_expense_id` ON `installments` (`expense_id`)")
            connection.execSQL("CREATE TABLE IF NOT EXISTS `goals_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `targetType` TEXT NOT NULL, `targetName` TEXT, `currency` TEXT NOT NULL, `deadline` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
            connection.execSQL("INSERT INTO goals_new (id, name, targetAmount, targetType, targetName, currency, deadline, createdAt, updatedAt) SELECT id, name, CAST(targetAmount AS INTEGER), targetType, targetName, currency, deadline, createdAt, updatedAt FROM goals")
            connection.execSQL("DROP TABLE goals")
            connection.execSQL("ALTER TABLE goals_new RENAME TO goals")
        }
    }

    val MIGRATION_24_25 = object : Migration(24, 25) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_category_id` ON `expenses` (`category_id`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_account_id` ON `expenses` (`account_id`)")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15,
        MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20,
        MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25
    )
}
