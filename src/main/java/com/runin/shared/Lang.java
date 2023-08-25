 /*
  * Copyright (C) 2023 Kristijan Đeri
  *
  * RunIN is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * RunIN is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with RunIN.  If not, see <https://www.gnu.org/licenses/>.
  */

package com.runin.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lang {

    private static Locale locale = null;
    private static final Properties resourceProperties = new Properties();
    private static String applicationPrefix="RunINLocaleBundle";

    public Lang(){
        if(locale != null){
            return;
        }
        loadSelectedLanguage();
        String localeCode = locale.getLanguage()+"_"+locale.getCountry().toUpperCase();
        try (InputStream is = Files.newInputStream(Paths.get(ProjectPaths.CONFIG.getPath()+"/locales/RunINLocaleBundle_"+localeCode+".properties"))) {
            resourceProperties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static void setPrefix(String prefix){
        applicationPrefix = prefix;
    }

    public static void loadSelectedLanguage(){

        PropertiesIO propertiesIO = new PropertiesIO(ProjectPaths.CONFIG.getPath()+"/settings.properties");
        String language = propertiesIO.read("language");
        setLanguage(language);
        String localeCode = locale.getLanguage()+"_"+locale.getCountry().toUpperCase();
        try (InputStream is = Files.newInputStream(Paths.get(ProjectPaths.CONFIG.getPath()+"/locales/RunINLocaleBundle_"+localeCode+".properties"))) {
            resourceProperties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Locale> getAllLocales(){
        return Stream.of(Objects.requireNonNull(new File(ProjectPaths.CONFIG.getPath()+"/locales").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .filter(name -> name.startsWith(applicationPrefix)&&name.endsWith(".properties"))
                .map(localePart -> localePart.substring(localePart.indexOf('_')+1,localePart.indexOf('.')).split("_"))
                .filter(parts->parts.length>1)
                .map(locale->Locale.of(locale[0],locale[1]))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unused")
    public static Set<String> getAllLanguages(){
        return getAllLocales().stream()
                .map(Locale::getDisplayLanguage)
                .collect(Collectors.toSet());
    }

    public static String get(String key){
        if(resourceProperties.containsKey(key)){
            return resourceProperties.getProperty(key);
        }
        return "";
    }

    public static Locale getLocale(){
        return locale;
    }

    public static void setLanguage(String languageTag){
        PropertiesIO propertiesIO = new PropertiesIO(ProjectPaths.CONFIG.getPath()+"/settings.properties");
        if(languageTag==null||languageTag.isEmpty()||!Files.exists(Path.of(ProjectPaths.CONFIG.getPath()+"/locales/"+applicationPrefix+"_"+languageTag+".properties"))){
            locale = getAllLocales().iterator().next();
            if(locale!=null) {
                propertiesIO.write("language", locale.getLanguage() + "_" + locale.getCountry());
            }
            return;
        }
        String[] split = languageTag.split("_");
        locale = Locale.of(split[0],split[1]);
        if(locale!=null) {
            propertiesIO.write("language", locale.getLanguage() + "_" + locale.getCountry());
        }
    }

}
