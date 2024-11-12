package de.bt.bw.mvc;

public enum Kommando {

	EINS(1, "Eins"), ZWEI(2,"Zwei"), DREI(3, "Drei"), VIER (4, "Vier"), FÜNF(5, "Fünf"), 
	SECHS(6,"Sechs"), AUTOMATISCH(7, "Automatisch/Manuell"), WÜRFELN(8, "Würfeln"), FERTIG(9,"Fertig");
	private int wert;
	private String literal;
	private Kommando(int wert, String literal) {this.wert = wert; this.literal = literal;}
	public int gibWert() {return wert;}
	public String gibLiteral() {return literal;}
	public static final int zahlKommandos = 9;
	public static Kommando gibKommando(int wert) {
	   if (wert == 1) return EINS;
	   else if (wert == 2) return ZWEI;
	   else if (wert == 3) return DREI;
	   else if (wert == 4) return VIER;
	   else if (wert == 5) return FÜNF;
	   else if (wert == 6) return SECHS;
	   else if (wert == 7) return AUTOMATISCH;
	   else if (wert == 8) return WÜRFELN;
	   else return FERTIG;
	}
	public static Kommando gibKommando(String literal) {
		if (literal.equalsIgnoreCase(EINS.gibLiteral())) return EINS;
		else if (literal.equalsIgnoreCase(ZWEI.gibLiteral())) return ZWEI;
		else if (literal.equalsIgnoreCase(DREI.gibLiteral())) return DREI;
		else if (literal.equalsIgnoreCase(VIER.gibLiteral())) return VIER;
		else if (literal.equalsIgnoreCase(FÜNF.gibLiteral())) return FÜNF;
		else if (literal.equalsIgnoreCase(SECHS.gibLiteral())) return SECHS;
		else if (literal.equalsIgnoreCase(AUTOMATISCH.gibLiteral())) return AUTOMATISCH;
		else if (literal.equalsIgnoreCase(WÜRFELN.gibLiteral())) return WÜRFELN;
		else return FERTIG;
	}
}
