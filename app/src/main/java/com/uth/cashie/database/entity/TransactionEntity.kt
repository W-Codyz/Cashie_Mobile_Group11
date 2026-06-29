package com.uth.cashie.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng transactions – lưu toàn bộ giao dịch thu/chi.
 * Bảng có lượng dữ liệu lớn nhất, được truy vấn thường xuyên nhất.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity        = UserEntity::class,
            parentColumns = ["id"],
            childColumns  = ["user_id"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity        = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns  = ["category_id"],
            onDelete      = ForeignKey.SET_NULL   // giao dịch không bị xóa khi danh mục bị xóa
        )
    ],
    indices = [
        Index(value = ["user_id", "transaction_date"]),  // index chính cho filter theo thời gian
        Index(value = ["category_id"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    /** Nullable – SET NULL khi danh mục bị xóa */
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,

    /** 'income' hoặc 'expense' */
    @ColumnInfo(name = "type")
    val type: String,

    /** Số tiền giao dịch (luôn dương, type xác định thu/chi) */
    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "note")
    val note: String? = null,

    /** Ngày user khai báo thực hiện giao dịch (Unix timestamp ms, có thể nhập quá khứ) */
    @ColumnInfo(name = "transaction_date")
    val transactionDate: Long,

    /** Lúc bản ghi được tạo trong DB */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** Null nếu chưa từng sửa, cập nhật mỗi khi user chỉnh sửa */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long? = null
)
