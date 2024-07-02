package com.itsvitaliio.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.itsvitaliio.backend.model.Entry;
import com.itsvitaliio.backend.service.EntryService;
import java.util.List;

@RestController
@RequestMapping("/entries")
public class EntryController {

    @Autowired
    private EntryService entryService;

    @PostMapping
    public void createEntry(@RequestBody Entry entry) {
        entryService.createEntry(entry);
    }

    @GetMapping("/{id}")
    public Entry getEntryById(@PathVariable Long id) {
        return entryService.getEntryById(id);
    }

    @GetMapping
    public List<Entry> getAllEntries() {
        return entryService.getAllEntries();
    }

    @PutMapping("/{id}")
    public void updateEntry(@PathVariable Long id, @RequestBody Entry entry) {
        entry.setEntryId(id);
        entryService.updateEntry(entry);
    }

    @DeleteMapping("/{id}")
    public void deleteEntry(@PathVariable Long id) {
        entryService.deleteEntry(id);
    }
}
