package io.pivotal.pal.tracker;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository("jdbcDb")
public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ResultSetExtractor<TimeEntry> timeEntryExtractor = rs -> {
        if (!rs.next()) return null;
        else {
            return TimeEntry.builder()
                    .id(rs.getLong(1))
                    .projectId(rs.getLong(2))
                    .userId(rs.getLong(3))
                    .date(rs.getDate(4).toLocalDate())
                    .hours(rs.getInt(5))
                    .build();
        }
    };

    private final String CREATE_SQL = "INSERT INTO time_entries (project_id, user_id, date, hours) VALUES (?, ?, ?, ?)";
    private final String FIND_SQL = "SELECT * FROM time_entries WHERE id = ?";
    private final String FIND_ALL_SQL = "SELECT * FROM time_entries";
    private final String UPDATE_SQL = "UPDATE time_entries SET project_id=?, user_id=?, date=?, hours=? WHERE id=?";
    private final String DELETE_SQL = "DELETE FROM time_entries WHERE id=?";

    public JdbcTimeEntryRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, timeEntry.getProjectId());
            ps.setLong(2, timeEntry.getUserId());
            ps.setDate(3, Date.valueOf(timeEntry.getDate()));
            ps.setInt(4, timeEntry.getHours());

            return ps;
        }, keyHolder);

        final long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return find(id);
    }

    @Override
    public TimeEntry find(long id) {
        return jdbcTemplate.query(connection -> {
            final PreparedStatement ps = connection.prepareStatement(FIND_SQL);
            ps.setLong(1, id);
            return ps;
        }, timeEntryExtractor);
    }

    @Override
    public List<TimeEntry> list() {
        final RowMapper<TimeEntry> timeEntryRowMapper = (rs, rowNum) -> TimeEntry.builder()
                .id(rs.getLong(1))
                .projectId(rs.getLong(2))
                .userId(rs.getLong(3))
                .date(rs.getDate(4).toLocalDate())
                .hours(rs.getInt(5))
                .build();

        return jdbcTemplate.query(connection -> connection.prepareStatement(FIND_ALL_SQL), timeEntryRowMapper);
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
            ps.setLong(1, timeEntry.getProjectId());
            ps.setLong(2, timeEntry.getUserId());
            ps.setDate(3, Date.valueOf(timeEntry.getDate()));
            ps.setInt(4, timeEntry.getHours());
            ps.setLong(5, id);

            return ps;
        });

        return timeEntry.toBuilder()
                .id(id)
                .build();
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(DELETE_SQL);
            ps.setLong(1, id);
            return ps;
        });
    }
}
