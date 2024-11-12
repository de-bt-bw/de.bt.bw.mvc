package de.bt.bw.mvc;

public class Init {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Modell modell = new Modell();
        Kontrolle kontrolle = new Kontrolle(modell);
	}

}