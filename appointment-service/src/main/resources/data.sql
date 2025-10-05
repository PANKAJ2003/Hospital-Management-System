-- Doctor 2 (Dr. Mehta)
INSERT INTO time_slots (slot_id, doctor_id, date, start_time, end_time, status)
VALUES
    ('44444444-4444-4444-4444-444444444444', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2025-10-05', '09:00:00', '09:30:00', 'AVAILABLE'),
    ('55555555-5555-5555-5555-555555555555', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2025-10-05', '09:30:00', '10:00:00', 'UNAVAILABLE'),
    ('66666666-6666-6666-6666-666666666666', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2025-10-06', '10:00:00', '10:30:00', 'AVAILABLE');

-- Doctor 3 (Dr. Verma)
INSERT INTO time_slots (slot_id, doctor_id, date, start_time, end_time, status)
VALUES
    ('77777777-7777-7777-7777-777777777777', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '2025-10-05', '11:00:00', '11:30:00', 'AVAILABLE'),
    ('88888888-8888-8888-8888-888888888888', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '2025-10-05', '11:30:00', '12:00:00', 'UNAVAILABLE'),
    ('99999999-9999-9999-9999-999999999999', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '2025-10-06', '09:00:00', '09:30:00', 'AVAILABLE');
