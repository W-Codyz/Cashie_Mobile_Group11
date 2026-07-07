package com.uth.cashie.data

import android.content.Context
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.model.Transaction
import com.uth.cashie.model.TransactionGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Repository trung tâm cho Transaction.
 * 
 * **Đã chuyển từ fake data sang Room Database.**
 * 
 * Tất cả method đều là suspend function (chạy trên IO thread).
 */
object TransactionRepository {

    private lateinit var db: CashieDatabase

    /** Khởi tạo — gọi từ Application.onCreate() */
    fun init(context: Context) {
        db = CashieDatabase.getInstance(context)
    }

    private fun getUserId(): Long = SessionManager.getCurrentUserId()

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD operations
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getAll(): List<Transaction> = withContext(Dispatchers.IO) {
        db.transactionDao().getAllByUserOnce(getUserId()).map { it.toModel() }
    }

    suspend fun getById(id: Int): Transaction? = withContext(Dispatchers.IO) {
        db.transactionDao().getAllByUserOnce(getUserId())
            .find { it.id == id.toLong() }?.toModel()
    }

    suspend fun getByMonth(month: Int, year: Int): List<Transaction> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMs = cal.timeInMillis

        cal.set(year, month - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endMs = cal.timeInMillis

        db.transactionDao().getByDateRangeOnce(getUserId(), startMs, endMs)
            .map { it.toModel() }
    }

    suspend fun getByYear(year: Int): List<Transaction> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.set(year, 0, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMs = cal.timeInMillis

        cal.set(year, 11, 31, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endMs = cal.timeInMillis

        db.transactionDao().getByDateRangeOnce(getUserId(), startMs, endMs)
            .map { it.toModel() }
    }

    suspend fun add(transaction: Transaction): Transaction = withContext(Dispatchers.IO) {
        val userId = getUserId()
        // Tra categoryId từ tên danh mục trong DB
        val catId = db.categoryDao().getByTypeOnce(
            userId,
            if (transaction.isIncome) "income" else "expense"
        ).find { it.name == transaction.category }?.id

        val entity = transaction.toEntity(userId).copy(categoryId = catId, walletId = transaction.walletId)
        val id = db.transactionDao().insert(entity)
        transaction.copy(id = id.toInt())
    }

    suspend fun update(transaction: Transaction) = withContext(Dispatchers.IO) {
        val userId = getUserId()
        val existing = db.transactionDao().getAllByUserOnce(userId)
            .find { it.id == transaction.id.toLong() } ?: return@withContext

        val catId = db.categoryDao().getByTypeOnce(
            userId,
            if (transaction.isIncome) "income" else "expense"
        ).find { it.name == transaction.category }?.id ?: existing.categoryId

        db.transactionDao().updateTransaction(
            id         = transaction.id.toLong(),
            userId     = userId,
            amount     = abs(transaction.amount).toDouble(),
            note       = transaction.title,
            date       = parseDateToMillis(transaction.date, transaction.time),
            categoryId = catId,
            walletId = transaction.walletId ?: existing.walletId,
            updatedAt  = System.currentTimeMillis()
        )
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        db.transactionDao().deleteById(id.toLong(), getUserId())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Grouping helper
    // ─────────────────────────────────────────────────────────────────────────

    fun getGroupedByDate(transactions: List<Transaction>): List<TransactionGroup> =
        transactions
            .groupBy { it.date }
            .entries
            .sortedByDescending { it.key }
            .map { (_, txList) ->
                val net = txList.sumOf { it.amount }
                val parts = txList.first().date.split("-")
                val label = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" 
                            else txList.first().date
                TransactionGroup(
                    dateLabel    = label,
                    dayNet       = net,
                    transactions = txList.sortedByDescending { it.time }
                )
            }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapper: Entity <-> Model
    // ─────────────────────────────────────────────────────────────────────────

    private fun com.uth.cashie.database.entity.TransactionEntity.toModel(): Transaction {
        val cal = Calendar.getInstance().apply { timeInMillis = transactionDate }
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
        
        // Load category info from database
        val category = categoryId?.let { catId ->
            runCatching {
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    db.categoryDao().getById(catId, userId)
                }
            }.getOrNull()
        }

        // Load wallet info from database
        val wallet = walletId?.let { wId ->
            runCatching {
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    db.walletDao().getById(wId, userId)
                }
            }.getOrNull()
        }

        return Transaction(
            id            = id.toInt(),
            title         = note ?: "",
            category      = category?.name ?: "Khác",
            emoji         = category?.iconEmoji ?: "📝",
            iconColorHex  = category?.color ?: "#888888",
            amount        = if (type == "income") amount.toLong() else -amount.toLong(),
            isIncome      = type == "income",
            time          = timeStr,
            date          = dateStr,
            walletId      = walletId,
            walletName    = wallet?.name
        )
    }

    private fun Transaction.toEntity(userId: Long): com.uth.cashie.database.entity.TransactionEntity {
        return com.uth.cashie.database.entity.TransactionEntity(
            id              = if (id == 0) 0 else id.toLong(),
            userId          = userId,
            categoryId      = null,  // Cần tra cứu từ category name nếu cần
            walletId        = walletId,
            type            = if (isIncome) "income" else "expense",
            amount          = abs(amount).toDouble(),
            note            = title,
            transactionDate = parseDateToMillis(date, time),
            createdAt       = System.currentTimeMillis()
        )
    }

    private fun parseDateToMillis(dateStr: String, timeStr: String): Long {
        val fullStr = "$dateStr $timeStr"
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return fmt.parse(fullStr)?.time ?: System.currentTimeMillis()
    }
}
