# Titanium Backup to Super Backup (XML Converter)
Convert XML files of **Titanium Backup (*com.keramidas.TitaniumBackup*)** to XML files of **Super Backup (*com.idea.backup.smscontacts*)**

### Messages
- Extract XML file from **com.keramidas.virtual.XML_MESSAGES-date-time.xml.gz** from *Titanium Backup* folder.
- Use given java file to convert it to format used by Super Backup.
 ```$ java titaniumBackup2SuperBackup_SMS.java -o input_file.xml output_file.xml ```


### Call Log
- Extract XML file from **com.keramidas.virtual.XML_CALL_LOG-date-time.xml.gz** from *Titanium Backup* folder.
- Use given java file to convert it to format used by Super Backup.
 ```$ java titaniumBackup2SuperBackup_Call.java -o input_file.xml output_file.xml ```
