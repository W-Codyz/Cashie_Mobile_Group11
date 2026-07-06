
# Checklist Kiểm Tra Chức Năng Thống Kê và Báo Cáo

## Tổng Quan
- [ ] Mở màn hình Thống kê (StatsActivity) thành công
- [ ] Hiển thị đúng tháng/năm hiện tại khi mở màn hình
- [ ] Áp dụng theme chính xác cho màn hình Thống kê

## Điều Hướng Tháng
- [ ] Nhấn nút "Tháng trước" (prevMonth) hoạt động đúng
- [ ] Nhấn nút "Tháng trước" khi tháng 1 sẽ chuyển về tháng 12 năm trước
- [ ] Nhấn nút "Tháng sau" (nextMonth) hoạt động đúng
- [ ] Nhấn nút "Tháng sau" khi tháng 12 sẽ chuyển về tháng 1 năm sau
- [ ] Dữ liệu cập nhật ngay sau khi thay đổi tháng/năm

## Hiển Thị Tổng Quan (Summary)
- [ ] Hiển thị tổng thu nhập đúng
- [ ] Hiển thị tổng chi tiêu đúng
- [ ] Hiển thị số dư đúng (thu nhập - chi tiêu)
- [ ] Hiển thị tổng số giao dịch đúng
- [ ] Hiển thị chi tiêu cao nhất đúng
- [ ] Hiển thị trung bình chi/ngày đúng
- [ ] Định dạng tiền tệ VND chính xác (sử dụng formatVND)

## So Sánh Tháng Trước (Comparison)
- [ ] Hiển thị thay đổi thu nhập so với tháng trước (số tiền + phần trăm)
- [ ] Hiển thị thay đổi chi tiêu so với tháng trước (số tiền + phần trăm)
- [ ] Màu sắc thay đổi đúng (tăng thu nhập: xanh dương, tăng chi tiêu: đỏ)
- [ ] Không hiển thị thông tin so sánh khi tháng trước không có dữ liệu

## Danh Mục Chi Tiêu (Expense by Category)
- [ ] Hiển thị danh sách danh mục chi tiêu đúng thứ tự (từ cao đến thấp)
- [ ] Hiển thị emoji, tên danh mục, số tiền, số giao dịch, tỉ lệ %
- [ ] Hiển thị thông báo "Chưa có dữ liệu" khi không có chi tiêu
- [ ] Ẩn RecyclerView khi không có dữ liệu

## Danh Mục Thu Nhập (Income by Category)
- [ ] Hiển thị danh sách danh mục thu nhập đúng thứ tự (từ cao đến thấp)
- [ ] Hiển thị emoji, tên danh mục, số tiền, số giao dịch, tỉ lệ %
- [ ] Hiển thị thông báo "Chưa có dữ liệu" khi không có thu nhập
- [ ] Ẩn RecyclerView khi không có dữ liệu

## Biểu Đồ Cột (Bar Chart)
- [ ] Hiển thị biểu đồ cột thu/chi 12 tháng
- [ ] Màu sắc cột thu nhập: xanh dương (#22CC00)
- [ ] Màu sắc cột chi tiêu: đỏ (#FF4444)
- [ ] Nhãn trục X hiển thị đúng (T1-T12)
- [ ] Định dạng giá trị trục Y đúng (K, M)
- [ ] Hiển thị thông báo "Chưa có dữ liệu" khi không có dữ liệu
- [ ] Biểu đồ animate hiện lên mượt mà

## Biểu Đồ Donut (Donut Chart)
- [ ] Hiển thị biểu đồ donut chi tiêu theo quý
- [ ] Màu sắc quý 1: #A5D6A7
- [ ] Màu sắc quý 2: #FFF59D
- [ ] Màu sắc quý 3: #FFCC80
- [ ] Màu sắc quý 4: #80DEEA
- [ ] Hiển thị tiêu đề trung tâm "Chi tiêu - Cả năm"
- [ ] Hiển thị legend giá trị cho từng quý
- [ ] Biểu đồ animate hiện lên mượt mà

## Xuất Báo Cáo PDF
- [ ] Nhấn nút "Xuất PDF" thành công
- [ ] Hiển thị progress bar khi đang xử lý
- [ ] Thông báo thành công và tên file sau khi xuất xong
- [ ] File được lưu vào thư mục Downloads
- [ ] Tên file đúng định dạng: Cashie_BaoCao_T{thang}_{nam}.pdf
- [ ] Nội dung PDF đầy đủ và chính xác (tổng quan, so sánh, danh mục, xu hướng quý)
- [ ] Tiếng Việt hiển thị đúng trong PDF
- [ ] Thông báo lỗi khi không có dữ liệu để xuất

## Xuất Báo Cáo CSV (Excel)
- [ ] Nhấn nút "Xuất Excel" thành công
- [ ] Hiển thị progress bar khi đang xử lý
- [ ] Thông báo thành công và tên file sau khi xuất xong
- [ ] File được lưu vào thư mục Downloads
- [ ] Tên file đúng định dạng: Cashie_BaoCao_T{thang}_{nam}.csv
- [ ] Nội dung CSV đầy đủ và chính xác (tổng quan, so sánh, danh mục, xu hướng tháng, xu hướng quý)
- [ ] Tiếng Việt hiển thị đúng trong Excel (UTF-8 BOM)
- [ ] Thông báo lỗi khi không có dữ liệu để xuất

## Tải Dữ Liệu (Loading)
- [ ] Hiển thị progress bar khi đang tải dữ liệu
- [ ] Ẩn progress bar khi tải xong
- [ ] Hiển thị thông báo lỗi nếu có lỗi khi tải dữ liệu

## Trạng Thái Không Có Dữ Liệu
- [ ] Hiển thị "Chưa có dữ liệu" cho biểu đồ cột
- [ ] Hiển thị "Chưa có" cho các quý không có chi tiêu
- [ ] Ẩn các phần không có dữ liệu phù hợp

## Tương Thích Android
- [ ] Hoạt động đúng trên Android 10+ (MediaStore)
- [ ] Hoạt động đúng trên Android 9 trở xuống (Environment)
- [ ] Quyền truy cập lưu trữ được xử lý đúng

## Theme
- [ ] Màu chủ đề áp dụng đúng cho các phần tử UI (tiêu đề, nút, bottom nav)
- [ ] Theme tối/ánh sáng hoạt động đúng (nếu có)

## Kiểm Tra Dữ Liệu Chính Xác
- [ ] Tổng thu nhập khớp với tổng các giao dịch thu nhập trong tháng
- [ ] Tổng chi tiêu khớp với tổng các giao dịch chi tiêu trong tháng
- [ ] Số dư = tổng thu nhập - tổng chi tiêu
- [ ] Tỉ lệ % tổng các danh mục = 100%
- [ ] Xu hướng tháng khớp với dữ liệu từng tháng
- [ ] Xu hướng quý khớp với dữ liệu từng quý
