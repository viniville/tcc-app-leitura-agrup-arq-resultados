package com.agrupresultados;

public class LineProcessedFile {
    private final int line;
    private final String name;
    private final int totalLines;
    private final int addedLines;
    private final int removedLines;
    private boolean processed;

    public LineProcessedFile(int line, String name, int totalLines, int addedLines, int removedLines) {
        this.line = line;
        this.name = name;
        this.totalLines = totalLines;
        this.addedLines = addedLines;
        this.removedLines = removedLines;
        this.processed = false;
    }

    public int getLine() {
        return line;
    }

    public String getName() {
        return name;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getAddedLines() {
        return addedLines;
    }

    public int getRemovedLines() {
        return removedLines;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        if (this.processed) {
            throw new RuntimeException("Registro já está marcado como processado");
        }
        this.processed = true;
    }

    public int effort() {
        return addedLines + removedLines;
    }

    public double percentageOfChange() {
        return (double) effort() / totalLines;
    }

    public double accuracy() {
        return Math.abs((double) totalLines / (totalLines + addedLines + removedLines));
    }

    public double recall() {
        return Math.abs((double) (totalLines - addedLines) / totalLines);
    }

    public double precision() {
        final int divider = totalLines - addedLines + removedLines;
        if (divider == 0)
            return 0;
        return Math.abs((double) (totalLines - addedLines) / divider);
    }

    public double measure() {
        final double divider = precision() + recall();
        if (divider == 0)
            return 0;
        return Math.abs(2 * ((precision() * recall()) / divider));
    }

    @Override
    public String toString() {
        return "FileProcess{" +
                "line=" + line +
                ", name='" + name + '\'' +
                ", totalLines=" + totalLines +
                ", addedLines=" + addedLines +
                ", removedLines=" + removedLines +
                ", processed=" + processed +
                ", effort=" + effort() +
                ", percentageOfChange=" + percentageOfChange() +
                ", accuracy=" + accuracy() +
                ", recall=" + recall() +
                ", precision=" + precision() +
                ", measure=" + measure() +
                '}';
    }
}
