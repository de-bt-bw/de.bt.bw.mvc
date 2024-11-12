/**
 * 
 */
package de.bt.bw.mvc;

import java.util.ArrayList;

/**
 * Modell für Mensch ärgere dich nicht.
 * Das Modell ist generisch implementiert, d.h.
 * die Zahl der Farben, der Steine etc. ist durch
 * Konstanten festgelegt.
 * 
 * @author Bernhard Westfechtel 
 * @version 12.09.2007
 */
public class Modell {
    /**
     * Zahl der Farben
     */
    public static final int farben = 4;
    
    public static final int rot = 0;
    public static final int blau = rot + 1;
    public static final int gruen = blau + 1;
    public static final int gelb = gruen + 1;
    
    /**
     * Zahl der Steine pro Farbe
     * (und damit der Start- und Zielfelder)
     */
    public static final int steine = 4;
    
    /**
     * Maximale Augenzahl beim Würfeln
     */
    public static final int maxAugen = 6;
    
    /**
     * Zahl der Schlagfelder pro Farbe
     */
    public static final int delta = 10;
    
    /**
     * Zahl der Schlagfelder insgesamt
     */
    public static final int spielfelder = farben * delta; 
    // Nur gemeinsame Spielfelder, ohne die Häuser!
    
    private int[][] positionen = new int[farben][steine];
    

    
    /** 
     * Liefert für jeden Stein jeder Farbe die aktuelle Position
     * mit folgender Interpretation: <br>
     * pos = 0: Stein ist draußen (Startfeld) <br>
     * 1 &lt;= pos &lt;= spielfelder: relative Position auf dem Spielfeld ab dem Startfeld der jeweiligen Farbe <br>
     * spielfelder &lt; pos &lt;= spielfelder + 4: Position des Steins auf einem Zielfeld
     */
    public int position(int farbe, int stein) {
        return positionen[farbe][stein];
    }
    
    private int aktiv = rot; // Wer ist am Zug?
    
    /** 
     * Liefert den Spieler (d.h. die Farbe) zurück, die am Zug ist.
     */
    public int aktiverSpieler() {
        return aktiv;
    }
    
    private int wurf; // Letzter Wurf
    
    /**
     * Liefert die Zahl des letzten Wurfs zurück
     */
    public int letzterWurf() {
        return wurf;
    }
    
    /**
     * Liefert die absolute Startposition auf dem Schlagfeld zurück
     * (d.h. die Nummer des Feldes, die ein Stein dieser Farbe beim
     * Herausziehen besetzt).
     */
    public int startPos(int farbe) // rot <= farbe <= gelb 
    {
        return farbe * delta + 1;  
    }
    
    /**
     * Rechnet die relative Position eines Steins dieser Farbe,
     * der sich auf einem Schlagfeld befindet,
     * in eine absolute Position um. Es muss gelten:
     * 1 <= relPos <= spielfelder
     */
    public int absPos(int relPos, int farbe)
    {
        return (relPos - 1 + startPos(farbe) - 1) % spielfelder + 1;
    }
    
    /**
     * Stellt fest, ob bereits alle Steine dieser Farbe das 
     * Zielfeld erreicht haben.
     */
    public boolean spielerFertig(int farbe) // rot <= farbe <= gelb
    {
        for (int stein = 0; stein < steine; stein++)
            if (positionen[farbe][stein] <= spielfelder)
                return false;
        return true;
    }
    
    /**
     * Stellt fest, ob bereits alle Spieler fertig sind. Der
     * letzte Spieler muss bis zum bitteren Ende würfeln.
     */
    public boolean spielFertig()
    {
        // Der letzte Spieler muss bis zum bitteren Ende würfeln
        for (int farbe = rot; farbe <= gelb; farbe++)
            if (!spielerFertig(farbe))
                return false;
        return true;
    }
    
    private void naechsterSpieler ()
    {
        if (spielFertig()) return;
        if ((wurf == maxAugen) && !spielerFertig(aktiv))
            return; // Spieler darf noch einmal werfen
        // Nächsten Spieler suchen
        int naechsteFarbe = (aktiv + 1) % farben;
        while (spielerFertig(naechsteFarbe))
            naechsteFarbe = (naechsteFarbe + 1) % farben;
        aktiv = naechsteFarbe;
    }
    
    /**
     * Der aktuelle Spieler macht einen Zug mit der 
     * vorgegebenen Augenzahl.
     */
    public void ziehen(int wurf)
    {
        if (spielFertig()) return;
        this.wurf = wurf;
        if (!herausziehen()) weiterziehen();
        naechsterSpieler();
        benachrichtige();
    }
    
    private boolean herausziehen()
    {
        if (wurf != maxAugen) return false;
        boolean gefunden = false;
        int stein;
        for (stein = 0; stein < steine; stein++)
            if (positionen[aktiv][stein] == 0) {
                gefunden = true;
                break;
            }
        if ((!gefunden) || blockiert(1)) return false;
        positionen[aktiv][stein] = 1;
        schlagen(1); // Ein Stein einer anderen Farbe ist ggf. zu entfernen
        return true;
    }
    
    private void weiterziehen()
    {
        /* Beim Weiterziehen versucht man als erstes, das Startfeld zu
         * räumen, falls es belegt ist. Sonst sucht man nach dem ersten
         * Stein, der ziehen kann. 
         */
        if (!freimachen()) {
            for (int stein = 0; stein < steine; stein++) {
                int altePos = positionen[aktiv][stein];
                if (altePos > 0) {
                    int neuePos = altePos + wurf;
                    if ((neuePos <= spielfelder + steine) && !blockiert(neuePos)) {
                        positionen[aktiv][stein] = neuePos;
                        if (neuePos <= spielfelder) schlagen(neuePos);
                        return;
                    }
                }
            }
        }
    }
    
    private boolean freimachen()
    {
        // Falls erstes Feld belegt, versuche dies freizumachen.
        for (int stein = 0; stein < steine; stein++) {
            int altePos = positionen[aktiv][stein];
            int neuePos = altePos + wurf;
            if ((altePos == 1) && !blockiert(neuePos)) {
                positionen[aktiv][stein] = neuePos;
                schlagen(neuePos);
                return true;
            }
        }
        return false;
    }
    
    private void schlagen(int relPos)
    {
        /* Geschlagen wird durch die aktive Farbe. relPos bezeichnet
         * die relative Zielposition des bereits platzierten Steins
         * der aktiven Farbe. Ein ggf. vorhandener Stein einer anderen
         * Farbe wird entfernt.
         * Beim Aufruf muss gelten: 1 <= relPos <= spielfelder
         */
        int absPosSchlagend = absPos(relPos, aktiv);
        int andereFarbe;
        for (int zaehler = 1; zaehler < farben; zaehler++) {
            andereFarbe = (aktiv + zaehler) % farben;
            for (int stein = 0; stein < steine; stein++) {
                int relPosAndereFarbe = positionen[andereFarbe][stein];
                if (1 <= relPosAndereFarbe &&
                    relPosAndereFarbe <= spielfelder &&
                    absPosSchlagend == 
                    absPos(relPosAndereFarbe, andereFarbe)) {
                    positionen[andereFarbe][stein] = 0;
                    return;
                }
            }
        }
    }
    
    private boolean blockiert (int relPos)
    {
        /* Stellt fest, ob das Zielfeld durch einen Stein der aktiven 
         * Farbe blockiert ist.
         */
        for (int stein = 0; stein < steine; stein++)
            if (positionen[aktiv][stein] == relPos)
                return true;
        return false;
    }
    
    // Implementierung der Subjekt-Schnittstelle
    
    private ArrayList<Beobachter> alleBeobachter = new ArrayList<Beobachter>();
    
	public void registriere(Beobachter beobachter) {
		alleBeobachter.add(beobachter);
	}
	
	public void deregistriere(Beobachter beobachter) {
		alleBeobachter.remove(beobachter);
	}
	
	public void benachrichtige() {
		for (Beobachter beobachter : alleBeobachter) beobachter.aktualisiere();
	}

    /**
     * Konstruktor für das Modell, stellt den initialen Spielstand her.
     */
    public Modell()
    {
        // Alle Steine sind draußen, und rot fängt an
    }
}
