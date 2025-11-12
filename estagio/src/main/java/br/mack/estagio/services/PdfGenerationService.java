package br.mack.estagio.services;

import br.mack.estagio.entities.Estudante;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGenerationService {

    public byte[] generateCurriculoPdf(Estudante estudante) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Currículo de " + estudante.getNome()).setBold().setFontSize(20));
            document.add(new Paragraph("Email: " + estudante.getEmail()));
            document.add(new Paragraph("Telefone: " + (estudante.getTelefone() != null ? estudante.getTelefone() : "N/A")));
            document.add(new Paragraph("CPF: " + estudante.getCpf()));
            document.add(new Paragraph("Curso: " + (estudante.getCurso() != null ? estudante.getCurso() : "N/A")));

            document.close();
        } catch (Exception e) {
            // Em um aplicativo real, você deve registrar este erro.
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
}