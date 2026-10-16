val ts = json.optJSONArray("timestamp")
(0 until close.length()).map { i ->
    val t = ts?.optLong(i) ?: 0L
    val date = if (t > 0) {
        java.time.Instant.ofEpochMilli(if (t < 1e12) t * 1000 else t)
            .atZone(java.time.ZoneId.of("Asia/Kolkata"))
            .toLocalDateTime().toString()
    } else ""
    DailyBar(date, t, open.optDouble(i), high.optDouble(i), low.optDouble(i), close.optDouble(i), vol?.optLong(i) ?: 0)
}
