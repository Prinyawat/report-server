package com.softsquare.report.core.report;

import java.io.File;

public interface StorageService {
    void init();

    void deleteAll();

    boolean jrxmlFileExists(String file);

    boolean jasperFileExists(String file);

    String loadJrxmlFile(String file);

    File loadJasperFile(String file);
    
    String loadPathFile(String file);

}