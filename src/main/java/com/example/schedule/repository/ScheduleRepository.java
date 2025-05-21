package com.example.schedule.repository;

import com.example.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleRepository {

    private final DataSource dataSource;

    public void save(Schedule schedule) {
        String sql = "INSERT INTO schedule (title, content, writer, password, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, schedule.getTitle());
            pstmt.setString(2, schedule.getContent());
            pstmt.setString(3, schedule.getWriter());
            pstmt.setString(4, schedule.getPassword());
            pstmt.setObject(5, schedule.getCreatedAt());
            pstmt.setObject(6, schedule.getModifiedAt());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                schedule.setId(rs.getLong(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("일정 저장 중 오류 발생", e);
        }
    }

    public Schedule findById(Long id) {
        String sql = "SELECT * FROM schedule WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.setId(rs.getLong("id"));
                schedule.setTitle(rs.getString("title"));
                schedule.setContent(rs.getString("content"));
                schedule.setWriter(rs.getString("writer"));
                schedule.setPassword(rs.getString("password"));
                schedule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                schedule.setModifiedAt(rs.getTimestamp("modified_at").toLocalDateTime());
                return schedule;
            } else {
                throw new RuntimeException("해당 ID의 일정이 존재하지 않습니다.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("일정 조회 중 오류 발생", e);
        }
    }

    public List<Schedule> findAll(String writer, LocalDate modifiedAt) {
        StringBuilder sql = new StringBuilder("SELECT * FROM schedule WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (writer != null) {
            sql.append(" AND writer = ?");
            params.add(writer);
        }

        if (modifiedAt != null) {
            sql.append(" AND DATE(modified_at) = ?");
            params.add(modifiedAt);
        }

        List<Schedule> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Schedule schedule = new Schedule();
                schedule.setId(rs.getLong("id"));
                schedule.setTitle(rs.getString("title"));
                schedule.setContent(rs.getString("content"));
                schedule.setWriter(rs.getString("writer"));
                schedule.setPassword(rs.getString("password"));
                schedule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                schedule.setModifiedAt(rs.getTimestamp("modified_at").toLocalDateTime());

                result.add(schedule);
            }

        } catch (SQLException e) {
            throw new RuntimeException("전체 일정 조회 중 오류 발생", e);
        }

        return result;
    }

    public void update(Schedule schedule) {
        String sql = "UPDATE schedule SET title = ?, content = ?, modified_at = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getTitle());
            pstmt.setString(2, schedule.getContent());
            pstmt.setObject(3, schedule.getModifiedAt());
            pstmt.setLong(4, schedule.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("일정 수정 중 오류 발생", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM schedule WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("일정 삭제 중 오류 발생", e);
        }
    }
}