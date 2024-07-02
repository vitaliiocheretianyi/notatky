package com.itsvitaliio.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.itsvitaliio.backend.model.Entry;

import java.util.List;

@Repository
public class EntryDaoImpl implements EntryDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void createEntry(Entry entry) {
        String sql = "INSERT INTO entries (entry_type, content, url) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, entry.getEntryType(), entry.getContent(), entry.getUrl());
    }

    @Override
    public Entry getEntryById(Long entryId) {
        String sql = "SELECT * FROM entries WHERE entry_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{entryId}, (rs, rowNum) ->
                new Entry(
                        rs.getLong("entry_id"),
                        rs.getString("entry_type"),
                        rs.getString("content"),
                        rs.getString("url")
                ));
    }

    @Override
    public List<Entry> getAllEntries() {
        String sql = "SELECT * FROM entries";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Entry(
                        rs.getLong("entry_id"),
                        rs.getString("entry_type"),
                        rs.getString("content"),
                        rs.getString("url")
                ));
    }

    @Override
    public void updateEntry(Entry entry) {
        String sql = "UPDATE entries SET entry_type = ?, content = ?, url = ? WHERE entry_id = ?";
        jdbcTemplate.update(sql, entry.getEntryType(), entry.getContent(), entry.getUrl(), entry.getEntryId());
    }

    @Override
    public void deleteEntry(Long entryId) {
        String sql = "DELETE FROM entries WHERE entry_id = ?";
        jdbcTemplate.update(sql, entryId);
    }
}
