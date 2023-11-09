package com.agrupresultados;

import java.util.Date;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        final var start = System.currentTimeMillis();
        System.out.println("Processamento iniciado: " + new Date(start));
        if (args.length < 2)
            throw new RuntimeException("Necessario parametro com path dos resultados SVN e Git");
        final var svnResultPath = args[0];
        final var gitResultPath = args[1];
        new ProcessResultFiles(svnResultPath, gitResultPath).run();
        final var end = System.currentTimeMillis();
        System.out.println("\n");
        System.out.println("Processamento finalizado: " + new Date(end));
        System.out.println("Duração da execução: " + (end - start) + " milisegundos");
    }
}