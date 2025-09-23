-- Lieferant
CREATE TABLE lieferant (
    lieferant_id	UUID NOT NULL DEFAULT gen_random_uuid(),
    name    		VARCHAR(50),
    adresse         VARCHAR(50),
    plz         	CHAR(4),
    ort            	VARCHAR(50),
    mail           	VARCHAR(50),

    CONSTRAINT pk_lieferant_lieferant_id PRIMARY KEY (lieferant_id)
);

-- Lieferantennummer
CREATE TABLE lieferantennummer (
   lieferant_nr		INTEGER NOT NULL,

   CONSTRAINT pk_lieferantennummer_lieferant_nr PRIMARY KEY (lieferant_nr)
);

-- Zuweisung
CREATE TABLE zuweisung (
   zuweisung_id   	UUID NOT NULL DEFAULT gen_random_uuid(),
   lieferant_nr   	INTEGER NOT NULL,
   lieferant_id   	UUID NOT NULL,
   zugewiesen_ab 	DATE NOT NULL,
   zugewiesen_bis 	DATE,

   CONSTRAINT pk_zuweisung_zuweisung_id PRIMARY KEY (zuweisung_id),
   CONSTRAINT fk_zuweisung_lieferant_nr FOREIGN KEY (lieferant_nr) REFERENCES lieferantennummer (lieferant_nr),
   CONSTRAINT fk_zuweisung_lieferant_id FOREIGN KEY (lieferant_id) REFERENCES lieferant (lieferant_id)
);

-- Milchlieferung
CREATE TABLE milchlieferung (
    lieferung_id	UUID NOT NULL DEFAULT gen_random_uuid(),
    lieferant_nr	INTEGER NOT NULL,
    datum			DATE NOT NULL,
    zeitfenster     VARCHAR(10) NOT NULL,
    menge_kg       	DECIMAL(10,2),

    CONSTRAINT pk_milchlieferung_lieferung_id PRIMARY KEY (lieferung_id),
    CONSTRAINT fk_milchlieferung_lieferant_nr FOREIGN KEY (lieferant_nr) REFERENCES lieferantennummer (lieferant_nr),
    CONSTRAINT ck_milchlieferung_menge CHECK (menge_kg >= 0),
    CONSTRAINT ck_milchlieferung_zeitfenster CHECK (zeitfenster IN ('MORGEN', 'ABEND'))
);