package pers.victor.smartgo;

import com.google.common.io.Closer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Created by Victor on 01/12/2017. (ง •̀_•́)ง
 */

class ResourceUtil {
    static void createResources(Filer filer, Map<Element, String> elements, String path, String suffix) throws Exception {
        Set<String> services = new HashSet<>();
        for (Map.Entry<Element, String> entry : elements.entrySet()) {
            TypeElement typeElement = (TypeElement) entry.getKey();
            String className = typeElement.getQualifiedName() + suffix;
            services.add(className);
        }
        try {
            FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);
            InputStream inputStream = existingFile.openInputStream();
            services.addAll(ResourceUtil.readServiceFile(inputStream));
            inputStream.close();
        } catch (Exception ignored) {
        }
        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", path);
        OutputStream outputStream = fileObject.openOutputStream();
        ResourceUtil.writeServiceFile(services, outputStream);
        outputStream.close();
    }

    static Set<String> readServiceFile(InputStream input) throws IOException {
        HashSet<String> serviceClasses = new HashSet<>();
        Closer closer = Closer.create();
        try {
            BufferedReader r = closer.register(new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8"))));
            String line;
            while ((line = r.readLine()) != null) {
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    serviceClasses.add(line);
                }
            }
            return serviceClasses;
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }

    static void writeServiceFile(Set<String> services, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, Charset.forName("UTF-8")));
        for (String service : services) {
            writer.write(service);
            writer.newLine();
        }
        writer.flush();
    }
}
