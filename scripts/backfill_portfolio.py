import json
import sqlite3
import os
import re
from datetime import datetime
import time

# Configuration
DB_PATH = "sans_finance_db"
DATA_DIR = "./portfolio_data"
SNAPSHOT_PATTERN = re.compile(r".*_snapshot\.json$")

def extract_price(details):
    if not details:
        return None
    # Match "Price: $1,234.56" or "Price: 1234.56"
    match = re.search(r"Price:\s*\$?([\d,]+\.?\d*)", details)
    if match:
        try:
            return float(match.group(1).replace(",", ""))
        except ValueError:
            return None
    return None

def parse_date_to_millis(date_str):
    # Parse yyyy-MM-dd to epoch millis (Jakarta time approx by just using local if same)
    # PortfolioJsonImporter uses SimpleDateFormat with Asia/Jakarta
    dt = datetime.strptime(date_str, "%Y-%m-%d")
    return int(dt.timestamp() * 1000)

def main():
    if not os.path.exists(DB_PATH):
        print(f"❌ Error: Database file {DB_PATH} not found!")
        return

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    print("🧹 Clearing existing portfolio data...")
    cursor.execute("DELETE FROM portfolio_snapshot_headers")
    # portfolio_holdings should be deleted by CASCADE, but let's be sure if not
    cursor.execute("DELETE FROM portfolio_holdings")

    files = [f for f in os.listdir(DATA_DIR) if SNAPSHOT_PATTERN.match(f)]
    files.sort()

    print(f"📂 Found {len(files)} snapshot files.")

    for filename in files:
        filepath = os.path.join(DATA_DIR, filename)
        print(f"📄 Processing {filename}...")
        
        with open(filepath, 'r') as f:
            data = json.load(f)
        
        metadata = data.get("metadata", {})
        date_str = metadata.get("date")
        if not date_str:
            print(f"⚠️ Warning: No date in {filename}, skipping.")
            continue
            
        snapshot_date = parse_date_to_millis(date_str)
        exchange_rate = metadata.get("exchange_rate") or 16000.0
        
        holdings = data.get("holdings", [])
        total_idr = sum((h.get("value_idr") or 0.0) for h in holdings)
        total_usd = total_idr / exchange_rate
        
        # Insert Header
        cursor.execute("""
            INSERT INTO portfolio_snapshot_headers 
            (snapshotDate, exchangeRateUsd, totalValueIdr, totalValueUsd, createdAt)
            VALUES (?, ?, ?, ?, ?)
        """, (snapshot_date, exchange_rate, total_idr, total_usd, int(time.time() * 1000)))
        
        # Insert Holdings
        for h in holdings:
            details = h.get("details")
            # Prioritize price field from JSON, fallback to extraction from details
            price = h.get("price")
            if price is None:
                price = extract_price(details)
            # Use quantity from JSON, fallback to amount, then value_idr for IDR assets
            quantity = h.get("quantity")
            if quantity is None:
                quantity = h.get("amount")
                
            if (quantity is None or quantity == 0) and h.get("currency") == "IDR":
                quantity = h.get("value_idr") or 0.0
            elif quantity is None:
                quantity = 0.0
 
            value_idr = h.get("value_idr")
            if value_idr is None:
                value_idr = 0.0
 
            cursor.execute("""
                INSERT INTO portfolio_holdings 
                (snapshot_date, source, category, asset, currency, quantity, price, value_idr, account, details, asset_class)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                snapshot_date,
                h.get("source") or "",
                h.get("category") or "",
                h.get("asset") or "",
                h.get("currency") or "IDR",
                quantity,
                price,
                value_idr,
                h.get("account") or "",
                details,
                h.get("asset_class") or "Other"
            ))

    conn.commit()
    conn.close()
    print("✅ Backfill complete!")

if __name__ == "__main__":
    main()
