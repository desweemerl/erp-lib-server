package com.fw.server.reports;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class PdfReport extends PdfDocument {  
    
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");            
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("kk:mm:ss");            
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");            

    private BaseFont arialBaseFont;
    private BaseFont gothicBaseFont;
    private BaseFont wingdingBaseFont;
    private BaseFont gothicItalicBaseFont; 
    
    private Font arial10;
    private Font arial10Bold;    
    private Font gothic10;
    private Font wingding10;
    private Font gothicItalic10;   
    
    
    public void onBeforeBuildDocument(PdfWriter writer, Document document) throws Exception {
        arialBaseFont = BaseFont.createFont(getClass().getClassLoader().getResource("com/fw/server/reports/arial.ttf").getPath(), BaseFont.WINANSI, BaseFont.EMBEDDED);
        arial10 = new Font(arialBaseFont, 10, Font.NORMAL);
        arial10Bold = new Font(arialBaseFont, 10, Font.BOLD);
        
        gothicBaseFont = BaseFont.createFont(getClass().getClassLoader().getResource("com/fm/server/reports/gothic.ttf").getPath(), BaseFont.WINANSI, BaseFont.EMBEDDED);
        gothic10 = new Font(gothicBaseFont, 10, Font.NORMAL);
        
        gothicItalicBaseFont = BaseFont.createFont(getClass().getClassLoader().getResource("com/fw/server/reports/gothici.ttf").getPath(), BaseFont.WINANSI, BaseFont.EMBEDDED);
        gothicItalic10 = new Font(gothicItalicBaseFont, 10, Font.ITALIC);
        
        wingdingBaseFont = BaseFont.createFont(getClass().getClassLoader().getResource("com/fw/server/reports/wingding.ttf").getPath(), BaseFont.WINANSI, BaseFont.EMBEDDED);                    
        wingding10 = new Font(wingdingBaseFont, 10, Font.NORMAL);
    }

    protected String formatDate(Date date) {
        
        if (date == null) return "";        
        return dateFormatter.format(date);
        
    }

    protected String formatTime(Date date) {
        
        if (date == null) return "";        
        return timeFormatter.format(date);
        
    }
    
    protected String formatDateTime(Date date) {
        
        if (date == null) return "";        
        return dateTimeFormatter.format(date);        

    }    
    
    protected BaseFont getArialBaseFont() {
        return arialBaseFont;
    }

    protected BaseFont getGothicBaseFont() {
        return gothicBaseFont;
    }

    protected BaseFont getWingdingBaseFont() {
        return wingdingBaseFont;
    }

    protected BaseFont getGothicItalicBaseFont() {
        return gothicItalicBaseFont;
    }
    
    protected Phrase getArialMessage(String message) {
        return new Phrase(getMessage(message), arial10);
    }   
    
    protected Phrase getArialMessage(String message, int size) {
        return new Phrase(getMessage(message), new Font(arialBaseFont, size, Font.NORMAL));
    }
    
    protected Phrase getArialPhrase(String phrase) {
        return new Phrase(phrase, arial10);
    }       
    
    protected Phrase getArialBoldMessage(String message) {
        return new Phrase(getMessage(message), arial10Bold);
    }      

    protected Phrase getArialBoldMessage(String message, int size) {
        return new Phrase(getMessage(message), new Font(arialBaseFont, size, Font.BOLD));
    }  
    
    protected Phrase getArialBoldPhrase(String phrase) {
        return new Phrase(phrase, arial10Bold);
    }
    
    protected Phrase getArialBoldPhrase(String phrase, int size) {
        return new Phrase(phrase, new Font(arialBaseFont, size, Font.BOLD));
    }
    
    protected Phrase getGothicItalicMessage(String message, int size) {
        return new Phrase(getMessage(message), new Font(gothicItalicBaseFont, size, Font.BOLD));
    }

    protected Font getArial10() {
        return arial10;
    }

    protected Font getArial10Bold() {
        return arial10Bold;
    }

    protected Font getGothic10() {
        return gothic10;
    }

    protected Font getGothicItalic10() {
        return gothicItalic10;
    }

    protected Font getWingding10() {
        return wingding10;
    }

    protected String getFormattedDateString(Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null) {
            return formatter.format(date);
        }

        return "/";

    }     

}
