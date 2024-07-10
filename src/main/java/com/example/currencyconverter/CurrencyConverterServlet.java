package com.example.currencyconverter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/convert")
public class CurrencyConverterServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String baseCurrency = request.getParameter("baseCurrency");
        double baseAmount = Double.parseDouble(request.getParameter("amount"));

        Map<String, Double> rates = getRates();
        double baseRate = rates.get(baseCurrency);

        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h1>Conversion Results</h1>");
        response.getWriter().println("<table border='1'><tr><th>Base Currency</th><th>Other Currencies</th></tr>");

        response.getWriter().println("<tr><td>" + baseCurrency + " " + baseAmount + "</td><td><table border='1'>");

        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            String currency = entry.getKey();
            if (!currency.equals(baseCurrency)) {
                double rate = entry.getValue();
                double convertedAmount = (baseAmount * baseRate) / rate;
                response.getWriter().println("<tr><td>" + currency + "</td><td>" + convertedAmount + "</td></tr>");
            }
        }

        response.getWriter().println("</table></td></tr>");
        response.getWriter().println("</table></body></html>");
    }

    private Map<String, Double> getRates() {
        Map<String, Double> rates = new HashMap<>();
        try {
            URL url = new URL("https://services.nbrb.by/xmlexrates.aspx");
            InputStream inputStream = url.openStream();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Currency");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String charCode = element.getElementsByTagName("CharCode").item(0).getTextContent();
                int scale = Integer.parseInt(element.getElementsByTagName("Scale").item(0).getTextContent());
                double rate = Double.parseDouble(element.getElementsByTagName("Rate").item(0).getTextContent());
                rates.put(charCode, rate / scale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rates;
    }
}