package com.quizduel.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizduel.util.Jdbc;

/**
 * Default badges (always) and demo data (first start only), matching the frontend mock content.
 *   Teacher:  teacher@school.edu.ph / password123      Section join code: MABINI
 *   Students: ana@school.edu.ph, miguel@..., ella@..., liza@..., paolo@..., carlo@..., nina@..., joy@... / password123
 */
@Service
public class DemoDataService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final BattleService battles;

    public DemoDataService(JdbcTemplate jdbc, PasswordEncoder encoder, BattleService battles) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.battles = battles;
    }

    @Transactional
    public void ensureDefaultBadges() {
        Object[][] defaults = {
                {"Rookie Scholar", "Earned 50 points", 50},
                {"Bayani Badge", "Earned 150 points", 150},
                {"History Buff", "Earned 300 points", 300},
                {"Kasaysayan Master", "Earned 500 points", 500},
        };
        for (Object[] b : defaults) {
            Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM badges WHERE name = ?", Integer.class, b[0]);
            if (exists == null || exists == 0) {
                jdbc.update("INSERT INTO badges (name, description, points_threshold) VALUES (?, ?, ?)", b[0], b[1], b[2]);
            }
        }
    }

    @Transactional
    public boolean seedDemoData() {
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, "teacher@school.edu.ph");
        if (exists != null && exists > 0) return false;

        String hash = encoder.encode("password123");
        long teacherId = Jdbc.insertReturningId(jdbc,
                "INSERT INTO users (name, email, password_hash, role, school) VALUES (?, ?, ?, 'teacher', ?)",
                "Mrs. Santos", "teacher@school.edu.ph", hash, "Mabini Elementary School");
        long sectionId = Jdbc.insertReturningId(jdbc,
                "INSERT INTO sections (teacher_id, name, grade_level, subject, join_code) VALUES (?, ?, 'Grade 3', 'Araling Panlipunan', ?)",
                teacherId, "Grade 3 - Mabini", "MABINI");

        Object[][] students = {
                {"Ana Cruz", "ana", 480}, {"Miguel Reyes", "miguel", 420}, {"Ella Torres", "ella", 320},
                {"Liza Santos", "liza", 300}, {"Paolo Diaz", "paolo", 260}, {"Carlo Ramos", "carlo", 120},
                {"Nina Bautista", "nina", 80}, {"Joy Lim", "joy", 40},
        };
        for (Object[] s : students) {
            long id = Jdbc.insertReturningId(jdbc,
                    "INSERT INTO users (name, email, password_hash, role, school, total_points) VALUES (?, ?, ?, 'student', 'Mabini Elementary School', ?)",
                    s[0], s[1] + "@school.edu.ph", hash, s[2]);
            jdbc.update("INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)", id, sectionId);
            battles.awardEarnedBadges(id);
        }

        String[][] questions = {
                {"Filipino Heroes", "Who is the national hero of the Philippines?", "Jose Rizal", "Andres Bonifacio", "Emilio Aguinaldo", "Antonio Luna", "A", "Easy"},
                {"Filipino Heroes", "In which province was Andres Bonifacio born?", "Cavite", "Manila", "Bulacan", "Batangas", "B", "Hard"},
                {"Filipino Heroes", "Who wrote the novel Noli Me Tangere?", "Jose Rizal", "Marcelo del Pilar", "Graciano Lopez Jaena", "Apolinario Mabini", "A", "Easy"},
                {"Filipino Heroes", "Who is known as the Sublime Paralytic?", "Andres Bonifacio", "Apolinario Mabini", "Juan Luna", "Gregorio del Pilar", "B", "Medium"},
                {"Spanish Colonization", "What year did Ferdinand Magellan arrive in the Philippines?", "1492", "1521", "1565", "1898", "B", "Medium"},
                {"Spanish Colonization", "Who was the first Spanish Governor-General of the Philippines?", "Miguel Lopez de Legazpi", "Ferdinand Magellan", "Ruy Lopez de Villalobos", "Juan de Salcedo", "A", "Medium"},
                {"National Symbols", "What is the national flower of the Philippines?", "Rose", "Sampaguita", "Orchid", "Sunflower", "B", "Easy"},
                {"National Symbols", "What color is shown on top of the flag when the country is at war?", "Blue", "White", "Red", "Yellow", "C", "Medium"},
                {"National Symbols", "What is the national bird of the Philippines?", "Maya", "Tarsier", "Philippine Eagle", "Parrot", "C", "Easy"},
                {"EDSA Revolution", "Who became the first female president of the Philippines?", "Gloria Arroyo", "Corazon Aquino", "Imelda Marcos", "Leni Robredo", "B", "Easy"},
                {"EDSA Revolution", "In what year did the EDSA People Power Revolution happen?", "1972", "1983", "1986", "1998", "C", "Medium"},
        };
        for (String[] q : questions) {
            jdbc.update("INSERT INTO questions (section_id, topic, text, choice_a, choice_b, choice_c, choice_d, correct_answer, difficulty) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", sectionId, q[0], q[1], q[2], q[3], q[4], q[5], q[6], q[7]);
        }
        return true;
    }
}
