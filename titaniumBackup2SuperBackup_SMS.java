import java.io.*;
import java.time.*;
import java.time.format.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class titaniumBackup2SuperBackup
{
    final static String timeZone = "Asia/Karachi";
    // Change this with your timezone from:
    // https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html
    
    public static void main(String args[])
    {
        if (args.length != 2 && args.length != 3)
        {
            System.out.println("Usage:\ttitaniumBackup2superBackup -args[optional] input_file output_file");
            System.out.println("\n\targs[optional]:\n\t-o\tAlso prints messages on stdout.");
            return;
        }
    
    
        int arg = 0;
        boolean cout = false;
        if (args.length == 3) {
            if (args[0].compareTo("-o") != 0) {
                System.err.println("Invalid argument " + args[0]);
                System.out.println("\nUsage:\ttitaniumBackup2superBackup -args[optional] input_file output_file");
                System.out.println("\n\targs[optional]:\n\t-o\tAlso prints messages on stdout.");
                return;
            } else {
                cout=true;
                arg++;
            }
        }
        
        try {
            File inputFile = new File(args[arg]);
            File outputFile = new File(args[arg+1]);
            
            SMS[] smsArray = readTitaniumBackupFile(inputFile);
            
            if (cout)
                for (int i = 0; i < smsArray.length; i++)
                    smsArray[i].print();
    
            writeSuperBackupFile(outputFile, smsArray);
            
    
        } catch (Exception e) {
            e.printStackTrace();
         }
    }
    
    static SMS[] readTitaniumBackupFile(File inputFile)
    {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            
            //int totalSMS = doc.getDocumentElement().getAttribute("count");
            NodeList nList = doc.getElementsByTagName("sms");
    
            SMS smsArray[] = new SMS[nList.getLength()];
            
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    smsArray[i] = new SMS((Element) nNode);
                }
            }
            return smsArray;
        } catch (Exception e) {
            System.err.println("Error parsing input XML file.");
            e.printStackTrace();
        }
        return new SMS[0];
    }
    
    static void writeSuperBackupFile(File outputFile, SMS[] smsArray)
    {
        try {
            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
        
            // root element
            Element rootElement = doc.createElement("allsms");
            rootElement.setAttribute("count", Integer.toString(smsArray.length));
            doc.appendChild(rootElement);
    
            for (int i = 0; i < smsArray.length; i++)
            {
                Element sms = doc.createElement("sms");
                sms.setAttribute("address", smsArray[i].getNumber());
                sms.setAttribute("time", smsArray[i].getTime());
                sms.setAttribute("date", Long.toString(smsArray[i].getDate()));
                sms.setAttribute("type", Integer.toString(smsArray[i].getType()));
                sms.setAttribute("body", smsArray[i].getText());
                sms.setAttribute("read", Integer.toString(smsArray[i].isRead()? 1 : 0));
                sms.setAttribute("service_center", smsArray[i].getServiceCenter());
                sms.setAttribute("name", "");
                rootElement.appendChild(sms);
            }
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //transformerFactory.setAttribute("indent-number", 1);
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outputFile);
            transformer.transform(source, result);
            
            // Output to console for testing
            //StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);
        
        } catch (Exception e) {
            System.err.println("Error writing file output.");
            e.printStackTrace();
        }
    }
    
    static void restoreWhiteSpaces(File outputFile)
    {}
    
    static class SMS
    {
        String number;
        String serviceCenter;
        boolean locked;
        boolean read;
        boolean seen;
        //String date;
        LocalDateTime date;
        int type;
        String text;
        
        public SMS(Element element)
        {
            number = element.getAttribute("address");
            serviceCenter = element.getAttribute("serviceCenter");
            locked = (element.getAttribute("locked").compareTo("true") == 0);
            seen = (element.getAttribute("seen").compareTo("true") == 0);
            read = (element.getAttribute("read").compareTo("true") == 0);
            text = element.getTextContent();
    
            Instant instant = Instant.parse(element.getAttribute("date"));
            date = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
    
            if (element.getAttribute("msgBox").compareTo("inbox") == 0)
                type = 1;
            else
                type = 2;
        }
        public String getNumber() {
            return number;
        }
    
        public String getServiceCenter() {
            return serviceCenter;
        }
    
        public boolean isLocked() {
            return locked;
        }
    
        public boolean isRead() {
            return read;
        }
    
        public boolean isSeen() {
            return seen;
        }
    
        public long getDate() {
            ZonedDateTime zdt = date.atZone(ZoneId.of(timeZone));
            return zdt.toInstant().toEpochMilli();
        }
        public String getTime() {
            return date.format(DateTimeFormatter.ofPattern("LLL d, yyyy h:mm:ss a"));
        }
    
        public int getType() {
            return type;
        }
    
        public String getText() {
            return text;
        }
        
        
        public void print()
        {
            System.out.println();
            System.out.println("Number : "
                    + this.getNumber());
            System.out.println("Service Center : "
                    + this.getServiceCenter());
            System.out.println("Locked : "
                    + this.isLocked());
            System.out.println("Time : "
                    + this.getTime());
            System.out.println("Date : "
                    + this.getDate());
            System.out.println("Type : "
                    + this.getType());
            System.out.println("Seen : "
                    + this.isSeen());
            System.out.println("Read : "
                    + this.isRead());
            System.out.println("Text :\n"
                    + this.getText());
            System.out.println("----------------------------");
        }
    
    }
    
}
