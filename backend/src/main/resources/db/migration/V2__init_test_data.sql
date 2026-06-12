-- ==========================================
-- 1. INSERT USERS
-- ==========================================

-- Admin User
INSERT INTO users (id, username, external_id, role, created_at, modified_at)
VALUES (
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'alice_admin',
           'entra-id-99991',
           'ADMIN',
           '2026-01-15 08:30:00.000000',
           '2026-01-15 08:30:00.000000'
       );

-- Standard User 1
INSERT INTO users (id, username, external_id, role, created_at, modified_at)
VALUES (
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
           'bob_jones',
           'entra-id-99992',
           'USER',
           '2026-02-20 14:15:22.000000',
           '2026-02-21 09:10:00.000000'
       );

-- Standard User 2
INSERT INTO users (id, username, external_id, role, created_at, modified_at)
VALUES (
           'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
           'charlie_brown',
           'entra-id-99993',
           'USER',
           '2026-03-01 10:00:00.000000',
           '2026-03-01 12:00:00.000000'
       );


-- ==========================================
-- 2. INSERT EVENTS
-- ==========================================

-- Event 1: Tech Conference
INSERT INTO events (id, title, location, description, from_date_time, to_date_time, featured_image, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
           'Annual Tech Innovation Summit',
           'Convention Center, Hall A',
           'A three-day summit exploring the future of AI, cloud architecture, and open-source ecosystems.',
           '2026-09-10 09:00:00.000000',
           '2026-09-12 17:00:00.000000',
           NULL, -- Left as NULL so both H2 and Postgres process the binary type identically
           '2026-03-15 11:00:00.000000',
           '2026-03-16 14:25:00.000000',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );

-- Event 2: Team Building
INSERT INTO events (id, title, location, description, from_date_time, to_date_time, featured_image, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           'f2a3b4c5-d6e7-8f9a-0b1c-2d3e4f5a6b7c',
           'Summer Team Barbecue',
           'Riverside Park, Pavilion 3',
           'Casual get-together for team building, food, and outdoor games. Families welcome!',
           '2026-07-18 12:00:00.000000',
           '2026-07-18 18:00:00.000000',
           NULL,
           '2026-04-10 16:45:10.000000',
           '2026-04-10 16:45:10.000000',
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e'
       );

INSERT INTO events (id, title, location, description, from_date_time, to_date_time, featured_image, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           '33333333-3333-4333-8333-333333333333',
           'Global Autumn Hackathon',
           'Tech Hub, Room 404',
           'A 48-hour challenge to build open-source tools that solve real-world climate issues.',
           '2026-10-23 18:00:00.000000',
           '2026-10-25 18:00:00.000000',
           NULL,
           '2026-05-01 09:00:00.000000',
           '2026-05-01 09:00:00.000000',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );

-- Event 4: Executive Leadership Roundtable
INSERT INTO events (id, title, location, description, from_date_time, to_date_time, featured_image, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           '44444444-4444-4444-8444-444444444444',
           'Executive Strategy Roundtable',
           'Grand Hotel, Boardroom B',
           'Quarterly alignment meeting for executives to finalize the product roadmap and budgets.',
           '2026-11-05 10:00:00.000000',
           '2026-11-05 15:00:00.000000',
           NULL,
           '2026-05-12 14:30:00.000000',
           '2026-05-13 11:15:00.000000',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );

-- Event 5: Community Charity Run
INSERT INTO events (id, title, location, description, from_date_time, to_date_time, featured_image, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           '55555555-5555-4555-8555-555555555555',
           'Annual Charity 5K Run',
           'City Park, North Entrance',
           'Join our annual community run. All registration proceeds go directly to local food banks.',
           '2026-09-27 07:30:00.000000',
           '2026-09-27 12:00:00.000000',
           NULL,
           '2026-06-01 08:00:00.000000',
           '2026-06-02 09:45:00.000000',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );


-- ==========================================
-- 3. INSERT COMMENTS
-- ==========================================

-- Comment on Tech Conference
INSERT INTO comments (id, event_id, content, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           '1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d',
           'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
           'Will there be remote streaming options for international teams?',
           '2026-03-20 09:15:00.000000',
           '2026-03-20 09:15:00.000000',
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
           'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e'
       );

-- Comment response from Admin
INSERT INTO comments (id, event_id, content, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           '2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e',
           'e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b',
           'Yes, we will provide a live link closer to the event date.',
           '2026-03-20 10:30:12.000000',
           '2026-03-20 10:35:00.000000',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );


-- ==========================================
-- 4. INSERT PERSONAL NOTES
-- ==========================================

INSERT INTO notes (id, content, created_at, modified_at, created_by_id, modified_by_id)
VALUES (
           '9f8e7d6c-5b4a-3f2e-1d0c-9b8a7f6e5d4c',
           'Review Q3 event budgets before the next board meeting.',
           '2026-01-20 17:00:00.000000',
           '2026-01-20 17:00:00.000000',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
           'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
       );
