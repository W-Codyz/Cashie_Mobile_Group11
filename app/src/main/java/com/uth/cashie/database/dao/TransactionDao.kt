package com.uth.cashie.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uth.cashie.database.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id AND user_id = :userId")
    suspend fun deleteById(id: Long, userId: Long): Int

    // ── Lấy danh sách ────────────────────────────────────────────────────────

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC, created_at DESC")
    fun getAllByUser(userId: Long): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY transaction_date DESC, created_at DESC")
    suspend fun getAllByUserOnce(userId: Long): List<TransactionEntity>

    /** Lọc theo khoảng thời gian – dùng BETWEEN với Unix timestamp ms */
    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId 
          AND transaction_date BETWEEN :startMs AND :endMs
        ORDER BY transaction_date DESC, created_at DESC
    """)
    fun getByDateRange(userId: Long, startMs: Long, endMs: Long): LiveData<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId 
          AND transaction_date BETWEEN :startMs AND :endMs
        ORDER BY transaction_date DESC
    """)
    suspend fun getByDateRangeOnce(userId: Long, startMs: Long, endMs: Long): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions 
        WHERE user_id = :userId AND type = :type
          AND transaction_date BETWEEN :startMs AND :endMs
        ORDER BY transaction_date DESC
    """)
    suspend fun getByTypeAndRange(userId: Long, type: String, startMs: Long, endMs: Long): List<TransactionEntity>

    // ── Thống kê ─────────────────────────────────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE user_id = :userId AND type = 'income'
          AND transaction_date BETWEEN :startMs AND :endMs
    """)
    suspend fun getTotalIncome(userId: Long, startMs: Long, endMs: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE user_id = :userId AND type = 'expense'
          AND transaction_date BETWEEN :startMs AND :endMs
    """)
    suspend fun getTotalExpense(userId: Long, startMs: Long, endMs: Long): Double

    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE user_id = :userId
          AND transaction_date BETWEEN :startMs AND :endMs
    """)
    suspend fun getCount(userId: Long, startMs: Long, endMs: Long): Int

    @Query("""
        SELECT COALESCE(MAX(amount), 0) FROM transactions 
        WHERE user_id = :userId AND type = 'expense'
          AND transaction_date BETWEEN :startMs AND :endMs
    """)
    suspend fun getHighestExpense(userId: Long, startMs: Long, endMs: Long): Double

    /** Tổng chi theo từng danh mục trong khoảng thời gian */
    @Query("""
        SELECT category_id, SUM(amount) as total 
        FROM transactions 
        WHERE user_id = :userId AND type = :type
          AND transaction_date BETWEEN :startMs AND :endMs
        GROUP BY category_id
        ORDER BY total DESC
    """)
    suspend fun getSumByCategory(userId: Long, type: String, startMs: Long, endMs: Long): List<CategorySum>

    /** Cập nhật updated_at khi user chỉnh sửa giao dịch */
    @Query("UPDATE transactions SET amount = :amount, note = :note, transaction_date = :date, category_id = :categoryId, wallet_id = :walletId, updated_at = :updatedAt WHERE id = :id AND user_id = :userId")
    suspend fun updateTransaction(id: Long, userId: Long, amount: Double, note: String?, date: Long, categoryId: Long?, walletId: Long?, updatedAt: Long = System.currentTimeMillis())

    /** Lọc giao dịch theo danh mục */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND category_id = :categoryId ORDER BY transaction_date DESC, created_at DESC")
    suspend fun getByCategoryOnce(userId: Long, categoryId: Long): List<TransactionEntity>

    /** Net (income - expense) của 1 ví trong khoảng thời gian */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE -amount END), 0)
        FROM transactions
        WHERE user_id = :userId AND wallet_id = :walletId
          AND transaction_date BETWEEN :startMs AND :endMs
    """)
    suspend fun getNetByWallet(userId: Long, walletId: Long, startMs: Long, endMs: Long): Double
}

/** Data class phụ dùng cho query thống kê theo danh mục */
data class CategorySum(
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "total")       val total: Double
)
