TRUNCATE TABLE manager RESTART IDENTITY CASCADE;

INSERT INTO manager (
    name, cpf, email, phone, is_active
)
VALUES
('Geniéve', '98574307084', 'ger1@bantads.com.br', '(41)92194-2818', TRUE),
('Godophredo', '64065268052', 'ger2@bantads.com.br', '(41)96414-7595', TRUE),
('Gyândula', '23862179060', 'ger3@bantads.com.br', '(41)98094-2121', TRUE),
('Gadamântio', '40501740066', 'ger4@bantads.com.br', '(41)93592-7764', TRUE);