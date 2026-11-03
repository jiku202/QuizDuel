-- QuizDuel schema (follows the project ERD). Safe to run on every start.

CREATE TABLE IF NOT EXISTS users (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(120) NOT NULL,
  email          VARCHAR(190) NOT NULL UNIQUE,
  password_hash  VARCHAR(100) NOT NULL,
  role           VARCHAR(10)  NOT NULL CHECK (role IN ('teacher','student')),
  school         VARCHAR(160),
  total_points   INT NOT NULL DEFAULT 0,
  streak         INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sections (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  teacher_id       BIGINT NOT NULL,
  name             VARCHAR(120) NOT NULL,
  grade_level      VARCHAR(40)  NOT NULL DEFAULT 'Grade 3',
  subject          VARCHAR(80)  NOT NULL DEFAULT 'Araling Panlipunan',
  join_code        VARCHAR(12)  NOT NULL UNIQUE,
  battle_time      VARCHAR(5)   NOT NULL DEFAULT '15:00',
  weekend_battles  BOOLEAN NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS enrollments (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id  BIGINT NOT NULL,
  section_id  BIGINT NOT NULL,
  joined_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (student_id, section_id),
  FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS questions (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  section_id      BIGINT NOT NULL,
  topic           VARCHAR(120) NOT NULL,
  text            VARCHAR(1000) NOT NULL,
  choice_a        VARCHAR(300) NOT NULL,
  choice_b        VARCHAR(300) NOT NULL,
  choice_c        VARCHAR(300) NOT NULL,
  choice_d        VARCHAR(300) NOT NULL,
  correct_answer  VARCHAR(1) NOT NULL CHECK (correct_answer IN ('A','B','C','D')),
  difficulty      VARCHAR(10) NOT NULL DEFAULT 'Easy' CHECK (difficulty IN ('Easy','Medium','Hard')),
  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS battles (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  section_id     BIGINT NOT NULL,
  player1_id     BIGINT NOT NULL,
  player2_id     BIGINT,                      -- NULL = Practice Bot
  battle_date    VARCHAR(10) NOT NULL,        -- YYYY-MM-DD
  topic          VARCHAR(120),
  player1_score  INT NOT NULL DEFAULT 0,
  player2_score  INT NOT NULL DEFAULT 0,
  result         VARCHAR(5) CHECK (result IN ('p1','p2','draw')),
  winner_id      BIGINT,
  status         VARCHAR(12) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','in_progress','completed')),
  FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
  FOREIGN KEY (player1_id) REFERENCES users(id),
  FOREIGN KEY (player2_id) REFERENCES users(id),
  FOREIGN KEY (winner_id)  REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS battle_questions (
  battle_id    BIGINT NOT NULL,
  question_id  BIGINT NOT NULL,
  seq          INT NOT NULL,
  PRIMARY KEY (battle_id, question_id),
  FOREIGN KEY (battle_id)   REFERENCES battles(id)   ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS battle_answers (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  battle_id        BIGINT NOT NULL,
  student_id       BIGINT NOT NULL,
  question_id      BIGINT NOT NULL,
  selected_answer  VARCHAR(1),
  is_correct       INT NOT NULL DEFAULT 0,
  time_taken       DOUBLE NOT NULL DEFAULT 0,
  UNIQUE (battle_id, student_id, question_id),
  FOREIGN KEY (battle_id)   REFERENCES battles(id)   ON DELETE CASCADE,
  FOREIGN KEY (student_id)  REFERENCES users(id),
  FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS badges (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  name              VARCHAR(80) NOT NULL UNIQUE,
  description       VARCHAR(255),
  points_threshold  INT                         -- NULL = manual/custom badge
);

CREATE TABLE IF NOT EXISTS user_badges (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT NOT NULL,
  badge_id     BIGINT NOT NULL,
  date_earned  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, badge_id),
  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
  FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE
);
