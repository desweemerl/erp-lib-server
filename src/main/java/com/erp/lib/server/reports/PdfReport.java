package com.erp.lib.server.reports;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class PdfReport extends PdfDocument {

    private static final float DEFAULT_FONT_SIZE = 10;

    // Define default formats
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm:ss");
    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");

    // Define default fonts
    @Override
    public void onBeforeBuildDocument(PdfWriter writer, Document document) throws Exception {
        FontFactory.register("/fonts/arial.ttf", "arial");
        FontFactory.register("/fonts/gothic.ttf", "gothic");
        FontFactory.register("/fonts/gothici.ttf", "gothici");
        FontFactory.register("/fonts/wingding.ttf", "wingding");
    }

    protected String formatDate(Date date) {
        return date == null ? "" : dateFormatter.format(date);
    }

    protected String formatTime(Date date) {
        return date == null ? "" : timeFormatter.format(date);
    }

    protected String formatDateTime(Date date) {
        return date == null ? "" : dateTimeFormatter.format(date);
    }

    protected Phrase getTrPhrase(String phrase, String fontName) {
        return new Phrase(getMessage(phrase), FontFactory.getFont(fontName, DEFAULT_FONT_SIZE));
    }

    protected Phrase getTrPhrase(String phrase, String fontName, float fontSize) {
        return new Phrase(getMessage(phrase), FontFactory.getFont(fontName, fontSize));
    }

    protected Phrase getTrPhrase(String phrase, String fontName, float fontSize, int fontStyle) {
        return new Phrase(getMessage(phrase), FontFactory.getFont(fontName, fontSize, fontStyle));
    }

    protected Phrase getPhrase(String phrase, String fontName) {
        return new Phrase(getMessage(phrase), FontFactory.getFont(fontName, DEFAULT_FONT_SIZE));
    }

    protected Phrase getPhrase(String phrase, String fontName, float fontSize) {
        return new Phrase(phrase, FontFactory.getFont(fontName, fontSize));
    }

    protected Phrase getPhrase(String phrase, String fontName, float fontSize, int fontStyle) {
        return new Phrase(phrase, FontFactory.getFont(fontName, fontSize, fontStyle));
    }

    protected String getFormattedDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null ? "/" : formatter.format(date);
    }
}
