TRUNCATE TABLE event RESTART IDENTITY CASCADE;

INSERT INTO event (
    object_id,
    version,
    type,
    payload,
    created_at
)
VALUES
(
    '1291',
    1,
    'criado',
    '{"nome":"Catharyna","cpf":"12912861012","manager_cpf":"98574307084"}',
    '2000-01-01 00:00:00'
),
(
    '1291',
    2,
    'deposito',
    '{"nome":"Catharyna","cpf":"12912861012","valor":"1000.00"}',
    '2020-01-01 10:00:00'
),
(
    '1291',
    3,
    'deposito',
    '{"nome":"Catharyna","cpf":"12912861012","valor":"900.00"}',
    '2020-01-01 11:00:00'
),
(
    '1291',
    4,
    'saque',
    '{"nome":"Catharyna","cpf":"12912861012","valor":"550.00"}',
    '2020-01-01 12:00:00'
),
(
    '1291',
    5,
    'saque',
    '{"nome":"Catharyna","cpf":"12912861012","valor":"350.00"}',
    '2020-01-01 13:00:00'
),
(
    '1291',
    6,
    'deposito',
    '{"nome":"Catharyna","cpf":"12912861012","valor":"2000.00"}',
    '2020-01-10 15:00:00'
),
(
    '1291',
    7,
    'saque',
    '{"nome":"Catharyna","cpf":"12912861012","valor":"500.00"}',
    '2020-01-15 08:00:00'
),
(
    '1291',
    8,
    'transferencia_origem',
    '{"destino": { "conta": "0950", "nome": "Cleuddônio", "cpf": "09506382000" }, "origem": { "nome": "Catharyna", "valor": "1700.00"}}',
    '2020-01-20 12:00:00'
),
(
    '0950',
    1,
    'criado',
    '{"nome":"Cleuddônio","cpf":"09506382000","manager_cpf":"64065268052"}',
    '1990-10-10 00:00:00'
),
(
    '0950',
    2,
    'transferencia_destino',
    '{"destino": { "conta": "0950", "nome": "Cleuddônio", "cpf": "09506382000" }, "origem": { "nome": "Catharyna", "valor": "1700.00"}}',
    '2020-01-20 12:00:00'
),
(
    '0950',
    3,
    'deposito',
    '{"nome":"Cleuddônio","cpf":"09506382000","valor":"1000.00"}',
    '2025-01-01 12:00:00'
),
(
    '0950',
    4,
    'deposito',
    '{"nome":"Cleuddônio","cpf":"09506382000","valor":"5000.00"}',
    '2025-01-02 10:00:00'
),
(
    '0950',
    5,
    'saque',
    '{"nome":"Cleuddônio","cpf":"09506382000","valor":"200.00"}',
    '2025-01-10 10:00:00'
),
(
    '0950',
    6,
    'deposito',
    '{"nome":"Cleuddônio","cpf":"09506382000","valor":"7000.00"}',
    '2025-02-05 10:00:00'
),
(
    '0950',
    7,
    'saque',
    '{"nome":"Cleuddônio","cpf":"09506382000","valor":"4500.00"}',
    '2025-03-06 11:00:00'
),
(
    '8573',
    1,
    'criado',
    '{"nome":"Catianna","cpf":"85733854057","manager_cpf":"23862179060"}',
    '2012-12-12 00:00:00'
),
(
   '8573',
    2,
    'deposito',
    '{"nome":"Catianna","cpf":"85733854057","valor":"1000.00"}',
    '2025-05-05 10:00:00'
),
(
   '8573',
    3,
    'saque',
    '{"nome":"Catianna","cpf":"85733854057","valor":"800.00"}',
    '2025-05-06 10:00:00'
),
(
    '5887',
    1,
    'criado',
    '{"nome":"Cutardo","cpf":"58872160006","manager_cpf":"98574307084"}',
    '2022-02-22 00:00:00'
),
(
    '5887',
    2,
    'deposito',
    '{"nome":"Cutardo","cpf":"58872160006","valor":"150000.00"}',
    '2025-06-01 10:00:00'
),
(
    '7617',
    1,
    'criado',
    '{"nome":"Coândrya","cpf":"76179646090","manager_cpf":"64065268052"}',
    '2025-01-01 00:00:00'
),
(
    '7617',
    2,
    'deposito',
    '{"nome":"Coândrya","cpf":"76179646090","valor":"1500.00"}',
    '2025-07-01 10:00:00'
);