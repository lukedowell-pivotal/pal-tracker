package io.pivotal.pal.tracker;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("memoryDb")
public class InMemoryTimeEntryRepository implements TimeEntryRepository {

    private long count;
    private final Map<Long, TimeEntry> entries;

    public InMemoryTimeEntryRepository() {
        count = 0L;
        this.entries = new HashMap<>();
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        count++;
        if (timeEntry.getId() == 0) {
            timeEntry.setId(count);
        }
        entries.put(timeEntry.getId(), timeEntry);
        return timeEntry;
    }

    @Override
    public TimeEntry find(long id) {
        return entries.get(id);
    }

    @Override
    public List<TimeEntry> list() {
        return new ArrayList<>(entries.values());
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        if (timeEntry.getId() == 0) {
            timeEntry.setId(id);
        }

        return entries.replace(id, timeEntry) != null ? timeEntry : null;
    }

    @Override
    public void delete(long id) {
        entries.remove(id);
    }
}
