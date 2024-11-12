package de.bt.bw.mvc;

/**
 * Verbindet Sicht und Modell. In der Sicht aktivierte Kommandos werden auf dem Modell
 * ausgeführt, und die Sicht wird aktualisiert, um den neuen Spielstand anzuzeigen.
 * Man kann entweder zufällig würfeln oder die Würfelzahl vorgeben (z.B. wenn man selber
 * mit dem Würfelbecher würfeln möchte oder gezielt Testfälle durchspielen will).
 * 
 * @author Bernhard Westfechtel
 * @version 13.09.2007
 */
public class Kontrolle {
    private Modell modell;
    private Sicht sicht;
    
    /**
     * Konstruktor zur Initialisierung des Spiels. Zu diesem Zweck werden 
     * Sicht und Modell instantiiert und gekoppelt. Nach Ausführung des
     * Konstruktors ist das Spielbrett angezeigt, und das Spiel kann beginnen.
     */
    public Kontrolle(Modell modell) {
        this.modell = modell;
        sicht = new Sicht(this, modell);
    }
    
    
    public void behandleKommando(Kommando kommando) {
    	int wert;
    	switch (kommando) {
    	case EINS: case ZWEI: case DREI: case VIER: case FÜNF: case SECHS:
    		wert = kommando.gibWert();
    	    modell.ziehen(wert); 
    	    break;
    	case AUTOMATISCH:
    		sicht.wechsleModus();
    		break;
    	case WÜRFELN:
    		wert = (int) (Math.random() * 6) + 1;
    		modell.ziehen(wert);
    		break;
    	case FERTIG:
    		System.exit(0);    	
    	}
    		
    }

}
