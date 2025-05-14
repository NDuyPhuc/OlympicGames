package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.db.DBConnectionManager;
import com.duyphuc.olympics.model.MedalEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedalDAO {

    /**
     * Lấy tất cả các bản ghi huy chương từ một bảng sự kiện Olympic cụ thể.
     * Các bản ghi được sắp xếp theo Vàng, Bạc, Đồng (giảm dần), rồi đến NOC (tăng dần).
     * @param tableName Tên bảng CSDL của kỳ Olympic (ví dụ: "medals_athens2004olympicsnationsmedalscsv")
     * @return Danh sách các đối tượng MedalEntry.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình truy vấn CSDL.
     */
    public List<MedalEntry> getMedalsByEventTable(String tableName) throws SQLException {
        List<MedalEntry> entries = new ArrayList<>();
        // Sử dụng backtick (`) cho tên bảng để đảm bảo an toàn nếu tên có ký tự đặc biệt
        // (mặc dù không có trong trường hợp này nhưng là thực hành tốt)
        // Giả định các bảng huy chương CÓ cột 'id'. Nếu không, cần loại bỏ 'id' khỏi SELECT.
        String sql = String.format("SELECT id, NOC, Gold, Silver, Bronze, Total FROM `%s` ORDER BY Gold DESC, Silver DESC, Bronze DESC, NOC ASC", tableName);

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                MedalEntry entry = new MedalEntry();
                entry.setId(rs.getInt("id")); // Giả định cột 'id' tồn tại
                entry.setNoc(rs.getString("NOC"));
                entry.setGold(rs.getInt("Gold"));
                entry.setSilver(rs.getInt("Silver"));
                entry.setBronze(rs.getInt("Bronze"));
                entry.setTotal(rs.getInt("Total"));
                entries.add(entry);
            }
        } catch (SQLException e) {
            // Ném lại SQLException để lớp gọi (Service) có thể xử lý
            throw new SQLException("Error fetching medals from table " + tableName + ": " + e.getMessage(), e);
        }
        return entries;
    }

    /**
     * Thêm một bản ghi huy chương mới vào bảng sự kiện Olympic cụ thể.
     * @param entry Đối tượng MedalEntry chứa thông tin cần thêm.
     * @param tableName Tên bảng CSDL của kỳ Olympic.
     * @return true nếu thêm thành công và ID được tạo, false nếu không.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình thêm vào CSDL.
     */
    public boolean addMedalEntry(MedalEntry entry, String tableName) throws SQLException {
        String sql = String.format("INSERT INTO `%s` (NOC, Gold, Silver, Bronze, Total) VALUES (?, ?, ?, ?, ?)", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, entry.getNoc());
            pstmt.setInt(2, entry.getGold());
            pstmt.setInt(3, entry.getSilver());
            pstmt.setInt(4, entry.getBronze());
            pstmt.setInt(5, entry.getTotal()); // Đảm bảo total đã được tính trong Service

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entry.setId(generatedKeys.getInt(1)); // Lấy ID tự tăng và gán lại cho entry
                        return true;
                    }
                }
            }
            return false; // Không có ID được tạo hoặc không có dòng nào bị ảnh hưởng
        } catch (SQLException e) {
            throw new SQLException("Error adding medal entry to " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin một bản ghi huy chương trong bảng sự kiện Olympic cụ thể.
     * @param entry Đối tượng MedalEntry chứa thông tin cập nhật (bao gồm cả ID của bản ghi cần sửa).
     * @param tableName Tên bảng CSDL của kỳ Olympic.
     * @return true nếu cập nhật thành công, false nếu không có bản ghi nào được cập nhật (ví dụ: ID không tồn tại).
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình cập nhật CSDL.
     */
    public boolean updateMedalEntry(MedalEntry entry, String tableName) throws SQLException {
        String sql = String.format("UPDATE `%s` SET NOC = ?, Gold = ?, Silver = ?, Bronze = ?, Total = ? WHERE id = ?", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getNoc());
            pstmt.setInt(2, entry.getGold());
            pstmt.setInt(3, entry.getSilver());
            pstmt.setInt(4, entry.getBronze());
            pstmt.setInt(5, entry.getTotal());
            pstmt.setInt(6, entry.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new SQLException("Error updating medal entry in " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Xóa một bản ghi huy chương khỏi bảng sự kiện Olympic cụ thể dựa trên ID.
     * @param entryId ID của bản ghi huy chương cần xóa.
     * @param tableName Tên bảng CSDL của kỳ Olympic.
     * @return true nếu xóa thành công, false nếu không có bản ghi nào được xóa (ví dụ: ID không tồn tại).
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình xóa khỏi CSDL.
     */
    public boolean deleteMedalEntry(int entryId, String tableName) throws SQLException {
        String sql = String.format("DELETE FROM `%s` WHERE id = ?", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new SQLException("Error deleting medal entry from " + tableName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách các mã quốc gia (NOC) duy nhất từ một bảng sự kiện Olympic.
     * @param tableName Tên bảng CSDL của kỳ Olympic.
     * @return Danh sách các chuỗi NOC, được sắp xếp theo thứ tự bảng chữ cái.
     * @throws SQLException Nếu có lỗi xảy ra trong quá trình truy vấn CSDL.
     */
    public List<String> getNOCsByEventTable(String tableName) throws SQLException {
        List<String> nocList = new ArrayList<>();
        String sql = String.format("SELECT DISTINCT NOC FROM `%s` ORDER BY NOC ASC", tableName);
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                nocList.add(rs.getString("NOC"));
            }
        } catch (SQLException e) {
            throw new SQLException("Error fetching NOCs from table " + tableName + ": " + e.getMessage(), e);
        }
        return nocList;
    }
}