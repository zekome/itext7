package com.itextpdf.core.font;


import com.itextpdf.basics.font.FontEncoding;
import com.itextpdf.basics.font.FontMetrics;
import com.itextpdf.basics.font.FontNames;
import com.itextpdf.basics.font.Type1Font;
import com.itextpdf.basics.font.cmap.CMapToUnicode;
import com.itextpdf.basics.font.otf.Glyph;
import com.itextpdf.core.pdf.PdfArray;
import com.itextpdf.core.pdf.PdfDictionary;
import com.itextpdf.core.pdf.PdfDocument;
import com.itextpdf.core.pdf.PdfName;
import com.itextpdf.core.pdf.PdfNumber;
import com.itextpdf.core.pdf.PdfStream;
import com.itextpdf.core.pdf.PdfString;

public class PdfType1Font extends PdfSimpleFont<Type1Font> {

    public PdfType1Font(PdfDictionary fontDictionary) {
        super(fontDictionary);
        checkFontDictionary(fontDictionary, PdfName.Type1);

        CMapToUnicode toUni = DocFontUtils.processToUnicode(fontDictionary);
        fontEncoding = DocFontEncoding.createDocFontEncoding(fontDictionary.get(PdfName.Encoding), toUni);
        fontProgram = DocType1Font.createSimpleFontProgram(fontDictionary, fontEncoding);

        if (fontProgram instanceof DocType1Font) {
            embedded = ((DocType1Font) fontProgram).getFontFile() != null;
        }
        subset = false;
    }

    public PdfType1Font(PdfDocument pdfDocument, Type1Font type1Font, String encoding, boolean embedded) {
        super(pdfDocument);
        setFontProgram(type1Font);
        this.embedded = embedded && !type1Font.isBuiltInFont();
        if ((encoding == null || encoding.length() == 0) && type1Font.isFontSpecific()) {
            encoding = FontEncoding.FontSpecific;
        }
        if (encoding != null && FontEncoding.FontSpecific.toLowerCase().equals(encoding.toLowerCase())) {
            fontEncoding = FontEncoding.createFontSpecificEncoding();
        } else {
            fontEncoding = FontEncoding.createFontEncoding(encoding);
        }
    }

    public PdfType1Font(PdfDocument pdfDocument, Type1Font type1Font, boolean embedded) {
        this(pdfDocument, type1Font, null, embedded);
    }

    public PdfType1Font(PdfDocument pdfDocument, Type1Font type1Font, String encoding) {
        this(pdfDocument, type1Font, encoding, false);
    }

    public PdfType1Font(PdfDocument pdfDocument, Type1Font type1Font) {
        this(pdfDocument, type1Font, null, false);
    }

    @Override
    public boolean isSubset() {
        return subset;
    }

    @Override
    public void setSubset(boolean subset) {
        this.subset = subset;
    }

    @Override
    public void flush() {
        flushFontData(fontProgram.getFontNames().getFontName(), PdfName.Type1);
    }

    @Override
    protected Type1Font initializeTypeFontForCopy(String encodingName) {
        throw new RuntimeException("Not implemented");
    }

//    @Override
//    protected Type1Font initializeTypeFont(String fontName, String encodingName) {
//        return Type1Font.createFont(fontName, encodingName);
//    }

    public Glyph getGlyph(int ch) {
        if (fontEncoding.canEncode(ch)) {
            Glyph glyph;
            if (fontEncoding.isFontSpecific()) {
                glyph = getFontProgram().getGlyphByCode(ch);
            } else {
                glyph = getFontProgram().getGlyph(fontEncoding.getUnicodeDifference(ch));
                if (glyph == null && (glyph = notdefGlyphs.get(ch)) == null) {
                    // Handle special layout characters like sfthyphen (00AD).
                    // This glyphs will be skipped while converting to bytes
                    glyph = new Glyph(-1, 0, ch);
                    notdefGlyphs.put(ch, glyph);
                }
            }
            return glyph;
        }
        return null;
    }


    private void flushCopyFontData() {
        super.flush();
    }

    @Override
    protected boolean isBuiltInFont() {
        return fontProgram.isBuiltInFont();
    }

    /**
     * If the embedded flag is {@code false} or if the font is one of the 14 built in types, it returns {@code null},
     * otherwise the font is read and output in a PdfStream object.
     *
     * @return the PdfStream containing the font or {@code null}.
     */
    protected void addFontStream(PdfDictionary fontDescriptor) {
        if (embedded) {
            if (fontProgram instanceof DocType1Font) {
                DocType1Font docType1Font = (DocType1Font)fontProgram;
                fontDescriptor.put(docType1Font.getFontFileName(),
                        docType1Font.getFontFile());
                if (docType1Font.getSubtype() != null) {
                    fontDescriptor.put(PdfName.Subtype, docType1Font.getSubtype());
                }
            } else {
                byte[] fontStreamBytes = getFontProgram().getFontStreamBytes();
                if (fontStreamBytes != null) {
                    PdfStream fontStream = new PdfStream(fontStreamBytes);
                    int[] fontStreamLengths = getFontProgram().getFontStreamLengths();
                    for (int k = 0; k < fontStreamLengths.length; ++k) {
                        fontStream.put(new PdfName("Length" + (k + 1)), new PdfNumber(fontStreamLengths[k]));
                    }
                    fontDescriptor.put(PdfName.FontFile, fontStream);
                }
            }
        }
    }
}
