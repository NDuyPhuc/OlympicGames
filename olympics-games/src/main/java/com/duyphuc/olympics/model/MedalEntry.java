// MedalEntry.java
package com.duyphuc.olympics.model;

public class MedalEntry {
    private int id; // Khóa chính
    private String noc;
    private int gold;
    private int silver;
    private int bronze;
    private int total;

    // Các trường mới cho ngữ cảnh báo cáo
    private int olympicEventYear;
    private String olympicEventType;

    public MedalEntry() {} // Default constructor for JavaFX and other frameworks

    // Constructor khi có ID (thường là khi đọc từ DB)
    public MedalEntry(int id, String noc, int gold, int silver, int bronze) {
        this.id = id;
        this.noc = noc;
        this.gold = gold;
        this.silver = silver;
        this.bronze = bronze;
        this.total = gold + silver + bronze; // Tự động tính tổng
    }

    // Constructor khi không có ID (thường là khi tạo mới trước khi lưu vào DB
    // hoặc khi tạo các đối tượng tổng hợp trong ReportService)
    public MedalEntry(String noc, int gold, int silver, int bronze) {
        this.noc = noc;
        this.gold = gold;
        this.silver = silver;
        this.bronze = bronze;
        this.total = gold + silver + bronze; // Tự động tính tổng
    }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoc() { return noc; }
    public void setNoc(String noc) { this.noc = noc; }

    public int getGold() { return gold; }
    public void setGold(int gold) {
        this.gold = gold;
        updateTotal();
    }

    public int getSilver() { return silver; }
    public void setSilver(int silver) {
        this.silver = silver;
        updateTotal();
    }

    public int getBronze() { return bronze; }
    public void setBronze(int bronze) {
        this.bronze = bronze;
        updateTotal();
    }

    public int getTotal() { return total; }
    public void setTotal(int total) {
        // Cho phép set total trực tiếp nếu dữ liệu được đọc từ cột 'Total' trong DB
        // Hoặc nếu giá trị total được tính toán từ bên ngoài và muốn ghi đè.
        // Tuy nhiên, cẩn thận khi dùng vì nó có thể làm mất đồng bộ với gold, silver, bronze.
        // Thông thường, total nên được tính tự động.
        this.total = total;
    }

    // Getters and Setters cho các trường mới
    public int getOlympicEventYear() {
        return olympicEventYear;
    }

    public void setOlympicEventYear(int olympicEventYear) {
        this.olympicEventYear = olympicEventYear;
    }

    public String getOlympicEventType() {
        return olympicEventType;
    }

    public void setOlympicEventType(String olympicEventType) {
        this.olympicEventType = olympicEventType;
    }

    // Phương thức private để cập nhật tổng số huy chương
    private void updateTotal() {
        this.total = this.gold + this.silver + this.bronze;
    }

    @Override
    public String toString() {
        return "MedalEntry{" +
                "id=" + id +
                ", noc='" + noc + '\'' +
                ", gold=" + gold +
                ", silver=" + silver +
                ", bronze=" + bronze +
                ", total=" + total +
                (olympicEventYear > 0 ? ", eventYear=" + olympicEventYear : "") +
                (olympicEventType != null ? ", eventType='" + olympicEventType + '\'' : "") +
                '}';
    }

    // Cân nhắc thêm equals() và hashCode() nếu bạn cần so sánh các đối tượng MedalEntry
    // dựa trên nhiều hơn là chỉ định danh đối tượng trong bộ nhớ, ví dụ:
    // if (medalEntries.contains(someEntry))
    // Đặc biệt nếu các trường year và eventType quan trọng cho việc xác định tính duy nhất.
    // Tuy nhiên, cho mục đích CRUD cơ bản và hiển thị, có thể chưa cần ngay.
}