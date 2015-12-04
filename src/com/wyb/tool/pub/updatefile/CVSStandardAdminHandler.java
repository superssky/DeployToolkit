package com.wyb.tool.pub.updatefile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.file.FileUtils;

public class CVSStandardAdminHandler extends StandardAdminHandler {
	private String entryEncoding;
    private static final Object ksEntries = new Object();
    private static Runnable t9yBeforeRename;

	@Override
	public Iterator getEntries(File directory) throws IOException {
        List entries = new LinkedList();

        final File entriesFile = seekEntries(directory);
        // if there is no Entries file we just return the empty iterator
        if (entriesFile == null) {
            return entries.iterator();
        }

        processEntriesDotLog(new File(directory, "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        try {
        	if(StringUtils.isBlank(entryEncoding)) {
        		reader = new BufferedReader(new InputStreamReader(new FileInputStream(entriesFile)));
        	} else {
        		reader = new BufferedReader(new InputStreamReader(new FileInputStream(entriesFile), entryEncoding));
        	}
            String line;
            while ((line = reader.readLine()) != null) {
                entry = new Entry(line);
                if (entry.getName() != null) {
                    entries.add(entry);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return entries.iterator();
    
	}

    /**
     * If Entries do not exist restore them from backup.
     *
     * @param  folder path where to seek CVS/Entries
     * @return CVS/Entries file or null
     */
    private static File seekEntries(File folder) {
        synchronized(ksEntries) {
            File entries = new File(folder, "CVS/Entries"); // NOI18N
            if (entries.exists()) {
                return entries;
            } else {
                File backup = new File(folder, "CVS/Entries.Backup"); // NOI18N
                if (backup.exists()) {
                    try {                    
                        if (t9yBeforeRename != null) t9yBeforeRename.run();
                        FileUtils.renameFile(backup, entries);
                        return entries;
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            return null;
        }
    }

    /**
     * Update the Entries file using information in the Entries.Log file
     * (if present). If Entries.Log is not present, this method does
     * nothing.
     * @param directory the directory that contains the Entries file
     * @throws IOException if an error occurs reading or writing the files
     */
    private void processEntriesDotLog(File directory) throws IOException {
        
        synchronized (ksEntries) {
            final File entriesDotLogFile = new File(directory, "Entries.Log"); //NOI18N
            if (!entriesDotLogFile.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(
                    entriesDotLogFile));

            // make up a list of changes to be made based on what is in
            // the .log file. Then apply them all later
            List additionsList = new LinkedList();
            HashSet removalSet = new HashSet();

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("A ")) { //NOI18N
                        final Entry entry = new Entry(line.substring(2));
                        additionsList.add(entry);
                    }
                    else if (line.startsWith("R ")) { //NOI18N
                        final Entry entry = new Entry(line.substring(2));
                        removalSet.add(entry.getName());
                    }
                    // otherwise ignore the line since we don't understand it
                }
            } finally {
                reader.close();
            }

            if ((additionsList.size() > 0) || (removalSet.size() > 0)) {
                final File backup = new File(directory, "Entries.Backup"); //NOI18N
                final BufferedWriter writer = new BufferedWriter(new FileWriter(
                        backup));
                final File entriesFile = new File(directory, "Entries"); //NOI18N
                reader = new BufferedReader(new FileReader(entriesFile));

                try {
                    // maintain a count of the number of directories so that
                    // we know whether to write the "D" line
                    int directoryCount = 0;

                    while ((line = reader.readLine()) != null) {
                        // we will write out the directory "understanding" line
                        // later, if necessary
                        if (line.trim().equals("D")) { //NOI18N
                            continue;
                        }

                        final Entry entry = new Entry(line);

                        if (entry.isDirectory()) {
                            directoryCount++;
                        }

                        if (!removalSet.contains(entry.getName())) {
                            writer.write(entry.toString());
                            writer.newLine();
                            if (entry.isDirectory()) {
                                directoryCount--;
                            }
                        }
                    }
                    Iterator it = additionsList.iterator();
                    while (it.hasNext()) {
                        final Entry entry = (Entry)it.next();
                        if (entry.isDirectory()) {
                            directoryCount++;
                        }
                        writer.write(entry.toString());
                        writer.newLine();
                    }
                    if (directoryCount == 0) {
                        writer.write("D"); //NOI18N
                        writer.newLine();
                    }
                } finally {
                    try {
                        reader.close();
                    } finally {
                        writer.close();
                    }
                }

                if (t9yBeforeRename != null) t9yBeforeRename.run();
                FileUtils.renameFile(backup, entriesFile);
            }
            entriesDotLogFile.delete();
        }
    }

	public String getEntryEncoding() {
		return entryEncoding;
	}

	public void setEntryEncoding(String entryEncoding) {
		this.entryEncoding = entryEncoding;
	}
    
}
