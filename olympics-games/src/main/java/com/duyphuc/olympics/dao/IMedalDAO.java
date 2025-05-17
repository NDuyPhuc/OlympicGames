package com.duyphuc.olympics.dao;

import com.duyphuc.olympics.model.MedalEntry;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IMedalDAO {
    List<MedalEntry> getMedalsByEventTable(String tableName) throws SQLException;
    boolean addMedalEntry(MedalEntry entry, String tableName) throws SQLException;
    boolean updateMedalEntry(MedalEntry entry, String tableName) throws SQLException;
    boolean deleteMedalEntry(int entryId, String tableName) throws SQLException;
    List<String> getNOCsByEventTable(String tableName) throws SQLException;
    void createMedalTable(String tableName, Connection conn) throws SQLException;
    void dropMedalTable(String tableName, Connection conn) throws SQLException;
}