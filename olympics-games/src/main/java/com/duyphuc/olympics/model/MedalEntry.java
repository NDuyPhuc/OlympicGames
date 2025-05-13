package com.duyphuc.olympics.model;

public class MedalEntry {
    private int id; // Khóa chính mới
    private String noc;
    private int gold;
    private int silver;
    private int bronze;
    private int total;

    public MedalEntry() {} // Default constructor for JavaFX

    public MedalEntry(int id, String noc, int gold, int silver, int bronze) {
        this.id = id;
        this.noc = noc;
        this.gold = gold;
        this.silver = silver;
        this.bronze = bronze;
        this.total = gold + silver + bronze; // Tự động tính tổng
    }
    
    public MedalEntry(String noc, int gold, int silver, int bronze) { // Constructor without ID for new entries
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
    public void setGold(int gold) { this.gold = gold; updateTotal(); }
    public int getSilver() { return silver; }
    public void setSilver(int silver) { this.silver = silver; updateTotal(); }
    public int getBronze() { return bronze; }
    public void setBronze(int bronze) { this.bronze = bronze; updateTotal(); }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; } // Allow setting total if read from DB directly

    private void updateTotal() {
        this.total = this.gold + this.silver + this.bronze;
    }
}