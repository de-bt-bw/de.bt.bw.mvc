package de.bt.bw.mvc;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Sicht für Mensch ärgere dich nicht. Der aktuelle Spielstand wird
 * auf einem Spielfeld mit festem Layout dargestellt. Dabei werden
 * für die Farben, die Zahl der Steine und die Zahl der Spielfelder
 * feste Werte angenommen (sonst müsste man einen generischen
 * Layoutalgorithmus implementieren).
 * 
 * @author Bernhard Westfechtel 
 * @version 13.09.2007
 */
public class Sicht extends JPanel implements Beobachter, ActionListener{
    private Kontrolle kontrolle;
    private Modell modell;
    
    private boolean automatisch; // Falls true, wird mit Zufallszahl gewuerfelt.
    
    // Alle Buttons
    private JButton[] buttons;
    private String[] labels;
    
    
    /* Die folgenden Konstanten dienen dazu, die logischen 
     * Koordinaten (s.u.) in physikalische Koordinaten (Pixel) umzurechnen.
     */ 
    private static final int offsetX = 60; 
    // x-Koordinate des linken oberen Eckpunktes relativ zum Fensterursprung
    private static final int offsetY = 100;
    // y-Koordinate des linken oberen Eckpunktes relativ zum Fensterursprung
    private static final int delta = 60; // Abstand der physikalischen Positionen
    private static final int radiusFeld = delta/2; // Radius eines Feldes
    private static final int radiusStein = radiusFeld/2; // Radius eines Steins
    private static final int offsetStein = radiusStein/2;
    // Offset eines Steins zur Positionierung in der Feldmitte
    
    private static final Color[] farbeInSicht = 
        {Color.red, Color.blue, Color.green, Color.orange};
        // In der Darstellung gewählte Farbe (gelb ist zu hell)
    
    /* Die folgenden Arrays legen die Positionen der Spielfelder
     * fest. Dies sind virtuelle Positionen auf einem
     * 11x11-Gitter.
     * startfelder: hier geht es los
     * zielfelder: hier kommt man an
     * schlagfelder: hier gibt es den Ärger
     */
    private static final int[][] startfelderX =
        {{0, 1, 0, 1}, // rot links
         {9, 10, 9, 10}, // blau rechts
         {9, 10, 9, 10}, // grün rechts
         {0, 1, 0, 1} // gelb links
        };
        
    private static final int[][] startfelderY =
        {{0, 0, 1, 1}, // rot oben
         {0, 0, 1, 1}, // blau oben
         {9, 9, 10, 10}, // grün unten
         {9, 9, 10, 10} // gelb unten
        };
        
    private static final int[][] zielfelderX =
        {{1, 2, 3, 4}, // rot links
         {5, 5, 5, 5}, // blau vertikal
         {9, 8, 7, 6}, // grün rechts
         {5, 5, 5, 5} // gelb vertikal
        };
        
    private static final int[][] zielfelderY =
        {{5, 5, 5, 5}, // rot horizontal
         {1, 2, 3, 4}, // blau oben
         {5, 5, 5, 5}, // grün horizontal
         {9, 8, 7, 6} // gelb unten
        };
        
    private static final int[] schlagfelderX = // Anfang bei rot
        {0, 1, 2, 3, 4, 4, 4, 4, 4, 5, // "roter" Teil
         6, 6, 6, 6, 6, 7, 8, 9, 10, 10, // "blauer" Teil
         10, 9, 8, 7, 6, 6, 6, 6, 6, 5, // "grüner" Teil
         4, 4, 4, 4, 4, 3, 2, 1, 0, 0 // "gelber" Teil
        };
        
    private static final int[] schlagfelderY = // Anfang bei rot
        {4, 4, 4, 4, 4, 3, 2, 1, 0, 0, // "roter" Teil
         0, 1, 2, 3, 4, 4, 4, 4, 4, 5, // "blauer" Teil
         6, 6, 6, 6, 6, 7, 8, 9, 10, 10, // "grüner" Teil
         10, 9, 8, 7, 6, 6, 6, 6, 6, 5 // "gelber" Teil
        };
    
    /* Die folgenden Arrays definieren die Eckpunkte eines Polygons,
     * das zur Verbindung der Schlagfelder durch Linien benutzt wird.
     * Die Schlagfelder werden über das Polygon gemalt.
     */
    private static final int[] polygonX = 
        {0, 4, 4, 6, 6, 10, 10, 6, 6, 4, 4, 0};
        
    private static final int[] polygonY =
        {4, 4, 0, 0, 4, 4, 6, 6, 10, 10, 6, 6};
         
    /**
     * Konstruktor zur Initialisierung der Sicht. 
     * @param k : Referenz auf die Kontrolle (reagiert auf Kommandos)
     * @param m : Referenz auf das Modell (zur Darstellung des Spielstands)
     */
    public Sicht(Kontrolle k, Modell m) {
        kontrolle = k;
        modell = m;
        int zahl = Kommando.zahlKommandos;
        labels = new String[zahl];
        buttons = new JButton[zahl];
        for (int i = 0; i < zahl; i++) {
        	labels[i] = Kommando.gibKommando(i+1).gibLiteral();
        	buttons[i] = new JButton(labels[i]);
        	buttons[i].addActionListener(this);
        	add(buttons[i]);
        }
        // Würfeln-Button deaktivieren
        buttons[Kommando.WÜRFELN.gibWert()-1].setEnabled(false);
        JFrame frame = new JFrame("Mensch ärgere dich nicht");
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,800);
        frame.setVisible(true);

        modell.registriere(this);
    }
    
    /**
     * Zeichnet das Spielfeld.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // U.a. Ausgabe der Buttons
        gibStatuszeileAus(g);
        zeichneSpielfeld(g);
        platziereSteine(g);
    }
   
    /* 
     * zeichneFeld zeichnet einen einzelnen Kreis.
     * Die Kreise des Spielfelds sind auf einem 11x11-Gitter
     * angeordnet, wobei die Gitterpositionen von 0..10 reichen.
     * Diese logischen Positionen (x, y) werden in physikalische 
     * Positionen umgerechnet.
     */
    private void zeichneFeld(Graphics g, Color farbe, int x, int y) {

        g.setColor(Color.white); // Weiße Füllung
        g.fillOval
            (offsetX + delta * x, offsetY + delta * y, radiusFeld, radiusFeld);
        g.setColor(farbe);
        g.drawOval
            (offsetX + delta * x, offsetY + delta * y, radiusFeld, radiusFeld);
    }
    
    // Zeichnet einen einzelnen Stein
    private void zeichneStein(Graphics g, Color farbe, int x, int y) {
        g.setColor(farbe);
        g.fillOval
            (offsetX + delta * x + offsetStein,
             offsetY + delta * y + offsetStein,
             radiusStein,
             radiusStein);
    }
    
    /* Das Polygon für die Verbindungslinien der Schlagfelder wird gezeichnet.
     * Dazu werden logische in physikalische Positionen umgerechnet.
     */
    private void zeichnePolygon(Graphics g) {
        int[] koordinatenX = new int[polygonX.length];
        int[] koordinatenY = new int[polygonY.length];
        // Umrechnung der logischen Positionen in physikalische Koordinaten
        int i;
        for (i = 0; i < koordinatenX.length; i++)
            koordinatenX[i] = offsetX + delta * polygonX[i] + radiusStein;
        for (i = 0; i < koordinatenY.length; i++)
            koordinatenY[i] = offsetY + delta * polygonY[i] + radiusStein;
        g.setColor(Color.black);
        g.drawPolygon(koordinatenX, koordinatenY, koordinatenX.length);
    }
    
    /* In der Statuszeile werden der letzte Wurf und die Farbe des aktiven
     * Spielers ausgegeben.
     */
    private void gibStatuszeileAus(Graphics g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("Letzter Wurf: " + modell.letzterWurf(), delta, delta);
        g.drawString("Am Zug:", delta + 300, delta); 
        g.setColor(farbeInSicht[modell.aktiverSpieler()]);
        g.fillRect(delta + 380, delta - 18, 18, 18);
    }
    
    // Zeichnen des (statischen) Spielfelds
    private void zeichneSpielfeld(Graphics g) {
        int farbe, stein;
        // Startfelder zeichnen
        for (farbe = 0; farbe < startfelderX.length; farbe++)
            for (stein = 0; stein < startfelderX[0].length; stein++)
                zeichneFeld
                    (g, farbeInSicht[farbe],
                     startfelderX[farbe][stein], startfelderY[farbe][stein]);
        // Zielfelder zeichnen
        for (farbe = 0; farbe < zielfelderX.length; farbe++)
            for (stein = 0; stein < zielfelderX[0].length; stein++)
                zeichneFeld
                    (g, farbeInSicht[farbe],
                     zielfelderX[farbe][stein], zielfelderY[farbe][stein]);
        // Verbindungslinien für Schlagfelder zeichnen
        zeichnePolygon(g);
        // Schlagfelder zeichnen
        for (int feld = 0; feld < schlagfelderX.length; feld++) {
            // Farbe bestimmen
            Color color = Color.black;
            if (feld % modell.delta == 0) 
                color = farbeInSicht[feld/modell.delta];
            zeichneFeld(g, color, schlagfelderX[feld], schlagfelderY[feld]);
        }
    }
    
    // (Dynamisches) Platzieren der Steine, deren Position dem Modell entnommen wird
    private void platziereSteine(Graphics g) {
        zeichneStein(g, Color.red, 0, 0);
        for (int farbe = 0; farbe < modell.farben; farbe++) {
            Color color = farbeInSicht[farbe];
            int draussen = 0;
            for (int stein = 0; stein < modell.steine; stein++) {
                int relPos = modell.position(farbe, stein);
                if (relPos == 0) { // Stein draussen
                    zeichneStein
                        (g, color, 
                         startfelderX[farbe][draussen], 
                         startfelderY[farbe][draussen]);
                    draussen++; 
                    }
                else if (relPos > modell.spielfelder) { // Stein drinnen
                    int index = relPos - modell.spielfelder - 1;
                    zeichneStein
                        (g, color, 
                         zielfelderX[farbe][index], 
                         zielfelderY[farbe][index]);
                    }
                else { // Stein auf Schlagfeld
                    int absPos = modell.absPos(relPos, farbe) - 1;
                    zeichneStein
                        (g, color, schlagfelderX[absPos], schlagfelderY[absPos]);
                }
            }
        }
    }
    
    // Implementierung der Beobachter-Schnittstelle
    public void aktualisiere() {
    	repaint();
    }
    
    // Zwischen automatischem und manuellem Würfeln umschalten
    public void wechsleModus() {
    	automatisch = !automatisch;
    	int wert = Kommando.WÜRFELN.gibWert();
    	buttons[wert-1].setEnabled(!buttons[wert-1].isEnabled());
    	int wert1 = Kommando.EINS.gibWert();
    	int wert6 = Kommando.SECHS.gibWert();
    	for (int i = wert1-1; i < wert6; i++)
    		buttons[i].setEnabled(!buttons[i].isEnabled());
    	repaint();
    }
    
    /**
     * Als Reaktion auf einen gedrückten Button wird ein Kommando
     * konstruiert und die Kontrolle aufgerufen, die dieses
     * Kommando behandelt. In diesem einfachen Fall reicht es,
     * einen Wert eines Aufzählungstyps "Kommando" zu bestimmen,
     * d.h. es wird keine Kommandoklasse benötigt.
     */
    public void actionPerformed(ActionEvent e) {
    	String cmd = e.getActionCommand();
    	Kommando kommando = Kommando.gibKommando(cmd);
        kontrolle.behandleKommando(kommando);
    }

}

