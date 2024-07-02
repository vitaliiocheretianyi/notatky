package com.itsvitaliio.backend.dao;

import java.util.List;
import com.itsvitaliio.backend.model.Entry;

public interface EntryDao {
    void createEntry(Entry entry);
    Entry getEntryById(Long entryId);
    List<Entry> getAllEntries();
    void updateEntry(Entry entry);
    void deleteEntry(Long entryId);
}