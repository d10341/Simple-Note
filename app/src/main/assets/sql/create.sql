-- Create table for NOTES
CREATE
	TABLE notes
	(
		creation INTEGER PRIMARY KEY,
		last_modification INTEGER,
		title TEXT,
		content TEXT,
		archived INTEGER,
		trashed INTEGER,
		alarm INTEGER DEFAULT null,
        reminder_fired INTEGER,
		recurrence_rule TEXT,
		latitude REAL,
		longitude REAL,
		address TEXT,
		category_id INTEGER DEFAULT null,
		locked INTEGER,  
		checklist  INTEGER
	);
	


-- Create table for ATTACHMENTS
CREATE
	TABLE attachments
	(
		attachment_id INTEGER PRIMARY KEY AUTOINCREMENT,
		uri TEXT,
		name TEXT,
		size INTEGER,
		length INTEGER,
		mime_type TEXT,
		note_id INTEGER
	);
	
