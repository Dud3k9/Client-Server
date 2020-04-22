/**
 *
 *  @author Petrykowski Maciej S19267
 *
 */

package S_PASSTIME_SERVER1;


import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Tools {
    public static Options createOptionsFromYaml(String fileName) {
        Options options = null;
        try {
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream(fileName);
            Map<String, Object> map = yaml.load(inputStream);
            options = new Options(map.get("host").toString(),
                    (int) map.get("port"), (boolean) map.get("concurMode"),
                    (boolean) map.get("showSendRes"),
                    (Map<String, List<String>>) map.get("clientsMap"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return options;
    }
}
