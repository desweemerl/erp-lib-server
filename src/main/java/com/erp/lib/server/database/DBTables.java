package com.erp.lib.server.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.erp.lib.server.config.Configuration;
import com.erp.lib.server.database.annotation.Table;
import com.erp.lib.server.utils.PackageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DBTables {

    private Provider<Configuration> configurationProvider;

    private Map<String, DBTable> cache = new HashMap();
    private Logger logger;

    @Inject
    public DBTables(Provider<Configuration> configurationProvider) {
        logger = LoggerFactory.getLogger(DBTables.class);

        try {
            logger.info("Initializing tables cache ...");
            List<Class> scannedClasses = new ArrayList();

            for (String packageName : configurationProvider.get().getModelPackages()) {
                scannedClasses.addAll(PackageUtil.scanPackage(packageName));
            }

            for (Class clazz : scannedClasses) {
                Class baseClazz = clazz;
                String baseName = clazz.getName();

                do {
                    Table table = (Table) clazz.getAnnotation(Table.class);
                    if (table != null) {
                        cache.put(baseName, new DBTable(baseClazz, table.name()));
                        break;
                    }
                } while ((clazz = clazz.getSuperclass()) != null);
            }
        } catch (Exception ex) {
            logger.error("Unable to create tables cache");
            ex.printStackTrace();
        }
    }

    public DBTable getDBTable(Class clazz) {
        return cache.get(clazz.getName());
    }
}
