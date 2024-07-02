package com.itsvitaliio.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itsvitaliio.backend.dao.EntryDao;
import com.itsvitaliio.backend.model.Entry;

import java.util.List;

@Service
public class EntryServiceImpl implements EntryService {

    @Autowired
    private EntryDao entryDao;

    @Override
    public void createEntry(Entry entry) {
        entryDao.createEntry(entry);
    }

    @Override
    public Entry getEntryById(Long entryId) {
        return entryDao.getEntryById(entryId);
    }

    @Override
    public List<Entry> getAllEntries() {
        return entryDao.getAllEntries();
    }

    @Override
    public void updateEntry(Entry entry) {
        entryDao.updateEntry(entry);
    }

    @Override
    public void deleteEntry(Long entryId) {
        entryDao.deleteEntry(entryId);
    }
}
