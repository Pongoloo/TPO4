/**
 *
 *  @author Karwowski Jakub S27780
 *
 */

package zad1;


import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

public class Tools {
    public static Options createOptionsFromYaml(String fileName) throws Exception {
        try(FileInputStream fileInputStream = new FileInputStream(fileName);){
            Map<String,Object> load = new Yaml().load(fileInputStream);
            return new Options((String)load.get("host")
                    ,(Integer)load.get("port")
                    ,(Boolean) load.get("concurMode")
                    ,(Boolean) load.get("showSendRes")
                    ,(Map<String, List<String>>)load.get("clientsMap"));
        }
    }
}
