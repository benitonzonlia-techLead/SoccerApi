-- Teams
INSERT INTO team (name, acronym, budget) VALUES
                                             ('Paris Saint-Germain', 'PSG', 800000000),
                                             ('Olympique de Marseille', 'OM', 250000000),
                                             ('Olympique Lyonnais', 'OL', 200000000);

-- Players (en récupérant les team_id dynamiquement)
INSERT INTO player (name, position, team_id) VALUES
                                                 ('Kylian Mbappe', 'FORWARD', (SELECT id FROM team WHERE name = 'Paris Saint-Germain')),
                                                 ('Lionel Messi', 'FORWARD', (SELECT id FROM team WHERE name = 'Paris Saint-Germain')),
                                                 ('Dimitri Payet', 'MIDFIELDER', (SELECT id FROM team WHERE name = 'Olympique de Marseille')),
                                                 ('Alexandre Lacazette', 'DEFENDER', (SELECT id FROM team WHERE name = 'Olympique Lyonnais'));