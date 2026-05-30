-- 1. Create Core Tables
CREATE TABLE users (
                       id UUID NOT NULL,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       external_id VARCHAR(255) UNIQUE,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
                       created_at TIMESTAMP(6) NOT NULL,
                       modified_at TIMESTAMP(6) ,
                       PRIMARY KEY (id)
);

CREATE TABLE events (
                        id UUID NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        location VARCHAR(255)  ,
                        description VARCHAR(4000) ,
                        from_date_time TIMESTAMP(6) NOT NULL,
                        to_date_time TIMESTAMP(6) NOT NULL,
                        featured_image BYTEA,
                        created_at TIMESTAMP(6) NOT NULL,
                        modified_at TIMESTAMP(6) ,
                        created_by_id UUID,
                        modified_by_id UUID,
                        PRIMARY KEY (id)
);

CREATE TABLE comments (
                          id UUID NOT NULL,
                          event_id UUID NOT NULL,
                          content VARCHAR(4000) NOT NULL,
                          created_at TIMESTAMP(6) NOT NULL,
                          modified_at TIMESTAMP(6) ,
                          created_by_id UUID,
                          modified_by_id UUID,
                          PRIMARY KEY (id)
);

CREATE TABLE notes (
                       id UUID NOT NULL,
                       content VARCHAR(4000)  ,
                       created_at TIMESTAMP(6) NOT NULL,
                       modified_at TIMESTAMP(6) ,
                       created_by_id UUID,
                       modified_by_id UUID,
                       PRIMARY KEY (id)
);

-- 2. Define Foreign Key Constraints
ALTER TABLE comments ADD CONSTRAINT FK_comments_event FOREIGN KEY (event_id) REFERENCES events(id);
ALTER TABLE comments ADD CONSTRAINT FK_comments_created_by FOREIGN KEY (created_by_id) REFERENCES users(id);
ALTER TABLE comments ADD CONSTRAINT FK_comments_modified_by FOREIGN KEY (modified_by_id) REFERENCES users(id);

ALTER TABLE events ADD CONSTRAINT FK_events_created_by FOREIGN KEY (created_by_id) REFERENCES users(id);
ALTER TABLE events ADD CONSTRAINT FK_events_modified_by FOREIGN KEY (modified_by_id) REFERENCES users(id);

ALTER TABLE notes ADD CONSTRAINT FK_notes_created_by FOREIGN KEY (created_by_id) REFERENCES users(id);
ALTER TABLE notes ADD CONSTRAINT FK_notes_modified_by FOREIGN KEY (modified_by_id) REFERENCES users(id);
