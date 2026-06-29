package com.uth.cashie.data

import com.uth.cashie.model.Transaction
import com.uth.cashie.model.TransactionGroup

/**
 * Repository trung tâm cho Transaction.
 *
 * Dùng dữ liệu fake (hardcoded) – có thể thay bằng Room/Firebase
 * mà không cần thay đổi code ở ViewModel hay Repository khác.
 */
object TransactionRepository {

    private val all: List<Transaction> = listOf(
        // ── Tháng 1 ──────────────────────────────────────────────────────────
        Transaction(101, "Lương tháng 1",    "Lương",       "💰", "#22CC00", 12_000_000, true,  "08:00", "2026-01-05"),
        Transaction(102, "Ăn trưa",          "Ăn uống",     "🍜", "#FF8C00",    -85_000, false, "12:00", "2026-01-06"),
        Transaction(103, "Taxi",             "Di chuyển",   "🚗", "#2196F3",    -45_000, false, "17:00", "2026-01-06"),
        Transaction(104, "Tiền điện",        "Hóa đơn",     "⚡", "#FFC107",   -450_000, false, "09:00", "2026-01-10"),
        Transaction(105, "Siêu thị",         "Mua sắm",     "🛒", "#E91E63",   -320_000, false, "11:00", "2026-01-12"),
        Transaction(106, "Freelance T1",     "Thu nhập",    "💼", "#22CC00",  2_000_000, true,  "14:00", "2026-01-15"),
        Transaction(107, "Café",             "Ăn uống",     "☕", "#795548",    -55_000, false, "07:30", "2026-01-18"),
        Transaction(108, "Internet",         "Hóa đơn",     "📶", "#9C27B0",   -200_000, false, "09:05", "2026-01-10"),
        Transaction(109, "Quần áo",          "Mua sắm",     "👗", "#E91E63",   -650_000, false, "15:00", "2026-01-20"),
        Transaction(110, "Gym",              "Tập thể dục", "🏋", "#FF5722",   -300_000, false, "06:00", "2026-01-22"),
        // ── Tháng 2 ──────────────────────────────────────────────────────────
        Transaction(201, "Lương tháng 2",    "Lương",       "💰", "#22CC00", 12_000_000, true,  "08:00", "2026-02-05"),
        Transaction(202, "Ăn tối",           "Ăn uống",     "🍱", "#FF8C00",   -120_000, false, "19:00", "2026-02-07"),
        Transaction(203, "Tiền nhà",         "Hóa đơn",     "🏠", "#607D8B", -3_500_000, false, "10:00", "2026-02-01"),
        Transaction(204, "Thưởng Tết",       "Thưởng",      "🎁", "#22CC00",  5_000_000, true,  "09:00", "2026-02-10"),
        Transaction(205, "Quà Tết",          "Quà tặng",    "🎀", "#E91E63",   -800_000, false, "11:00", "2026-02-11"),
        Transaction(206, "Siêu thị Tết",     "Mua sắm",     "🛒", "#E91E63", -1_200_000, false, "14:00", "2026-02-12"),
        Transaction(207, "Café",             "Ăn uống",     "☕", "#795548",    -45_000, false, "07:30", "2026-02-14"),
        // ── Tháng 3 ──────────────────────────────────────────────────────────
        Transaction(301, "Lương tháng 3",    "Lương",       "💰", "#22CC00", 12_000_000, true,  "08:00", "2026-03-05"),
        Transaction(302, "Ăn trưa",          "Ăn uống",     "🍜", "#FF8C00",    -75_000, false, "12:00", "2026-03-06"),
        Transaction(303, "Tiền điện",        "Hóa đơn",     "⚡", "#FFC107",   -420_000, false, "09:00", "2026-03-10"),
        Transaction(304, "Đầu tư cổ phiếu", "Đầu tư",      "📈", "#22CC00",  3_000_000, true,  "10:00", "2026-03-15"),
        Transaction(305, "Sách",             "Giáo dục",    "📚", "#3F51B5",   -250_000, false, "16:00", "2026-03-17"),
        Transaction(306, "Gym",              "Tập thể dục", "🏋", "#FF5722",   -300_000, false, "06:00", "2026-03-22"),
        Transaction(307, "Internet",         "Hóa đơn",     "📶", "#9C27B0",   -200_000, false, "09:05", "2026-03-10"),
        // ── Tháng 4 ──────────────────────────────────────────────────────────
        Transaction(401, "Lương tháng 4",    "Lương",       "💰", "#22CC00", 12_000_000, true,  "08:00", "2026-04-05"),
        Transaction(402, "Vé máy bay",       "Du lịch",     "✈", "#00BCD4", -1_800_000, false, "05:00", "2026-04-10"),
        Transaction(403, "Khách sạn",        "Du lịch",     "🏨", "#00BCD4", -2_400_000, false, "14:00", "2026-04-11"),
        Transaction(404, "Ăn du lịch",       "Ăn uống",     "🍣", "#FF8C00",   -600_000, false, "12:00", "2026-04-12"),
        Transaction(405, "Freelance T4",     "Thu nhập",    "💼", "#22CC00",  1_500_000, true,  "10:00", "2026-04-18"),
        Transaction(406, "Tiền điện",        "Hóa đơn",     "⚡", "#FFC107",   -380_000, false, "09:00", "2026-04-10"),
        // ── Tháng 5 ──────────────────────────────────────────────────────────
        Transaction(501, "Lương tháng 5",    "Lương",       "💰", "#22CC00", 12_000_000, true,  "08:00", "2026-05-05"),
        Transaction(502, "Tiền nhà",         "Hóa đơn",     "🏠", "#607D8B", -3_500_000, false, "10:00", "2026-05-01"),
        Transaction(503, "Học tiếng Anh",    "Giáo dục",    "📖", "#3F51B5", -1_200_000, false, "09:00", "2026-05-10"),
        Transaction(504, "Freelance T5",     "Thu nhập",    "💼", "#22CC00",  2_500_000, true,  "15:00", "2026-05-15"),
        Transaction(505, "Siêu thị",         "Mua sắm",     "🛒", "#E91E63",   -480_000, false, "11:00", "2026-05-18"),
        Transaction(506, "Điện thoại mới",   "Mua sắm",     "📱", "#607D8B", -8_500_000, false, "14:00", "2026-05-25"),
        Transaction(507, "Tiền điện",        "Hóa đơn",     "⚡", "#FFC107",   -410_000, false, "09:00", "2026-05-10"),
        // ── Tháng 6 ──────────────────────────────────────────────────────────
        Transaction(601, "Lương tháng 6",    "Lương",       "💰", "#22CC00", 12_000_000, true,  "08:00", "2026-06-05"),
        Transaction(602, "Ăn trưa",          "Ăn uống",     "🍜", "#FF8C00",    -85_000, false, "12:15", "2026-06-16"),
        Transaction(603, "Taxi về nhà",      "Di chuyển",   "🚗", "#2196F3",    -45_000, false, "17:30", "2026-06-16"),
        Transaction(604, "Siêu thị Coopmart","Mua sắm",     "🛒", "#E91E63",   -350_000, false, "10:00", "2026-06-15"),
        Transaction(605, "Freelance T6",     "Thu nhập",    "💼", "#22CC00",  2_500_000, true,  "14:00", "2026-06-15"),
        Transaction(606, "Tiền điện",        "Hóa đơn",     "⚡", "#FFC107",   -450_000, false, "09:00", "2026-06-14"),
        Transaction(607, "Internet",         "Hóa đơn",     "📶", "#9C27B0",   -200_000, false, "09:05", "2026-06-14"),
        Transaction(608, "Café buổi sáng",   "Ăn uống",     "☕", "#795548",    -35_000, false, "07:30", "2026-06-14"),
    )

    fun getAll(): List<Transaction> = all

    fun getByMonth(month: Int, year: Int): List<Transaction> = all.filter { tx ->
        val p = tx.date.split("-")
        p.getOrNull(0)?.toIntOrNull() == year && p.getOrNull(1)?.toIntOrNull() == month
    }

    fun getByYear(year: Int): List<Transaction> = all.filter { it.date.startsWith(year.toString()) }

    /** Nhóm theo ngày, sắp xếp mới nhất lên đầu */
    fun getGroupedByDate(transactions: List<Transaction>): List<TransactionGroup> =
        transactions
            .groupBy { it.date }
            .entries
            .sortedByDescending { it.key }
            .map { (_, txList) ->
                val net = txList.sumOf { if (it.isIncome) it.amount else -it.amount }
                val parts = txList.first().date.split("-")
                val label = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else txList.first().date
                TransactionGroup(
                    dateLabel    = label,
                    dayNet       = net,
                    transactions = txList.sortedByDescending { it.time }
                )
            }
}
