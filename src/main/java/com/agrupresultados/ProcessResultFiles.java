package com.agrupresultados;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ProcessResultFiles {
    final String pathSvn;
    final String pathGit;
    final int POS_FILE = 0;
    final int POS_TOTAL_LINES = 1;
    final int POS_ADDED_LINES = 2;
    final int POS_REMOVED_LINES = 3;

    public ProcessResultFiles(String strPathSvn, String strPathGit) {
        pathSvn = strPathSvn;
        pathGit = strPathGit;
        if (!(new File(pathSvn)).exists() || !(new File(pathSvn)).isDirectory()) {
            throw new RuntimeException("Diretório com arquivos de resultado do SVN não existe");
        }
        if (!(new File(pathGit)).exists() || !(new File(pathGit)).isDirectory()) {
            throw new RuntimeException("Diretório com arquivos de resultado do GIT não existe");
        }
        System.out.println("Diretório com arquivos de resultado do SVN: " + strPathSvn);
        System.out.println("Diretório com arquivos de resultado do GIT: " + strPathGit);
        System.out.println("\n");
    }

    public void run() {
        Stream.of(Objects.requireNonNull((new File(pathSvn)).listFiles()))
                .filter(file -> !file.isDirectory())
                .sorted()
                .map(File::getName)
                .forEach(file -> {
                    System.out.println("Processando arquivo: " + file);
                    this.processaArqResultado(file);
                    System.out.println("-------------------------------------------------------");
                });
    }

    private void processaArqResultado(final String fileProcessing) {
        final var start = System.currentTimeMillis();
        //Load file result SVN (main)
        File fileSvn = new File(pathSvn + File.separator + fileProcessing);
        if (!fileExistsAndIsFile(fileSvn)) {
            throw new RuntimeException("Arquivo não existe.");
        }
        //Load file result GIT
        File fileGit = new File(pathGit + File.separator + fileProcessing);
        if (!fileExistsAndIsFile(fileGit)) {
            System.out.println("Pulando execução, pois arquivo não existe no repositório Git");
            return;
        }
        //list with processing results
        List<String> result = new ArrayList<>();
        //GIT file loaded into memory to avoid multiple readings
        List<LineProcessedFile> memoryFileGitProcess = loadFileToList(fileGit);
        //SVN file is only read once, so we process it line by line.
        try (BufferedReader reader = new BufferedReader(new FileReader(fileSvn))) {
            int lineSvnFile = 0;
            while (reader.ready()) {
                final var splitLine = reader.readLine().split(";");
                if (splitLine.length == 4) {
                    final var svnLineFile = lineFileToObjFileProcess(++lineSvnFile, splitLine);
                    memoryFileGitProcess
                            .stream()
                            .filter(fp -> fp.getName().equalsIgnoreCase(svnLineFile.getName())
                                    && fp.getTotalLines() == svnLineFile.getTotalLines()
                                    && !fp.isProcessed()
                            )
                            .min(Comparator.comparingInt(LineProcessedFile::getLine))
                            .ifPresent(fp -> {
                                //Ignoramos os casos onde o arquivo estava limpo, pois nao temos como calcular
                                //o percentual de alteracao, esforço e accuray, recall e etc
                                if (svnLineFile.getTotalLines() != 0) {
                                    result.add(buildOutputLineResult(svnLineFile, fp));
                                }
                                fp.setProcessed();
                            });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        saveListToResultFile(fileProcessing, result);
        System.out.println("Arquivo de resultado:\n\tQtde linhas: " + result.size() +
                "\n\tTempo de processamento: " + (System.currentTimeMillis() - start) + " milisegundos");
    }

    private void saveListToResultFile(final String fileProcessing, final List<String> result) {
        File directory = new File("output-processing");
        boolean existsDir = true;
        if (!directory.exists()) {
            existsDir = directory.mkdir();
        }
        if (existsDir) {
            final var nameFileCsv = fileProcessing.substring(0, fileProcessing.lastIndexOf(".")).concat(".csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(directory, nameFileCsv)))) {
                writer.write(buildOutputLineHeaderResult().concat(System.lineSeparator()));
                for (String line : result) {
                    writer.write(line.concat(System.lineSeparator()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean fileExistsAndIsFile(File file) {
        return file.exists() && file.isFile();
    }

    private List<LineProcessedFile> loadFileToList(File file) {
        List<LineProcessedFile> listLineProcessedFile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int line = 0;
            while (reader.ready()) {
                final var splitLine = reader.readLine().split(";");
                if (splitLine.length == 4) {
                    listLineProcessedFile.add(lineFileToObjFileProcess(++line, splitLine));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listLineProcessedFile;
    }

    private LineProcessedFile lineFileToObjFileProcess(int lineCont, String[] values) {
        return new LineProcessedFile(lineCont,
                values[POS_FILE],
                Integer.parseInt(values[POS_TOTAL_LINES]),
                Integer.parseInt(values[POS_ADDED_LINES]),
                Integer.parseInt(values[POS_REMOVED_LINES])
        );
    }

    private String buildOutputLineHeaderResult() {
        return String.join(";",
                "file",
                "totalLines_svn",
                "addedLines_svn",
                "removedLines_svn",
                "percentageOfChange_svn",
                "accuracy_svn",
                "measure_svn",
                "recall_svn",
                "precision_svn",
                "effort_svn",
                "totalLines_git",
                "addedLines_git",
                "removedLines_git",
                "percentageOfChange_git",
                "accuracy_git",
                "measure_git",
                "recall_git",
                "precision_git",
                "effort_git"
        );
    }

    private String buildOutputLineResult(LineProcessedFile svnFile, LineProcessedFile gitFile) {
        return String.join(";",
                svnFile.getName(),
                //svn
                String.valueOf(svnFile.getTotalLines()),
                String.valueOf(svnFile.getAddedLines()),
                String.valueOf(svnFile.getRemovedLines()),
                String.valueOf(svnFile.percentageOfChange()),
                String.valueOf(svnFile.accuracy()),
                String.valueOf(svnFile.measure()),
                String.valueOf(svnFile.recall()),
                String.valueOf(svnFile.precision()),
                String.valueOf(svnFile.effort()),
                //--git
                String.valueOf(gitFile.getTotalLines()),
                String.valueOf(gitFile.getAddedLines()),
                String.valueOf(gitFile.getRemovedLines()),
                String.valueOf(gitFile.percentageOfChange()),
                String.valueOf(gitFile.accuracy()),
                String.valueOf(gitFile.measure()),
                String.valueOf(gitFile.recall()),
                String.valueOf(gitFile.precision()),
                String.valueOf(gitFile.effort())
        );
    }

}
