package de.bt.bw.mvc;

public interface Subjekt {
	void registriere(Beobachter beobachter);
	void deregistriere(Beobachter beobachter);
	void benachrichtige();
}
