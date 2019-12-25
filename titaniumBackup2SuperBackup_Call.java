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
            
            Call[] callArray = readTitaniumBackupFile(inputFile);
            
            if (cout)
                for (int i = 0; i < callArray.length; i++)
                    callArray[i].print();
    
            writeSuperBackupFile(outputFile, callArray);
            
    
        } catch (Exception e) {
            e.printStackTrace();
         }
    }
    
    static Call[] readTitaniumBackupFile(File inputFile)
    {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            
            //int totalCall = doc.getDocumentElement().getAttribute("count");
            NodeList nList = doc.getElementsByTagName("call");
    
            Call callArray[] = new Call[nList.getLength()];
            
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    callArray[i] = new Call((Element) nNode);
                }
            }
            return callArray;
        } catch (Exception e) {
            System.err.println("Error parsing input XML file.");
            e.printStackTrace();
        }
        return new Call[0];
    }
    
    static void writeSuperBackupFile(File outputFile, Call[] callArray)
    {
        try {
            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
        
            // root element
            Element rootElement = doc.createElement("alllogs");
            rootElement.setAttribute("count", Integer.toString(callArray.length));
            doc.appendChild(rootElement);
    
            for (int i = 0; i < callArray.length; i++)
            {
                Element call = doc.createElement("log");
                call.setAttribute("number", callArray[i].getNumber());
                call.setAttribute("time", callArray[i].getTime());
                call.setAttribute("date", Long.toString(callArray[i].getDate()));
                call.setAttribute("type", Integer.toString(callArray[i].getType()));
                call.setAttribute("name", callArray[i].getName());
                call.setAttribute("new", Integer.toString(callArray[i].isNewCall()? 1 : 0));
                call.setAttribute("dur", Integer.toString(callArray[i].getDuration()));
                rootElement.appendChild(call);
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
    
    static class Call
    {
        String number;
        String name;
        boolean newCall;
        LocalDateTime date;
        int type;
        int duration;
        
        public Call(Element element)
        {
            number = element.getAttribute("number");
            name = element.getAttribute("cachedName");
            newCall = (element.getAttribute("isNew").compareTo("true") == 0);
            duration = Integer.parseInt(element.getAttribute("duration"));
    
            Instant instant = Instant.parse(element.getAttribute("date"));
            date = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
    
            if (element.getAttribute("type").compareTo("incoming") == 0)
                type = 1;
            else if (element.getAttribute("type").compareTo("outgoing") == 0)
                type = 2;
            else
                type = 3;
        }
        public long getDate() {
            ZonedDateTime zdt = date.atZone(ZoneId.of(timeZone));
            return zdt.toInstant().toEpochMilli();
        }
        public String getTime() {
            return date.format(DateTimeFormatter.ofPattern("LLL d, yyyy h:mm:ss a"));
        }
        public String getNumber() {
            return number;
        }
    
        public String getName() {
            return name;
        }
    
        public boolean isNewCall() {
            return newCall;
        }
    
        public int getType() {
            return type;
        }
    
        public int getDuration() {
            return duration;
        }
    
    
        public void print()
        {
            System.out.println();
            System.out.println("Name : " + name);
            System.out.println("Number : " + number);
            System.out.println("Time : " + getTime());
            System.out.println("Date : " + getDate());
            System.out.println("Type : " + Integer.toString(getType()));
            System.out.println("New : " + newCall);
            System.out.println("Duration : " + duration);
            System.out.println("----------------------------");
        }
    
    }
    
}
