package com.itextpdf.core.pdf;

import com.itextpdf.core.exceptions.PdfException;

import java.io.IOException;

public class PdfDocumentInfo extends PdfObjectWrapper<PdfDictionary> {

    public PdfDocumentInfo(PdfDictionary pdfObject) {
        super(pdfObject);
    }

    public PdfDocumentInfo(PdfDocument pdfDocument) {
        super(new PdfDictionary(), pdfDocument);
    }

    public PdfDocumentInfo setTitle(String title) {
        pdfObject.put(PdfName.Title, new PdfString(title));
        return this;
    }

    public PdfDocumentInfo setAuthor(String author) {
        pdfObject.put(PdfName.Author, new PdfString(author));
        return this;
    }

    public PdfDocumentInfo setSubject(String subject) {
        pdfObject.put(PdfName.Subject, new PdfString(subject));
        return this;
    }

    public PdfDocumentInfo setKeywords(String keywords) {
        pdfObject.put(PdfName.Keywords, new PdfString(keywords));
        return this;
    }

    public PdfDocumentInfo setCreator(String creator) {
        pdfObject.put(PdfName.Creator, new PdfString(creator));
        return this;
    }

    @Override
    public void flush() throws PdfException {
        pdfObject.flush(false);
    }
}
