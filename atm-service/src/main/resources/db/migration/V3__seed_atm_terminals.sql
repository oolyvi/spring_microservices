INSERT INTO atm_terminal
(terminal_code, location, status, ip_address, software_version,
 cassette_1_count, cassette_5_count, cassette_10_count, cassette_20_count,
 cassette_50_count, cassette_100_count, cassette_200_count, cassette_500_count)
VALUES ('ATM-BAKU-01', 'Nizami Street 24, Baku', 'ACTIVE', '192.168.1.101', 'v2.1.0', 100, 100, 200, 200, 100, 50, 20,
        10),
       ('ATM-BAKU-02', '28 May Mall, Baku', 'ACTIVE', '192.168.1.102', 'v2.1.0', 50, 50, 100, 100, 50,
        25, 10, 5),
       ('ATM-GANJA-01', 'Javad Khan Street, Ganja', 'MAINTENANCE', '192.168.2.50', 'v2.0.5', 0, 0, 0, 0, 0, 0, 0, 0);